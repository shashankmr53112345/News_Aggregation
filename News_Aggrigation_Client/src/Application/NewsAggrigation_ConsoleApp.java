package Application;

import UI.HomeMenuUI;
import UI.InputValidator;
import data.HttpRequestClient;
import service.ExternalAPIService;
import service.UserAuthenticationService;
import service.UserNewsOperationsService;

public class NewsAggrigation_ConsoleApp {
	public static void main(String[] args) {
		String serverBaseUrl = "http://localhost:8080/News_Aggrigation_Server";
		HttpRequestClient httpRequestClient = new HttpRequestClient(serverBaseUrl);
		UserAuthenticationService authenticationService = new UserAuthenticationService(httpRequestClient);
		ExternalAPIService externalApiService = new ExternalAPIService(httpRequestClient);
		InputValidator inputValidator = new InputValidator();
		UserNewsOperationsService userNewsOperationsService = new UserNewsOperationsService();
		HomeMenuUI homeMenuInterface = new HomeMenuUI(authenticationService, userNewsOperationsService,
				externalApiService, inputValidator);

		homeMenuInterface.startApplication();
	}
}