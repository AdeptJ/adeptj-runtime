/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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

package com.adeptj.runtime.osgi;

import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.logging.LogbackConfig;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

/**
 * OSGi {@link ServiceListener} for getting logger config properties from LoggerConfigFactory services.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LoggerConfigFactoryListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfigFactoryListener.class);

    private static final String KEY_LOGGER_NAME = "logger.name";

    private static final String KEY_LOGGER_LEVEL = "logger.level";

    private static final String KEY_LOGGER_ADDITIVITY = "logger.additivity";

    @Override
    public void serviceChanged(ServiceEvent event) {
        ServiceReference<?> reference = event.getServiceReference();
        String logger = (String) reference.getProperty(KEY_LOGGER_NAME);
        String level = (String) reference.getProperty(KEY_LOGGER_LEVEL);
        boolean additivity = (boolean) reference.getProperty(KEY_LOGGER_ADDITIVITY);
        switch (event.getType()) {
            case REGISTERED:
                LOGGER.debug("REGISTERED: {}", reference);
                LogbackManagerHolder.getInstance().getLogbackManager()
                        .addOSGiLogger(LogbackConfig.builder()
                                .logger(logger)
                                .level(level)
                                .additivity(additivity)
                                .build());
                break;
            case UNREGISTERING:
                LOGGER.debug("UNREGISTERING: {}", reference);
                LogbackManagerHolder.getInstance().getLogbackManager()
                        .resetOSGiLogger(LogbackConfig.builder()
                                .logger(logger)
                                .level(level)
                                .additivity(additivity)
                                .build());
                break;
            default:
                LOGGER.debug("Unknown ServiceEvent!");
        }
    }
}
