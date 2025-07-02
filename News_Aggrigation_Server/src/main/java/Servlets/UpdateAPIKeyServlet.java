package Servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;

import Service.ManageExternalAPIsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ExternalAPIs;
import util.JsonResponseBody;

@WebServlet("/api/update-api-key")
public class UpdateAPIKeyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final ManageExternalAPIsService service;

	public UpdateAPIKeyServlet(ManageExternalAPIsService service) {
		this.service = service;

	}

	public UpdateAPIKeyServlet() {
		this(new ManageExternalAPIsService());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		try {
			StringBuilder buffer = new StringBuilder();
			try (BufferedReader reader = request.getReader()) {
				String line;
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
			}

			JSONObject jsonRequest;
			try {
				jsonRequest = new JSONObject(buffer.toString());
			} catch (Exception e) {
				JsonResponseBody errorResponse = new JsonResponseBody(false, "Invalid JSON format: " + e.getMessage());
				out.write(errorResponse.toJson());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			Integer apiId;
			String newApiKey;
			try {
				apiId = jsonRequest.has("apiId") ? jsonRequest.getInt("apiId") : null;
				newApiKey = jsonRequest.has("newApiKey") ? jsonRequest.getString("newApiKey") : null;
			} catch (Exception e) {
				JsonResponseBody errorResponse = new JsonResponseBody(false,
						"Missing or invalid parameters: apiId (number) and newApiKey (string) are required");
				out.write(errorResponse.toJson());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			if (apiId == null || newApiKey == null || newApiKey.trim().isEmpty()) {
				JsonResponseBody errorResponse = new JsonResponseBody(false,
						"Missing or invalid parameters: apiId and newApiKey are required");
				out.write(errorResponse.toJson());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			ExternalAPIs api = service.getApiDetails(apiId);
			if (api == null) {
				JsonResponseBody errorResponse = new JsonResponseBody(false, "API ID not found");
				out.write(errorResponse.toJson());
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			boolean success = service.updateApiKey(apiId, newApiKey);

			if (success) {
				JsonResponseBody successResponse = new JsonResponseBody(true, "API key updated successfully",
						new JSONObject().put("apiId", apiId));
				out.write(successResponse.toJson());
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				JsonResponseBody errorResponse = new JsonResponseBody(false,
						"Failed to update API key due to database error");
				out.write(errorResponse.toJson());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}

		} catch (Exception e) {
			JsonResponseBody errorResponse = new JsonResponseBody(false, "Internal server error: " + e.getMessage());
			out.write(errorResponse.toJson());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.flush();
			out.close();
		}
	}
}
