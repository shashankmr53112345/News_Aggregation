package Servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;

import Service.UserAuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.JsonResponseBody;

@WebServlet("/UserLogin")
public class LoginServlet extends HttpServlet {
	private final UserAuthenticationService userService;

	public LoginServlet() {
		this.userService = new UserAuthenticationService();
	}

	public LoginServlet(UserAuthenticationService userService) {
		this.userService = userService;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		try {
			JSONObject jsonInput = parseRequestBody(request);
			String username = jsonInput.getString("username");
			String password = jsonInput.getString("password");

			User user = userService.authenticateUser(username, password);
			response.setStatus(HttpServletResponse.SC_OK);
			JSONObject userData = new JSONObject();
			userData.put("username", user.getUsername());
			userData.put("email", user.getEmail());
			userData.put("isAdmin", user.isAdmin());
			out.println(new JsonResponseBody(true, "Login successful", userData).toJson());
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			out.println(new JsonResponseBody(false, e.getMessage()).toJson());
		} catch (JSONException e) {
			System.err.println("Invalid JSON input for login: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.println(new JsonResponseBody(false, "Invalid request format").toJson());
		} catch (Exception e) {
			System.err.println("Unexpected error during login: " + e.getMessage());
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

}
