package net.teujaem.nrDonation.common.data.soop.doantion;

import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DonationList {

    private static class Donation {
        public final String sender;
        public int count;

        public Donation(String sender, int count) {
            this.sender = sender;
            this.count = count;
        }
    }

    private final List<Donation> donations = new CopyOnWriteArrayList<>();

    public DonationList() {

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (donations.isEmpty()) return;

            // List에서 첫 번째 요소 가져오기
            Donation first = donations.get(0);

            String sender = first.sender;
            int count = first.count;

            MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
            mcWebSocketSendMessage.to("event//donation//soop//" + sender + "//" + count + "//null");

            // 첫 번째 요소 제거
            donations.remove(0);

        }, 100, 100, TimeUnit.MILLISECONDS);
    }


    public void addDonation(String sender, int count) {
        donations.add(new Donation(sender, count));
    }

    public boolean hasSender(String sender) {
        for (Donation donation : donations) {
            if (donation.sender.equalsIgnoreCase(sender)) {
                return true;
            }
        }
        return false;
    }

    // sender의 첫번재 값 제거
    public void removeFirstOf(String sender) {
        for (Donation donation : donations) {
            if (donation.sender.equalsIgnoreCase(sender)) {
                donations.remove(donation);
                break;
            }
        }
    }

    // sender의 첫번째 값 확인
    public int getFirstCountOf(String sender) {
        for (Donation donation : donations) {
            if (donation.sender.equalsIgnoreCase(sender)) {
                return donation.count;
            }
        }
        return 0;
    }

}
