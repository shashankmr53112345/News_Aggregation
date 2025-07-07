package util;

import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponse {
    private static final Logger logger = LoggerFactory.getLogger(JsonResponse.class);

    public static void writeSuccess(HttpServletResponse response, String message, JSONObject data) throws IOException {
        JSONObject responseJson = new JSONObject()
                .put("success", true)
                .put("message", message);
        if (data != null) {
            responseJson.put("data", data);
        }
        writeResponse(response, responseJson);
    }

    public static void writeError(HttpServletResponse response, String message) throws IOException {
        JSONObject responseJson = new JSONObject()
                .put("success", false)
                .put("message", message);
        writeResponse(response, responseJson);
    }

    private static void writeResponse(HttpServletResponse response, JSONObject json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.println(json.toString());
            logger.debug("Response sent: {}", json);
        }
    }
}
