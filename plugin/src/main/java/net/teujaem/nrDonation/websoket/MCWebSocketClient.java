package net.teujaem.nrDonation.websoket;

import net.teujaem.nrDonation.Main;
import net.teujaem.nrDonation.event.DonationEvent;
import net.teujaem.nrDonation.handler.EventRunHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.UUID;

public class MCWebSocketClient extends WebSocketClient {
    public MCWebSocketClient() throws Exception {
        super(new URI("ws://" + Main.getInstance().getConfigManager().getHost() + ":" + Main.getInstance().getConfigManager().getPort() + "/ws"));
        connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Bukkit.getLogger().info("WebSocket 클라이언트 연결 완료");
        String id = String.valueOf(UUID.randomUUID()).replace("-", "");
        send("setSessionsId//" + id);
    }

    @Override
    public void onMessage(String message) {
        Bukkit.getLogger().info(message);
        if (message == null) return;
        if (message.isEmpty()) return;
        if (!message.contains("//")) return;
        String[] messages = message.split("//");
        EventRunHandler eventRunHandler = new EventRunHandler();
        eventRunHandler.ofClient(messages);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Bukkit.getLogger().info("연결 종료 : " + reason + " (" + code + ")");
    }

    @Override
    public void onError(Exception ex) {
        Bukkit.getLogger().info("클라이언트 WebSocket 오류 : " + ex.getMessage());
    }

    @Override
    public void send(String text) {
        super.send("server//" + text);
    }

}
