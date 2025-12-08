package net.teujaem.nrDonation.common.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class UrlEncoding {

    public static String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("\"")
                    .append(entry.getKey().replace("\"", "\\\""))
                    .append("\":\"")
                    .append(entry.getValue().replace("\"", "\\\""))
                    .append("\"");

            if (i < map.size() - 1) sb.append(",");
            i++;
        }

        sb.append("}");
        return sb.toString();
    }

    public static String toXWwwFormUrl(Map<String, String> map) {
        StringBuilder body = new StringBuilder();

        for (Map.Entry<String, String> entry : map.entrySet()) {

            if (body.length() > 0) body.append("&");

            body.append(encode(entry.getKey()));
            body.append("=");
            body.append(encode(entry.getValue()));
        }

        return body.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8"); // Java 8: charset as String
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
