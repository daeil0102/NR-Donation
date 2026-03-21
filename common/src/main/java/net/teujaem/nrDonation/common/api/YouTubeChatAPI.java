package net.teujaem.nrDonation.common.api;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class YouTubeChatAPI {

    private String continuation;
    private final Gson gson = new Gson();

    public YouTubeChatAPI(String videoId) throws Exception {

        String html = request(
                "https://www.youtube.com/live_chat?is_popout=1&v=" + videoId
        );

        String json = extractInitialData(html);

        JsonObject data = JsonParser.parseString(json).getAsJsonObject();

        continuation = findContinuation(data);
    }

    public interface ChatListener {
        void onChat(String nickname, String message);
        void onDonation(String nickname, String amount, String message);
    }

    private ChatListener listener;

    public void setListener(ChatListener listener) {
        this.listener = listener;
    }

    public void listen() throws Exception {

        while (true) {

            String url =
                    "https://www.youtube.com/youtubei/v1/live_chat/get_live_chat?key=" +
                            "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";

            String body =
                    "{"
                            + "\"context\":{"
                            + "\"client\":{"
                            + "\"clientName\":\"WEB\","
                            + "\"clientVersion\":\"2.20260311.00.00\""
                            + "}"
                            + "},"
                            + "\"continuation\":\"" + continuation + "\""
                            + "}";

            String response = post(url, body);

            JsonObject root = JsonParser.parseString(response).getAsJsonObject();

            JsonObject cont = root
                    .getAsJsonObject("continuationContents")
                    .getAsJsonObject("liveChatContinuation");

            JsonArray actions = cont.getAsJsonArray("actions");

            if (actions != null) {
                for (JsonElement action : actions) {

                    JsonObject actionObj = action.getAsJsonObject();

                    if (!actionObj.has("addChatItemAction")) continue;

                    JsonObject item = actionObj
                            .getAsJsonObject("addChatItemAction")
                            .getAsJsonObject("item");

                    if (item.has("liveChatTextMessageRenderer")) {

                        JsonObject renderer = item.getAsJsonObject("liveChatTextMessageRenderer");

                        String author =
                                renderer.get("authorName")
                                        .getAsJsonObject()
                                        .get("simpleText")
                                        .getAsString();

                        String message = "";

                        if (renderer.has("message")) {
                            JsonArray runs = renderer
                                    .getAsJsonObject("message")
                                    .getAsJsonArray("runs");

                            StringBuilder sb = new StringBuilder();

                            for (JsonElement r : runs) {
                                JsonObject run = r.getAsJsonObject();
                                if (run.has("text"))
                                    sb.append(run.get("text").getAsString());
                            }

                            message = sb.toString();
                        }

                        if (listener != null) {
                            listener.onChat(author, message);
                        }
                    }

                    if (item.has("liveChatPaidMessageRenderer")) {

                        JsonObject renderer = item.getAsJsonObject("liveChatPaidMessageRenderer");

                        String author =
                                renderer.get("authorName")
                                        .getAsJsonObject()
                                        .get("simpleText")
                                        .getAsString();

                        String amount =
                                renderer.get("purchaseAmountText")
                                        .getAsJsonObject()
                                        .get("simpleText")
                                        .getAsString();

                        String message = "";

                        if (renderer.has("message")) {
                            JsonArray runs = renderer
                                    .getAsJsonObject("message")
                                    .getAsJsonArray("runs");

                            StringBuilder sb = new StringBuilder();

                            for (JsonElement r : runs) {
                                JsonObject run = r.getAsJsonObject();
                                if (run.has("text"))
                                    sb.append(run.get("text").getAsString());
                            }

                            message = sb.toString();
                        }

                        if (listener != null) {
                            listener.onDonation(author, amount, message);
                        }
                    }
                }
            }

            JsonArray continuations = cont.getAsJsonArray("continuations");

            JsonObject c = continuations.get(0).getAsJsonObject();

            if (c.has("invalidationContinuationData")) {
                continuation = c
                        .getAsJsonObject("invalidationContinuationData")
                        .get("continuation")
                        .getAsString();
            } else if (c.has("timedContinuationData")) {
                continuation = c
                        .getAsJsonObject("timedContinuationData")
                        .get("continuation")
                        .getAsString();
            }

            Thread.sleep(3000);
        }
    }

    private String findContinuation(JsonObject data) {

        JsonObject contents = data.getAsJsonObject("contents");
        JsonObject liveChatRenderer = contents.getAsJsonObject("liveChatRenderer");

        JsonArray continuations = liveChatRenderer
                .getAsJsonArray("continuations");

        JsonObject c = continuations.get(0).getAsJsonObject();

        if (c.has("invalidationContinuationData")) {
            return c
                    .getAsJsonObject("invalidationContinuationData")
                    .get("continuation")
                    .getAsString();
        }

        return c
                .getAsJsonObject("timedContinuationData")
                .get("continuation")
                .getAsString();
    }

    private String extractInitialData(String html) {

        int start = html.indexOf("ytInitialData");

        if (start == -1)
            throw new RuntimeException("ytInitialData not found");

        start = html.indexOf("{", start);

        int brace = 0;
        int end = start;

        for (int i = start; i < html.length(); i++) {

            char c = html.charAt(i);

            if (c == '{') brace++;
            if (c == '}') brace--;

            if (brace == 0) {
                end = i + 1;
                break;
            }
        }

        return html.substring(start, end);
    }

    private String request(String urlStr) throws Exception {

        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36");

        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Referer", "https://www.youtube.com/");

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    private String post(String urlStr, String body) throws Exception {

        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

}