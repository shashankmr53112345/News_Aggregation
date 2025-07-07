package Servlets;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonResponse;
import util.RequestParser;

@WebServlet("/UserLogin")
public class LoginServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
	private final AuthenticationService authService;
	private final RequestParser requestParser;

	public LoginServlet() {
		this.authService = new AuthenticationService();
		this.requestParser = new RequestParser();
		;
	}

	public LoginServlet(AuthenticationService userService, RequestParser requestParser) {
		this.authService = new AuthenticationService();
		this.requestParser = new RequestParser();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		try {
			JSONObject jsonInput = requestParser.parseRequestBody(request);
			String username = jsonInput.getString("username");
			String password = jsonInput.getString("password");

			logger.info("Processing login request for username: {}", username);
			var user = authService.authenticate(username, password);

			response.setStatus(HttpServletResponse.SC_OK);
			JSONObject userData = new JSONObject().put("username", user.getUsername()).put("email", user.getEmail())
					.put("isAdmin", user.isAdmin());
			JsonResponse.writeSuccess(response, "Login successful", userData);
			logger.info("Login successful for username: {}", username);
		} catch (IllegalArgumentException e) {
			logger.warn("Authentication failed: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			JsonResponse.writeError(response, e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during login", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}