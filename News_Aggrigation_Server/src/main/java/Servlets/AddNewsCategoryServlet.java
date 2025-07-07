package Servlets;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonResponse;
import util.RequestParser;

@WebServlet("/AddNewsCategory")
public class AddNewsCategoryServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(AddNewsCategoryServlet.class);
	private final CategoryService categoryService;
	private final RequestParser requestParser;

	public AddNewsCategoryServlet() {
		this.categoryService = new CategoryService();
		this.requestParser = new RequestParser();
	}

	public AddNewsCategoryServlet(CategoryService newsCategoryService, RequestParser requestParser) {
		this.categoryService = newsCategoryService;
		this.requestParser = new RequestParser();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		String contentType = request.getContentType();
		if (contentType == null || !contentType.contains("application/json")) {
			logger.warn("Invalid Content-Type: {}", contentType);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, "Content-Type must be application/json");
			return;
		}

		try {
			JSONObject jsonInput = requestParser.parseRequestBody(request);
			String categoryName = jsonInput.optString("name", null);
			logger.debug("Processing add category request for name: {}", categoryName);

			if (categoryName == null || categoryName.trim().isEmpty()) {
				logger.warn("Category name is missing or empty");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "Category name is required");
				return;
			}

			categoryService.addCategory(categoryName);
			response.setStatus(HttpServletResponse.SC_CREATED);
			JsonResponse.writeSuccess(response, "Category added successfully",
					new JSONObject().put("name", categoryName));
			logger.info("Category added successfully: {}", categoryName);
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid request: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error adding category", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}