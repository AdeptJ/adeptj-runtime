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
package com.adeptj.core.util;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.adeptj.core.initializer.StartupHandler;

/**
 * The annotated type's {@link StartupHandler#onStartup(javax.servlet.ServletContext)} must be call in the 
 * StartupOrder#order() specified as ascending order. If the order of one or many StartupHandler same then they
 * are called in an unspecified order.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface StartupOrder {

	public int value() default 0;
}
