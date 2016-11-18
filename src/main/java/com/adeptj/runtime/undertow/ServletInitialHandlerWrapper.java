/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
package com.adeptj.runtime.undertow;

import java.util.List;

import com.adeptj.runtime.config.Configs;
import com.typesafe.config.Config;

import io.undertow.Handlers;
import io.undertow.predicate.Predicates;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;

/**
 * Handler returned by this HandlerWrapper is invoked before any Servlet handlers are invoked.
 * 
 * Here it registers Undertow ClassPath based ResourceHandler for serving static content.
 * If the Predicate grouping is true then it invokes the non blocking ResourceHandler
 * completely bypassing security handler chain, which is desirable as we don't need security
 * and blocking I/O to kick in while serving static content.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class ServletInitialHandlerWrapper implements HandlerWrapper {

	@Override
	public HttpHandler wrap(HttpHandler intialHandler) {
		Config undertowCfg = Configs.INSTANCE.undertow();
		List<String> extns = undertowCfg.getStringList("common.static-resource-extns");
		return new PredicateHandler(
				Predicates.and(Predicates.prefix(undertowCfg.getString("common.static-resource-prefix")),
						Predicates.suffixes(extns.toArray(new String[extns.size()]))),
				Handlers.resource(new ClassPathResourceManager(getClass().getClassLoader())), intialHandler);
	}
}
