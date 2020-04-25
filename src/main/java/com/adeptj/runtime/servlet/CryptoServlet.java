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

package com.adeptj.runtime.servlet;

import com.adeptj.runtime.common.CryptoSupport;
import com.adeptj.runtime.common.ResponseUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.adeptj.runtime.common.Constants.CRYPTO_SERVLET_URI;

/**
 * A simple servlet that generates salt and corresponding hashed text.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@WebServlet(name = "AdeptJ CryptoServlet", urlPatterns = CRYPTO_SERVLET_URI, asyncSupported = true)
public class CryptoServlet extends HttpServlet {

    private static final long serialVersionUID = -3839904764769823479L;

    private static final String RESP_JSON_FORMAT = "{" + "\"salt\":\"%s\"," + "\"hash\":\"%s\"" + "}";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String text = req.getParameter("text");
        if (StringUtils.isEmpty(text)) {
            resp.setContentType("text/plain");
            ResponseUtil.write(resp, "request parameter [text] can't be null!!");
        } else {
            resp.setContentType("application/json");
            String salt = CryptoSupport.saltBase64();
            ResponseUtil.write(resp, String.format(RESP_JSON_FORMAT, salt, CryptoSupport.hashBase64(text, salt)));
        }
    }
}