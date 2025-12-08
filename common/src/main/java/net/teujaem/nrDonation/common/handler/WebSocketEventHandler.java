package net.teujaem.nrDonation.common.handler;

import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.data.PlatformType;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

public class WebSocketEventHandler {

    private static String playerName;

    public WebSocketEventHandler(String playerName) {
        WebSocketEventHandler.playerName = playerName;
    }

    public void action(String[] messages) {

        String event = messages[0];

        if (event.equals("getSessionsId")) {
            MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
            mcWebSocketSendMessage.to("setSessionsId//" + playerName);
        }

        if (event.equals("put.apiKey")) {
            if (messages[1].equals("soop")) {
                if (messages[2] == null) return;
                if (messages[3] == null) return;
                if (messages[2].isEmpty()) return;
                if (messages[3].isEmpty()) return;
                MainAPI.getInstance().getDataClassManager().getApiKey().setId(PlatformType.SOOP, messages[2]);
                MainAPI.getInstance().getDataClassManager().getApiKey().setSecret(PlatformType.SOOP, messages[3]);
            }
            if (messages[1].equals("chzzk")) {
                if (messages[2] == null) return;
                if (messages[3] == null) return;
                if (messages[2].isEmpty()) return;
                if (messages[3].isEmpty()) return;
                MainAPI.getInstance().getDataClassManager().getApiKey().setId(PlatformType.CHZZK, messages[2]);
                MainAPI.getInstance().getDataClassManager().getApiKey().setSecret(PlatformType.CHZZK, messages[3]);
            }
        }

        if (event.equals("put.apiServer")) {
            if (messages[1].equals("soop")) {
                if (messages[2] == null) return;
                if (messages[2].isEmpty()) return;
                MainAPI.getInstance().getDataClassManager().getNodeJSUrl().setURL(messages[2]);
            }
        }

        if (event.equals("loginTry")) {
            PlatformType platformType = null;
            if (messages[1].equals("soop")) {
                platformType = PlatformType.SOOP;
            }
            if (messages[1].equals("chzzk")) {
                platformType = PlatformType.CHZZK;
            }
            if (platformType == null) return;
            MainAPI.getInstance().eventLoginTry(platformType);
        }

    }

}
