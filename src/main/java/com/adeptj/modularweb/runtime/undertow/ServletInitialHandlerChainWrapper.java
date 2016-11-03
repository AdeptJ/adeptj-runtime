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
package com.adeptj.modularweb.runtime.undertow;

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
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class ServletInitialHandlerChainWrapper implements HandlerWrapper {

	@Override
	public HttpHandler wrap(HttpHandler intialHandler) {
		return new PredicateHandler(
				Predicates.and(Predicates.prefix("/static"), Predicates.suffixes("css", "js", "jpg", "png", "jpeg")),
				Handlers.resource(new ClassPathResourceManager(getClass().getClassLoader())), intialHandler);
	}
}
