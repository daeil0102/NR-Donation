package net.teujaem.nrDonation.common.handler.donation.soop;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;

public class SoopCreateCode {

    private static final String BASE_URL = "https://openapi.sooplive.co.kr/auth/code";

    private static String clientId;

    public SoopCreateCode(String clientId) {
        SoopCreateCode.clientId = clientId;
    }

    public URI getLoginUrl() throws IOException {

        String query = "client_id=" + URLEncoder.encode(clientId, "UTF-8");
        String url = BASE_URL + "?" + query;

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "*/*")
                .get()
                .build();

        Response response = client.newCall(request).execute();

        return URI.create(response.request().url().toString());
    }
}
