package net.teujaem.nrDonation.websoket;

import net.teujaem.nrDonation.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

public class MCWebSocketServerApplication {

    private MCWebSocketServer server;
    private volatile boolean started;

    public MCWebSocketServerApplication() {
        start();
    }

    private void start() {
        String host = Main.getInstance().getConfigManager().getHost();
        int port = Main.getInstance().getConfigManager().getPort();

        server = new MCWebSocketServer(host, port);
        try {
            server.start();
            started = true;
            Bukkit.getLogger().info("WS started: ws://" + host + ":" + port);
        } catch (Exception e) {
            started = false;
            Bukkit.getLogger().severe("WS start failed: " + e.getMessage());
            stop();
        }
    }

    public void broadcastServer(String message) {
        if (!started) return;
        server.broadcastServer(message);
    }

    public void sendUser(WebSocket ws, String message) {
        if (!started) return;
        server.sendUser(ws, message);
    }

    public void sendUser(Player player, String message) {
        if (!started) return;
        WebSocket ws = server.getWSUser(player.getName());
        server.sendUser(ws, message);
    }

    public void close(WebSocket ws) {
        if (!started) return;
        server.close(ws);
    }

    public boolean containSessionsIdUser(String id) {
        if (!started) return false;
        return server.containSessionsIdUser(id);
    }

    public void addSessionsIdUser(WebSocket ws, String id) {
        if (!started) return;
        server.addSessionsIdUser(ws, id);
    }

    public boolean containSessionsIdServer(String id) {
        if (!started) return false;
        return server.containSessionsIdServer(id);
    }

    public void addSessionsIdServer(WebSocket ws, String id) {
        if (!started) return;
        server.addSessionsIdServer(ws, id);
    }

    public String getKeyUser(WebSocket ws) {
        return server.getKeyUser(ws);
    }

    public void stop() {
        try {
            if (server != null) {
                server.stop(1000);
                Bukkit.getLogger().info("WS stopped");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("WS stop error: " + e.getMessage());
        } finally {
            started = false;
        }
    }

}
