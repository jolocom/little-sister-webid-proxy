package com.jolocom.webidproxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jolocom.webidproxy.users.User;

public class ForgotPasswordServlet extends NonProxyServlet {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Log log = LogFactory.getLog(ForgotPasswordServlet.class);

	private static final int RECOVERYCODE_LENGTH = 8;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter("username");

		User user = WebIDProxyServlet.users.get(username);

		if (user != null) {

			String recoverycode = generateRecoverycode();
			user.setRecoverycode(recoverycode);
			WebIDProxyServlet.users.put(user);
		}

		log.debug("User " + username + " forgot password, created recovery code.");

		String content = "{}";

		this.success(request, response, content, "application/json");
	}

	private static String generateRecoverycode() {

		return RandomStringUtils.randomAlphanumeric(RECOVERYCODE_LENGTH);
	}
}
