package Servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.NewsArticleRepository;
import Service.ArticleService;
import Service.ValidateUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonResponse;
import util.RequestParser;

@WebServlet("/api/articles/hide")
public class HideArticlesServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(HideArticlesServlet.class);
	private static final List<String> VALID_CATEGORIES = Arrays.asList("Business", "Entertainment", "Sports",
			"Technology", "Keywords");
	private final ArticleService articleService;
	private final ValidateUserService userService;
	private final RequestParser requestParser;

	public HideArticlesServlet() {
		this.articleService = new ArticleService();
		this.userService = new ValidateUserService();
		this.requestParser = new RequestParser();
	}

	public HideArticlesServlet(ArticleService articleService, ValidateUserService userService,
			RequestParser requestParser) {
		this.articleService = articleService;
		this.userService = userService;
		this.requestParser = requestParser;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		logger.debug("Processing POST request to hide articles");

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
				logger.warn("Username missing in request");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Username is required");
				return;
			}
			if (!userService.validateUser(username)) {
				logger.warn("Invalid or non-admin user: {}", username);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponse.writeError(response, "User is not authorized or not an admin");
				return;
			}
			if (action == null || action.trim().isEmpty()) {
				logger.warn("Action missing for username: {}", username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Action is required");
				return;
			}

			switch (action.toLowerCase()) {
			case "hide_by_id":
				String articleId = jsonInput.optString("articleId", null);
				if (articleId == null || articleId.trim().isEmpty()) {
					logger.warn("Article ID missing for username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Article ID is required");
					return;
				}
				try {
					articleService.hideArticleById(articleId);
					response.setStatus(HttpServletResponse.SC_OK);
					JsonResponse.writeSuccess(response, "Article hidden successfully", null);
					logger.info("Article {} hidden by username: {}", articleId, username);
				} catch (NewsArticleRepository.NotFoundException e) {
					logger.warn("Article not found: {} for username: {}", articleId, username);
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					JsonResponse.writeError(response, e.getMessage());
				}
				break;
			case "hide_by_keyword":
				String keyword = jsonInput.optString("keyword", null);
				if (keyword == null || keyword.trim().isEmpty()) {
					logger.warn("Keyword missing for username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Keyword is required");
					return;
				}
				if (keyword.length() > 255) {
					logger.warn("Keyword too long for username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Keyword must be 255 characters or less");
					return;
				}
				articleService.hideArticlesByKeyword(keyword);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Articles hidden successfully", null);
				logger.info("Articles hidden by keyword: {} for username: {}", keyword, username);
				break;
			case "hide_by_category":
				String category = jsonInput.optString("category", null);
				if (category == null || category.trim().isEmpty()) {
					logger.warn("Category missing for username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Category is required");
					return;
				}
				if (!VALID_CATEGORIES.contains(category)) {
					logger.warn("Invalid category: {} for username: {}", category, username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Invalid category: " + category);
					return;
				}
				articleService.hideArticlesByCategory(category);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Articles hidden successfully", null);
				logger.info("Articles hidden by category: {} for username: {}", category, username);
				break;
			default:
				logger.warn("Invalid action: {} for username: {}", action, username);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Invalid action");
			}
		} catch (SQLException e) {
			logger.error("Database error processing hide articles request", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid request: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error processing hide articles request", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}