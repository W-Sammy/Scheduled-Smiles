package Server.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.lang.reflect.Type;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import Server.utils.DatabaseGenericParameter;
import static Server.Enum.HttpConstants.*; // only need the CHARSET attribute, is there a better way to import this? -Kyle

public class Json {
    private Json() {
        // restrict instantiation -Kyle
    }    
    public static JsonElement convertToJsonElement(final String jsonString) {
        return new Gson().fromJson(jsonString, JsonElement.class);
    }
    public static JsonElement convertToJsonElement(final InputStream jsonStream) throws UnsupportedEncodingException {
        final InputStreamReader isr = new InputStreamReader(jsonStream, CHARSET.name());
        return new Gson().fromJson(isr, JsonElement.class);
    }
    
    public static String convertFromJson(List<List<DatabaseGenericParameter>> obj) {
        final List<List<String>> newObj = new ArrayList<>();
        int i = 0, j = 0;
        while (i < obj.size()) {
            newObj.add(new ArrayList<String>());
            j = 0;
            while (j < obj.get(i).size()) {
                newObj.get(i).add(obj.get(i).get(j).toString());
                j++;
            }
            i++;
        }
        return convertFromJson(newObj, String.class);
    }
    
    public static <T> String convertFromJson(List<List<T>> obj, Class<T> type) {
        Type genericListType = TypeToken.getParameterized(List.class, type).getType();
        Type genericListListType = TypeToken.getParameterized(List.class, genericListType).getType();
        return new Gson().toJson(obj, genericListListType);   
    }
        
    public static String convertFromJson(Object jsonObj) {
        return new Gson().toJson(jsonObj);
    }
    public static Set<String> getKeys(JsonElement json) {
        Set<String> keys = new HashSet<String>();
        for(Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
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