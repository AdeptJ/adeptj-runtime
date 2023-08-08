package com.adeptj.runtime.jetty.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;

public class HealthCheckHandler extends AbstractHandler {

    private static final String HC_URI = "/hc";

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (request.getRequestURI().equals(HC_URI)) {
            response.setStatus(SC_OK);
            baseRequest.setHandled(true);
        }
    }
}
