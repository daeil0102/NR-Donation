package net.teujaem.nrDonation.handler;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.teujaem.nrDonation.util.TextColorFormatter;

import java.net.URI;

/**
 * NeoForge 1.21.1 용 MessageHandler
 * - net.minecraft.network.chat.Component API 사용
 * - LocalPlayer (NeoForge 1.21.1 클라이언트 플레이어)
 */
public class MessageHandler {

    private static final String PREFIX = "&b[ NR-STUDIO ] &f";

    private final LocalPlayer player;

    public MessageHandler(LocalPlayer player) {
        this.player = player;
    }

    // =====================================================================
    // 메시지 전송 메서드
    // =====================================================================

    public void sendMessage(String message) {
        if (player != null) {
            player.sendSystemMessage(TextColorFormatter.toColoredComponent(PREFIX + message));
        }
    }

    public void loginSuccess() {
        sendMessage("&a로그인 완료");
    }

    public void alreadyLogin() {
        sendMessage("&c이미 로그인 되어있습니다");
    }

    public void loginTrying() {
        sendMessage("&c이미 로그인을 시도하고 있습니다");
    }

    public void emptyAPI() {
        sendMessage("&cAPI Key가 전달되지 않았습니다");
    }

    public void emptyNodeJSUrl() {
        sendMessage("&cAPI Server가 전달되지 않았습니다");
    }

    public void logoutSuccess() {
        sendMessage("&a성공적으로 로그아웃 하였습니다.");
    }

    public void alreadyLogout() {
        sendMessage("&c이미 로그인되어 있지 않습니다.");
    }

    /**
     * 클릭 가능한 로그인 URL 메시지 전송
     */
    public void loginAttempt(URI url) {
        if (player == null) return;

        Component message = Component.literal(
                TextColorFormatter.toColoredString(PREFIX + "&6로그인을 시도합니다. 열리지 않는다면 메시지를 클릭하세요.")
        ).setStyle(
            Style.EMPTY
                .withUnderlined(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString()))
        );

        player.sendSystemMessage(message);
    }

    public void emptyUrl() {
        sendMessage("&cUrl 또는 유튜브 API Key가 Config에 없습니다.");
    }

}
