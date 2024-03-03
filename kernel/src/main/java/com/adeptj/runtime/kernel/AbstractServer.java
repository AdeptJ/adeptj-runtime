/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.kernel;

import com.adeptj.runtime.kernel.security.DefaultUserManager;
import com.adeptj.runtime.kernel.util.Times;
import com.typesafe.config.Config;
import org.slf4j.Logger;

import java.util.List;

import static com.adeptj.runtime.kernel.Constants.SYS_PROP_SERVER_PORT;

/**
 * AbstractServer providing the common code for Server implementations such as Jetty, Tomcat and Undertow.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public abstract class AbstractServer implements Server {

    private final UserManager userManager;

    private ServerPostStopTask postStopTask;

    protected AbstractServer() {
        this.userManager = new DefaultUserManager();
    }

    public void setServerPostStopTask(ServerPostStopTask postStopTask) {
        this.postStopTask = postStopTask;
    }

    protected abstract Logger getLogger();

    @Override
    public void stop() {
        long startTime = System.nanoTime();
        this.getLogger().info("Stopping AdeptJ Runtime!!");
        try {
            this.doStop();
            this.getLogger().info("AdeptJ Runtime stopped in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Throwable ex) { // NOSONAR
            this.getLogger().error("Exception while stopping AdeptJ Runtime!!", ex);
        } finally {
            this.postStopTask.execute();
        }
    }

    protected abstract void doStop() throws Exception;

    @Override
    public void registerServlets(List<ServletInfo> servletInfos) {
        servletInfos.forEach(this::doRegisterServlet);
    }

    @Override
    public void registerFilters(List<FilterInfo> filterInfos) {
        filterInfos.forEach(this::doRegisterFilter);
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    protected abstract void doRegisterServlet(ServletInfo info);

    protected abstract void doRegisterFilter(FilterInfo info);

    protected int resolvePort(Config appConfig) {
        ServerRuntime runtime = this.getRuntime();
        // First check - system property.
        Integer port = Integer.getInteger("adeptj.rt.port");
        if (port == null || port == 0) {
            this.getLogger().info("No port specified via system property: [{}], will resolve port from configs!",
                    SYS_PROP_SERVER_PORT);
            // Second check - server configs.
            port = appConfig.getConfig(runtime.getLowerCaseName()).getInt("http.port");
            if (port == 0) {
                // Third and last check - kernel configs.
                port = appConfig.getInt("kernel.server.port");
                this.getLogger().info("Resolved port({}) from kernel configs!", port);
            } else {
                this.getLogger().info("Resolved port({}) from server({}) configs!", port, runtime);
            }
        }
        this.getLogger().info("Starting {} on port: {}", runtime, port);
        return port;
    }
}
