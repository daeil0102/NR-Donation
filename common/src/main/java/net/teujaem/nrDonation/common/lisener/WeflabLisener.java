package net.teujaem.nrDonation.common.lisener;

import kr.astar.wfliv.WeflabBuilder;
import kr.astar.wfliv.data.alert.Donation;
import kr.astar.wfliv.listener.WeflabListener;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

public class WeflabLisener {

    private final ConfigManager configManager = MainAPI.getInstance().getDataClassManager().getConfigManager();

    private static String url;

    public WeflabLisener(String url) {
        WeflabLisener.url = url;
        run();
    }

    private void run() {
        new WeflabBuilder(url)
                .addListener(new WeflabListener() {
                    @Override
                    public void onDonation(Donation donation) {
                        WeflabLisener.this.onDonation(donation.user().nickname(), (int) donation.amount(), donation.content(), donation.platformData().platform());
                        System.out.println(donation.donationData());
                        System.out.println(donation.platformData());
                    }

                    @Override
                    public void onDisconnect(int code, String reason) {
                        System.out.println("closed");
                    }
                })
                .build();
    }

    private void onDonation(String nickname, int amount, String message, String platform) {
        nickname = nickname.replace("\"", "");
        message = message.replace("\"", "");
        System.out.println("[Soop Donation] " + nickname + ": " + amount + "원" + ": " + message);

        if (!configManager.getDonation()) return;

        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//donation//weflab_" + platform + "//" + nickname + "//" + amount + "//" + message);
    }

}
