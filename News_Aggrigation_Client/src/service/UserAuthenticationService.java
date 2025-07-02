package service;

import org.json.JSONObject;

import data.HttpRequestClient;

public class UserAuthenticationService {
	private final HttpRequestClient httpRequestClient;

	public UserAuthenticationService(HttpRequestClient httpRequestClient) {
		this.httpRequestClient = httpRequestClient;
	}

	public JSONObject login(String username, String password) throws Exception {
		JSONObject requestBody = new JSONObject();
		requestBody.put("username", username);
		requestBody.put("password", password);
		return httpRequestClient.post("/UserLogin", requestBody.toString());
	}

	public JSONObject signup(String username, String email, String password, boolean isAdmin) throws Exception {
		JSONObject requestBody = new JSONObject();
		requestBody.put("username", username);
		requestBody.put("email", email);
		requestBody.put("password", password);
		requestBody.put("isAdmin", isAdmin);
		return httpRequestClient.post("/UserSignup", requestBody.toString());
	}
}