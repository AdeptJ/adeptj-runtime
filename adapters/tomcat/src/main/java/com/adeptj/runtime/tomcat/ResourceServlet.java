package com.adeptj.runtime.tomcat;

import org.apache.catalina.servlets.DefaultServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class ResourceServlet extends DefaultServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            super.doGet(req, resp);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
