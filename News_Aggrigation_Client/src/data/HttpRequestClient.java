package data;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class HttpRequestClient {
	private final HttpClient httpClient;
	private final String serverBaseUrl;

	public HttpRequestClient(String serverBaseUrl) {
		this.httpClient = HttpClient.newHttpClient();
		this.serverBaseUrl = serverBaseUrl.endsWith("/") ? serverBaseUrl : serverBaseUrl + "/";
	}

	public JSONObject get(String endpoint) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(serverBaseUrl + endpoint))
				.header("Content-Type", "application/json").GET().build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return new JSONObject(response.body());
	}

	public JSONObject post(String endpoint, String body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(serverBaseUrl + endpoint))
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return new JSONObject(response.body());
	}

	public JSONObject put(String endpoint, String body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(serverBaseUrl + endpoint))
				.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body)).build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return new JSONObject(response.body());
	}

	public JSONObject delete(String endpoint) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(serverBaseUrl + endpoint))
				.header("Content-Type", "application/json").method("DELETE", HttpRequest.BodyPublishers.noBody())
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return new JSONObject(response.body());
	}
}