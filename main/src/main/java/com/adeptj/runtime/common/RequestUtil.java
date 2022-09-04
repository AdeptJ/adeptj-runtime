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

import com.adeptj.runtime.exception.ServerException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;

/**
 * Utils for {@link HttpServletRequest}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class RequestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);

    private RequestUtil() {
    }

    public static Object getAttribute(HttpServletRequest req, String name) {
        return req.getAttribute(name);
    }

    public static boolean hasException(HttpServletRequest req) {
        return getAttribute(req, ERROR_EXCEPTION) != null;
    }

    public static String getException(HttpServletRequest req) {
        return ExceptionUtils.getStackTrace((Throwable) RequestUtil.getAttribute(req, ERROR_EXCEPTION));
    }

    public static void logout(HttpServletRequest req) {
        try {
            req.logout();
        } catch (ServletException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new ServerException(ex);
        }
    }
}
