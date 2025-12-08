package net.teujaem.nrDonation.clinet;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.data.PlatformType;
import net.teujaem.nrDonation.common.event.EventManager;
import net.teujaem.nrDonation.common.handler.donation.chzzk.ChzzkCreateCode;
import net.teujaem.nrDonation.common.handler.donation.soop.SoopCreateCode;
import net.teujaem.nrDonation.common.manager.DataClassManager;
import net.teujaem.nrDonation.common.server.CallbackServer;
import net.teujaem.nrDonation.clinet.config.ConfigManager;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;
import net.teujaem.nrDonation.handler.MessageHandler;

import java.io.IOException;
import java.net.URI;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class NrDonationClient implements ClientModInitializer {

    private static NrDonationClient instance;

    private static DataClassManager dataClassManager;
    private static MessageHandler messageHandler;
    private static MainAPI mainAPI;
    private static EventManager eventManager;

    @Override
    public void onInitializeClient() {
        instance = this;
        load();
    }

    public static NrDonationClient getInstance() {
        return instance;
    }

    public MainAPI getMainAPI() {
        return mainAPI;
    }

    private void load() {

        loadCommand();

        //서버 접속시 메인 시스템 연결 (player null 방지용)
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {

            mainAPI = new MainAPI(MinecraftClient.getInstance().player.getName().getString());

            // 로그인 감지를 위한 interface 추가
            eventManager = mainAPI.getEventManager();

            eventManager.addListener(message -> {
                String[] messages = message.split("//");
                String event = messages[0];
                String platform = messages[1];
                if (event.equals("login")) {
                    messageHandler.loginSuccess();
                    MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
                    mcWebSocketSendMessage.to("event//login//" + platform);
                }
                if (event.equals("loginTry")) {
                    if (platform.equals("soop")) login(PlatformType.SOOP);
                    if (platform.equals("chzzk")) login(PlatformType.CHZZK);
                }
            });

            // 마인크래프트 config 로딩
            new ConfigManager();

            // 메인 시스템에 data 관리 class 로딩
            dataClassManager = mainAPI.getDataClassManager();

            // 숲, 치지직 연동을 위한 callback 서버 실행
            try {
                new CallbackServer();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 메세지를 보내기 위한 handler class 로딩
            messageHandler = new MessageHandler(MinecraftClient.getInstance().player);

            // 마크 서버와 연동할 ws 로딩
            mainAPI.getDataClassManager().crateMcWebSocketClient();

        });

    }

    /*

    이 아래 부터는 private 메서드 입니다

    */

    //클라이언트 커맨드 추가
    private void loadCommand() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            // soop eventLogin command
            dispatcher.register(literal("숲")
                .then(literal("로그인").executes(ctx -> {
                    login(PlatformType.SOOP);
                    return 1;
                }))
                .then(literal("로그아웃").executes(ctx -> {
                    logout(PlatformType.SOOP);
                    return 1;
                }))
            );

            // chzzk eventLogin command
            dispatcher.register(literal("치지직")
                .then(literal("로그인").executes(ctx -> {
                    login(PlatformType.CHZZK);
                    return 1;
                }))
                .then(literal("로그아웃").executes(ctx -> {
                    logout(PlatformType.CHZZK);
                    return 1;
                }))
            );
        });

    }

    /*

    이 이래 부터는 forge에서도 사용할 수 있는
    fabric API 사용 안하는 private 메서드 입니다

    */

    //로그인 시도
    private void login(PlatformType platformType) {
        if (platformType.equals(PlatformType.SOOP)) {
            if (isLoginReturnType(PlatformType.SOOP)) {
                return;
            }

            SoopCreateCode tokenCreate = new SoopCreateCode(dataClassManager.getApiKey().getId(PlatformType.SOOP));
            dataClassManager.getLoginPlatform().setPlatformType(PlatformType.SOOP);
            try {
                URI url = tokenCreate.getLoginUrl();
                openLoginPage(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (platformType.equals(PlatformType.CHZZK)) {
            if (isLoginReturnType(PlatformType.CHZZK)) {
                return;
            }

            ChzzkCreateCode tokenCreate = new ChzzkCreateCode(dataClassManager.getApiKey().getId(PlatformType.CHZZK));
            dataClassManager.getLoginPlatform().setPlatformType(PlatformType.CHZZK);
            URI url = tokenCreate.getLoginUrl();
            openLoginPage(url);
        }
    }

    //로그아웃 시도
    private void logout(PlatformType platformType) {
        if (!isLogin(platformType)) {
            messageHandler.alreadyLogout();
            return;
        }

        dataClassManager.getLoginPlatform().setPlatformType(null);
        dataClassManager.getAccessToken().reset(platformType);

        if (platformType.equals(PlatformType.SOOP)) {
            if (dataClassManager.getSoopClient().getLatch() != null) {
                dataClassManager.getSoopClient().stop();
            }
            return;
        }
        if (platformType.equals(PlatformType.CHZZK)) {
            if (dataClassManager.getChzzkClient().getSocket() != null) {
                dataClassManager.getChzzkClient().stop();
            }
        }

        messageHandler.logoutSuccess();
        MCWebSocketSendMessage mcWebSocketSendMessage = new MCWebSocketSendMessage();
        mcWebSocketSendMessage.to("event//logout//" + platformType.toString().toLowerCase());
    }

    //로그인 시도 실패이유
    private boolean isLoginReturnType(PlatformType platformType) {
        if (isLogin(platformType)) {
            messageHandler.alreadyLogin();
            return true;
        }
        if (isLoginTrying()) {
            messageHandler.loginTrying();
            return true;
        }
        if (isEmptyAPI(platformType)) {
            messageHandler.emptyAPI();
            return true;
        }
        if (platformType.equals(PlatformType.SOOP)) {
            if (isEmptyNodeJSUrl()) {
                messageHandler.emptyNodeJSUrl();
                return true;
            }
        }
        return false;
    }

    //로그인중인지 확인
    private boolean isLoginTrying() {
        return dataClassManager.getLoginPlatform().getPlatformType() != null;
    }

    //api key가 전달 되었는지 확인
    private boolean isEmptyAPI(PlatformType platformType) {
        return (dataClassManager.getApiKey().getId(platformType) == null || dataClassManager.getApiKey().getSecret(platformType) == null);
    }

    //nodejs 서버가 전달 되었는지 확인
    private boolean isEmptyNodeJSUrl() {
        return dataClassManager.getNodeJSUrl().getURL() == null;
    }

    //로그인 상태인지 감지
    private boolean isLogin(PlatformType platformType) {
        if (platformType.equals(PlatformType.SOOP)) {
            return dataClassManager.getAccessToken().getSoop() != null;
        }
        if (platformType.equals(PlatformType.CHZZK)) {
            return dataClassManager.getAccessToken().getChzzk() != null;
        }
        return false;
    }

    //로그인 페이지 열기
    private void openLoginPage(URI url) {
        messageHandler.loginAttempt(url);
        Util.getOperatingSystem().open(url);
    }

}
