package service;

import org.json.JSONObject;

import data.HttpRequestClient;

public class ExternalAPIService {
	private final HttpRequestClient httpRequestClient;

	public ExternalAPIService(HttpRequestClient httpRequestClient) {
		this.httpRequestClient = httpRequestClient;
	}

	public JSONObject addNewsCategory(String categoryName) throws Exception {
		JSONObject requestBody = new JSONObject();
		requestBody.put("name", categoryName);
		return httpRequestClient.post("/AddNewsCategory", requestBody.toString());
	}

	public JSONObject viewExternalApiDetails() throws Exception {
		return httpRequestClient.get("/ViewExternalAPIDetails");
	}

	public JSONObject updateExternalApiKey(String id, String apiKey) throws Exception {

		int apiId;
		try {
			apiId = Integer.parseInt(id);
		} catch (NumberFormatException e) {
			JSONObject errorResponse = new JSONObject();
			errorResponse.put("success", false);
			errorResponse.put("message", "Invalid ID format: must be a number");
			errorResponse.put("data", new JSONObject());
			return errorResponse;
		}

		if (apiKey == null || apiKey.trim().isEmpty()) {
			JSONObject errorResponse = new JSONObject();
			errorResponse.put("success", false);
			errorResponse.put("message", "API key cannot be empty");
			errorResponse.put("data", new JSONObject());
			return errorResponse;
		}

		JSONObject requestBody = new JSONObject();
		requestBody.put("apiId", apiId);
		requestBody.put("newApiKey", apiKey);
		return httpRequestClient.post("/api/update-api-key", requestBody.toString());
	}
}