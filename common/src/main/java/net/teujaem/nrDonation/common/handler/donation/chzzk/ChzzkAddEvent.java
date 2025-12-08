package net.teujaem.nrDonation.common.handler.donation.chzzk;

import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.util.UrlEncoding;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChzzkAddEvent {

    private static final String BASE_URL = "https://openapi.chzzk.naver.com/open/v1/sessions/events/subscribe/";

    private static final String accessToken = MainAPI.getInstance().getDataClassManager().getAccessToken().getChzzk();

    // OkHttpClient는 재사용해야 성능 좋고 연결 누수 안 남
    private static final OkHttpClient client = new OkHttpClient();

    public ChzzkAddEvent(String sessionKey) throws IOException {
        addChatEvent(sessionKey);
        addDonationEvent(sessionKey);
    }

    private void addChatEvent(String sessionKey) throws IOException {
        // body setting (Map.of 사용 불가 → HashMap으로 대체)
        Map<String, String> map = new HashMap<>();
        map.put("sessionKey", sessionKey);

        String body = UrlEncoding.toXWwwFormUrl(map);

        // POST chat
        Request request = new Request.Builder()
                .url(BASE_URL + "chat")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(body, MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        Response response = client.newCall(request).execute();
        response.close(); // body 필요 없음 → 바로 close
    }

    private void addDonationEvent(String sessionKey) throws IOException {
        // body setting
        Map<String, String> map = new HashMap<>();
        map.put("sessionKey", sessionKey);

        String body = UrlEncoding.toXWwwFormUrl(map);

        // POST donation
        Request request = new Request.Builder()
                .url(BASE_URL + "donation")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(body, MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());

        response.close();
    }

}
