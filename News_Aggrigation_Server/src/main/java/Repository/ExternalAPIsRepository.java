package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.ExternalAPIs;
import util.DatabaseConnection;

public class ExternalAPIsRepository {
	private static final Logger logger = LoggerFactory.getLogger(ExternalAPIsRepository.class);

	public static class DuplicateKeyException extends Exception {
		public DuplicateKeyException(String message) {
			super(message);
		}
	}

	public List<ExternalAPIs> getAllExternalAPIs() {
		List<ExternalAPIs> externalAPIList = new ArrayList<>();
		String sql = "SELECT * FROM external_apis";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			while (rs.next()) {
				ExternalAPIs externalAPI = new ExternalAPIs();
				externalAPI.setId(rs.getInt("id"));
				externalAPI.setName(rs.getString("name"));
				externalAPI.setApiKey(rs.getString("api_key"));
				externalAPI.setIsActive(rs.getBoolean("is_active"));
				externalAPI.setLastAccessed(rs.getTimestamp("last_accessed"));
				externalAPIList.add(externalAPI);
			}
			logger.debug("Retrieved {} external APIs from database", externalAPIList.size());
			return externalAPIList;
		} catch (SQLException e) {
			logger.error("Error retrieving external APIs", e);
			throw new RuntimeException("Database error while retrieving external APIs", e);
		}
	}

	public ExternalAPIs getExternalAPIById(int apiId) {
		String sql = "SELECT * FROM external_apis WHERE id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, apiId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					ExternalAPIs externalAPI = new ExternalAPIs();
					externalAPI.setId(rs.getInt("id"));
					externalAPI.setName(rs.getString("name"));
					externalAPI.setApiKey(rs.getString("api_key"));
					externalAPI.setIsActive(rs.getBoolean("is_active"));
					externalAPI.setLastAccessed(rs.getTimestamp("last_accessed"));
					logger.debug("Retrieved API with ID: {}", apiId);
					return externalAPI;
				}
				logger.debug("No API found for ID: {}", apiId);
				return null;
			}
		} catch (SQLException e) {
			logger.error("Error retrieving API with ID: {}", apiId, e);
			throw new RuntimeException("Database error while retrieving API", e);
		}
	}

	public boolean updateExternalAPIKey(int apiId, String newApiKey) throws DuplicateKeyException {
		String sql = "UPDATE external_apis SET api_key = ? WHERE id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, newApiKey);
			stmt.setInt(2, apiId);
			int rowsAffected = stmt.executeUpdate();
			logger.debug("API key update attempt for ID: {}, rows affected: {}", apiId, rowsAffected);
			return rowsAffected > 0;
		} catch (SQLException e) {
			if (isDuplicateKeyError(e)) {
				String message = "API key '" + newApiKey + "' already exists";
				logger.warn("Duplicate key error: {}", message);
				throw new DuplicateKeyException(message);
			}
			logger.error("Error updating API key for ID: {}", apiId, e);
			throw new RuntimeException("Database error while updating API key", e);
		}
	}

	private boolean isDuplicateKeyError(SQLException e) {
		String sqlState = e.getSQLState();
		int errorCode = e.getErrorCode();
		return sqlState != null && sqlState.equals("23505") || errorCode == 1062;
	}
}