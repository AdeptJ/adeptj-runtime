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
package com.adeptj.modularweb.micro.bootstrap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * HttpServletRequestWrapper to deal with
 * {@link RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * OSGi HttpService is still taking into account {@link #getPathInfo()} and thus
 * the included Servlet was not found in the ServletRegistry by the actual
 * {@link #getRequestURI()}
 * 
 * Making use of javax.servlet.include.request_uri to get the included path.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public class IncludeHttpServletRequest extends HttpServletRequestWrapper {

	private String reqURI;

	public IncludeHttpServletRequest(HttpServletRequest request) {
		super(request);
		this.reqURI = (String) request.getAttribute("javax.servlet.include.request_uri");
	}

	@Override
	public String getPathInfo() {
		// return StringUtils.replace(this.reqURI, "/osgi", StringUtils.EMPTY);
		return this.reqURI;
	}

	@Override
	public String getServletPath() {
		// return "/osgi";
		return "/";
	}
}
