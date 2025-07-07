package Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.UserRepository;
import model.User;
import util.PasswordHasher;

public class AuthenticationService {
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	public AuthenticationService() {
		this.userRepository = new UserRepository();
		this.passwordHasher = new PasswordHasher();
	}

	public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
	}

	public User authenticate(String username, String password) {
		logger.debug("Attempting to authenticate user: {}", username);
		User user = userRepository.findByUsername(username);
		if (user == null || !passwordHasher.verifyPassword(password, user.getPasswordHash())) {
			logger.warn("Authentication failed for username: {}", username);
			throw new IllegalArgumentException("Invalid username or password");
		}
		logger.info("Authentication successful for username: {}", username);
		return user;
	}
}