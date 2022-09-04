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
package com.adeptj.runtime.undertow.predicate;

import io.undertow.predicate.Predicate;
import io.undertow.server.HttpServerExchange;

/**
 * Predicate just checks if the request path is / i.e. root
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ContextPathPredicate implements Predicate {

    private final String contextPath;

    public ContextPathPredicate(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public boolean resolve(HttpServerExchange value) {
        return value.getRequestPath().equals(this.contextPath);
    }
}
