package Servlets;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONObject;

import Exceptions.DuplicateCategoryException;
import Service.AddNewsCategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonResponseBody;

@WebServlet("/AddNewsCategory")
public class AddNewsCategoryServlet extends HttpServlet {
	private final AddNewsCategoryService NewsCategoryService;

	public AddNewsCategoryServlet() {
		this.NewsCategoryService = new AddNewsCategoryService();
	}

	public AddNewsCategoryServlet(AddNewsCategoryService newsCategoryService) {
		this.NewsCategoryService = newsCategoryService;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		JsonResponseBody responsebody;
		String contentType = request.getContentType();
		if (contentType == null || !contentType.contains("application/json")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responsebody = new JsonResponseBody(false, "Content-Type must be application/json");
			response.getWriter().write(responsebody.toJson());
			return;
		}
		try {
			StringBuilder requestBody = new StringBuilder();
			try (BufferedReader reader = request.getReader()) {
				String line;
				while ((line = reader.readLine()) != null) {
					requestBody.append(line);
				}
			}
			JSONObject jsonRequest = new JSONObject(requestBody.toString());
			String categoryName = jsonRequest.optString("name", null);

			if (categoryName == null || categoryName.trim().isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				responsebody = new JsonResponseBody(false, "Category name is required");
			} else {
				boolean added = NewsCategoryService.addCategory(categoryName);
				JSONObject data = new JSONObject();
				data.put("added", added);
				responsebody = new JsonResponseBody(added, added ? "Success" : "Failed to add category", data);
			}
		} catch (DuplicateCategoryException e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			responsebody = new JsonResponseBody(false, e.getMessage());
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responsebody = new JsonResponseBody(false, e.getMessage());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			responsebody = new JsonResponseBody(false, "Invalid JSON format or error processing request");
		}
		response.getWriter().write(responsebody.toJson());
	}
}
