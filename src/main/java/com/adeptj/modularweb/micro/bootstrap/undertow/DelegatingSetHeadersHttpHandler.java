/* 
 * =============================================================================
 * 
 * Copyright (c) 2016 AdeptJ
 * Copyright (c) 2016 Rakesh Kumar <irakeshk@outlook.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * =============================================================================
 */
package com.adeptj.modularweb.micro.bootstrap.undertow;

import java.util.Map;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

/**
 * DelegatingSetHeadersHttpHandler
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public class DelegatingSetHeadersHttpHandler implements HttpHandler {
	
	private final HttpHandler delegatee;
	
	private Map<HttpString, String> headers;
	
	public DelegatingSetHeadersHttpHandler(HttpHandler delegatee, Map<HttpString, String> headers) {
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
