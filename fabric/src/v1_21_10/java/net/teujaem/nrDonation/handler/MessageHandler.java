package net.teujaem.nrDonation.handler;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.teujaem.nrDonation.util.TextColorFormatter;

import java.net.URI;

public class MessageHandler {

    private static final String messagePrefix = "&b[ NR-STUDIO ] &f";

    private static ClientPlayerEntity player;

    public MessageHandler(ClientPlayerEntity player) {
        MessageHandler.player = player;
    }

    public void alreadyLogout() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&c이미 로그인되어 있지 않습니다."), false);
    }

    public void alreadyLogin() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&c이미 로그인 되어있습니다"), false);
    }

    public void loginSuccess() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&a로그인 완료"), false);
    }

    public void logoutSuccess() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&a성공적으로 로그아웃 하였습니다."), false);
    }

    public void loginTrying() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&c이미 로그인을 시도하고 있습니다"), false);
    }

    public void emptyAPI() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&cAPI Key가 전달되지 않았습니다"), false);
    }

    public void emptyNodeJSUrl() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&cAPI Server가 전달되지 않았습니다"), false);
    }

    public void loginAttempt(URI url) {
        Text message =
                Text.literal(TextColorFormatter.toColoredString(
                                messagePrefix + "&6로그인을 시도 합니다. 열리지 않는 다면 메세지를 클릭하세요."))
                        .setStyle(
                                Style.EMPTY
                                        .withUnderline(true)
                                        .withClickEvent(new ClickEvent.OpenUrl(url))
                        );
        player.sendMessage(message, false);
    }

    public void emptyUrl() {
        player.sendMessage(TextColorFormatter.toColoredText(messagePrefix + "&cUrl 또는 유튜브 API Key가 Config에 없습니다."), false);
    }

}
