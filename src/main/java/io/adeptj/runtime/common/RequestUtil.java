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

package io.adeptj.runtime.common;

import io.adeptj.runtime.exception.SystemException;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;

/**
 * Utils for {@link HttpServletRequest}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class RequestUtil {

    private RequestUtil() {
    }

    public static Object getAttribute(HttpServletRequest req, String name) {
        return req.getAttribute(name);
    }

    public static boolean hasAttribute(HttpServletRequest req, String name) {
        return getAttribute(req, name) != null;
    }

    public static boolean hasException(HttpServletRequest req) {
        return req.getAttribute(ERROR_EXCEPTION) != null;
    }

    public static void logException(HttpServletRequest req, Logger logger, String message) {
        if (RequestUtil.hasException(req)) {
            logger.error(message, req.getAttribute(ERROR_EXCEPTION));
        }
    }

    public static void logout(HttpServletRequest req) {
        try {
            req.logout();
        } catch (Exception ex) {
            throw new SystemException(ex.getMessage(), ex);
        }
    }
}
