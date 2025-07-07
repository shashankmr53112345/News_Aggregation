package UI;

import service.ExternalAPIService;
import service.UserAuthenticationService;
import service.UserNewsOperationsService;

public class AdminMenuUI {
	private final UserAuthenticationService authenticationService;
	private final UserNewsOperationsService newsService;
	private final ExternalAPIService externalApiService;
	private final InputValidator inputValidator;
	private final String username;

	public AdminMenuUI(UserAuthenticationService authenticationService, UserNewsOperationsService newsService,
			ExternalAPIService externalApiService, InputValidator inputValidator, String username) {
		this.authenticationService = authenticationService;
		this.newsService = newsService;
		this.externalApiService = externalApiService;
		this.inputValidator = inputValidator;
		this.username = username;
	}

	public void displayAdminMenu() {
		ExternalServerHandler serverHandler = new ExternalServerHandler(externalApiService, inputValidator);
		CategoryHandler categoryHandler = new CategoryHandler(externalApiService, inputValidator);
		ArticleHandler articleHandler = new ArticleHandler(newsService, inputValidator, username);
		MenuController menuController = new MenuController(serverHandler, categoryHandler, articleHandler, username);

		boolean loggedOut = menuController.displayMenu();
		if (loggedOut) {
			HomeMenuUI homeUI = new HomeMenuUI(authenticationService, newsService, externalApiService, inputValidator);
			homeUI.displayHomeMenu();
		}
	}
}