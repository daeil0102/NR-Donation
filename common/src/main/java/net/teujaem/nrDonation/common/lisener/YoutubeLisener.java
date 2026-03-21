package net.teujaem.nrDonation.common.lisener;

import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.api.YouTubeChatAPI;
import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

public class YoutubeLisener {

    private final ConfigManager configManager = MainAPI.getInstance().getDataClassManager().getConfigManager();

    private static String url;
    public YoutubeLisener(String url) throws Exception {
        YoutubeLisener.url = url;
        run();
    }

    private void run() throws Exception {

        String videoId = url;

        YouTubeChatAPI chat = new YouTubeChatAPI(videoId);

        chat.setListener(new YouTubeChatAPI.ChatListener() {

            @Override
            public void onChat(String nickname, String message) {
                YoutubeLisener.this.onChat(nickname, message);
            }

            @Override
            public void onDonation(String nickname, String amount, String message) {
                int money = Integer.parseInt(amount.replaceAll("[^0-9]", ""));
                YoutubeLisener.this.onDonation(nickname, money, message);
            }
        });

        chat.listen();
    }

    private void onDonation(String nickname, int amount, String message) {

        System.out.println("[Youtube Donation] " + nickname + "(" + amount + "원: " +  "): " + message);

        if (!configManager.getDonation()) return;

        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//donation//youtube//" + nickname + "//" + amount + "//" + message);
    }

    private void onChat(String nickname, String message) {

        System.out.println("[Youtube Chat] " + nickname + ": " + message);

        if (!configManager.getChat()) return;

        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//chat//youtube//" + nickname + "//" + message);
    }

}
