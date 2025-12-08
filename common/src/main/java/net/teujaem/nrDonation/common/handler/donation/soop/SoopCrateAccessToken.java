package net.teujaem.nrDonation.common.handler.donation.soop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.teujaem.nrDonation.common.util.UrlEncoding;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class SoopCrateAccessToken {

    private static final String BASE_URL = "https://openapi.sooplive.co.kr/auth/token";

    private static String clientId;
    private static String clientSecret;

    public SoopCrateAccessToken(String clientId, String clientSecret) {
        SoopCrateAccessToken.clientId = clientId;
        SoopCrateAccessToken.clientSecret = clientSecret;
    }

    public String getAccessToken(String code) throws Exception {

        // body setting
        Map<String, String> map = new HashMap<String, String>();
        map.put("grant_type", "authorization_code");
        map.put("client_id", clientId);
        map.put("client_secret", clientSecret);
        map.put("redirect_uri", "https://localhost:8080/callback");
        map.put("code", code);
        map.put("refresh_token", "null");

        String body = UrlEncoding.toXWwwFormUrl(map);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(
                body, MediaType.parse("application/x-www-form-urlencoded")
        );

        // GET token
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body().string());

        return rootNode.get("access_token").asText(null);
    }
}
