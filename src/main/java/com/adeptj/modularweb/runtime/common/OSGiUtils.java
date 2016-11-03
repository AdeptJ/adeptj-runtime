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
	private OSGiUtils() {
	}

	public static Filter filter(BundleContext context, Class<?> objectClass, String filterExpr) {
		try {
			StringBuilder filterExprBuilder = new StringBuilder();
			filterExprBuilder.append("(&(").append(Constants.OBJECTCLASS).append("=");
			filterExprBuilder.append(objectClass.getName()).append(")");
			filterExprBuilder.append(filterExpr).append(")");
			return context.createFilter(filterExprBuilder.toString());
		} catch (InvalidSyntaxException ex) {
			// Probable causes.
			// 1. objectClass is malformed.
			// 2. Filter expression is malformed, not RFC 1960-based Filter.
			throw new IllegalArgumentException("Unexpected InvalidSyntaxException!!", ex);
		}
	}
}
