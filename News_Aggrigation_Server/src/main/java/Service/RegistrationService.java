package Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Repository.UserRepository;
import model.User;
import util.PasswordHasher;

public class RegistrationService {
	private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
	private final UserRepository userRepository;
	private final PasswordHasher passwordHasher;

	public RegistrationService() {
		this.userRepository = new UserRepository();
		this.passwordHasher = new PasswordHasher();
	}

	public RegistrationService(UserRepository userRepository, PasswordHasher passwordHasher) {
		this.userRepository = userRepository;
		this.passwordHasher = passwordHasher;
	}

	public void register(User user) throws UserRepository.DuplicateKeyException {
		logger.debug("Registering user: {}", user.getUsername());
		if (user.getUsername() == null || user.getEmail() == null || user.getPasswordHash() == null) {
			logger.warn("Invalid user data for registration: {}", user);
			throw new IllegalArgumentException("Missing required fields");
		}
		String hashedPassword = passwordHasher.hashPassword(user.getPasswordHash());
		User newUser = new User(user.getUsername(), user.getEmail(), hashedPassword, user.isAdmin());
		userRepository.save(newUser);
		logger.info("User registered: {}", user.getUsername());
	}
}