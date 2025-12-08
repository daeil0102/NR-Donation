package net.teujaem.nrDonation.common.handler.donation.chzzk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.teujaem.nrDonation.common.util.UrlEncoding;
import okhttp3.*;

import java.util.HashMap;
import java.util.Map;

public class ChzzkCrateAccessToken {

    private static final String BASE_URL = "https://openapi.chzzk.naver.com/auth/v1/token";

    private final String clientId;
    private final String clientSecret;

    private static final OkHttpClient client = new OkHttpClient();

    public ChzzkCrateAccessToken(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken(String code, String state) throws Exception {

        // body setting (Map.of → JDK 8 불가 → HashMap 사용)
        Map<String, String> map = new HashMap<>();
        map.put("grantType", "authorization_code");
        map.put("clientId", clientId);
        map.put("clientSecret", clientSecret);
        map.put("code", code);
        map.put("state", state);

        String body = UrlEncoding.toJson(map);

        // POST tokens (java.net.http 제거 → OkHttp로 대체)
        Request request = new Request.Builder()
                .url(BASE_URL)
                .post(RequestBody.create(
                        body,
                        MediaType.parse("application/json")
                ))
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body().string());
            JsonNode content = json.path("content");

            return content.path("accessToken").asText(null);
        }

        return null;
    }
}
