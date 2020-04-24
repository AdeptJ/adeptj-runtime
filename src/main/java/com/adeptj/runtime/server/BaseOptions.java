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
package com.adeptj.runtime.server;

import io.undertow.UndertowOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Option;
import org.xnio.Options;

import java.lang.reflect.Field;

/**
 * Base class for setting Undertow Server and Socket Options.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public abstract class BaseOptions {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    <T> Option<T> toOption(String name) {
        try {
            for (Field field : UndertowOptions.class.getFields()) {
                if (field.getName().equals(name)) {
                    return (Option<T>) field.get(null);
                }
            }
            for (Field field : Options.class.getFields()) {
                if (field.getName().equals(name)) {
                    return (Option<T>) field.get(null);
                }
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            this.logger.error("Exception while accessing field: [{}]", name, ex);
        }
        return null;
    }

}
