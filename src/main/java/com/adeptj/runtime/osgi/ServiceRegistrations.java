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
import com.adeptj.runtime.extensions.logging.LogbackManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

/**
 * Utility for performing operations on OSGi {@link ServiceRegistration} instances.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum ServiceRegistrations {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistrations.class);

    private ServiceRegistration<LogbackManager> logbackManager;

    public void registerLogbackManager(BundleContext systemBundleContext) {
        this.logbackManager = systemBundleContext
                .registerService(LogbackManager.class,
                        LogbackManagerHolder.getInstance().getLogbackManager(),
                        new Hashtable<>());
    }

    public void unregisterLogbackManager() {
        if (this.logbackManager != null) {
            try {
                LOGGER.info("Removing LogbackManager!!");
                this.logbackManager.unregister();
            } catch (Exception ex) { // NOSONAR
                LOGGER.error(ex.getMessage(), ex);
            }
            this.logbackManager = null;
        }
    }

    public static ServiceRegistrations getInstance() {
        return INSTANCE;
    }
}
