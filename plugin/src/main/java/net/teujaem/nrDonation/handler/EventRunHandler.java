package net.teujaem.nrDonation.handler;

import net.teujaem.nrDonation.Main;
import net.teujaem.nrDonation.config.ConfigManager;
import net.teujaem.nrDonation.event.*;
import net.teujaem.nrDonation.websoket.MCWebSocketServerApplication;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.java_websocket.WebSocket;

import java.util.List;

public class EventRunHandler {

    private static final ConfigManager configManager = Main.getInstance().getConfigManager();

    private static final MCWebSocketServerApplication application = Main.getInstance().getWebSocketServerApplication();

    public void ofServer(WebSocket ws, String[] messages) {
        
        String client = messages[0];
        String event = messages[1];

        if (client.equals("user")) { // ws 보낸 클라이언트가 유저라면
            if (event.equals("setSessionsId")) { // 서버에 접속했을 때 초기 설정
                String id = messages[2];
                if (id == null) return;
                // 이미 세션이 추가되어 있을 경우 취소
                if (application.containSessionsIdUser(id)){
                    application.close(ws);
                    return;
                }
                application.addSessionsIdUser(ws, id); // 세션에 새로운 id 추가
                APISetting(ws); // API가 설정 되어 있으면 클라이언트에 전송
            }
            if (event.equals("event")) { // 이벤트
                String eventType = messages[2];
                if (!isEventType(eventType)) return;
                String id = application.getKeyUser(ws);
                if (id == null) return; // 세션에 추가되지 않은 상태 예외처리
                if (eventType.equals("donation")) {
                    String flatform = messages[3];
                    String sender = messages[4];
                    int amount;
                    String eventMessage = "null";
                    if (messages[6] != null) {
                        eventMessage = messages[6];
                    }
                    //예외처리
                    try {
                        amount = Integer.parseInt(messages[5]);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException(e);
                    }
                    if (
                        flatform == null ||
                        sender == null
                    ) return;
                    onDonation(id, flatform, sender, amount, eventMessage);

                }

                if (eventType.equals("chat")) {
                    String flatform = messages[3];
                    String sender = messages[4];
                    String eventMessage = "null";
                    if (messages[5] != null) {
                        eventMessage = messages[5];
                    }
                    if ( //예외처리
                        flatform == null ||
                        sender == null
                    ) return;
                    onChat(id, flatform, sender, eventMessage);
                }
                if (eventType.equals("login")) {
                    String flatform = messages[3];
                    if (flatform == null) return;
                    onLogin(id, flatform);
                }
                if (eventType.equals("logout")) {
                    String flatform = messages[3];
                    if (flatform == null) return;
                    onLogout(id, flatform);
                }
            }
        }

        if (client.equals("server")) {
            if (event.equals("setSessionsId")) {
                String id = messages[2];
                if (id == null) return;
                // 이미 세션이 추가되어 있을 경우 취소
                if (application.containSessionsIdServer(id)){
                    application.close(ws);
                    return;
                }
                application.addSessionsIdServer(ws, id); // 세션에 새로운 id 추가
            }
        }
    }

    public void ofClient(String[] messages) {
        String id = messages[0];
        String type = messages[1];
        if (type.equals("event")) {
            String eventType = messages[2];
            String flatform = messages[3];
            Player player = Bukkit.getPlayer(id);
            if (player == null) return;
            if (eventType.equals("donation")) {
                String sender = messages[4];
                int amount = Integer.parseInt(messages[5]);
                String eventMessage = messages[6];
                onDonation(player, flatform, sender, amount, eventMessage);
            }
            if (eventType.equals("chat")) {
                String sender = messages[4];
                String eventMessage = messages[5];
                onChat(player, flatform, sender, eventMessage);
            }
            if (eventType.equals("login")) {
                onLogin(player, flatform);
            }
            if (eventType.equals("logout")) {
                onLogout(player, flatform);
            }
        }
    }

