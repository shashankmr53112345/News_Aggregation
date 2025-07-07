package Servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.NotificationRepository;
import Service.NotificationService;
import Service.ValidateUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Email;
import util.JsonResponse;
import util.RequestParser;

@WebServlet("/api/notifications/configure")
public class ConfigureNotificationsServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(ConfigureNotificationsServlet.class);
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");
	private final NotificationService notificationService;
	private final RequestParser requestParser;
	private final ValidateUserService userService;

	public ConfigureNotificationsServlet() {
		this.notificationService = new NotificationService(new NotificationRepository(), new Email());
		this.userService = new ValidateUserService();
		this.requestParser = new RequestParser();
	}

	public ConfigureNotificationsServlet(NotificationService notificationService, ValidateUserService userService,
			RequestParser requestParser) {
		this.notificationService = notificationService;
		this.userService = userService;
		this.requestParser = requestParser;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		logger.debug("Processing GET request for path: {}", pathInfo);

		if (pathInfo == null || pathInfo.equals("/")) {
			logger.warn("Username missing in GET request");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response,
					"Username is required in path (e.g., /api/notifications/preferences/{username})");
			return;
		}

		String username = pathInfo.substring(1);
		if (username.trim().isEmpty()) {
			logger.warn("Empty username in GET request");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Username cannot be empty");
			return;
		}

		try {
			if (!userService.validateUser(username)) {
				logger.warn("Invalid user: {}", username);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponse.writeError(response, "Invalid user");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		notificationService.getNotificationPreferences(username, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		logger.debug("Processing POST request");

		if (request.getContentType() == null || !request.getContentType().contains("application/json")) {
			logger.warn("Invalid Content-Type: {}", request.getContentType());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Content-Type must be application/json");
			return;
		}

		try {
			JSONObject jsonInput = requestParser.parseRequestBody(request);
			String username = jsonInput.optString("username", null);
			String action = jsonInput.optString("action", null);

			if (username == null || username.trim().isEmpty()) {
				logger.warn("Username missing in POST request");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Username is required");
				return;
			}
			if (!userService.validateUser(username)) {
				logger.warn("Invalid user: {}", username);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponse.writeError(response, "Invalid user");
				return;
			}
			if (action == null || action.trim().isEmpty()) {
				logger.warn("Action missing for username: {}", username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Action is required");
				return;
			}

			switch (action.toLowerCase()) {
			case "update_category":
				String category = jsonInput.optString("category", null);
				String enabledStr = jsonInput.optString("enabled", null);
				if (category == null || !VALID_CATEGORIES.contains(category)) {
					logger.warn("Invalid or missing category: {} for username: {}", category, username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Invalid or missing category");
					return;
				}
				if (enabledStr == null || !enabledStr.matches("true|false")) {
					logger.warn("Invalid enabled parameter for username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Invalid or missing enabled parameter (must be true/false)");
					return;
				}
				Boolean enabled = Boolean.parseBoolean(enabledStr);
				notificationService.updateNotificationPreference(username, category, enabled, response);
				break;
			case "add_keyword":
				String keyword = jsonInput.optString("keyword", null);
				notificationService.addKeyword(username, keyword, response);
				break;
			case "delete_keyword":
				String keywordToDelete = jsonInput.optString("keyword", null);
				notificationService.deleteKeyword(username, keywordToDelete, response);
				break;
			case "clear_notifications":
				notificationService.clearNotifications(username, response);
				break;
			default:
				logger.warn("Invalid action: {} for username: {}", action, username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Invalid action");
			}
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid request for username: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error processing POST request", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}