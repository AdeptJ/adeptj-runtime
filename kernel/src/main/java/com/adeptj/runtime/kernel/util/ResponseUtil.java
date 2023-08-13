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

package com.adeptj.runtime.kernel.util;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.exception.ServerException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * Utils for {@link HttpServletResponse}
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static void serverError(HttpServletResponse resp) {
        try {
            resp.sendError(SC_INTERNAL_SERVER_ERROR);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new ServerException(ex);
        }
    }

    public static void sendError(HttpServletResponse resp, int errorCode) {
        try {
            resp.sendError(errorCode);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new ServerException(ex);
        }
    }

    public static void unavailable(HttpServletResponse resp) {
        try {
            resp.sendError(SC_SERVICE_UNAVAILABLE);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new ServerException(ex);
        }
    }

    public static void redirect(HttpServletResponse resp, String location) {
        try {
            resp.sendRedirect(resp.encodeRedirectURL(location));
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new ServerException(ex);
        }
    }

    public static void redirectToSystemConsole(HttpServletResponse resp) {
        String systemConsoleUrl = ConfigProvider.getInstance()
                .getMainConfig()
                .getString("common.system-console-path");
        redirect(resp, systemConsoleUrl);
    }

    public static void write(HttpServletResponse resp, String content) {
        try {
            resp.getWriter().write(content);
        } catch (IOException ex) {
            // Now what? may be wrap and re-throw. Let the container handle it.
            throw new ServerException(ex);
        }
    }
}
