package Server.utils;
import java.io.File;
import static Server.Enum.Pages.*;

public class FileHandler {
    private FileHandler() {
        // restrict instantiation -Kyle
    }
    public static File getFile(final String rawPath, final String serverContext, final String localContext) {
        final String path = rawPath.split(serverContext, 2)[1];
        final String filePath = (MAPPED_FILES.containsKey(path)) ? MAPPED_FILES.get(path) : localContext + path;
        final File file = new File(filePath);
        return (file.exists() && !file.isDirectory()) ? file : null;
    }
    public static String getFileType(final String filename) {
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}