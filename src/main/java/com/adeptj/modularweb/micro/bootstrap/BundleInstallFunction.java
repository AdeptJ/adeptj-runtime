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

import java.net.URL;
import java.util.function.Function;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BundleInstallFunction.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@FunctionalInterface
public interface BundleInstallFunction extends Function<URL, Bundle> {
	
	Logger LOGGER = LoggerFactory.getLogger(BundleInstallFunction.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Bundle apply(URL url) {
		LOGGER.debug("Installing Bundle from location: [{}]", url);
		Bundle bundle = null;
		try {
			bundle = this.applyWithThrows(url);
		} catch (Exception ex) {
			LOGGER.error("Exception while installing bundle: [{}]. Exception: {}", url, ex);
		}
		return bundle;
	}
	
	/**
	 * To deal with checked exception in Lambda function.
	 */
	Bundle applyWithThrows(URL url) throws Exception;
}
