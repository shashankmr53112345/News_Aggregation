package Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.ExternalAPIs;
import util.DatabaseConnection;

public class ManageExternalAPIsRepository {

	public List<ExternalAPIs> getAllExternalAPIs() {
		List<ExternalAPIs> externalAPIList = new ArrayList<>();
		String selectQuery = "SELECT * FROM external_apis";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				ExternalAPIs externalAPI = new ExternalAPIs();
				externalAPI.setId(resultSet.getInt("id"));
				externalAPI.setName(resultSet.getString("name"));
				externalAPI.setApiKey(resultSet.getString("api_key"));
				externalAPI.setIsActive(resultSet.getBoolean("is_active"));
				externalAPI.setLastAccessed(resultSet.getTimestamp("last_accessed"));
				externalAPIList.add(externalAPI);
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return externalAPIList;
	}

	public ExternalAPIs getExternalAPIById(int apiId) {
		String selectQuery = "SELECT * FROM external_apis WHERE id = ?";
		ExternalAPIs externalAPI = null;

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

			preparedStatement.setInt(1, apiId);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				externalAPI = new ExternalAPIs();
				externalAPI.setId(resultSet.getInt("id"));
				externalAPI.setName(resultSet.getString("name"));
				externalAPI.setApiKey(resultSet.getString("api_key"));
				externalAPI.setIsActive(resultSet.getBoolean("is_active"));
				externalAPI.setLastAccessed(resultSet.getTimestamp("last_accessed"));
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return externalAPI;
	}

	public boolean updateExternalAPIKey(int apiId, String newApiKey) {
		String updateQuery = "UPDATE external_apis SET api_key = ? WHERE id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

			preparedStatement.setString(1, newApiKey);
			preparedStatement.setInt(2, apiId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
			return false;
		}
	}
}