package net.teujaem.nrDonation.common.lisener;

import kr.astar.tooliv.ToonationBuilder;
import kr.astar.tooliv.data.Donation;
import kr.astar.tooliv.listener.ToonationEventListener;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

public class ToonationLisener {
    
    private final ConfigManager configManager = MainAPI.getInstance().getDataClassManager().getConfigManager();

    private static String url;

    public ToonationLisener(String url) {
        ToonationLisener.url = url;
        run();
    }

    private void run() {
        new ToonationBuilder()
            .setKey(url)
            .addListener(new ToonationEventListener() {
                @Override
                public void onDonation(Donation donation) {
                    ToonationLisener.this.onDonation(donation.getNickName(), (int) donation.getAmount(), donation.getComment());
                }

                @Override
                public void onFail() {
                    System.out.println("error");
                }
            }).build();
    }

    private void onDonation(String nickname, int amount, String message) {
        nickname = nickname.replace("\"", "");
        message = message.replace("\"", "");
        System.out.println("[Soop Donation] " + nickname + ": " + amount + "원" + ": " + message);

        if (!configManager.getDonation()) return;

        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//donation//toonation//" + nickname + "//" + amount + "//" + message);
    }

}
