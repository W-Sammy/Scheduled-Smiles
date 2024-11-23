package Server.Enum;
// This is a map of all http protcool methods, codes, and other values used during connections, with their meanings.

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpConstants {
    public static final int NO_RESPONSE_LENGTH = -1;
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    private HttpConstants() {
        // restrict instantiation -Kyle
    }
    public static final Map<String, Integer> STATUS_CODES = Map.of(
        "OK", 200,
        "BAD_REQUEST", 400,
        "NOT_FOUND", 404,
        "NOT_ALLOWED", 405,
        "INTERNAL_SERVER_ERROR", 500
    );
    public static final Map<String, String> METHODS = Map.of(
        "GET", "GET",
        "PUT", "PUT",
        "POST", "POST",
        "PATCH", "PATCH",
        "OPTIONS", "OPTIONS"
    );
    public static final Map<String, String> HEADER_KEYS = Map.of(
        "CONTENT_TYPE", "Content-Type",
        "CONTENT_DISPOSITION", "Content-Disposition",
        "ALLOW", "Allow"
    );
    public static final Map<String, String> HEADER_TYPES = Map.of(
        "JSON", String.format("application/json; charset=%s", CHARSET),
        "TEXT", String.format("text/plain; charset=%s", CHARSET),
        "HTML", String.format("text/html; charset=%s", CHARSET),
        "JS", String.format("text/javascript; charset=%s", CHARSET),
        "CSS", String.format("text/css; charset=%s", CHARSET)
    );
    public static final String ALLOWED_METHODS = "GET,POST,OPTIONS"; // we only allow these in our server anyways, so I've defined them as a constant here. If we expand our accepted list of methods, remove this. -Kyle
}