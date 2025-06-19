package Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import Repository.ExternalApisDao;
import model.ExternalAPIs;

public class ApiService {
	private final ExternalApisDao externalApisDao = new ExternalApisDao();
	private final HttpClient httpClient = HttpClient.newHttpClient();

	public void fetchAndProcessApiData() throws Exception {
		List<ExternalAPIs> activeApis = externalApisDao.getActiveApis();
		for (ExternalAPIs api : activeApis) {
			String response = fetchDataFromApi(api);
			processApiResponse(api, response);
			updateLastAccessed(api.getId(), api.getName());
		}
	}

	private String fetchDataFromApi(ExternalAPIs api) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(api.getApiUrl()))
				.header("Authorization", "Bearer " + api.getApiKey()).GET().build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response.body();
	}

	private void processApiResponse(ExternalAPIs api, String response) {
		// Implement your response processing logic here
		// e.g., parse JSON, store in database, or trigger other actions
		System.out.println("Processing response for API " + api.getName() + ": " + response);
	}

	private void updateLastAccessed(int apiId, String apiName) {
		externalApisDao.updateLastAccessed(apiId, apiName, Timestamp.from(Instant.now()));
	}
}