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

import java.util.function.Consumer;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BundleStartConsumer.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@FunctionalInterface
public interface BundleStartConsumer extends Consumer<Bundle> {

	Logger LOGGER = LoggerFactory.getLogger(BundleStartConsumer.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void accept(Bundle bundle) {
		LOGGER.info("Starting bundle: [{}] version: [{}]", bundle, bundle.getVersion());
		try {
			this.acceptWithThrows(bundle);
		} catch (Exception ex) {
			LOGGER.error("Exception while starting bundle: [{}]. Exception: {}", bundle, ex);
		}
	}

	/**
	 * To deal with checked exception in Lambda function.
	 */
	void acceptWithThrows(Bundle bundle) throws Exception;
}
