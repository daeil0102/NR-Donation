package net.teujaem.nrDonation.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LogoutEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String platform;
    private final Player player;

    public LogoutEvent(String platform, Player player) {
        this.platform = platform;
        this.player = player;
    }

    public String getPlatform() {
        return this.platform;
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
