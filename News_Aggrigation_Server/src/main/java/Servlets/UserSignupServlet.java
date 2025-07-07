package Servlets;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.UserRepository;
import Service.RegistrationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.JsonResponse;
import util.RequestParser;

@WebServlet("/UserSignup")
public class UserSignupServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(UserSignupServlet.class);
	private final RegistrationService registrationService;
	private final RequestParser requestParser;

	public UserSignupServlet() {
		this.registrationService = new RegistrationService();
		this.requestParser = new RequestParser();
	}

	public UserSignupServlet(RegistrationService userService, RequestParser requestParser) {
		this.registrationService = userService;
		this.requestParser = new RequestParser();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		try {
			JSONObject jsonInput = requestParser.parseRequestBody(request);
			User user = createUserFromJson(jsonInput);
			logger.info("Processing registration request for username: {}", user.getUsername());
			registrationService.register(user);
			response.setStatus(HttpServletResponse.SC_CREATED);
			JsonResponse.writeSuccess(response, "User registered successfully", null);
			logger.info("Registration successful for username: {}", user.getUsername());
		} catch (UserRepository.DuplicateKeyException e) {
			logger.warn("Registration failed: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			JsonResponse.writeError(response, e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid registration request: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error during registration", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	private User createUserFromJson(JSONObject jsonInput) {
		String username = jsonInput.getString("username");
		String email = jsonInput.getString("email");
		String password = jsonInput.getString("password");
		boolean isAdmin = jsonInput.optBoolean("isAdmin", false);
		return new User(username, email, password, isAdmin);
	}
}