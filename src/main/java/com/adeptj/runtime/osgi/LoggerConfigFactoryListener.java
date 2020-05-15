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
import com.adeptj.runtime.logging.LogbackManager;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.osgi.framework.ServiceEvent.REGISTERED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

/**
 * OSGi {@link ServiceListener} for getting logger config properties from LoggerConfigFactory services.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LoggerConfigFactoryListener implements ServiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfigFactoryListener.class);

    private final Lock lock;

    LoggerConfigFactoryListener() {
        this.lock = new ReentrantLock();
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        this.lock.tryLock();
        try {
            LogbackManager logbackManager = LogbackManagerHolder.getInstance().getLogbackManager();
            switch (event.getType()) {
                case REGISTERED:
                    logbackManager.addOSGiLoggers(event.getServiceReference());
                    break;
                case UNREGISTERING:
                    logbackManager.resetLoggers(event.getServiceReference());
                    break;
                default:
                    LOGGER.warn("Ignored ServiceEvent: [{}]", event.getType());
            }
        } finally {
            this.lock.unlock();
        }
    }
}
