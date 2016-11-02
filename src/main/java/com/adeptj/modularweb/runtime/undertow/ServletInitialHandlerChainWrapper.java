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
