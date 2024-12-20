package Server.Enum;

// This is a map of all accepted pages to redirect to upon url request. Note that these map from url path to fileNAME only, no paths! (might change at a later date) -Kyle
import java.util.Map;

public class Pages {
    private Pages() {
        // restrict instantiation -Kyle
    }

    // map of arg limit is 10, so... no more pages
    public static final Map<String, String> MAPPED_FILES = Map.of(
            "/", "index.html",
            "404", "404.html",
            "staffPayroll", "staffPayroll.html",
            // Delete adminpayroll if we direly need another map
            // Could just do what it does via user Permission + JS
            "adminPayroll", "adminPayroll.html",
            "session", "session.html",
            "dashboard", "dashboard.html",
            "calendar", "calendar.html",
            "billing", "billing.html",
            "chat", "chat.html",
            "history", "history.html"
            );
}