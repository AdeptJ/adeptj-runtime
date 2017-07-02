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

import javax.servlet.http.HttpServletRequest;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;

/**
 * Utils for {@link javax.servlet.http.HttpServletRequest}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Requests {

    private Requests() {
    }

    public static Object attr(HttpServletRequest req, String name) {
        return req.getAttribute(name);
    }

    public static boolean hasAttribute(HttpServletRequest req, String name) {
        return req.getAttribute(name) != null;
    }

    public static boolean hasException(HttpServletRequest req) {
        return req.getAttribute(ERROR_EXCEPTION) != null;
    }
}
