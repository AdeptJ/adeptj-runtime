/*
###############################################################################
#                                                                             #
#    Copyright 2016-2026, AdeptJ (http://www.adeptj.com)                      #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/
package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.UserManager;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.apache.catalina.realm.GenericPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Tomcat adapter's realm and credential handler.
 * <p>
 * {@code RealmBase#authenticate(String, String)} calls
 * {@code CredentialHandler#matches(password, storedCredential)} - input first - which is the opposite
 * of what the parameter names in some Tomcat versions suggest, so the delegation order is pinned here
 * explicitly: swapping the two arguments would silently turn the admin login off, or worse.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tomcat MVStoreRealm / MVStoreCredentialHandler")
class MVStoreRealmTest {

    private static final String STORED_FOR_SECRET = encoded("s3cr3t");

    @Mock
    private UserManager userManager;

    @Nested
    @DisplayName("Credential handling")
    class Credentials {

        @Test
        void matchesDelegatesWithTheInputCredentialFirst() {
            when(userManager.matchPassword("s3cr3t", STORED_FOR_SECRET)).thenReturn(true);
            MVStoreCredentialHandler handler = new MVStoreCredentialHandler(userManager);

            assertTrue(handler.matches("s3cr3t", STORED_FOR_SECRET));

            // Argument order is the security property: reversing it would hash the stored digest.
            verify(userManager).matchPassword("s3cr3t", STORED_FOR_SECRET);
        }

        @Test
        void aMismatchIsReportedAsSuch() {
            when(userManager.matchPassword("wrong", STORED_FOR_SECRET)).thenReturn(false);
            MVStoreCredentialHandler handler = new MVStoreCredentialHandler(userManager);

            assertFalse(handler.matches("wrong", STORED_FOR_SECRET));
        }

        @Test
        @DisplayName("SEC-07: the no-such-user path still costs one hash")
        void mutateIsTheWorkFactorForAnUnknownUser() {
            when(userManager.encodePassword("guess")).thenReturn(encoded("guess"));
            MVStoreCredentialHandler handler = new MVStoreCredentialHandler(userManager);

            assertEquals(encoded("guess"), handler.mutate("guess"));

            verify(userManager).encodePassword("guess");
        }

        @Test
        @DisplayName("VULN: the handler forwards secrets to the UserManager untouched")
        void credentialsAreForwardedAsPlainStrings() {
            when(userManager.matchPassword("hunter2", "stored")).thenReturn(true);
            MVStoreCredentialHandler handler = new MVStoreCredentialHandler(userManager);

            assertTrue(handler.matches("hunter2", "stored"));

            // Both the plaintext input and the stored digest are Strings by the time they get here, and
            // a String cannot be wiped, so the password outlives the login attempt in the heap.
            verify(userManager).matchPassword("hunter2", "stored");
        }
    }

    @Nested
    @DisplayName("Realm lookups")
    class RealmLookups {

        @Test
        void theStoredCredentialComesFromTheCredentialStore() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            MVStoreRealm realm = new MVStoreRealm(userManager);

            assertEquals(STORED_FOR_SECRET, realm.getPassword("admin"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void anUnknownAccountHasNoCredential(String stored) {
            when(userManager.getPassword("nobody")).thenReturn(stored);
            MVStoreRealm realm = new MVStoreRealm(userManager);

            assertEquals(stored, realm.getPassword("nobody"),
                    "SEC-09: the realm forwards whatever the store returns, including nothing");
        }

        @Test
        void thePrincipalCarriesTheConfiguredRoles() {
            when(userManager.getRoles("admin")).thenReturn(List.of("OSGiAdmin", "AdeptJRuntimeAdmin"));
            MVStoreRealm realm = new MVStoreRealm(userManager);

            Principal principal = realm.getPrincipal("admin");

            assertNotNull(principal);
            assertEquals("admin", principal.getName());
            GenericPrincipal generic = (GenericPrincipal) principal;
            assertTrue(generic.hasRole("OSGiAdmin"));
            assertTrue(generic.hasRole("AdeptJRuntimeAdmin"));
            assertFalse(generic.hasRole("AnythingElse"),
                    "only the mapped roles may be granted");
        }

        @Test
        void anUnmappedPrincipalGetsNoRoles() {
            when(userManager.getRoles("guest")).thenReturn(List.of());
            MVStoreRealm realm = new MVStoreRealm(userManager);

            GenericPrincipal generic = (GenericPrincipal) realm.getPrincipal("guest");

            assertEquals(0, generic.getRoles().length, "a principal with no mapping must have no roles");
            assertFalse(generic.hasRole("OSGiAdmin"));
        }

        @Test
        @DisplayName("VULN SEC-09: the username is used verbatim as the role lookup key")
        void theUsernameReachesRoleLookupUnsanitised() {
            String dotted = "admin.roles";
            when(userManager.getRoles(dotted)).thenReturn(List.of("OSGiAdmin"));
            MVStoreRealm realm = new MVStoreRealm(userManager);

            GenericPrincipal generic = (GenericPrincipal) realm.getPrincipal(dotted);

            verify(userManager).getRoles(dotted);
            assertTrue(generic.hasRole("OSGiAdmin"),
                    "SEC-09: a principal named 'admin.roles' is resolved as a configuration path, so it"
                            + " inherits the roles sitting at that node");
        }

        @Test
        @DisplayName("VULN: a principal is created even when the account has no credential")
        void aPrincipalIsBuiltForAnAccountThatCannotAuthenticate() {
            when(userManager.getPassword("ghost")).thenReturn(null);
            when(userManager.getRoles("ghost")).thenReturn(List.of("OSGiAdmin"));
            MVStoreRealm realm = new MVStoreRealm(userManager);

            // getPrincipal() never consults the credential store, so a stray role mapping for an account
            // with no password still produces a usable-looking principal. Authentication still requires
            // a matching credential, but the principal itself is not the place that is checked.
            assertNotNull(realm.getPrincipal("ghost"));
            assertNull(realm.getPassword("ghost"));
        }
    }

    private static String encoded(String password) {
        return new String(PasswordEncoder.encodePassword(password), StandardCharsets.UTF_8);
    }
}
