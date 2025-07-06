package Servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;

import Repository.UserAuthenticationRepository.DuplicateKeyException;
import Service.UserAuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.JsonResponseBody;

@WebServlet("/UserSignup")
public class UserSignupServlet extends HttpServlet {
	private final UserAuthenticationService userService;

	public UserSignupServlet() {
		this.userService = new UserAuthenticationService();
	}

	public UserSignupServlet(UserAuthenticationService userService) {
		this.userService = userService;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		try {
			JSONObject jsonInput = parseRequestBody(request);
			User user = createUserFromJson(jsonInput);
			userService.registerUser(user);
			response.setStatus(HttpServletResponse.SC_CREATED);
			out.println(new JsonResponseBody(true, "User registered successfully").toJson());
		} catch (DuplicateKeyException e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			out.println(new JsonResponseBody(false, e.getMessage()).toJson());
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.println(new JsonResponseBody(false, e.getMessage()).toJson());
		} catch (JSONException e) {
			System.err.println("Invalid JSON input for signup: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.println(new JsonResponseBody(false, "Invalid request format").toJson());
		} catch (Exception e) {
			System.err.println("Unexpected error during signup: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.println(new JsonResponseBody(false, "Server error").toJson());
		}
	}

	private JSONObject parseRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		}
		return new JSONObject(buffer.toString());
	}

	private User createUserFromJson(JSONObject jsonInput) {
		String username = jsonInput.getString("username");
		String email = jsonInput.getString("email");
		String password = jsonInput.getString("password");
		boolean isAdmin = jsonInput.optBoolean("isAdmin", false);

		return new User(username, email, password, isAdmin);
	}
}