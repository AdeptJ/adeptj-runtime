package com.adeptj.modularweb.runtime.common;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Utility for creating OSGi Filter for tracking/finding Services etc.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
public final class OSGiUtils {
	
	// No instantiation. Utility methods only.
	private OSGiUtils() {}

	public static Filter filter(BundleContext context, Class<?> objClass, String filterExpr) throws InvalidSyntaxException {
		StringBuilder filterExprBuilder = new StringBuilder();
		filterExprBuilder.append("(&(").append(Constants.OBJECTCLASS).append("=");
		filterExprBuilder.append(objClass.getName()).append(")");
		filterExprBuilder.append(filterExpr).append(")");
		return context.createFilter(filterExprBuilder.toString());
	}
}
