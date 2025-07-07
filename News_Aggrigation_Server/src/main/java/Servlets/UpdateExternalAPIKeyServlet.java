package Servlets;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Service.ExternalAPIsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.JsonResponse;
import util.RequestParser;

@WebServlet("/UpdateExternalAPIKey")
public class UpdateExternalAPIKeyServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(UpdateExternalAPIKeyServlet.class);
	private final ExternalAPIsService service;
	private final RequestParser requestParser;

	public UpdateExternalAPIKeyServlet(ExternalAPIsService service, RequestParser requestParser) {
		this.service = service;
		this.requestParser = new RequestParser();
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
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
			Integer apiId = jsonInput.has("apiId") ? jsonInput.getInt("apiId") : null;
			String newApiKey = jsonInput.has("newApiKey") ? jsonInput.getString("newApiKey") : null;
			logger.debug("Processing API key update request for ID: {}", apiId);

			if (apiId == null || newApiKey == null || newApiKey.trim().isEmpty()) {
				logger.warn("Missing or invalid parameters: apiId={}, newApiKey={}", apiId, newApiKey);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				JsonResponse.writeError(response, "apiId and newApiKey are required");
				return;
			}

			if (service.getApiDetails(apiId) == null) {
				logger.warn("API not found for ID: {}", apiId);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "API ID not found");
				return;
			}

			service.updateApiKey(apiId, newApiKey);
			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response, "API key updated successfully", new JSONObject().put("apiId", apiId));
			logger.info("API key updated successfully for ID: {}", apiId);
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid request: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			JsonResponse.writeError(response, e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error updating API key", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}
}