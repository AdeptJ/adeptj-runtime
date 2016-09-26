/**
 * 
 */
package com.adeptj.modularweb.undertow.bootstrap;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;

/**
 * This enum provides the access to the {@link ServletContext}
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public enum ServletContextAware {

	INSTANCE;

	private ServletContext context;

	protected void setServletContext(ServletContext context) {
		this.context = context;
	}

	public ServletContext getServletContext() {
		return this.context;
	}

	public <T> T getAttr(String name, Class<T> type) {
		return type.cast(this.context.getAttribute(name));
	}
	
	public BundleContext getBundleContext() {
		return this.getAttr(BundleContext.class.getName(), BundleContext.class);
	}
}
