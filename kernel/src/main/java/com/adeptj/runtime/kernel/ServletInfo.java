package com.adeptj.runtime.kernel;

import jakarta.servlet.http.HttpServlet;

public record ServletInfo(String servletName, String path, Class<? extends HttpServlet> servletClass) {
}
