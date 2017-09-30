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

package com.adeptj.runtime.core;

import com.adeptj.runtime.common.ServletContextHolder;
import com.adeptj.runtime.exception.InitializationException;
import com.adeptj.runtime.osgi.FrameworkShutdownHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_KEY;
import static com.adeptj.runtime.common.Constants.BUNDLES_ROOT_DIR_VALUE;

/**
 * An ServletContainerInitializer that is called by the Container while startup is in progress.
 * This will further call onStartup method of all of the {@link HandlesTypes} classes registered with this ContainerInitializer.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@HandlesTypes(StartupAware.class)
public class ContainerInitializer implements ServletContainerInitializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartup(Set<Class<?>> startupAwareClasses, ServletContext context) throws ServletException {
        Logger logger = LoggerFactory.getLogger(ContainerInitializer.class);
        if (startupAwareClasses == null || startupAwareClasses.isEmpty()) {
            // We can't go ahead if FrameworkStartupHandler is not passed by container.
            logger.error("No @HandlesTypes(StartupAware) on classpath!!");
            throw new IllegalStateException("No @HandlesTypes(StartupAware) on classpath!!");
        } else {
            ServletContextHolder.INSTANCE.setServletContext(context);
            context.setInitParameter(BUNDLES_ROOT_DIR_KEY, BUNDLES_ROOT_DIR_VALUE);
            startupAwareClasses.forEach(startupAwareClass -> {
                logger.info("Handling @HandlesTypes: [{}]", startupAwareClass);
                try {
                    if (StartupAware.class.isAssignableFrom(startupAwareClass)) {
                        StartupAware.class.cast(startupAwareClass.getDeclaredConstructor().newInstance())
                                .onStartup(context);
                    } else {
                        logger.warn("Unknown @HandlesTypes: [{}]", startupAwareClass);
                        throw new IllegalStateException("Only StartupAware types are supported!!");
                    }
                } catch (Exception ex) {
                    logger.error("StartupAware Exception!!", ex);
                    throw new InitializationException("StartupAware Exception!!", ex);
                }
            });
            // If we are here means startup went well above, register FrameworkShutdownHandler now.
            context.addListener(FrameworkShutdownHandler.class);
        }
    }

}
