package Servlets;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Repository.NotificationRepository;
import Service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Notification;
import util.Email;
import util.JsonResponseBody;

@WebServlet("/api/notifications/view")
public class ViewNotificationsServlet extends HttpServlet {
	private NotificationService notificationService;

	@Override
	public void init() {
		notificationService = new NotificationService(new NotificationRepository(), new Email());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		if (username == null || username.trim().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Username is required");
			response.getWriter().write(responseBody.toJson());
			System.err.println("Error: Username is required in query parameter");
			return;
		}

		try {
			List<Notification> notifications = notificationService.getNotifications(username);
			JSONArray notificationsArray = new JSONArray();
			for (Notification notification : notifications) {
				JSONObject notificationObj = new JSONObject();
				notificationObj.put("username", notification.getUsername());
				notificationObj.put("articleId", notification.getArticleId());
				notificationObj.put("message", notification.getMessage());
				notificationObj.put("createdAt", notification.getCreatedAt().toString());
				notificationsArray.put(notificationObj);
			}

			JSONObject data = new JSONObject();
			data.put("notifications", notificationsArray);

			JsonResponseBody responseBody = new JsonResponseBody(true, "Notifications retrieved", data);
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(responseBody.toJson());

			JSONObject jsonResponse = new JSONObject(responseBody.toJson());
			boolean success = jsonResponse.getBoolean("success");
			String message = jsonResponse.getString("message");
			System.out.println("Debug: Notifications retrieved for username: " + username + " - success: " + success
					+ ", message: " + message);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponseBody responseBody = new JsonResponseBody(false,
					"Error retrieving notifications: " + e.getMessage());
			response.getWriter().write(responseBody.toJson());
			System.err.println("Error retrieving notifications for username: " + username + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
}