package com.adeptj.runtime.tomcat.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.Serial;

public class HealthCheckServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -302226466401000851L;

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        this.doHead(req, resp);
    }
}
