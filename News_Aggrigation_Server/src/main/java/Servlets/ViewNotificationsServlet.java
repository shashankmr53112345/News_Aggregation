package Servlets;

import java.io.IOException;
import java.sql.SQLException;
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
import model.Notification;
import util.Email;
import util.JsonResponse;
import util.JsonUtils;

@WebServlet("/api/notifications/view")
public class ViewNotificationsServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(ViewNotificationsServlet.class);
	private final NotificationService notificationService;
	private final ValidateUserService userService;

	public ViewNotificationsServlet() {
		this.notificationService = new NotificationService(new NotificationRepository(), new Email());
		this.userService = new ValidateUserService();
	}

	public ViewNotificationsServlet(NotificationService notificationService, ValidateUserService userService) {
		this.notificationService = notificationService;
		this.userService = userService;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String username = request.getParameter("username");
		logger.debug("Processing GET request for notifications, username: {}", username);

		if (username == null || username.trim().isEmpty()) {
			logger.warn("Username missing in GET request");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Username is required");
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

		try {
			List<Notification> notifications = notificationService.getNotifications(username);
			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response, "Notifications retrieved",
					new JSONObject().put("notifications", JsonUtils.notificationsToJsonArray(notifications)));
			logger.info("Retrieved {} notifications for username: {}", notifications.size(), username);
		} catch (SQLException e) {
			logger.error("Database error retrieving notifications for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error retrieving notifications for username: {}", username, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}