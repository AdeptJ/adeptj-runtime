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

package io.adeptj.runtime.servlet;

import io.adeptj.runtime.common.CryptoSupport;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.adeptj.runtime.common.Constants.TOOLS_CRYPTO_URI;

/**
 * CryptoServlet that generates salt and corresponding hashed text.
 * <p>
 * Note: This is independent of OSGi and directly managed by Undertow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(
        name = "AdeptJ CryptoServlet",
        urlPatterns = {
                TOOLS_CRYPTO_URI
        },
        asyncSupported = true
)
public class CryptoServlet extends HttpServlet {

    private static final long serialVersionUID = -3839904764769823479L;

    private static final String RESP_JSON_FORMAT = "{\n" + "  \"salt\" : \"%s\",\n" + "  \"hash\" : \"%s\"\n" + "}";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String text = req.getParameter("text");
        if (StringUtils.isEmpty(text)) {
            resp.setContentType("text/plain");
            resp.getWriter().write("request parameter [text] can't be null!!");
            return;
        }
        String salt = CryptoSupport.saltBase64();
        resp.setContentType("application/json");
        resp.getWriter().write(String.format(RESP_JSON_FORMAT, salt, CryptoSupport.hashBase64(text, salt)));
    }
}