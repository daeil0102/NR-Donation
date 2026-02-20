package net.teujaem.nrDonation.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.teujaem.nrDonation.NrDonation;
import net.teujaem.nrDonation.client.config.ConfigManager;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.data.PlatformType;
import net.teujaem.nrDonation.common.event.EventManager;
import net.teujaem.nrDonation.common.handler.donation.chzzk.ChzzkCreateCode;
import net.teujaem.nrDonation.common.handler.donation.soop.SoopCreateCode;
import net.teujaem.nrDonation.common.manager.DataClassManager;
import net.teujaem.nrDonation.common.server.CallbackServer;
import net.teujaem.nrDonation.common.websocket.sender.MCWebSocketSendMessage;
import net.teujaem.nrDonation.handler.MessageHandler;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import static net.minecraft.commands.Commands.literal;

/**
 * NeoForge 클라이언트 메인 클래스
 *
 * ★ NeoForge 이벤트 버스 (NeoForge.EVENT_BUS) 소속 이벤트 목록:
 *   - RegisterClientCommandsEvent  → NeoForge.EVENT_BUS ✅
 *   - ClientPlayerNetworkEvent.*   → NeoForge.EVENT_BUS ✅
 *
 * ★ Mod 이벤트 버스 (modEventBus) 소속 이벤트:
 *   - FMLClientSetupEvent          → NrDonation.java 에서 처리
 *
 * ★ 등록 방식: NeoForge.EVENT_BUS.register(this) → 인스턴스 등록
 *   → 모든 @SubscribeEvent 메서드를 인스턴스 메서드로 작성 가능 (static 불필요)
 */
public class NrDonationClient {

    private static NrDonationClient instance;

    private DataClassManager dataClassManager;
    private MessageHandler   messageHandler;
    private MainAPI          mainAPI;
    private EventManager     eventManager;

    private boolean isJoined = false;

    private boolean isYoutubeLogin = false;
    private boolean isToonationLogin = false;
    private boolean isWeflaboLogin = false;

    public NrDonationClient() {
        instance = this;

        // ★ 인스턴스 등록 → @SubscribeEvent 인스턴스 메서드 정상 작동
        NeoForge.EVENT_BUS.register(this);
    }

    public static NrDonationClient getInstance() {
        return instance;
    }

    public MainAPI getMainAPI() {
        return mainAPI;
    }

    // =====================================================================
    // NeoForge.EVENT_BUS 이벤트 핸들러 (인스턴스 메서드)
    // =====================================================================

