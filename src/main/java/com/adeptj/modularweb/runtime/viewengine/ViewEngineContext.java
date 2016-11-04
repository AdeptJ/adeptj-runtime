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
package com.adeptj.modularweb.runtime.viewengine;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ViewEngineContext.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public class ViewEngineContext {

	private final String view;

	private final Models models;

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final Locale locale;

	public ViewEngineContext(String view, Models models, HttpServletRequest req, HttpServletResponse resp, Locale locale) {
		this.view = view;
		this.models = models;
		this.request = req;
		this.response = resp;
		this.locale = locale;
	}

	public String getView() {
		return view;
	}

	public Models getModels() {
		return models;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public Locale getLocale() {
		return locale;
	}
}
