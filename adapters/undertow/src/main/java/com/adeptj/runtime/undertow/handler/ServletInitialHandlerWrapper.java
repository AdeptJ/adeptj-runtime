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

package com.adeptj.runtime.undertow.handler;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.typesafe.config.Config;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.handlers.ServletInitialHandler;

import static com.adeptj.runtime.kernel.ServerRuntime.UNDERTOW;

/**
 * Handler returned by this HandlerWrapper is invoked before any Servlet handlers are invoked.
 * <p>
 * Here it registers Undertow's ClassPath based ResourceHandler for serving static content.
 * If the Predicate grouping is true then it invokes the non blocking ResourceHandler
 * completely bypassing security handler chain, which is desirable as we don't need security
 * and blocking I/O to kick in while serving static content.
 * <p>
 * Undertow order of precedence to invoke HandlerChainWrapper(s).
 * <p>
 * 1. OuterHandlerChainWrapper - Run after the servlet initial handler, but before any other handlers.
 * These are only run on REQUEST invocations, they are not invoked on a FORWARD or INCLUDE.
 * <p>
 * 2. InnerHandlerChainWrapper - This handler will run after the security handler, but before any other
 * servlet handlers, and will run for every request.
 * <p>
 * 3. InitialHandlerChainWrapper - Run before security and servlet initial handler.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ServletInitialHandlerWrapper implements HandlerWrapper {

    private static final String RESOURCE_PREFIX = "common.static-resource-prefix";

    private static final String RESOURCE_EXTNS = "common.static-resource-extns";

    private static final String RESOURCE_MGR_PREFIX = "common.resource-mgr-prefix";

    /**
     * Wraps the passed {@link ServletInitialHandler} with a PredicateHandler for serving static content.
     *
     * @param servletInitialHandler the ServletInitialHandler
     * @return PredicateHandler which decides whether to invoke ResourceHandler or pass on the request to
     * next handler in the chain which is ServletInitialHandler.
     * @see ServletInitialHandlerWrapper class header for detailed information.
     */
    @Override
    public HttpHandler wrap(HttpHandler servletInitialHandler) {
        Config cfg = ConfigProvider.getInstance().getServerConfig(UNDERTOW);
        Predicate prefix = Predicates.prefix(cfg.getString(RESOURCE_PREFIX));
        Predicate suffixes = Predicates.suffixes(cfg.getStringList(RESOURCE_EXTNS).toArray(new String[0]));
        ClassPathResourceManager rm = new ClassPathResourceManager(this.getClass().getClassLoader(),
                cfg.getString(RESOURCE_MGR_PREFIX));
        return Handlers.predicate(Predicates.and(prefix, suffixes), Handlers.resource(rm), servletInitialHandler);
    }
}
