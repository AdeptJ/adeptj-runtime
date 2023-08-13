package com.adeptj.runtime.tomcat.filter;

import com.adeptj.runtime.kernel.util.RequestUtil;
import com.adeptj.runtime.kernel.util.ResponseUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ContextPathFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest req
                && response instanceof HttpServletResponse resp && RequestUtil.isContextRootRequest(req)) {
            ResponseUtil.redirectToSystemConsole(resp);
            return;
        }
        chain.doFilter(request, response);
    }
}
