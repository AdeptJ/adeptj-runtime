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
package com.adeptj.runtime.undertow.core;

import com.adeptj.runtime.kernel.UserManager;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.PasswordCredential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Undertow adapter's {@link IdentityManager} equivalent.
 * <p>
 * {@code verify(Account)} runs on <em>every</em> request for a protected resource through Undertow's
 * cached-authentication mechanism, so its re-authorization rule is the most security relevant line in
 * the adapter and is covered first.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Undertow SimpleIdentityManager")
class SimpleIdentityManagerTest {

    private static final String STORED_FOR_SECRET = "aW52YWxpZC1idC1zdG9yZWQtaGFzaA==";

    @Mock
    private UserManager userManager;

    private SimpleIdentityManager identityManager;

    private SimpleIdentityManager identityManager() {
        if (this.identityManager == null) {
            this.identityManager = new SimpleIdentityManager(this.userManager);
        }
        return this.identityManager;
    }

    private static Account account(String name, String... roles) {
        return new SimpleAccount(new SimplePrincipal(name), Set.of(roles));
    }

    @Nested
    @DisplayName("Re-authorizing an existing session")
    class SessionReverification {

        @Test
        void aSessionWhoseRolesStillMatchIsAccepted() {
            when(userManager.getRoles("admin")).thenReturn(List.of("OSGiAdmin"));

            Account accepted = identityManager().verify(account("admin", "OSGiAdmin"));

            assertNotNull(accepted);
            assertEquals("admin", accepted.getPrincipal().getName());
        }

        @Test
        void aSessionWithNoOverlappingRolesIsRejected() {
            when(userManager.getRoles("admin")).thenReturn(List.of("AdeptJRuntimeAdmin"));

            assertNull(identityManager().verify(account("admin", "OSGiAdmin")),
                    "a session that no longer holds any current role must be refused");
        }

        @Test
        void aSessionWithNoRolesAtAllIsRejected() {
            when(userManager.getRoles("admin")).thenReturn(List.of("OSGiAdmin"));

            assertNull(identityManager().verify(account("admin")),
                    "an account carrying no roles can never be re-authorized");
        }

        @Test
        void anAccountForAnUnknownPrincipalIsRejected() {
            when(userManager.getRoles("ghost")).thenReturn(List.of());

            assertNull(identityManager().verify(account("ghost", "OSGiAdmin")),
                    "roles removed from the configuration must take effect on the next request");
        }

        @Test
        @DisplayName("VULN SEC-17: a partially revoked session keeps its cached, over-privileged roles")
        void aPartiallyRevokedSessionKeepsTheRoleThatWasTakenAway() {
            // The account was granted both roles at login. The configuration now grants only one.
            when(userManager.getRoles("admin")).thenReturn(List.of("AdeptJRuntimeAdmin"));
            Account cached = account("admin", "OSGiAdmin", "AdeptJRuntimeAdmin");

            Account accepted = identityManager().verify(cached);

            assertSame(cached, accepted,
                    "SEC-17: the stored account is returned as-is instead of being rebuilt from the"
                            + " current role list");
            assertTrue(accepted.getRoles().contains("OSGiAdmin"),
                    "SEC-17: OSGiAdmin was removed from the configuration, yet this session still holds it,"
                            + " because any single overlap is enough and the roles are never refreshed");
        }

        @Test
        @DisplayName("VULN SEC-17: one overlapping role is enough, the rest are not checked")
        void anySingleOverlapAuthorisesTheWholeCachedRoleSet() {
            when(userManager.getRoles("admin")).thenReturn(List.of("AdeptJRuntimeAdmin"));

            Account accepted = identityManager().verify(account("admin", "AdeptJRuntimeAdmin", "Whatever"));

            assertNotNull(accepted);
            assertEquals(Set.of("AdeptJRuntimeAdmin", "Whatever"), accepted.getRoles(),
                    "SEC-17: 'Whatever' is not a configured role and is still carried by the accepted account");
        }

        @Test
        @DisplayName("VULN SEC-09: the cached principal name drives role lookup verbatim")
        void theCachedNameIsUsedAsTheRoleLookupKey() {
            String dotted = "deploy-bot.roles";
            when(userManager.getRoles(dotted)).thenReturn(List.of("AdeptJRuntimeAdmin"));

            assertNotNull(identityManager().verify(account(dotted, "AdeptJRuntimeAdmin")));

            verify(userManager).getRoles(dotted);
        }
    }

    @Nested
    @DisplayName("Authenticating a login attempt")
    class Login {

        @Test
        void correctCredentialsProduceAnAccountWithTheConfiguredRoles() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            when(userManager.matchPassword(any(char[].class), any(char[].class))).thenReturn(true);
            when(userManager.getRoles("admin")).thenReturn(List.of("OSGiAdmin"));

            Account accepted = identityManager().verify("admin", new PasswordCredential("s3cr3t".toCharArray()));

