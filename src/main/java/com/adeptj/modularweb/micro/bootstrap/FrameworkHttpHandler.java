package com.adeptj.modularweb.micro.bootstrap;

import java.util.Map;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 * FrameworkHttpHandler
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class FrameworkHttpHandler implements HttpHandler {
	
	private final HttpHandler delegatee;
	
	private Map<HttpString, String> headers;
	
	public FrameworkHttpHandler(HttpHandler delegatee, Map<HttpString, String> headers) {
		this.delegatee = delegatee;
		this.headers = headers;
	}

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		HeaderMap responseHeaders = exchange.getResponseHeaders();
		this.headers.forEach((headerName, headerValue) -> {
			responseHeaders.put(headerName, headerValue);
		});
		delegatee.handleRequest(exchange);
	}

}
