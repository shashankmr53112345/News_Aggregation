package util;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

public class RequestParser {
	private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

	public JSONObject parseRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder buffer = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		}
		logger.debug("Parsed request body: {}", buffer);
		return new JSONObject(buffer.toString());
	}
}