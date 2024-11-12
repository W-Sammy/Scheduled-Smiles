package Server.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import com.google.gson.*;

import static Server.Enum.HttpConstants.*; // only need the CHARSET attribute, is there a better way to import this? -Kyle

public class Json {
    private Json() {
        // restrict instantiation -Kyle
    }
    public static JsonObject convertToJson(final String jsonString) {
        return new Gson().fromJson(jsonString, JsonObject.class);
    }
    public static JsonObject convertToJson(final InputStream jsonStream) throws UnsupportedEncodingException {
        return new Gson().fromJson(new InputStreamReader(jsonStream, CHARSET.name()), JsonObject.class);
    }
    public static String convertFromJson(Object jsonObj) {
        return new Gson().toJson(jsonObj);
    }
    public static Set<String> getKeys(JsonObject jsonObj) {
        Set<String> keys = new HashSet<String>();
        for(Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }
    public static boolean membersMatch(final Set<String> members, final String ...keys) {
        final Set<String> keySet = new HashSet<String>();
        keySet.addAll(Arrays.asList(keys));
        return members.equals(keySet);
    }   
}