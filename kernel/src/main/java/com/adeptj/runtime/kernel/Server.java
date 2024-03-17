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

import com.typesafe.config.Config;
import jakarta.servlet.ServletContainerInitializer;

import java.util.List;

/**
 * Server interface to be implemented by a given server adapter such as Tomcat, Jetty, Undertow etc.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public interface Server {

    ServerRuntime getRuntime();

    /**
     * Starts the given {@link Server} instance.
     *
     * @param deployment Servlet deployment information for (Tomcat or Jetty or Undertow) instance.
     * @param appConfig  the application configuration.
     * @param args       the program arguments to the server instance.
     */
    void start(ServletDeployment deployment, Config appConfig, String[] args) throws Exception;

    default void postStart() {
        // NOOP
    }

    void stop();

    default void registerServlets(List<ServletInfo> servletInfos) {
    }

    default void registerFilters(List<FilterInfo> filterInfos) {
    }

    default void registerErrorPages(List<Integer> errorCodes) {
    }

    void addServletContextAttribute(String name, Object value);

    UserManager getUserManager();
}
