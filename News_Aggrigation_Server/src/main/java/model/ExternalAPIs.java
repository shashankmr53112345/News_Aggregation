package model;

import java.sql.Timestamp;

public class ExternalAPIs {
	private int id;
	private String name;
	private String apiKey;
	private boolean isActive;
	private Timestamp lastAccessed;
	private String apiUrl;

	public ExternalAPIs() {
	}

	public ExternalAPIs(int id, String name, String apiKey, boolean isActive, Timestamp lastAccessed, String apiUrl) {
		this.id = id;
		this.name = name;
		this.apiKey = apiKey;
		this.isActive = isActive;
		this.lastAccessed = lastAccessed;
		this.apiUrl = apiUrl;
	}

	// Getters
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getApiKey() {
		return apiKey;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public Timestamp getLastAccessed() {
		return lastAccessed;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	// Setters
	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setLastAccessed(Timestamp timestamp) {
		this.lastAccessed = timestamp;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	@Override
	public String toString() {
		return "{\"apiId\":" + id + ",\"apiKey\":\"" + apiKey + "\",\"apiUrl\":\"" + apiUrl + "\",\"isActive\":"
				+ isActive + ",\"lastAccessedTimestamp\":\"" + lastAccessed + "\"}";
	}
}