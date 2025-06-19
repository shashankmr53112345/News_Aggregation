package util;

import org.json.JSONObject;

public class JsonResponseBody {
	private final boolean success;
	private final String message;
	private final JSONObject data;

	public JsonResponseBody(boolean success, String message) {
		this(success, message, new JSONObject());
	}

	public JsonResponseBody(boolean success, String message, JSONObject data) {
		this.success = success;
		this.message = message;
		this.data = data != null ? data : new JSONObject();
	}

	public String toJson() {
		JSONObject json = new JSONObject();
		json.put("success", success);
		json.put("message", message);
		json.put("data", data);
		return json.toString();
	}
}
