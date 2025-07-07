package Servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
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
import model.NewsArticles;
import util.JsonResponse;
import util.RequestParser;

@WebServlet(name = "NewsServlet", urlPatterns = { "/api/news/*" })
public class NewsServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(NewsServlet.class);
	private final ArticleService articleService;
	private final ValidateUserService userService;
	private final RequestParser requestParser;

	public NewsServlet() {
		this.articleService = new ArticleService();
		this.userService = new ValidateUserService();
		this.requestParser = new RequestParser();
	}

	public NewsServlet(ArticleService articleService, ValidateUserService userService, RequestParser requestParser) {
		this.articleService = articleService;
		this.userService = userService;
		this.requestParser = requestParser;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		String username = request.getParameter("username");
		logger.debug("Processing GET request for path: {}", pathInfo);

		try {
			if (pathInfo == null || pathInfo.equals("/headlines")) {
				String startDate = request.getParameter("startDate");
				String endDate = request.getParameter("endDate");
				String category = request.getParameter("category");
				String allParam = request.getParameter("all");
				boolean allCategories = allParam != null && allParam.equalsIgnoreCase("true");

				if (startDate == null || startDate.isEmpty()) {
					startDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				}

				String effectiveCategory = allCategories ? null : category;
				logger.debug("Fetching headlines: startDate={}, endDate={}, category={}", startDate, endDate,
						effectiveCategory);
				List<NewsArticles> articles = articleService.getHeadlines(startDate, endDate, effectiveCategory);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Headlines retrieved successfully",
						new JSONObject().put("articles", articlesToJsonArray(articles)));
				logger.info("Retrieved {} headlines", articles.size());
			} else if (pathInfo.equals("/saved")) {
				if (username == null || username.isEmpty()) {
					logger.warn("Username missing for saved articles request");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Username required");
					return;
				}
				if (!userService.validateUser(username)) {
					logger.warn("Invalid user: {}", username);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					JsonResponse.writeError(response, "Invalid user");
					return;
				}

				String category = request.getParameter("category");
				logger.debug("Fetching saved articles for username: {}, category: {}", username, category);
				List<NewsArticles> savedArticles = articleService.getSavedArticles(username, category);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Saved articles retrieved successfully",
						new JSONObject().put("articles", articlesToJsonArray(savedArticles)));
				logger.info("Retrieved {} saved articles for username: {}", savedArticles.size(), username);
			} else if (pathInfo.equals("/search")) {
				if (username == null || username.isEmpty()) {
					logger.warn("Username missing for search request");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Username required");
					return;
				}
				if (!userService.validateUser(username)) {
					logger.warn("Invalid user: {}", username);
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					JsonResponse.writeError(response, "Invalid user");
					return;
				}

				String query = request.getParameter("query");
				if (query == null || query.trim().isEmpty()) {
					logger.warn("Query parameter missing for search request by username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Query parameter is required");
					return;
				}

				String startDate = request.getParameter("startDate");
				String endDate = request.getParameter("endDate");
				String sortBy = request.getParameter("sortBy");
				logger.debug("Searching articles for username: {}, query: {}, startDate: {}, endDate: {}, sortBy: {}",
						username, query, startDate, endDate, sortBy);
				List<NewsArticles> articles = articleService.searchArticles(query, startDate, endDate, sortBy);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Articles retrieved successfully",
						new JSONObject().put("articles", articlesToJsonArray(articles)));
				logger.info("Found {} articles for query: {} by username: {}", articles.size(), query, username);
			} else {
				logger.warn("Invalid endpoint: {}", pathInfo);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "Invalid endpoint");
			}
		} catch (SQLException e) {
			logger.error("Database error processing GET request for path: {}", pathInfo, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error processing GET request for path: {}", pathInfo, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		logger.debug("Processing POST request for path: {}", pathInfo);

		if (request.getContentType() == null || !request.getContentType().contains("application/json")) {
			logger.warn("Invalid Content-Type: {}", request.getContentType());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Content-Type must be application/json");
			return;
		}

		try {
			JSONObject jsonInput = requestParser.parseRequestBody(request);
			String username = jsonInput.optString("username", null);
			if (username == null || username.isEmpty()) {
				logger.warn("Username missing for POST request: {}", pathInfo);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Username required");
				return;
			}
			if (!userService.validateUser(username)) {
				logger.warn("Invalid user: {} for POST request: {}", username, pathInfo);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponse.writeError(response, "Invalid user");
				return;
			}

			if (pathInfo != null && pathInfo.equals("/save")) {
				String articleId = jsonInput.optString("articleId", null);
				if (articleId == null || articleId.isEmpty()) {
					logger.warn("Article ID missing for save request by username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Article ID required");
					return;
				}
				articleService.saveUserArticle(username, articleId);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Article saved successfully", null);
				logger.info("Article {} saved by username: {}", articleId, username);
			} else if (pathInfo != null && pathInfo.equals("/like")) {
				String articleId = jsonInput.optString("articleId", null);
				if (articleId == null || articleId.isEmpty()) {
					logger.warn("Article ID missing for like request by username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Article ID required");
					return;
				}
				articleService.likeArticle(username, articleId);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Article liked successfully", null);
				logger.info("Article {} liked by username: {}", articleId, username);
			} else if (pathInfo != null && pathInfo.equals("/dislike")) {
				String articleId = jsonInput.optString("articleId", null);
				if (articleId == null || articleId.isEmpty()) {
					logger.warn("Article ID missing for dislike request by username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Article ID required");
					return;
				}
				articleService.dislikeArticle(username, articleId);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Article disliked successfully", null);
				logger.info("Article {} disliked by username: {}", articleId, username);
			} else if (pathInfo != null && pathInfo.equals("/report")) {
				String articleId = jsonInput.optString("articleId", null);
				String reason = jsonInput.optString("reason", null);
				if (articleId == null || articleId.isEmpty()) {
					logger.warn("Article ID missing for report request by username: {}", username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Article ID required");
					return;
				}
				if (reason == null || reason.trim().isEmpty()) {
					logger.warn("Report reason missing for article: {} by username: {}", articleId, username);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponse.writeError(response, "Report reason required");
					return;
				}
				articleService.reportArticle(username, articleId, reason);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Article reported successfully", null);
				logger.info("Article {} reported by username: {}", articleId, username);
			} else {
				logger.warn("Invalid endpoint: {}", pathInfo);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "Invalid endpoint");
			}
		} catch (NewsArticleRepository.NotFoundException e) {
			logger.warn("Not found error for POST request: {} - {}", pathInfo, e.getMessage());
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			JsonResponse.writeError(response, e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid request for POST: {} - {}", pathInfo, e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, e.getMessage());
		} catch (SQLException e) {
			logger.error("Database error processing POST request: {}", pathInfo, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error processing POST request: {}", pathInfo, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		logger.debug("Processing DELETE request for path: {}", pathInfo);

		if (request.getContentType() == null || !request.getContentType().contains("application/json")) {
			logger.warn("Invalid Content-Type: {}", request.getContentType());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Content-Type must be application/json");
			return;
		}

		try {
			JSONObject jsonInput = requestParser.parseRequestBody(request);
			String username = jsonInput.optString("username", null);
			String articleId = jsonInput.optString("articleId", null);
			if (username == null || username.isEmpty() || articleId == null || articleId.isEmpty()) {
				logger.warn("Username or articleId missing for DELETE request: {}", pathInfo);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Username and Article ID required");
				return;
			}
			if (!userService.validateUser(username)) {
				logger.warn("Invalid user: {} for DELETE request: {}", username, pathInfo);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponse.writeError(response, "Invalid user");
				return;
			}

			if (pathInfo != null && pathInfo.equals("/saved")) {
				articleService.deleteUserArticle(username, articleId);
				response.setStatus(HttpServletResponse.SC_OK);
				JsonResponse.writeSuccess(response, "Article deleted successfully", null);
				logger.info("Article {} deleted by username: {}", articleId, username);
			} else {
				logger.warn("Invalid endpoint: {}", pathInfo);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "Invalid endpoint");
			}
		} catch (SQLException e) {
			logger.error("Database error processing DELETE request: {}", pathInfo, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error processing DELETE request: {}", pathInfo, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	private JSONArray articlesToJsonArray(List<NewsArticles> articles) {
		JSONArray jsonArticles = new JSONArray();
		for (NewsArticles article : articles) {
			JSONObject json = new JSONObject();
			json.put("id", article.getId());
			json.put("title", article.getTitle());
			json.put("description", article.getDescription());
			json.put("source", article.getSource());
			json.put("url", article.getUrl());
			json.put("category", article.getCategory());
			json.put("publishedAt", article.getPublishedAt());
			json.put("likes", article.getLikes());
			json.put("dislikes", article.getDislikes());
			json.put("reportCount", article.getReportCount());
			json.put("insertedAt", article.getInsertedAt() != null ? article.getInsertedAt().toString() : null);
			jsonArticles.put(json);
		}
		return jsonArticles;
	}
}