package Servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Service.ArticleService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ArticleReport;
import util.JsonResponse;
import util.JsonUtils;

@WebServlet("/admin/reports")
public class AdminReportServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(AdminReportServlet.class);
	private final ArticleService articleService;

	public AdminReportServlet() {
		this.articleService = new ArticleService();
	}

	public AdminReportServlet(ArticleService articleService) {
		this.articleService = articleService;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		logger.debug("Processing request to retrieve all article reports");

		try {
			List<ArticleReport> reports = articleService.getArticleReports();
			logger.info("Retrieved {} article reports", reports.size());

			JSONObject data = new JSONObject().put("reports", JsonUtils.reportsToJsonArray(reports));
			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response, "Reports retrieved successfully", data);
		} catch (SQLException e) {
			logger.error("Database error retrieving article reports", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Database error: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error retrieving article reports", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}