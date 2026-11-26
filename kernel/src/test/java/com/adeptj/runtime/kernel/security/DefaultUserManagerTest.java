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
package com.adeptj.runtime.kernel.security;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.util.MVStoreUtil;
import com.adeptj.runtime.kernel.util.PasswordEncoder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultUserManager}, the authentication and authorization source behind the
 * Jetty, Tomcat and Undertow adapters.
 * <p>
 * Tests marked {@code VULN} are characterization tests for <em>SEC-09</em>: the submitted username is
 * concatenated into a HOCON path without validation. They pin today's behaviour so the remediation
 * cannot be merged without the expectations being reviewed.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultUserManager")
class DefaultUserManagerTest {

    /** {@code Base64(SHA-256("s3cr3t"))}, the shape stored in {@code credentials.dat}. */
    private static final String STORED_FOR_SECRET = encoded("s3cr3t");

    private static final String CONFIG = """
            common {
              auth-roles = [OSGiAdmin, AdeptJRuntimeAdmin]
              user-roles-mapping {
                admin = [OSGiAdmin, AdeptJRuntimeAdmin]
                deploy-bot {
                  roles = [AdeptJRuntimeAdmin]
                }
              }
            }
            """;

    @Mock
    private ConfigProvider configProvider;

    private DefaultUserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new DefaultUserManager();
    }

    @Nested
    @DisplayName("Credential lookup")
    class CredentialLookup {

        @Test
        void delegatesToTheCredentialsStore() {
            try (MockedStatic<MVStoreUtil> mvStore = mockStatic(MVStoreUtil.class)) {
                mvStore.when(() -> MVStoreUtil.getPassword("admin")).thenReturn("stored-digest");

                assertEquals("stored-digest", userManager.getPassword("admin"));

                mvStore.verify(() -> MVStoreUtil.getPassword("admin"));
            }
        }

        @Test
        void unknownPrincipalHasNoCredential() {
            try (MockedStatic<MVStoreUtil> mvStore = mockStatic(MVStoreUtil.class)) {
                mvStore.when(() -> MVStoreUtil.getPassword("nobody")).thenReturn(null);

                assertEquals(null, userManager.getPassword("nobody"),
                        "an unknown principal must not be handed a default credential");
            }
        }
    }

    @Nested
    @DisplayName("Password matching fails closed")
    class FailClosedMatching {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"admin", " "})
        void blankSubmittedPasswordNeverMatches(String submitted) {
            assertFalse(userManager.matchPassword(submitted, STORED_FOR_SECRET),
                    "a blank submitted password must never authenticate");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void missingStoredPasswordNeverMatches(String stored) {
            assertFalse(userManager.matchPassword("s3cr3t", stored),
                    "an account without a stored credential must be unreachable");
        }

        @Test
        void correctPasswordIsAccepted() {
            assertTrue(userManager.matchPassword("s3cr3t", STORED_FOR_SECRET));
        }

        @Test
        void wrongPasswordIsRejected() {
            assertFalse(userManager.matchPassword("wrong", STORED_FOR_SECRET));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "s3cr3t"})
        void charArrayOverloadAgreesWithTheStringOverload(String submitted) {
            boolean viaString = userManager.matchPassword(submitted, STORED_FOR_SECRET);
            char[] asChars = submitted == null ? null : submitted.toCharArray();

            assertEquals(viaString, userManager.matchPassword(asChars, STORED_FOR_SECRET.toCharArray()));
        }

        @Test
        void caseAndSurroundingWhitespaceAreSignificant() {
            assertFalse(userManager.matchPassword("S3CR3T", STORED_FOR_SECRET));
            assertFalse(userManager.matchPassword("s3cr3t ", STORED_FOR_SECRET),
                    "trailing whitespace must not be silently trimmed into a valid credential");
        }

        @Test
        void aDigestOfTheWrongLengthIsRejected() {
            assertFalse(userManager.matchPassword("s3cr3t", "c2hvcnQ="),
                    "a truncated stored credential must not match a full length digest");
        }

        @Test
        void utf8MultiBytePasswordsRoundTrip() {
            String password = "p\u00e4ssw\u00f6r\u4e1c";
            String stored = encoded(password);

            assertTrue(userManager.matchPassword(password, stored),
                    "must hash the UTF-8 bytes, not the platform default encoding");
        }
    }

    @Nested
    @DisplayName("Caller-visible state")
    class CallerVisibleState {

        @Test
        void doesNotMutateTheCallersCharArray() {
            char[] submitted = "s3cr3t".toCharArray();

            assertTrue(userManager.matchPassword(submitted, STORED_FOR_SECRET.toCharArray()));
            assertArrayEquals("s3cr3t".toCharArray(), submitted,
                    "matchPassword wipes its own copies but must not clobber caller-owned input");
        }

        @Test
        void matchingVerifiesTheSuppliedDigestWithoutReadingTheStore() {
            try (MockedStatic<MVStoreUtil> mvStore = mockStatic(MVStoreUtil.class)) {
                assertTrue(userManager.matchPassword("s3cr3t", STORED_FOR_SECRET));

                // matchPassword must verify the values it is handed instead of quietly substituting the
                // real stored credential, otherwise a caller bug becomes an authentication bypass.
                mvStore.verifyNoInteractions();
            }
        }

        @Test
        void repeatedCallsAreIdempotent() {
            assertTrue(userManager.matchPassword("s3cr3t", STORED_FOR_SECRET));
            assertTrue(userManager.matchPassword("s3cr3t", STORED_FOR_SECRET));
        }
    }

    @Nested
    @DisplayName("Role resolution")
    class RoleResolution {

        @Test
        void returnsTheMappedRolesForAKnownPrincipal() {
            withMainConfig(CONFIG, () ->
                    assertEquals(List.of("OSGiAdmin", "AdeptJRuntimeAdmin"), userManager.getRoles("admin")));
        }

        @Test
        void unmappedPrincipalGetsZeroRoles() {
            withMainConfig(CONFIG, () -> assertTrue(userManager.getRoles("intruder").isEmpty(),
                    "principals with no role mapping must fail closed"));
        }

        @Test
        @DisplayName("VULN SEC-09: a dotted username reads a sibling account's node")
        void dottedUsernameTraversesIntoAnotherAccountsMapping() {
            withMainConfig(CONFIG, () -> {
                // The real account is "deploy-bot"; its roles sit at ...deploy-bot.roles.
                List<String> borrowed = userManager.getRoles("deploy-bot.roles");

                assertEquals(List.of("AdeptJRuntimeAdmin"), borrowed,
                        "SEC-09: the username is used as a raw config path, so a principal literally named"
                                + " 'deploy-bot.roles' inherits deploy-bot's roles instead of its own");
            });
        }

        @Test
        @DisplayName("VULN SEC-09: an object-shaped mapping throws on the authentication path")
        void objectShapedMappingThrowsWrongType() {
            withMainConfig(CONFIG, () -> assertThrows(ConfigException.WrongType.class,
                    () -> userManager.getRoles("deploy-bot"),
                    "SEC-09: a mapping declared as an object aborts role resolution with an unchecked exception"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"admin\"", "admin=", "${foo}", "admin*", "admin.", ""})
        @DisplayName("VULN SEC-09: malformed usernames throw before authentication completes")
        void malformedUsernameThrowsBadPath(String username) {
            withMainConfig(CONFIG, () -> assertThrows(ConfigException.BadPath.class,
                    () -> userManager.getRoles(username),
                    "SEC-09: '[" + username + "]' reaches the HOCON path parser unvalidated"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"a b", "admin.dot", "admin.auth-roles", "intruder"})
        void plainOrUnconfiguredUsernamesResolveToNoRoles(String username) {
            withMainConfig(CONFIG, () -> assertTrue(userManager.getRoles(username).isEmpty(),
                    "control case: '[" + username + "]' cannot reach anyone else's roles"));
        }

        @Test
        @DisplayName("VULN SEC-09: the thrown message echoes the internal config path")
        void failureMessageLeaksTheConfigPath() {
            withMainConfig(CONFIG, () -> {
                ConfigException.BadPath failure = assertThrows(ConfigException.BadPath.class,
                        () -> userManager.getRoles("admin\""));

                assertTrue(failure.getMessage().contains("common.user-roles-mapping."),
                        "SEC-09: the message discloses the internal configuration layout: " + failure.getMessage());
            });
        }
    }

    private void withMainConfig(String hocon, Runnable body) {
        Config config = ConfigFactory.parseString(hocon).resolve();
        when(configProvider.getMainConfig()).thenReturn(config);
        try (MockedStatic<ConfigProvider> provider = mockStatic(ConfigProvider.class)) {
            provider.when(ConfigProvider::getInstance).thenReturn(configProvider);
            body.run();
        }
    }

    private static String encoded(String password) {
        return new String(PasswordEncoder.encodePassword(password), StandardCharsets.UTF_8);
    }
}
