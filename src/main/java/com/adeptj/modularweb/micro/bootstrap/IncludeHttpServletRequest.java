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
