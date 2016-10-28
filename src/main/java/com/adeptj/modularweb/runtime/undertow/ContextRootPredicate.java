package com.adeptj.modularweb.runtime.undertow;

import com.adeptj.modularweb.runtime.common.Constants;

import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;

/**
 * Predicate checks if the request is for context root "/".
 * 
 * Rakesh.Kumar, AdeptJ
 */
public class ContextRootPredicate implements Predicate {

	@Override
	public boolean resolve(HttpServerExchange exchange) {
		return Constants.CONTEXT_PATH.equals(exchange.getRequestURI());
	}

}
