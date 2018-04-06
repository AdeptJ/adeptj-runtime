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

package io.adeptj.runtime.core;

import io.adeptj.runtime.common.ServletContextHolder;
import io.adeptj.runtime.exception.InitializationException;
import io.adeptj.runtime.osgi.FrameworkShutdownHandler;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

/**
 * An SCI(ServletContainerInitializer) that is called by the Container while startup is in progress.
 * This will further call onStartup method of all of the {@link HandlesTypes} classes registered with this SCI.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@HandlesTypes(StartupAware.class)
public class RuntimeInitializer implements ServletContainerInitializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartup(Set<Class<?>> startupAwareClasses, ServletContext context) {
        Logger logger = LoggerFactory.getLogger(RuntimeInitializer.class);
        if (startupAwareClasses == null || startupAwareClasses.isEmpty()) {
            logger.error("No @HandlesTypes(StartupAware) on classpath!!");
            throw new IllegalStateException("No @HandlesTypes(StartupAware) on classpath!!");
        } else {
            ServletContextHolder.INSTANCE.setServletContext(context);
            startupAwareClasses
                    .stream()
                    .sorted(new StartupAwareComparator())
                    .forEach(clazz -> {
                        logger.info("@HandlesTypes: [{}]", clazz);
                        try {
                            StartupAware.class.cast(ConstructorUtils.invokeConstructor(clazz))
                                    .onStartup(context);
                        } catch (Exception ex) { // NOSONAR
                            logger.error("Exception while executing StartupAware#onStartup!!", ex);
                            throw new InitializationException(ex);
                        }
                    });
            context.addListener(FrameworkShutdownHandler.class);
        }
    }
}
