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
import com.adeptj.runtime.common.OSGiUtil;
import com.adeptj.runtime.logging.LogbackConfig;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * OSGi {@link ServiceListener} for getting logger config properties from LoggerConfigFactory services.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LoggerConfigFactoryListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfigFactoryListener.class);

    private static final String KEY_LOGGER_NAMES = "logger.names";

    private static final String KEY_LOGGER_LEVEL = "logger.level";

    private static final String KEY_LOGGER_ADDITIVITY = "logger.additivity";

    private final Lock lock;

    private final List<String> configPids;

    public LoggerConfigFactoryListener() {
        this.lock = new ReentrantLock();
        this.configPids = new ArrayList<>();
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        this.lock.tryLock();
        try {
            ServiceReference<?> reference = event.getServiceReference();
            Set<String> categories = new HashSet<>(OSGiUtil.getCollection(reference, KEY_LOGGER_NAMES));
            String level = OSGiUtil.getString(reference, KEY_LOGGER_LEVEL);
            String pid = OSGiUtil.getString(reference, SERVICE_PID);
            switch (event.getType()) {
                case REGISTERED:
                    int size = categories.size();
                    if (size == 0 || categories.remove(EMPTY)) {
                        LOGGER.warn("Can't add loggers because logger.names array property is empty!!");
                        return;
                    }
                    if (size == 1 && categories.contains(ROOT_LOGGER_NAME)) {
                        LOGGER.warn("Adding a ROOT logger is not allowed!!");
                        return;
                    }
                    if (categories.remove(ROOT_LOGGER_NAME)) {
                        LOGGER.warn("Removed ROOT logger from the categories!!");
                    }
                    LOGGER.info("Adding loggers for categories {} and level {}", categories, level);
                    LogbackManagerHolder.getInstance().getLogbackManager()
                            .addOSGiLoggers(LogbackConfig.builder()
                                    .categories(categories)
                                    .level(level)
                                    .additivity(OSGiUtil.getBoolean(reference, KEY_LOGGER_ADDITIVITY))
                                    .build());
                    this.configPids.add(pid);
                    break;
                case UNREGISTERING:
                    if (this.configPids.remove(pid)) {
                        LOGGER.info("Removing loggers for categories {} and level {}", categories, level);
                        LogbackManagerHolder.getInstance().getLogbackManager().reset();
                    }
                    break;
                default:
                    LOGGER.warn("Ignored ServiceEvent: [{}]", event.getType());
            }
        } finally {
            this.lock.unlock();
        }
    }
}
