package Application;

import UI.HomeMenuUI;
import UI.InputValidator;
import data.HttpRequestClient;
import service.ExternalAPIService;
import service.UserAuthenticationService;

public class NewsAggrigation_ConsoleApp {
	public static void main(String[] args) {
		String serverBaseUrl = "http://localhost:8080/News_Aggrigation_Server";
		HttpRequestClient httpRequestClient = new HttpRequestClient(serverBaseUrl);
		UserAuthenticationService authenticationService = new UserAuthenticationService(httpRequestClient);
		ExternalAPIService externalApiService = new ExternalAPIService(httpRequestClient);
		InputValidator inputValidator = new InputValidator();
		HomeMenuUI homeMenuInterface = new HomeMenuUI(authenticationService, externalApiService, inputValidator);

		homeMenuInterface.startApplication();
	}
}