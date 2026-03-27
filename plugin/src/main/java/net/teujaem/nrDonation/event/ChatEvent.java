package net.teujaem.nrDonation.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String platform;
    private final String sender;
    private final String message;
    private final Player player;

    public ChatEvent(String platform, String sender, String message, Player player) {
        this.platform = platform;
        this.sender = sender;
        this.message = message;
        this.player = player;
    }

    public String getPlatform() {
        return this.platform;
    }

    public String getSender() {
        return this.sender;
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
