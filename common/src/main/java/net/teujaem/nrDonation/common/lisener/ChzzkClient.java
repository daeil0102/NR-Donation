package net.teujaem.nrDonation.common.lisener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.handler.donation.chzzk.ChzzkAddEvent;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

import java.net.URISyntaxException;

public class ChzzkClient {

    private Socket socket;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConfigManager configManager = MainAPI.getInstance().getDataClassManager().getConfigManager();

    public void run(String sessionUrl) {

        new Thread(() -> {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = false;
                options.forceNew = true;
                options.timeout = 3000;
                options.transports = new String[]{"websocket"};

                System.out.println("[ChzzkClient] Socket.IO 옵션 초기화 완료");

                socket = IO.socket(sessionUrl, options);

                // 연결 성공
                socket.on(Socket.EVENT_CONNECT, args -> System.out.println("[ChzzkClient] CHZZK Socket.IO 연결 성공"));

                // 연결 오류
                socket.on("connect_error", args -> System.out.println("[ChzzkClient] 연결 오류: " + (args.length > 0 ? args[0] : "unknown")));

                // 연결 종료
                socket.on("disconnect", args -> System.out.println("[ChzzkClient] 연결 종료"));

                // 시스템 이벤트 수신
                socket.on("SYSTEM", args -> {
                    try {
                        JsonNode json = mapper.readTree(args[0].toString());
                        System.out.println("[ChzzkClient] 시스템 이벤트: " + json);

                        if ("connected".equals(json.path("type").asText())) {
                            String sessionKey = json.path("data").path("sessionKey").asText();
                            new ChzzkAddEvent(sessionKey);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // 채팅 이벤트 수신
                socket.on("CHAT", args -> {
                    try {
                        JsonNode node = mapper.readTree((String) args[0]);
                        onChat(node);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });

                // 도네이션 이벤트 수신
                socket.on("DONATION", args -> {
                    try {
                        JsonNode node = mapper.readTree((String) args[0]);
                        onDonation(node);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                });

                System.out.println("[ChzzkClient] 소켓 연결 시도 중...");
                socket.connect();

            } catch (URISyntaxException e) {
                System.err.println("[ChzzkClient] URL 형식 오류: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("[ChzzkClient] 예외 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }, "Chzzk-Socket-Thread").start();
    }

    public void stop() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
            socket.close();
            System.out.println("[ChzzkClient] 소켓 종료됨");
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private void onDonation(JsonNode node) {
        String nickname = node.path("donatorNickname").asText();
        String amount = node.path("payAmount").asText();
        String message = node.path("donationText").asText();

        if (message.isEmpty()) message = "null";

        System.out.println("[Chzzk Donation] " + nickname + "(" + amount + "치즈: " +  "): " + message);

        if (!configManager.getDonation()) return;

        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//donation//chzzk//" + nickname + "//" + amount + "//" + message);
    }

    private void onChat(JsonNode node) {
        String nickname = node.path("profile").path("nickname").asText();
        String message = node.path("content").asText();

        System.out.println("[Chzzk Chat] " + nickname + ": " + message);

        if (!configManager.getChat()) return;

        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//chat//chzzk//" + nickname + "//" + message);
    }
}