    private void onDonation(String id, String flatform, String sender, int amount, String eventMessage) {
        Player player = Bukkit.getPlayer(id);

        if (player != null && player.isOnline()) { // 유저가 backend server에 접속중이면 이벤트 처리
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                DonationEvent bukkitEvent = new DonationEvent(flatform, sender, amount, eventMessage, player);
                Bukkit.getPluginManager().callEvent(bukkitEvent);
            });
        }
        else { // 접속중이지 않으면 모든 다른 서버에 메세지 전송
            application.broadcastServer( id + "//event//donation//" + flatform + "//" + sender + "//" + amount + "//" + eventMessage);
        }
    }

    private void onChat(String id, String flatform, String sender, String eventMessage) {
        Player player = Bukkit.getPlayer(id);

        if (player != null && player.isOnline()) { // 유저가 backend server에 접속중이면 이벤트 처리
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                ChatEvent bukkitEvent = new ChatEvent(flatform, sender, eventMessage, player);
                Bukkit.getPluginManager().callEvent(bukkitEvent);
            });
        }
        else { // 접속중이지 않으면 모든 다른 서버에 메세지 전송
            application.broadcastServer( id + "//event//chat//" + flatform + "//" + sender + "//" + eventMessage);
        }
    }

    private void onLogin(String id, String flatform) {
        Player player = Bukkit.getPlayer(id);

        if (player != null && player.isOnline()) { // 유저가 backend server에 접속중이면 이벤트 처리
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                LoginEvent loginEvent = new LoginEvent(flatform, player);
                Bukkit.getPluginManager().callEvent(loginEvent);
            });
        }
        else { // 접속중이지 않으면 모든 다른 서버에 메세지 전송
            application.broadcastServer( id + "//event//login//" + flatform);
        }
    }

    private void onLogout(String id, String flatform) {
        Player player = Bukkit.getPlayer(id);

        if (player != null && player.isOnline()) { // 유저가 backend server에 접속중이면 이벤트 처리
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                LogoutEvent logoutEvent = new LogoutEvent(flatform, player);
                Bukkit.getPluginManager().callEvent(logoutEvent);
            });
        }
        else { // 접속중이지 않으면 모든 다른 서버에 메세지 전송
            application.broadcastServer( id + "//event//logout//" + flatform);
        }
    }

    private void onDonation(Player player, String flatform, String sender, int amount, String eventMessage) {
        if (!(player != null || player.isOnline())) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            DonationEvent bukkitEvent = new DonationEvent(flatform, sender, amount, eventMessage, player);
            Bukkit.getPluginManager().callEvent(bukkitEvent);
        });
    }

    private void onChat(Player player, String flatform, String sender, String eventMessage) {
        if (!(player != null || player.isOnline())) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            ChatEvent bukkitEvent = new ChatEvent(flatform, sender, eventMessage, player);
            Bukkit.getPluginManager().callEvent(bukkitEvent);
        });
    }

    private void onLogin(Player player, String flatform) {
        if (!(player != null || player.isOnline())) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            LoginEvent loginEvent = new LoginEvent(flatform, player);
            Bukkit.getPluginManager().callEvent(loginEvent);
        });
    }

    private void onLogout(Player player, String flatform) {
        if (!(player != null || player.isOnline())) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            LogoutEvent logoutEvent = new LogoutEvent(flatform, player);
            Bukkit.getPluginManager().callEvent(logoutEvent);
        });
    }

    private void onSessionSet(Player player) {
        if (!(player != null || player.isOnline())) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            SessionSetEvent sessionSetEvent = new SessionSetEvent(player);
            Bukkit.getPluginManager().callEvent(sessionSetEvent);
        });
    }

    private void APISetting(WebSocket ws) {

        // 숲 API 세팅 전송
        if (!(
                configManager.getSoopId() == null ||
                configManager.getSoopId() == "id" ||
                configManager.getSoopSecret() == null ||
                configManager.getSoopSecret() == "secret" ||
                configManager.getSoopNodejs() == null
        )) {
            application.sendUser(ws, "put.apiKey//soop//" + configManager.getSoopId() + "//" + configManager.getSoopSecret());
            application.sendUser(ws, "put.apiServer//soop//" + configManager.getSoopNodejs());
        }

        // 치지직 API 세팅 전송
        if (!(
                configManager.getChzzkId() == null ||
                configManager.getChzzkId() == "id" ||
                configManager.getChzzkSecret() == null ||
                configManager.getChzzkSecret() == "secret"
        )) {
            application.sendUser(ws, "put.apiKey//chzzk//" + configManager.getChzzkId() + "//" + configManager.getChzzkSecret());
        }

        Player player = Bukkit.getPlayer(application.getKeyUser(ws));
        onSessionSet(player);

    }

    private boolean isEventType(String type) {
        List<String> list = List.of(
                "donation",
                "chat",
                "login",
                "logout"
        );
        return list.contains(type);
    }

}
