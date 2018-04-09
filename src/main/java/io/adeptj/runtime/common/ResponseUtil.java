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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * Utils for {@link HttpServletResponse}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static void sendError(HttpServletResponse resp, int errorCode) {
        try {
            resp.sendError(errorCode);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new SystemException(ex.getMessage(), ex);
        }
    }

    public static void unavailable(HttpServletResponse resp) {
        try {
            resp.sendError(SC_SERVICE_UNAVAILABLE);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new SystemException(ex.getMessage(), ex);
        }
    }

    public static void redirect(HttpServletResponse resp, String redirectUrl) {
        try {
            resp.sendRedirect(resp.encodeRedirectURL(redirectUrl));
        } catch (Exception ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new SystemException(ex.getMessage(), ex);
        }
    }

    public static void write(HttpServletResponse resp, String content) {
        try {
            resp.getWriter().write(content);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new SystemException(ex.getMessage(), ex);
        }
    }
}