            assertNotNull(accepted);
            assertEquals("admin", accepted.getPrincipal().getName());
            assertEquals(Set.of("OSGiAdmin"), accepted.getRoles());
        }

        @Test
        void wrongCredentialsProduceNoAccount() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            when(userManager.matchPassword(any(char[].class), any(char[].class))).thenReturn(false);

            assertNull(identityManager().verify("admin", new PasswordCredential("nope".toCharArray())));

            verify(userManager, never()).getRoles(anyString());
        }

        @Test
        void theSubmittedAndStoredValuesAreComparedInTheRightOrder() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            when(userManager.matchPassword(any(char[].class), any(char[].class))).thenReturn(true);

            identityManager().verify("admin", new PasswordCredential("s3cr3t".toCharArray()));

            ArgumentCaptor<char[]> submitted = ArgumentCaptor.forClass(char[].class);
            ArgumentCaptor<char[]> stored = ArgumentCaptor.forClass(char[].class);
            verify(userManager).matchPassword(submitted.capture(), stored.capture());

            assertArrayEquals("s3cr3t".toCharArray(), submitted.getValue());
            assertArrayEquals(STORED_FOR_SECRET.toCharArray(), stored.getValue(),
                    "reversing these two would hash the stored digest against itself");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("SEC-07: a lookup miss still costs one hash so existence is not timed")
        void anUnknownUserStillPaysTheHashingCost(String stored) {
            when(userManager.getPassword("nobody")).thenReturn(stored);

            assertNull(identityManager().verify("nobody", new PasswordCredential("guess".toCharArray())));

            verify(userManager).encodePassword("guess");
            verify(userManager, never()).matchPassword(any(char[].class), any(char[].class));
        }

        @Test
        void aKnownUserDoesNotTakeTheTimingCompensationPath() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            when(userManager.matchPassword(any(char[].class), any(char[].class))).thenReturn(false);

            assertNull(identityManager().verify("admin", new PasswordCredential("nope".toCharArray())));

            verify(userManager, never()).encodePassword(anyString());
        }

        @Test
        @DisplayName("VULN: the timing path copies the password into an unerasable String")
        void theCompensationPathCopiesThePasswordIntoAnImmutableString() {
            when(userManager.getPassword("nobody")).thenReturn(null);
            char[] secret = "hunter2".toCharArray();

            assertNull(identityManager().verify("nobody", new PasswordCredential(secret)));

            ArgumentCaptor<String> captured = ArgumentCaptor.forClass(String.class);
            verify(userManager).encodePassword(captured.capture());
            assertEquals("hunter2", captured.getValue(),
                    "SEC-18: the char[] exists so the caller can wipe it, but this path copies it into a"
                            + " String, which is immutable, interned-able and cannot be zeroed");
        }

        @Test
        @DisplayName("VULN: a foreign credential type aborts the authenticator")
        void aForeignCredentialTypeThrows() {
            Credential foreign = new Credential() {
            };

            assertThrows(ClassCastException.class,
                    () -> identityManager().verify("admin", foreign),
                    "the cast to PasswordCredential is unchecked, so an unexpected mechanism throws");
        }

        @Test
        @DisplayName("VULN: no attempt is made to wipe the stored digest")
        void theStoredDigestIsMaterialisedAsACharArrayAndLeftAlone() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            when(userManager.matchPassword(any(char[].class), any(char[].class))).thenReturn(true);

            identityManager().verify("admin", new PasswordCredential("s3cr3t".toCharArray()));

            ArgumentCaptor<char[]> stored = ArgumentCaptor.forClass(char[].class);
            verify(userManager).matchPassword(any(char[].class), stored.capture());
            assertArrayEquals(STORED_FOR_SECRET.toCharArray(), stored.getValue(),
                    "the copy is still intact after the comparison, so it is readable in a heap dump");
        }
    }

    @Nested
    @DisplayName("Other mechanisms")
    class OtherMechanisms {

        @Test
        @DisplayName("a certificate or GSSAPI credential must never be accepted")
        void noCredentialOnlyMechanismEverYieldsAnAccount() {
            assertNull(identityManager().verify(new Credential() {
            }), "verify(Credential) must stay a hard refusal: it authenticates on nothing at all");
        }
    }

    @Nested
    @DisplayName("SimplePrincipal")
    class PrincipalContract {

        @Test
        void equalityIsByNameOnly() {
            assertEquals(new SimplePrincipal("admin"), new SimplePrincipal("admin"));
            assertFalse(new SimplePrincipal("admin").equals(new SimplePrincipal("Admin")));
            assertEquals(new SimplePrincipal("admin").hashCode(), new SimplePrincipal("admin").hashCode());
        }

        @Test
        void theNameIsWhatEveryAuthorizationCheckReads() {
            Principal principal = new SimplePrincipal("admin");

            assertEquals("admin", principal.getName());
            assertTrue(principal.toString().contains("admin"),
                    "SEC-08: the username lands in every toString of the account, so it reaches logs");
        }
    }
}
