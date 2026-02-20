package net.teujaem.nrDonation.handler;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.teujaem.nrDonation.util.TextColorFormatter;

import java.net.URI;

public class MessageHandler {

    private final EntityPlayerSP player;
    private static final String PREFIX = "&8[&bNR-Donation&8] &r";

    public MessageHandler(EntityPlayerSP player) {
        this.player = player;
    }

    public void sendMessage(String message) {
        if (player != null) {
            player.sendMessage(TextColorFormatter.toColoredText(PREFIX + message));
        }
    }

    public void loginSuccess() {
        sendMessage("&a로그인에 성공했습니다!");
    }

    public void loginAttempt(URI url) {
        ITextComponent message = new TextComponentString(TextColorFormatter.toColoredString(PREFIX + "&e로그인 페이지를 열고 있습니다... "));
        ITextComponent clickableUrl = new TextComponentString(TextColorFormatter.toColoredString("&b[클릭하여 열기]"));
        clickableUrl.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString()));
        clickableUrl.getStyle().setColor(TextFormatting.AQUA);
        message.appendSibling(clickableUrl);

        if (player != null) {
            player.sendMessage(message);
        }
    }

    public void alreadyLogin() {
        sendMessage("&c이미 로그인되어 있습니다.");
    }

    public void loginTrying() {
        sendMessage("&c로그인 시도 중입니다. 잠시만 기다려주세요.");
    }

    public void emptyAPI() {
        sendMessage("&cAPI 키가 설정되지 않았습니다.");
    }

    public void emptyNodeJSUrl() {
        sendMessage("&cNodeJS 서버 URL이 설정되지 않았습니다.");
    }

    public void logoutSuccess() {
        sendMessage("&a로그아웃에 성공했습니다!");
    }

    public void alreadyLogout() {
        sendMessage("&c이미 로그아웃되어 있습니다.");
    }

    public void emptyUrl() {
        sendMessage("&cUrl 또는 유튜브 API Key가 Config에 없습니다.");
    }
}
