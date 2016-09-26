package com.adeptj.modularweb.micro.undertow.bootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * StartupHandler that will be called by the FrameworkServletContainerInitializer while startup is in
 * progress.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public interface StartupHandler {

	/**
	 * This method will be called by the FrameworkServletContainerInitializer while startup is in
	 * progress.
	 * 
	 * @param context
	 * @throws ServletException
	 */
	void onStartup(ServletContext context) throws ServletException;
}
