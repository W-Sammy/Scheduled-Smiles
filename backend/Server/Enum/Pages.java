package Server.Enum;
// This is a map of all accepted pages to redirect to upon url request. Note that these map from url path to fileNAME only, no paths! (might change at a later date) -Kyle
import java.util.Map;

public class Pages {
    private Pages() {
        // restrict instantiation -Kyle
    }
    public static final Map<String, String> MAPPED_FILES = Map.of(
        "home", "index.html",
        "style", "styles.css",
        "login", "login.html",
        "404", "404.html",
        "", "index.html" // temporary
    );
}