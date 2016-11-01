package com.adeptj.modularweb.runtime.osgi;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.modularweb.runtime.common.CommonUtils;

/**
 * ErrorPageServlet that serves the error page w.r.t status(401, 403, 404, 500 etc.).
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class ErrorPageServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI();
		ServletOutputStream outputStream = resp.getOutputStream();
		if ("/error".equals(requestURI)) {
			outputStream.write(CommonUtils.toBytes(getClass().getResourceAsStream("/views/error/generic.html")));
		} else {
			InputStream resource = getClass()
					.getResourceAsStream(String.format("/views/error/%s.html", this.getStatusCode(requestURI)));
			if (resource == null) {
				outputStream.write(CommonUtils.toBytes(getClass().getResourceAsStream("/views/error/404.html")));
			} else {
				outputStream.write(CommonUtils.toBytes(resource));
			}
		}
	}

	private String getStatusCode(String requestURI) {
		return requestURI.substring(requestURI.lastIndexOf('/') + 1);
	}
}