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
package com.adeptj.runtime.html;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Test support that initialises the {@link TemplateEngine} singleton without booting the runtime.
 * <p>
 * {@code TemplateEngine} is an enum singleton whose constructor calls
 * {@code ConfigProvider.getInstance().getMainConfig().getConfig("common.pebble")}. Left alone, the
 * first test that touches the enum would start the whole configuration machinery and write files into
 * the working directory. This class forces that one-time initialisation while {@code ConfigProvider}
 * is stubbed, using the pebble settings {@code main/src/main/resources/application.conf} ships.
 * <p>
 * Class initialisation runs once per JVM, so the first test class to call {@link #get()} decides how
 * the engine is built and every later caller reuses it, which keeps results independent of test
 * execution order. Callers that need {@code ConfigProvider} stubbed for their own reasons must call
 * {@link #get()} <em>before</em> registering their own static mock, because Mockito allows only one
 * static mock per class per thread.
 *
 * @author AdeptJ Security Review.
 */
public final class TemplateEngines {

    /**
     * Mirrors the {@code common.pebble} section of the shipped {@code application.conf}, plus the
     * system console path the servlets redirect to. Deliberately a literal copy: a change to the
     * shipped defaults has to be acknowledged here as well.
     */
    private static final String RUNTIME_CONF = """
            # The pebble section is a sibling of common in the shipped application.conf.
            pebble {
              prefix: webapp/templates/
              suffix: ".html"
              cache-enabled: false
              strict-variables: false
              resource-bundle-dir: webapp/i18n/
            }
            common {
              system-console-path: "/system/console/bundles"
            }
            """;

    private static final TemplateEngine ENGINE = initEngine();

    private TemplateEngines() {
    }

    public static TemplateEngine get() {
        return ENGINE;
    }

    /**
     * A {@link Config} equivalent to the parts of the shipped configuration the web layer reads.
     */
    public static Config conf() {
        return ConfigFactory.parseString(RUNTIME_CONF);
    }

    private static TemplateEngine initEngine() {
        ConfigProvider provider = mock(ConfigProvider.class, withSettings().lenient());
        when(provider.getMainConfig()).thenReturn(conf());
        try (MockedStatic<ConfigProvider> mocked = mockStatic(ConfigProvider.class)) {
            mocked.when(ConfigProvider::getInstance).thenReturn(provider);
            // Touching the enum constant runs its constructor, the only code that reads the stub.
            return TemplateEngine.getInstance();
        }
    }
}
