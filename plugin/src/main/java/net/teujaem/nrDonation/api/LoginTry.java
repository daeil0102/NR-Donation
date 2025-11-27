package net.teujaem.nrDonation.api;

import net.teujaem.nrDonation.Main;
import net.teujaem.nrDonation.websoket.MCWebSocketServerApplication;
import org.bukkit.entity.Player;

public class LoginTry {

    private static final MCWebSocketServerApplication application = Main.getInstance().getWebSocketServerApplication();

    public static void send(Player player, PlatformType platformType) {
        application.sendUser(player, "loginTry//" + platformType.toString().toLowerCase());
    }

}
