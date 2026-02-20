package net.teujaem.nrDonation.common.lisener;

import com.fasterxml.jackson.databind.JsonNode;
import kr.astar.uliv.YouTubeBuilder;
import kr.astar.uliv.data.Chat;
import kr.astar.uliv.data.SuperChat;
import kr.astar.uliv.listener.YouTubeEventListener;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

public class YoutubeClient {

    private final ConfigManager configManager = MainAPI.getInstance().getDataClassManager().getConfigManager();

    private static String url;
    private static String apiKey;

    public YoutubeClient(String url, String apiKey) {
        YoutubeClient.url = url;
        YoutubeClient.apiKey = apiKey;
        run();
    }

    private void run() {

        new YouTubeBuilder()
            .setApiKey(apiKey)
            .setVideoId(url)
            .addListener(new YouTubeEventListener() {
                @Override
                public void onChat(Chat chat) {
                    System.out.println(chat.author()+": "+chat.getMessage());
                    YoutubeClient.this.onChat(chat.author().name(), chat.getMessage());
                }
                @Override
                public void onSuperChat(SuperChat chat) {
                    System.out.println(chat.author()+": "+chat.getAmount());
                    YoutubeClient.this.onDonation(chat.author().name(), Integer.parseInt(chat.getAmount()), chat.getMessage());
                }
            }).build();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void onDonation(String nickname, int amount, String message) {

        System.out.println("[Youtube Donation] " + nickname + "(" + amount + "치즈: " +  "): " + message);

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
