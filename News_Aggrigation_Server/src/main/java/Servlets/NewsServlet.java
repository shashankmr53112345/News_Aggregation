package Servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import Service.NewsService;
import Service.ValidateUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.NewsArticles;
import util.JsonResponseBody;

@WebServlet(name = "NewsServlet", urlPatterns = { "/api/news/*" })
public class NewsServlet extends HttpServlet {
	private NewsService newsService;
	private ValidateUserService userService;

	@Override
	public void init() throws ServletException {
		newsService = new NewsService();
		userService = new ValidateUserService();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		String username = request.getParameter("username");

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
				List<NewsArticles> articles = newsService.getHeadlines(startDate, endDate, effectiveCategory);
				JSONArray articlesJson = articlesToJsonArray(articles);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Headlines retrieved successfully",
						new JSONObject().put("articles", articlesJson));
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else if (pathInfo.equals("/saved")) {
				if (username == null || username.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Username required");
					response.getWriter().write(responseBody.toJson());
					return;
				}

				if (!userService.validateUser(username)) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Invalid user");
					response.getWriter().write(responseBody.toJson());
					return;
				}

				String category = request.getParameter("category");
				List<NewsArticles> savedArticles = newsService.getSavedArticles(username, category);
				JSONArray articlesJson = articlesToJsonArray(savedArticles);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Saved articles retrieved successfully",
						new JSONObject().put("articles", articlesJson));
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Invalid endpoint");
				response.getWriter().write(responseBody.toJson());
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Database error: " + e.getMessage());
			response.getWriter().write(responseBody.toJson());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		String username = request.getParameter("username");

		try {
			if (username == null || username.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Username required");
				response.getWriter().write(responseBody.toJson());
				return;
			}

			if (!userService.validateUser(username)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Invalid user");
				response.getWriter().write(responseBody.toJson());
				return;
			}

			if (pathInfo != null && pathInfo.equals("/save")) {
				String articleId = request.getParameter("articleId");
				if (articleId == null || articleId.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Article ID required");
					response.getWriter().write(responseBody.toJson());
					return;
				}
				newsService.saveUserArticle(username, articleId);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Article saved successfully");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else if (pathInfo != null && pathInfo.equals("/like")) {
				String articleId = request.getParameter("articleId");
				if (articleId == null || articleId.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Article ID required");
					response.getWriter().write(responseBody.toJson());
					return;
				}
				newsService.likeArticle(username, articleId);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Article liked successfully");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else if (pathInfo != null && pathInfo.equals("/dislike")) {
				String articleId = request.getParameter("articleId");
				if (articleId == null || articleId.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Article ID required");
					response.getWriter().write(responseBody.toJson());
					return;
				}
				newsService.dislikeArticle(username, articleId);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Article disliked successfully");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else if (pathInfo != null && pathInfo.equals("/report")) {
				String articleId = request.getParameter("articleId");
				String reason = request.getParameter("reason");
				if (articleId == null || articleId.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Article ID required");
					response.getWriter().write(responseBody.toJson());
					return;
				}
				if (reason == null || reason.trim().isEmpty()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					JsonResponseBody responseBody = new JsonResponseBody(false, "Report reason required");
					response.getWriter().write(responseBody.toJson());
					return;
				}
				newsService.reportArticle(username, articleId, reason);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Article reported successfully");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Invalid endpoint");
				response.getWriter().write(responseBody.toJson());
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Database error: " + e.getMessage());
			response.getWriter().write(responseBody.toJson());
		}
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String pathInfo = request.getPathInfo();
		String username = request.getParameter("username");
		String articleId = request.getParameter("articleId");

		try {
			if (username == null || username.isEmpty() || articleId == null || articleId.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Username and Article ID required");
				response.getWriter().write(responseBody.toJson());
				return;
			}

			if (!userService.validateUser(username)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Invalid user");
				response.getWriter().write(responseBody.toJson());
				return;
			}

			if (pathInfo != null && pathInfo.equals("/saved")) {
				newsService.deleteUserArticle(username, articleId);
				JsonResponseBody responseBody = new JsonResponseBody(true, "Article deleted successfully");
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(responseBody.toJson());
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponseBody responseBody = new JsonResponseBody(false, "Invalid endpoint");
				response.getWriter().write(responseBody.toJson());
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponseBody responseBody = new JsonResponseBody(false, "Database error: " + e.getMessage());
			response.getWriter().write(responseBody.toJson());
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
			json.put("published_at", article.getPublishedAt());
			json.put("likes", article.getLikes());
			json.put("dislikes", article.getDislikes());
			json.put("report_count", article.getReportCount());
			jsonArticles.put(json);
		}
		return jsonArticles;
	}
}