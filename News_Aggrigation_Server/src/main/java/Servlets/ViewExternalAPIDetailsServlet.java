package Servlets;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Service.ExternalAPIsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ExternalAPIs;
import util.JsonResponse;

@WebServlet("/ViewExternalAPIDetails")
public class ViewExternalAPIDetailsServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(ViewExternalAPIDetailsServlet.class);
	private final ExternalAPIsService externalAPIsService;

	public ViewExternalAPIDetailsServlet() {
		this.externalAPIsService = new ExternalAPIsService();
	}

	public ViewExternalAPIDetailsServlet(ExternalAPIsService externalapiservice) {
		this.externalAPIsService = externalapiservice;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		logger.debug("Processing request to retrieve all external APIs");

		try {
			List<ExternalAPIs> apis = externalAPIsService.getAllApis();
			if (apis == null || apis.isEmpty()) {
				logger.warn("No external APIs found");
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				JsonResponse.writeError(response, "No APIs found");
				return;
			}

			JSONArray apisJson = new JSONArray();
			for (ExternalAPIs api : apis) {
				apisJson.put(toJSONObject(api));
			}
			response.setStatus(HttpServletResponse.SC_OK);
			JsonResponse.writeSuccess(response, "APIs retrieved successfully", new JSONObject().put("apis", apisJson));
			logger.info("Retrieved {} external APIs", apis.size());
		} catch (Exception e) {
			logger.error("Unexpected error retrieving external APIs", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JsonResponse.writeError(response, "Server error");
		}
	}

	private JSONObject toJSONObject(ExternalAPIs api) {
		return new JSONObject().put("id", api.getId()).put("name", api.getName()).put("apiKey", api.getApiKey())
				.put("isActive", api.getIsActive())
				.put("lastAccessed", api.getLastAccessed() != null ? api.getLastAccessed().toString() : null);
	}
}