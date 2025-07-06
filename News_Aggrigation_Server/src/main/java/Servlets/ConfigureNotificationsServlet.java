package Servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import Repository.NotificationRepository;
import Service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Email;
import util.JsonResponseBody;

@WebServlet("/api/notifications/configure")
public class ConfigureNotificationsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private NotificationService notificationService;
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology");

	@Override
	public void init() {
		notificationService = new NotificationService(new NotificationRepository(), new Email());
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Username is required");
			response.getWriter().write(responseBody.toJson());
			System.err.println("Error: Username missing in GET request");
			return;
		}

		String username = pathInfo.substring(1);
		JsonResponseBody responseBody = notificationService.getNotificationPreferences(username);
		response.setContentType("application/json");
		JSONObject jsonResponse = new JSONObject(responseBody.toJson());
		boolean success = jsonResponse.getBoolean("success");
		String message = jsonResponse.getString("message");

		response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
		response.getWriter().write(responseBody.toJson());
		if (success) {
			System.out.println("Debug: Retrieved notification config for username: " + username);
		} else {
			System.err.println("Error retrieving notification config for username: " + username + ": " + message);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		String action = request.getParameter("action");
		if (username == null || username.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Username is required");
			response.getWriter().write(responseBody.toJson());
			System.err.println("Error: Username parameter is missing or empty");
			return;
		}
		if (action == null || action.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Action is required");
			response.getWriter().write(responseBody.toJson());
			System.err.println("Error: Action parameter is missing or empty for username: " + username);
			return;
		}

		JsonResponseBody responseBody;
		switch (action.toLowerCase()) {
		case "update_category":
			String category = request.getParameter("category");
			String enabledStr = request.getParameter("enabled");
			if (category == null || !Arrays.asList("Business", "Entertainment", "Sports", "Technology", "Keywords")
					.contains(category)) {
				responseBody = new JsonResponseBody(false, "Invalid or missing category");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else if (enabledStr == null || !enabledStr.matches("true|false")) {
				responseBody = new JsonResponseBody(false, "Invalid or missing enabled parameter (must be true/false)");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				Boolean enabled = Boolean.parseBoolean(enabledStr);
				responseBody = notificationService.updateNotificationPreference(username, category, enabled);
			}
			break;
		case "add_keyword":
			String keyword = request.getParameter("keyword");
			if (keyword == null || keyword.trim().isEmpty() || keyword.length() > 255) {
				responseBody = new JsonResponseBody(false, "Invalid or missing keyword (max 255 characters)");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				responseBody = notificationService.addKeyword(username, keyword);
			}
			break;
		default:
			responseBody = new JsonResponseBody(false, "Invalid action");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		response.setContentType("application/json");
		response.getWriter().write(responseBody.toJson());
		JSONObject jsonResponse = new JSONObject(responseBody.toJson());
		boolean success = jsonResponse.getBoolean("success");
		String message = jsonResponse.getString("message");

		System.out.println("Debug: Action '" + action + "' processed for username: " + username + " - success: "
				+ success + ", message: " + message);
	}
}