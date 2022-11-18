package com.adeptj.runtime.jetty.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_FOUND;

public class ContextPathHandler extends HandlerWrapper {

    private static final String HEADER_LOC = "Location";

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().equals("/") || request.getRequestURI().startsWith("/;jsessionid")) {
            response.setStatus(SC_FOUND);
            response.setHeader(HEADER_LOC, "/system/console/bundles");
            baseRequest.setHandled(true);
            return;
        }
        super.handle(target, baseRequest, request, response);
    }
}
