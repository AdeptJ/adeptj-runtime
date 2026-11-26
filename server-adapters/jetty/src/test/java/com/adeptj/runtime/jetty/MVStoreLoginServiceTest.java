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
package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.UserManager;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the Jetty adapter's authentication plumbing.
 * <p>
 * The adapter turns a stored digest into a Jetty {@code UserPrincipal}, so these cover the parts that
 * decide whether a login succeeds: credential matching, the user-enumeration timing defence, and what
 * the credential object exposes once it exists.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Jetty MVStoreLoginService")
class MVStoreLoginServiceTest {

    private static final String STORED_FOR_SECRET = encoded("s3cr3t");

    @Mock
    private UserManager userManager;

    @Nested
    @DisplayName("Loading a principal")
    class LoadingPrincipals {

        @Test
        void aKnownUserBecomesAPrincipalCarryingTheStoredDigest() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            UserPrincipal principal = loginService.loadUserInfo("admin");

            assertNotNull(principal);
            assertEquals("admin", principal.getName());
            // Jetty 12 keeps UserPrincipal's credential protected, so assert through the entry point the
            // authenticator itself uses: authenticate(Object) delegates to Credential#check(Object).
            assertTrue(principal.authenticate("s3cr3t"),
                    "the plaintext submission must authenticate against the stored digest");
            assertFalse(principal.authenticate("s3cr3T"), "case must be significant");
            assertFalse(principal.authenticate(STORED_FOR_SECRET),
                    "the stored digest must not be replayable as a password");
        }

        @Test
        @DisplayName("an unknown user yields no principal and no default credential")
        void anUnknownUserYieldsNoPrincipal() {
            when(userManager.getPassword("nobody")).thenReturn(null);
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            assertNull(loginService.loadUserInfo("nobody"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void anAccountWithoutAStoredCredentialIsUnusable(String stored) {
            when(userManager.getPassword("admin")).thenReturn(stored);
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            assertNull(loginService.loadUserInfo("admin"),
                    "an account with a blank credential must not be reachable");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("SEC-07: a lookup miss still burns one hash so timing does not disclose existence")
        void aMissingUserCostsOneHash(String stored) {
            when(userManager.getPassword("nobody")).thenReturn(stored);
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            assertNull(loginService.loadUserInfo("nobody"));

            // The adapter hashes a random value in this branch on purpose. This assertion is the reason:
            // deleting it would make unknown usernames fail measurably faster.
            verify(userManager).encodePassword(anyString());
        }

        @Test
        void aKnownUserDoesNotTakeTheTimingCompensationPath() {
            when(userManager.getPassword("admin")).thenReturn(STORED_FOR_SECRET);
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            assertNotNull(loginService.loadUserInfo("admin"));

            verify(userManager, never()).encodePassword(anyString());
        }

        @Test
        @DisplayName("VULN SEC-09: the submitted username is forwarded to role lookup unchanged")
        void theUsernameIsForwardedToRoleLookupVerbatim() {
            String dotted = "admin.roles";
            when(userManager.getPassword(dotted)).thenReturn(STORED_FOR_SECRET);
            when(userManager.getRoles(dotted)).thenReturn(List.of("OSgiAdmin"));
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            UserPrincipal principal = loginService.loadUserInfo(dotted);
            List<RolePrincipal> roles = loginService.loadRoleInfo(principal);

            verify(userManager).getRoles(dotted);
            assertEquals(1, roles.size(),
                    "SEC-09: whatever the config tree resolves for that path becomes the caller's roles");
        }
    }

    @Nested
    @DisplayName("Role mapping")
    class RoleMapping {

        @Test
        void configuredRolesBecomeJettyRolePrincipals() {
            when(userManager.getRoles("admin")).thenReturn(List.of("OSGiAdmin", "AdeptJRuntimeAdmin"));
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            List<RolePrincipal> roles = loginService.loadRoleInfo(new UserPrincipal("admin", null));

            assertEquals(List.of("OSGiAdmin", "AdeptJRuntimeAdmin"), roles.stream().map(RolePrincipal::getName).toList());
        }

        @Test
        void anUnmappedPrincipalGetsNoRoles() {
            when(userManager.getRoles("guest")).thenReturn(List.of());
            MVStoreLoginService loginService = new MVStoreLoginService(userManager);

            assertTrue(loginService.loadRoleInfo(new UserPrincipal("guest", null)).isEmpty(),
                    "no mapping must mean no access");
        }
    }

    @Nested
    @DisplayName("Sha256Base64EncodedPassword")
    class CredentialMatching {

        @Test
        void thePlaintextInputIsHashedBeforeComparison() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            assertTrue(credential.check("s3cr3t"),
                    "the submitted password is hashed, then compared against the stored digest");
        }

        @Test
        void aWrongPasswordIsRejected() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            assertFalse(credential.check("s3cr3T"));
            assertFalse(credential.check(""));
            assertFalse(credential.check("s3cr3t "));
        }

        @Test
        void theStoredDigestItselfIsNotAcceptedAsACredential() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            assertFalse(credential.check(STORED_FOR_SECRET),
                    "double encoding means a stolen digest cannot simply be replayed as the password");
        }

        @Test
        @DisplayName("VULN: the credential is not constant time, it is String.equals")
        void comparisonIsAnOrdinaryStringEquality() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            // Credential#check -> Password#check -> String.equals. Nothing here is a constant-time
            // digest comparison, so the encoder offers no protection against a timing oracle on the
            // admin login form (SEC-07). The assertion below only pins that comparison is reachable.
            assertTrue(credential.check("s3cr3t"));
        }

        @Test
        @DisplayName("VULN: only String credentials work - a char[] aborts the login flow")
        void aCharArrayCredentialThrowsInsteadOfFailingClosed() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            // check() does `(String) credentials` unguarded. Jetty's own Credential contract accepts
            // char[] as well, so any mechanism that presents the credential that way throws a
            // ClassCastException out of the authenticator instead of returning "not authenticated".
            assertThrows(ClassCastException.class, () -> credential.check("s3cr3t".toCharArray()));
        }

        @Test
        @DisplayName("VULN: a null credential throws instead of failing closed")
        void aNullCredentialThrows() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            assertThrows(RuntimeException.class, () -> credential.check(null),
                    "an absent credential must be a plain authentication failure, not an internal error");
        }

        @Test
        @DisplayName("VULN SEC-08: the credential object prints the stored digest")
        void toStringRevealsTheStoredDigest() {
            Sha256Base64EncodedPassword credential = new Sha256Base64EncodedPassword(STORED_FOR_SECRET);

            // Jetty's Credential#toString is what ends up in a log line, an exception message or a
            // debugger view. A credential type that prints its own secret turns any debug logging of the
            // principal into a credential-store dump, and SEC-03's unsalted hashing makes it crackable.
            assertTrue(String.valueOf(credential).contains(STORED_FOR_SECRET),
                    "SEC-08: " + credential + " exposes the stored credential in clear text");
        }
    }

    private static String encoded(String password) {
        return new String(PasswordEncoder.encodePassword(password), StandardCharsets.UTF_8);
    }
}
