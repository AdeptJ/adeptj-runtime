package com.adeptj.runtime.jetty.handler;

import com.adeptj.runtime.kernel.ConfigProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_FOUND;

public class ContextPathHandler extends HandlerWrapper {

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/") || requestURI.startsWith("/;jsessionid")) {
            response.setStatus(SC_FOUND);
            String systemConsolePath = ConfigProvider.getInstance()
                    .getMainConfig()
                    .getString("common.system-console-path");
            response.setHeader(HttpHeader.LOCATION.toString(), systemConsolePath);
            baseRequest.setHandled(true);
            return;
        }
        super.handle(target, baseRequest, request, response);
    }
}
