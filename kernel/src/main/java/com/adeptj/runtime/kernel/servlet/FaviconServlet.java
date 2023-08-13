package com.adeptj.runtime.kernel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serial;

public class FaviconServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -4158958392097965401L;

    private static final String CONTENT_TYPE = "image/x-icon";

    private static final String FAVICON_PATH = "/static/img/favicon.ico";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType(CONTENT_TYPE);
        req.getRequestDispatcher(FAVICON_PATH).include(req, resp);
    }
}
