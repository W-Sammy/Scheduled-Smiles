package Server.utils;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URI;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HexFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static Server.Enum.HttpConstants.*; // only need the CHARSET attribute, is there a better way to import this? -Kyle
import static Server.utils.Json.*;
import static Users.Enum.RoleConstant.*;

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
    
    public static byte[] hash256(final byte[] content) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(content);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("This should never happen, but it did. \"SHA-256\" is has not been recognized as a valid algorithm to use!");
            return null;
        }
    }
    
    public static byte[] hash256(final String content) {
        return hash256(content.getBytes(CHARSET));
    }
    
    public static byte[] getRoleId(final String email) {
        final int splitIdx = email.lastIndexOf("@");
        if (splitIdx != -1) {
            final String domain = email.substring(splitIdx);
            for (Map.Entry<String, String> entry : ROLE_DOMAINS.entrySet()) {
                if (entry.getValue().equals(domain)) {
                    return ROLE_IDS.get(entry.getKey());
                }
            }
        }
        return ROLE_IDS.get("Patient");
    }
    
    public static byte[] unhex(final String value) {
        return HexFormat.of().parseHex(value);
    }
    public static String hex(final byte[] digest) {
        String stringValue = "";
        for (byte i : digest) {
            stringValue += String.format("%02X", i);
        }
        return stringValue;
    }
}