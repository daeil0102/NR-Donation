package net.teujaem.nrDonation.websoket;

import net.teujaem.nrDonation.handler.EventRunHandler;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class MCWebSocketServer extends WebSocketServer {

    private static final HashMap<String, WebSocket> SESSIONS_CLIENT_ID = new HashMap<>();
    private static final HashMap<String, WebSocket> SESSIONS_SERVER_ID = new HashMap<>();

    public MCWebSocketServer(String host, int port) {
        super(new InetSocketAddress(host, port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Bukkit.getLogger().info("open: " + conn.getRemoteSocketAddress());
        conn.send("getSessionsId");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (SESSIONS_CLIENT_ID.containsValue(conn)) {
            String key = getKeyUser(conn);
            SESSIONS_CLIENT_ID.remove(key);
        }
        Bukkit.getLogger().info("close: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Bukkit.getLogger().info(message);
        String[] messages = message.split("//");
        EventRunHandler eventRunHandler = new EventRunHandler();
        eventRunHandler.ofServer(conn, messages);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Bukkit.getLogger().info("error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        Bukkit.getLogger().info("WebSocket server started");
    }

    public void broadcastServer(String message) {
        for (WebSocket ws : SESSIONS_SERVER_ID.values()) {
            ws.send(message);
        }
    }

    public void sendUser(WebSocket ws, String message) {
        ws.send(message);
    }

    public void close(WebSocket ws) {
        ws.close();
    }

    public boolean containSessionsIdUser(String id) {
        return SESSIONS_CLIENT_ID.containsKey(id);
    }

    public void addSessionsIdUser(WebSocket ws, String id) {
        SESSIONS_CLIENT_ID.put(id, ws);
    }

    public boolean containSessionsIdServer(String id) {
        return SESSIONS_SERVER_ID.containsKey(id);
    }

    public void addSessionsIdServer(WebSocket ws, String id) {
        SESSIONS_SERVER_ID.put(id, ws);
    }

    public String getKeyUser(WebSocket ws) {
        for (Map.Entry<String, WebSocket> entry : SESSIONS_CLIENT_ID.entrySet()) {
            if (entry.getValue().equals(ws)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public WebSocket getWSUser(String name) {
        return SESSIONS_CLIENT_ID.get(name);
    }

}
