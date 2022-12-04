package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.ConfigProvider;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_SEE_OTHER;

public class ContextPathFilter implements Filter {

    private static final String HEADER_LOC = "Location";

    private static final String HTTP_1_1 = "HTTP/1.1";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            if (req.getRequestURI().equals("/") || req.getRequestURI().startsWith("/;jsessionid")) {
                if (HTTP_1_1.equals(request.getProtocol())) {
                    resp.setStatus(SC_SEE_OTHER);
                } else {
                    resp.setStatus(SC_FOUND);
                }
                String redirectUrl = ConfigProvider.getInstance().getMainConfig().getString("common.system-console-path");
                resp.setHeader(HEADER_LOC, redirectUrl);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
