/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
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
package com.adeptj.runtime.jaxrs;

import org.jboss.resteasy.spi.Registry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResourceTracker is an OSGi ServiceTracker which tracks the services annotated with @Path JAX-RS annotation.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public class ResourceTracker extends ServiceTracker<Object, Object> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTracker.class);
	
	private Registry registry;
    
	public ResourceTracker(BundleContext context, Filter filter, Registry registry) {
		super(context, filter, null);
		this.registry = registry;
	}

	@Override
	public Object addingService(ServiceReference<Object> reference) {
		Object resource = super.addingService(reference);
		LOGGER.info("Adding JAX-RS Resource: [{}]", resource);
		this.registry.addSingletonResource(resource);
		return resource;
	}

	@Override
	public void removedService(ServiceReference<Object> reference, Object service) {
		super.removedService(reference, service);
		LOGGER.info("Removing JAX-RS Resource: [{}]", service);
		this.registry.removeRegistrations(service.getClass());
	}

}
