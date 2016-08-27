package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindrot.jbcrypt.BCrypt;

import com.jolocom.webidproxy.users.User;

public class ResetPasswordServlet extends NonProxyServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(ResetPasswordServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");
		String code = request.getParameter("code");
		String password = request.getParameter("password");

		User user = WebIDProxyServlet.users.get(username);

		if (user == null || code == null || password == null || ! code.equals(user.getRecoverycode())) {

			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User " + username + " cannot reset password.");
			log.debug("User " + username + " cannot reset password.");
			return;
		}

		user.setPassword(BCrypt.hashpw(password,BCrypt.gensalt()));
		user.setRecoverycode(null);
		WebIDProxyServlet.users.put(user);
		
		String content = "{}";

		this.success(request, response, content, "application/json");
	}
}
