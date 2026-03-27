package net.teujaem.nrDonation.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DonationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String platform;
    private final String sender;
    private final int amount;
    private final String message;
    private final Player player;

    public DonationEvent(String platform, String sender, int amount, String message, Player player) {
        this.platform = platform;
        this.sender = sender;
        this.amount = amount;
        this.message = message;
        this.player = player;
    }

    public String getPlatform() {
        return this.platform;
    }

    public String getSender() {
        return this.sender;
    }

    public int getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }

    public Player getPlayer() {
        return this.player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
