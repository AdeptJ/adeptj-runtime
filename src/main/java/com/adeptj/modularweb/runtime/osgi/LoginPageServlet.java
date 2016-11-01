package com.adeptj.modularweb.runtime.osgi;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.adeptj.modularweb.runtime.common.CommonUtils;

/**
 * LoginPageServlet renders the login page.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LoginPageServlet extends HttpServlet {

	private static final long serialVersionUID = -3339904764769823449L;

	/**
	 * Render login page.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getOutputStream().write(CommonUtils.toBytes(getClass().getResourceAsStream("/views/auth/login.html")));
	}

	/**
	 * Post comes here when login to "/j_security_check" fails.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendRedirect("/login");
	}
}