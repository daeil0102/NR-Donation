package net.teujaem.nrDonation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
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
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class NrDonationClient {

    private static NrDonationClient instance;

    private static DataClassManager dataClassManager;
    private static MessageHandler messageHandler;
    private static MainAPI mainAPI;
    private static EventManager eventManager;
    private List<Runnable> tasks = new ArrayList<>();

    public NrDonationClient() {
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
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            ArrayList<Runnable> tasks = new ArrayList<>(this.tasks);
            this.tasks.clear();
            for (Runnable task : tasks) {
                try {
                    task.run();
                } catch (Exception ex) {
                    NrDonation.getLogger().log(Level.WARN, "Error while executing task", ex);
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        scheduleInitializeOnJoin();
    }

    private void scheduleInitializeOnJoin() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player != null) {
            initializeOnJoin(player);
        } else {
            tasks.add(this::scheduleInitializeOnJoin);
        }
    }

    private void initializeOnJoin(EntityPlayerSP player) {
        mainAPI = new MainAPI(player.getName());

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
        messageHandler = new MessageHandler(player);

        // 마크 서버와 연동할 ws 로딩
        mainAPI.getDataClassManager().crateMcWebSocketClient();
    }

    /*

    이 아래 부터는 private 메서드 입니다

    */

    // 클라이언트 커맨드 추가
    private void loadCommand() {
        // soop login command
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getName() {
                return "숲";
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return "/숲 <로그인|로그아웃>";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                if (args.length == 0) return;

                if (args[0].equals("로그인")) {
                    login(PlatformType.SOOP);
                } else if (args[0].equals("로그아웃")) {
                    logout(PlatformType.SOOP);
                }
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }
        });

        // chzzk login command
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getName() {
                return "치지직";
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return "/치지직 <로그인|로그아웃>";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                if (args.length == 0) return;

                if (args[0].equals("로그인")) {
                    login(PlatformType.CHZZK);
                } else if (args[0].equals("로그아웃")) {
                    logout(PlatformType.CHZZK);
                }
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }
        });

        // youtube and toonation login command
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getName() {
                return "연결";
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return "/연결 <유튜브|투네이션|위플랩>";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                if (args.length == 0) return;

                if (args[0].equals("유튜브")) {
                    login(PlatformType.YOUTUBE);
                } else if (args[0].equals("투네이션")) {
                    login(PlatformType.TOONATION);
                }else if (args[0].equals("위플랩")) {
                    login(PlatformType.WEFLAB);
                }
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }
        });
    }

    // 로그인 페이지 열기
    private void openLoginPage(URI url) {
        messageHandler.loginAttempt(url);
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(url);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
