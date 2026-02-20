package net.teujaem.nrDonation.command;

import net.teujaem.nrDonation.Main;
import net.teujaem.nrDonation.util.TextColorFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player player = (Player) sender;
        if (player == null) return false;

        if (!player.hasPermission("nrdonation.reload")) {
            player.sendMessage(TextColorFormatter.toColored("&c[!]&f 당신은 권한이 없으므로 해당 명령어를 실행하실 수 없습니다."));
            return false;
        }

        Main.getInstance().reload();
        sender.sendMessage(TextColorFormatter.toColored("&a[!]&f 리로드 완료"));
        return true;

    }
}
