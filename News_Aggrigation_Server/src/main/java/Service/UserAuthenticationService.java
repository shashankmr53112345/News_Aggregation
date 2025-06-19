package Service;

import Repository.UserAuthenticationRepository;
import Repository.UserAuthenticationRepository.DuplicateKeyException;
import model.User;
import util.HashPasswordUtil;

public class UserAuthenticationService {
	private final UserAuthenticationRepository userRepository;
	private final HashPasswordUtil passwordUtil;

	public UserAuthenticationService() {
		this.userRepository = new UserAuthenticationRepository();
		this.passwordUtil = new HashPasswordUtil();
	}

	public UserAuthenticationService(UserAuthenticationRepository userRepository, HashPasswordUtil passwordUtil) {
		this.userRepository = userRepository;
		this.passwordUtil = passwordUtil;
	}

	public User authenticateUser(String username, String password) throws IllegalArgumentException {
		User user = userRepository.findByUsername(username);
		if (user == null || !passwordUtil.verifyPassword(password, user.getPasswordHash())) {
			throw new IllegalArgumentException("Invalid username or password");
		}
		return user;
	}

	public void registerUser(User user) throws DuplicateKeyException {
		if (user.getUsername() == null || user.getEmail() == null || user.getPasswordHash() == null) {
			throw new IllegalArgumentException("Missing required fields");
		}
		String hashedPassword = passwordUtil.hashPassword(user.getPasswordHash());
		User newUser = new User(0, user.getUsername(), user.getEmail(), hashedPassword, user.isAdmin());
		userRepository.save(newUser);
	}
}