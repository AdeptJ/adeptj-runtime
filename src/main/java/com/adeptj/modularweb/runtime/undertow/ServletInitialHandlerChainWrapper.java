package com.adeptj.modularweb.runtime.undertow;

import io.undertow.predicate.Predicates;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;

/**
 * Handler returned by this HandlerWrapper is invoked before any Servlet handlers are invoked.
 * 
 * Here it registers Undertow ClassPath based ResourceHandler for serving static content.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class ServletInitialHandlerChainWrapper implements HandlerWrapper {

	@Override
	public HttpHandler wrap(HttpHandler initial) {
		return new PredicateHandler(
				Predicates.and(Predicates.prefix("/static"), Predicates.suffixes("css", "js", "jpg", "png", "jpeg")),
				new ResourceHandler(new ClassPathResourceManager(getClass().getClassLoader())), initial);
	}
}
