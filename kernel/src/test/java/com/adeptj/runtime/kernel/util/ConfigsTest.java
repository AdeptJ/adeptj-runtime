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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link Configs} is the accessor behind authorization data such as
 * {@code common.user-roles-mapping.*}, so its failure behaviour is security relevant: a missing
 * entry must produce "no access", never "all access" and never an unchecked failure.
 *
 * @author AdeptJ Security Review.
 */
@DisplayName("Configs (authorization data access)")
class ConfigsTest {

    private static final Config CONFIG = ConfigFactory.parseString("""
            common {
              auth-roles = [OSGiAdmin, AdeptJRuntimeAdmin]
              empty-roles = []
              user-roles-mapping {
                admin = [OSGiAdmin]
              }
            }
            """).resolve();

    @Test
    void reportsPresenceForAConfiguredPath() {
        assertTrue(Configs.isPresent(CONFIG, "common.auth-roles"));
        assertTrue(Configs.isPresent(CONFIG, "common.user-roles-mapping.admin"));
    }

    @Test
    void reportsAbsenceForAnUnknownPath() {
        assertFalse(Configs.isPresent(CONFIG, "common.nope"));
        assertFalse(Configs.isPresent(CONFIG, "common.user-roles-mapping.nobody"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void aNullOrBlankPathIsNeverPresent(String path) {
        assertFalse(Configs.isPresent(CONFIG, path), "must not consult the config for a blank path");
        assertEquals(List.of(), Configs.getStringList(CONFIG, path));
    }

    @Test
    void aNullConfigNeverYieldsAnything() {
        assertFalse(Configs.isPresent(null, "common.auth-roles"));
        assertEquals(List.of(), Configs.getStringList(null, "common.auth-roles"),
                "a missing config section must mean no access, not an error");
    }

    @Test
    void readsAConfiguredStringList() {
        assertEquals(List.of("OSGiAdmin", "AdeptJRuntimeAdmin"), Configs.getStringList(CONFIG, "common.auth-roles"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"common.missing", "common.user-roles-mapping.nobody", "main.common.auth-roles"})
    void aMissingListYieldsNoEntriesRatherThanAnError(String path) {
        List<String> values = Configs.getStringList(CONFIG, path);

        assertTrue(values.isEmpty(), "SEC-09: a principal with no mapping must resolve to zero roles");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void aNullOrBlankPathYieldsAnEmptyList(String path) {
        assertEquals(List.of(), Configs.getStringList(CONFIG, path));
    }

    @Test
    void theReturnedEmptyListIsImmutable() {
        List<String> values = Configs.getStringList(CONFIG, "common.missing");

        assertThrows(UnsupportedOperationException.class, () -> values.add("OSGiAdmin"),
                "a caller must not be able to grant itself a role through the returned list");
    }

    @Test
    @DisplayName("a scalar where a list is expected propagates - callers must handle it")
    void wrongTypePropagates() {
        Config config = ConfigFactory.parseString("common { auth-roles: \"OSGiAdmin\" }");

        assertThrows(com.typesafe.config.ConfigException.WrongType.class,
                () -> Configs.getStringList(config, "common.auth-roles"));
    }

    @Test
    void nestedMapShapesAreReadableAsMaps() {
        Map<String, Object> mapping = CONFIG.getConfig("common.user-roles-mapping").root().unwrapped();

        assertEquals(Map.of("admin", List.of("OSGiAdmin")), Map.copyOf(mapping));
    }

    @Test
    void anExplicitlyEmptyListIsStillEmpty() {
        assertTrue(Configs.getStringList(CONFIG, "common.empty-roles").isEmpty());
    }

    @Test
    void stubbedConfigKeepsTheContract() {
        Config config = mock(Config.class);
        when(config.hasPath("common.auth-roles")).thenReturn(true);
        when(config.getStringList("common.auth-roles")).thenReturn(List.of("OSGiAdmin"));

        assertTrue(Configs.isPresent(config, "common.auth-roles"));
        assertEquals(List.of("OSGiAdmin"), Configs.getStringList(config, "common.auth-roles"));
    }
}
