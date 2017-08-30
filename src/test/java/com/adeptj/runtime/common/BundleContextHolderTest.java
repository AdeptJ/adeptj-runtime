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
package com.adeptj.runtime.common;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;

/**
 * BundleContextHolderTest.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@Ignore
public class BundleContextHolderTest {

	@Test
	public void testIsBundleContextSetWhenSetBundleContextCalled() {
		Mockito.when(BundleContextHolder.INSTANCE.getBundleContext()).thenReturn(Mockito.mock(BundleContext.class));
		Assert.assertTrue(BundleContextHolder.INSTANCE.isBundleContextAvailable());
	}
	
	@Test
	public void testIsBundleContextSetWhenSetBundleContextNotCalled() {
		//Assert.assertFalse(BundleContextHolder.INSTANCE.isBundleContextSet());
		Mockito.eq(BundleContextHolder.INSTANCE.isBundleContextAvailable());
	}
}
