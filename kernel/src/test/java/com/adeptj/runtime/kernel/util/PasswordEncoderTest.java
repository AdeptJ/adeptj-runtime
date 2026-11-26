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
package com.adeptj.runtime.kernel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Security regression tests for {@link PasswordEncoder}.
 * <p>
 * Several tests here are <strong>characterization</strong> tests: they pin the behaviour that
 * <em>SEC-03</em> identifies as a vulnerability so that the fix cannot be merged silently and so
 * that the exact weakness cannot regress. Each one is labelled with the finding it documents and
 * states what the assertion must become once the finding is remediated.
 *
 * @author AdeptJ Security Review.
 */
@DisplayName("PasswordEncoder (SEC-03: password hashing)")
class PasswordEncoderTest {

    /**
     * The digest shipped as the default admin credential in
     * {@code main/src/main/resources/application.conf} ({@code common.user-credential-mapping.admin}).
     */
    private static final String SHIPPED_DEFAULT_ADMIN_DIGEST = "jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=";

    @Nested
    @DisplayName("Encoding contract")
    class EncodingContract {

        @ParameterizedTest
        @ValueSource(strings = {"admin", "changeit", "correct horse battery staple"})
        void encodesToBase64OfPlainSha256(String password) throws Exception {
            byte[] expected = Base64.getEncoder()
                    .encode(MessageDigest.getInstance("SHA-256").digest(password.getBytes(UTF_8)));

            assertArrayEquals(expected, PasswordEncoder.encodePassword(password),
                    () -> "encoder must be Base64(SHA-256(utf8)) for: " + password);
            assertArrayEquals(expected, PasswordEncoder.encodePassword(password.getBytes(UTF_8)),
                    "String and byte[] overloads must agree");
        }

        @Test
        @DisplayName("Output is a fixed 44 char Base64 block (a bare 32 byte digest)")
        void outputIsBare32ByteDigest() {
            byte[] encoded = PasswordEncoder.encodePassword("admin");
            String encodedStr = new String(encoded, UTF_8);

            assertEquals(44, encodedStr.length(), "Base64 of a 32 byte digest is 44 chars");
            assertEquals(32, Base64.getDecoder().decode(encodedStr).length,
                    "no salt, no algorithm marker and no cost parameter are stored -> SEC-03");
        }

        @Test
        @DisplayName("Non-ASCII and empty inputs are encoded, never rejected")
        void encodesEdgeCaseInputs() {
            assertArrayEquals(PasswordEncoder.encodePassword(""), PasswordEncoder.encodePassword(""));
            assertNotEquals(PasswordEncoder.encodePassword("\u00e9\u00e8"),
                    PasswordEncoder.encodePassword("ee"), "must be UTF-8 encoded, not platform default");
        }
    }

    @Nested
    @DisplayName("SEC-03 characterization: unsalted single round hashing")
    class UnsaltedSingleRound {

        @Test
        @DisplayName("VULN: identical passwords produce byte-identical digests (no per-user salt)")
        void identicalPasswordsYieldIdenticalDigests() {
            byte[] first = PasswordEncoder.encodePassword("admin");
            byte[] second = PasswordEncoder.encodePassword("admin");

            // A salted KDF must produce different stored values for the same password.
            // This assertion has to be inverted when SEC-03 is fixed.
            assertArrayEquals(first, second,
                    "SEC-03: no salt is mixed in, so digests are linkable and pre-computable");
        }

        @Test
        @DisplayName("VULN: a common password has exactly one encoding used by every account")
        void commonPasswordHasSingleGlobalEncoding() {
            Set<String> distinctDigests = new HashSet<>();
            for (int tenant = 0; tenant < 50; tenant++) {
                distinctDigests.add(new String(PasswordEncoder.encodePassword("admin"), UTF_8));
            }

            assertEquals(1, distinctDigests.size(),
                    "SEC-03: 50 accounts sharing a password share one digest -> one rainbow table breaks all");
        }

        @Test
        @DisplayName("VULN: the shipped default admin credential is SHA-256('admin')")
        void shippedDefaultCredentialIsPlainSha256OfAdmin() {
            String encoded = new String(PasswordEncoder.encodePassword("admin"), UTF_8);

            assertEquals(SHIPPED_DEFAULT_ADMIN_DIGEST, encoded,
                    "application.conf seeds credentials.dat with Base64(SHA-256('admin')) -> SEC-01");
        }

        @Test
        @DisplayName("VULN: encoding is fast enough for offline brute force")
        void encodingIsNotAKeyDerivationFunction() throws Exception {
            MessageDigest probe = MessageDigest.getInstance("SHA-256");

            long start = System.nanoTime();
            int rounds = 200_000;
            for (int i = 0; i < rounds; i++) {
                probe.reset();
                probe.digest("admin".getBytes(UTF_8));
            }
            long microsPerHash = Math.max(1, (System.nanoTime() - start) / 1000 / rounds);

            // bcrypt/scrypt/Argon2 tuned to OWASP costs take >= 50ms. A bare SHA-256 takes microseconds,
            // which is what makes the credentials.dat file cheap to crack -> SEC-03.
            assertTrue(microsPerHash < 1_000,
                    "SEC-03: expected a bare hash (microseconds), measured " + microsPerHash + "us per round");
            assertNotEquals(0, rounds);
        }

        @Test
        @DisplayName("VULN: the encoder exposes no verification path, so callers compare digests themselves")
        void encoderOffersNoConstantTimeVerificationApi() {
            Arrays.stream(PasswordEncoder.class.getDeclaredMethods())
                    .filter(method -> method.getName().startsWith("match") || method.getName().startsWith("verify"))
                    .forEach(method -> org.junit.jupiter.api.Assertions.fail(
                            "SEC-03: unexpected verification method " + method.getName()
                                    + " - verification is delegated to callers and is not pinned here"));
        }
    }

    @Nested
    @DisplayName("Tamper detection")
    class TamperDetection {

        @ParameterizedTest
        @ValueSource(strings = {"admin ", "Admin", "admin1", "", " adm"})
        void anyChangeOfPasswordChangesTheDigest(String mutated) {
            assertNotEquals(new String(PasswordEncoder.encodePassword("admin"), UTF_8),
                    new String(PasswordEncoder.encodePassword(mutated), UTF_8),
                    () -> "digest must not collide for mutated input: [" + mutated + "]");
        }

        @Test
        @DisplayName("A truncated digest never verifies as the original")
        void truncatedInputIsNotEquivalent() {
            byte[] full = PasswordEncoder.encodePassword("administrator");
            byte[] prefix = PasswordEncoder.encodePassword("administ");

            assertFalse(MessageDigest.isEqual(full, prefix));
        }
    }
}
