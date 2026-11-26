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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static com.adeptj.runtime.kernel.Constants.FRAMEWORK_CONF_FILE;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_SERVER_MODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Environment} decides whether stack traces may be rendered to a browser ({@code SEC-13}) and
 * where the runtime reads its configuration from ({@code SEC-11}), so its defaults are security
 * relevant.
 *
 * @author AdeptJ Security Review.
 */
@DisplayName("Environment")
class EnvironmentTest {

    private String previousMode;

    @BeforeEach
    void rememberMode() {
        previousMode = System.getProperty(SYS_PROP_SERVER_MODE);
    }

    @AfterEach
    void restoreMode() {
        if (previousMode == null) {
            System.clearProperty(SYS_PROP_SERVER_MODE);
        } else {
            System.setProperty(SYS_PROP_SERVER_MODE, previousMode);
        }
    }

    @Nested
    @DisplayName("Server mode")
    class ServerModeHandling {

        @Test
        void devModeIsOffWhenThePropertyIsAbsent() {
            System.clearProperty(SYS_PROP_SERVER_MODE);

            assertFalse(Environment.isDev(),
                    "SEC-13: with no -Dadeptj.rt.mode the runtime must not be in dev mode");
        }

        @ParameterizedTest
        @ValueSource(strings = {"PROD", "prod", "Prod"})
        void productionModeIsNotDev(String mode) {
            System.setProperty(SYS_PROP_SERVER_MODE, mode);

            assertFalse(Environment.isDev());
        }

        @ParameterizedTest
        @ValueSource(strings = {"DEV", "dev", "Dev"})
        void devModeIsCaseInsensitive(String mode) {
            System.setProperty(SYS_PROP_SERVER_MODE, mode);

            assertTrue(Environment.isDev(),
                    "SEC-13: any capitalisation of DEV turns on stack-trace rendering");
        }

        @ParameterizedTest
        @ValueSource(strings = {"development", "staging", "true", "1", "DEV "})
        void anythingElseIsTreatedAsProduction(String mode) {
            System.setProperty(SYS_PROP_SERVER_MODE, mode);

            assertFalse(Environment.isDev(), "unrecognised modes must fail closed to the safer default");
        }

        @Test
        void theModeIsGlobalJvmState() {
            // isDev() reads a system property, so it cannot vary per tenant, per vhost or per request,
            // and anything else in the JVM can flip it at runtime.
            System.setProperty(SYS_PROP_SERVER_MODE, "PROD");
            assertFalse(Environment.isDev());

            System.setProperty(SYS_PROP_SERVER_MODE, "DEV");
            assertTrue(Environment.isDev(), "a single setProperty call enables the dev-only error body");
        }
    }

    @Nested
    @DisplayName("Configuration locations")
    class ConfigurationLocations {

        @Test
        void theConfDirectoryHangsOffTheWorkingDirectory() {
            Path confDir = Environment.getConfDirPath();

            assertTrue(confDir.endsWith(Path.of("adeptj-runtime", "deployment", "conf")),
                    "SEC-11: every *.conf under this path overrides security settings");
            assertTrue(confDir.isAbsolute());
        }

        @Test
        void theFrameworkPropertiesFileIsPickedUpFromTheWorkingDirectory() {
            Path frameworkConf = Environment.getFrameworkConfPath();

            assertEquals(FRAMEWORK_CONF_FILE, frameworkConf.getFileName().toString());
            assertTrue(frameworkConf.endsWith(Path.of("adeptj-runtime", "deployment", FRAMEWORK_CONF_FILE)),
                    "SEC-02: this is where the generated framework.properties, crypto.key included, is written");
        }

        @Test
        void bothLocationsResolveBelowTheSameDeploymentRoot() {
            Path confDir = Environment.getConfDirPath();
            Path frameworkConf = Environment.getFrameworkConfPath();

            assertEquals(confDir.getParent(), frameworkConf.getParent(),
                    "one writable directory controls both the HOCON config and the OSGi framework config");
        }
    }
}
