package com.adeptj.runtime.jetty.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HealthCheckHandler extends HandlerWrapper {

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().equals("/hc") || request.getRequestURI().equals("/health")) {
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            return;
        }
        super.handle(target, baseRequest, request, response);
    }
}