    /**
     * ★ RegisterClientCommandsEvent → NeoForge.EVENT_BUS ✅ (Mod 버스 ❌)
     * 인스턴스로 등록했으므로 인스턴스 메서드로 작성 가능
     */

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("숲")
                .then(literal("로그인").executes(ctx -> {
                    login(PlatformType.SOOP);
                    return 1;
                }))
                .then(literal("로그아웃").executes(ctx -> {
                    logout(PlatformType.SOOP);
                    return 1;
                }))
        );

        event.getDispatcher().register(
            LiteralArgumentBuilder.<CommandSourceStack>literal("치지직")
                .then(literal("로그인").executes(ctx -> {
                    login(PlatformType.CHZZK);
                    return 1;
                }))
                .then(literal("로그아웃").executes(ctx -> {
                    logout(PlatformType.CHZZK);
                    return 1;
                }))
        );

        event.getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("연결")
                        .then(literal("유튜브").executes(ctx -> {
                            login(PlatformType.YOUTUBE);
                            return 1;
                        }))
                        .then(literal("투네이션").executes(ctx -> {
                            login(PlatformType.TOONATION);
                            return 1;
                        }))
                        .then(literal("위플랩").executes(ctx -> {
                            logout(PlatformType.WEFLAB);
                            return 1;
                        }))
        );
    }

    /**
     * ★ ClientPlayerNetworkEvent.LoggingIn → NeoForge.EVENT_BUS ✅
     */
    @SubscribeEvent
    public void onPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (isJoined) return;
        isJoined = true;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            NrDonation.getLogger().warn("[NR-Donation] player가 null - 초기화를 건너뜁니다.");
            isJoined = false;
            return;
        }
        initializeOnJoin(player);
    }

    /**
     * ★ ClientPlayerNetworkEvent.LoggingOut → NeoForge.EVENT_BUS ✅
     */
    @SubscribeEvent
    public void onPlayerLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        isJoined = false;
    }

    // =====================================================================
    // 초기화
    // =====================================================================

    private void initializeOnJoin(LocalPlayer player) {
        try {
            mainAPI = new MainAPI(player.getName().getString());

            eventManager = mainAPI.getEventManager();
            eventManager.addListener(message -> {
                String[] parts = message.split("//");
                if (parts.length < 2) return;

                String event    = parts[0];
                String platform = parts[1];

                if (event.equals("login")) {
                    if (messageHandler != null) messageHandler.loginSuccess();
                    new MCWebSocketSendMessage().to("event//login//" + platform);
                }
                if (event.equals("loginTry")) {
                    if (platform.equals("soop"))  login(PlatformType.SOOP);
                    if (platform.equals("chzzk")) login(PlatformType.CHZZK);
                }
            });

            new ConfigManager();
            dataClassManager = mainAPI.getDataClassManager();
            new CallbackServer();
            messageHandler = new MessageHandler(player);
            mainAPI.getDataClassManager().crateMcWebSocketClient();

        } catch (Exception e) {
            NrDonation.getLogger().error("[NR-Donation] 초기화 중 오류 발생", e);
        }
    }

    private void openLoginPage(URI url) {
        if (messageHandler != null) messageHandler.loginAttempt(url);
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(url);
            }
        } catch (IOException e) {
            NrDonation.getLogger().error("[NR-Donation] 브라우저 열기 실패", e);
        }
    }

    /*

    이 이래 부터는 forge에서도 사용할 수 있는
    fabric API 사용 안하는 private 메서드 입니다

    */

    //로그인 시도
    private void login(PlatformType platformType) {
        if (isLoginReturnType(platformType)) {
            return;
        }

        if (platformType.equals(PlatformType.SOOP)) {
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
            ChzzkCreateCode tokenCreate = new ChzzkCreateCode(dataClassManager.getApiKey().getId(PlatformType.CHZZK));
            dataClassManager.getLoginPlatform().setPlatformType(PlatformType.CHZZK);
            URI url = tokenCreate.getLoginUrl();
            openLoginPage(url);
        }

        if (platformType.equals(PlatformType.YOUTUBE)) {
            mainAPI.runYoutubeClient();
            isYoutubeLogin = true;
            messageHandler.loginSuccess();
        }

        if (platformType.equals(PlatformType.TOONATION)) {
            mainAPI.runToonationClient();
            isToonationLogin = true;
            messageHandler.loginSuccess();
        }

        if (platformType.equals(PlatformType.WEFLAB)) {
            isWeflaboLogin = true;
            messageHandler.loginSuccess();
        }
    }

    // 유튜브/투네이션 url 존재 여부
    private boolean isEmptyUrl(PlatformType platformType) {
        if (platformType.equals(PlatformType.YOUTUBE)) {
            if (mainAPI.getDataClassManager().getConfigManager().getYoutubeUrl() == null) return true;
            if (mainAPI.getDataClassManager().getConfigManager().getYoutubeUrl().isEmpty()) return true;
            if (mainAPI.getDataClassManager().getConfigManager().getYoutubeAPI() == null) return true;
            if (mainAPI.getDataClassManager().getConfigManager().getYoutubeAPI().isEmpty()) return true;
        }
        if (platformType.equals(PlatformType.TOONATION)) {
            if (mainAPI.getDataClassManager().getConfigManager().getToonationUrl() == null) return true;
            if (mainAPI.getDataClassManager().getConfigManager().getToonationUrl().isEmpty()) return true;
        }
        if (platformType.equals(PlatformType.WEFLAB)) {
            if (mainAPI.getDataClassManager().getConfigManager().getWeflabUrl() == null) return true;
            if (mainAPI.getDataClassManager().getConfigManager().getWeflabUrl().isEmpty()) return true;
        }
        return false;
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

        if (platformType.equals(PlatformType.SOOP)||platformType.equals(PlatformType.CHZZK)) {
            if (isEmptyAPI(platformType)) {
                messageHandler.emptyAPI();
                return true;
            }
        } else {
            if (isEmptyUrl(platformType)) {
                messageHandler.emptyUrl();
            }
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
        if (platformType.equals(PlatformType.YOUTUBE)) {
            return isYoutubeLogin;
        }
        if (platformType.equals(PlatformType.TOONATION)) {
            return isToonationLogin;
        }
        if (platformType.equals(PlatformType.WEFLAB)) {
            return isWeflaboLogin;
        }
        return false;
    }

}
