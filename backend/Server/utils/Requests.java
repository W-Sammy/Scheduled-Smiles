package Server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URI;
import java.util.Map;
import java.util.LinkedHashMap;

import static Server.Enum.HttpConstants.*; // only need the CHARSET attribute, is there a better way to import this? -Kyle
import static Server.utils.Json.*;

public class Requests {
    private Requests() {
        // restrict instantiation -Kyle
    }
    public static String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException e) {
            return null;
        }
    }
    public static String getRequestFragment(final String rawRequestFragment) {
        if (rawRequestFragment != null) {
            return decodeUrlComponent(rawRequestFragment);
        }
        return null;
    }
    public static Map<String, String> getRequestQueryParameters(final String rawRequestQuery) {
        final Map<String, String> queryParameters = new LinkedHashMap<>();
        if (rawRequestQuery != null) {
            final String[] keyValues = rawRequestQuery.split("[&;]", -1);
            for (final String keyValue : keyValues) {
                final String[] parameter = keyValue.split("=", 2);
                final String key = decodeUrlComponent(parameter[0]);
                final String value = (parameter.length > 1) ? decodeUrlComponent(parameter[1]) : null;
                queryParameters.putIfAbsent(key, value);
            }
        }
        return queryParameters;
    }
    
}