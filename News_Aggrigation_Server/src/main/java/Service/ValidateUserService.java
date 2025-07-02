package Service;

import java.sql.SQLException;

import Repository.ValidateUserRepository;

public class ValidateUserService {
	private final ValidateUserRepository userDAO;

	public ValidateUserService() {
		this.userDAO = new ValidateUserRepository();
	}

	public boolean validateUser(String username) throws SQLException {
		return userDAO.userExists(username);
	}
}
