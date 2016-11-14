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
package com.adeptj.runtime.viewengine;

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

	private ViewEngineContext(String view, Models models, HttpServletRequest req, HttpServletResponse resp, Locale locale) {
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
	
	/**
	 * Builder for ViewEngineContext.
	 * 
	 * @author Rakesh.Kumar, AdeptJ.
	 */
	public static class Builder {
		
		private String view;

		private Models models;

		private HttpServletRequest request;

		private HttpServletResponse response;

		private Locale locale;
		
		public Builder view(String view) {
			this.view = view;
			return this;
		}
		
		public Builder models(Models models) {
			this.models = models;
			return this;
		}
		
		public Builder request(HttpServletRequest request) {
			this.request = request;
			return this;
		}
		
		public Builder response(HttpServletResponse response) {
			this.response = response;
			return this;
		}
		
		public Builder locale(Locale locale) {
			this.locale = locale;
			return this;
		}
		
		public ViewEngineContext build() {
			return new ViewEngineContext(view, models, request, response, locale);
		}
	}
}
