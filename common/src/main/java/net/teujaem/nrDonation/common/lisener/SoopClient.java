package net.teujaem.nrDonation.common.lisener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.nodeJSAPI.SoopChatApiClient;
import net.teujaem.nrDonation.common.data.PlatformType;
import net.teujaem.nrDonation.common.data.soop.doantion.DonationList;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class SoopClient {

    private static String BASE_URL;
    private static String CLIENT_ID;
    private static String CLIENT_SECRET;
    private static String ACCESS_TOKEN;
    private static String SESSION_ID;
    private static CountDownLatch latch;
    private static DonationList donationList;

    private final static ConfigManager configManager = MainAPI.getInstance().getDataClassManager().getConfigManager();

    public void run() throws IOException, InterruptedException {

        CLIENT_ID = MainAPI.getInstance().getDataClassManager().getApiKey().getId(PlatformType.SOOP);
        CLIENT_SECRET = MainAPI.getInstance().getDataClassManager().getApiKey().getSecret(PlatformType.SOOP);
        SESSION_ID = MainAPI.getInstance().getPlayerName() + "_" + UUID.randomUUID().toString().replace("-", "");
        ACCESS_TOKEN = MainAPI.getInstance().getDataClassManager().getAccessToken().getSoop();
        BASE_URL = "http://" + MainAPI.getInstance().getDataClassManager().getNodeJSUrl().getURL();
        donationList = MainAPI.getInstance().getDataClassManager().getDonationList();
        System.out.println(BASE_URL);
        SoopChatApiClient client = new SoopChatApiClient(BASE_URL);

        sessionManagement(client);
        eventStreaming(client);

    }

    private static void sessionManagement(SoopChatApiClient client) throws IOException, InterruptedException {

        System.out.println("[SoopClient] 세션 시작");
        client.startSession(SESSION_ID, CLIENT_ID, CLIENT_SECRET, ACCESS_TOKEN);

        client.getSessionStatus(SESSION_ID);

        // 1.3. Set Auth (accessToken이 있는 경우)
        if (ACCESS_TOKEN != null && !ACCESS_TOKEN.isEmpty()) {
            client.setAuth(SESSION_ID, ACCESS_TOKEN);
        }
        client.connect(SESSION_ID);
        client.getSessionStatus(SESSION_ID);
    }

    private static void eventStreaming(SoopChatApiClient client) throws InterruptedException {
        System.out.println("[SoopClient] 채팅 이벤트 감지 시작");

        latch = new CountDownLatch(1);

        // SSE 연결
        client.openEventStream(SESSION_ID, event -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode root = mapper.readTree(event);
                String type = root.path("type").asText();
                String action = root.path("action").asText();

                if ("MESSAGE_RECEIVED".equals(type)) {
                    JsonNode node = root.path("message");

                    if (action.equals("MESSAGE")) {
                        onChat(node);
                    }
                    if (action.equals("BALLOON_GIFTED")) {
                        onDonation(node);
                    }

                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, error -> System.out.println("[SoopClient] SSE Error: {}" + error.getMessage()));

        latch.await();
    }

    public void stop() {
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    private static void onDonation(JsonNode node) {
        String nickname = node.path("userNickname").asText();
        int count = node.path("count").asInt();
        System.out.println("[Soop Donation] " + nickname + ": " + count);

        donationList.addDonation(nickname, count);
    }

    private static void onChat(JsonNode node) {
        String nickname = node.path("userNickname").asText();
        String message = node.path("message").asText();
        System.out.println("[Soop Chat] " + nickname + ": " + message);

        if (donationList.hasSender(nickname)) {
            if (!configManager.getDonation()) return;
            MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
            mcWebSocketSendMessage.to("event//donation//soop//" + nickname + "//" + donationList.getFirstCountOf(nickname) + "//" + message);
            donationList.removeFirstOf(nickname);
        } else {
            if (!configManager.getChat()) return;
            MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
            mcWebSocketSendMessage.to("event//chat//soop//" + nickname + "//" + message);
        }
    }
}
