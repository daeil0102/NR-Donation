package net.teujaem.nrDonation.common.data.soop.doantion;

import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DonationList {

    private static class Donation {
        private final String sender;
        private final int count;

        public Donation(String sender, int count) {
            this.sender = sender;
            this.count = count;
        }
    }

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    // 현재 대기중인 donation 목록
    private final List<Donation> pendingDonations =
            new CopyOnWriteArrayList<>();

    public void addDonation(String sender, int count) {

        Donation donation = new Donation(sender, count);
        pendingDonations.add(donation);

        // 300ms
        scheduler.schedule(() -> {

            // 이미 제거됐으면 무시
            if (!pendingDonations.remove(donation)) {
                return;
            }

            MCWebSocketSendMessage mcWebSocketSendMessage =
                    new MCWebSocketSendMessage();

            mcWebSocketSendMessage.to(
                    "event//donation//soop//"
                            + donation.sender + "//"
                            + donation.count + "//null"
            );

        }, 300, TimeUnit.MILLISECONDS);
    }

    public boolean hasSender(String sender) {
        for (Donation donation : pendingDonations) {
            if (donation.sender.equalsIgnoreCase(sender)) {
                return true;
            }
        }
        return false;
    }

    public void removeFirstOf(String sender) {
        for (Donation donation : pendingDonations) {
            if (donation.sender.equalsIgnoreCase(sender)) {
                pendingDonations.remove(donation);
                break;
            }
        }
    }

    public int getFirstCountOf(String sender) {
        for (Donation donation : pendingDonations) {
            if (donation.sender.equalsIgnoreCase(sender)) {
                return donation.count;
            }
        }
        return 0;
    }

}