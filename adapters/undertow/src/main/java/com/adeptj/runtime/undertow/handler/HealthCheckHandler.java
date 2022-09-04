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

package com.adeptj.runtime.undertow.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import static io.undertow.util.StatusCodes.OK;

/**
 * Just returns a 200 OK for a health check request and simply terminate the request.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class HealthCheckHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(OK);
        exchange.endExchange();
    }
}
