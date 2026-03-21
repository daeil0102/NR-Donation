package net.teujaem.nrDonation.common.manager;

import net.teujaem.nrDonation.common.config.ConfigManager;
import net.teujaem.nrDonation.common.data.APIKey;
import net.teujaem.nrDonation.common.data.AccessToken;
import net.teujaem.nrDonation.common.data.LoginPlatform;
import net.teujaem.nrDonation.common.data.SocketManager;
import net.teujaem.nrDonation.common.data.chzzk.StateData;
import net.teujaem.nrDonation.common.data.soop.NodeJSUrl;
import net.teujaem.nrDonation.common.data.soop.doantion.DonationList;
import net.teujaem.nrDonation.common.lisener.ChzzkLisener;
import net.teujaem.nrDonation.common.lisener.SoopLisener;
import net.teujaem.nrDonation.common.lisener.ToonationLisener;
import net.teujaem.nrDonation.common.lisener.YoutubeLisener;
import net.teujaem.nrDonation.common.websocket.MCWebSocketClient;

import java.io.IOException;

public class DataClassManager {

    private static String playerName;

    private static DataClassManager instance;

    private static ConfigManager configManager;
    private static StateData stateData;
    private static LoginPlatform loginPlatform;
    private static AccessToken accessToken;
    private static SocketManager socketManager;
    private static ChzzkLisener chzzkClient;
    private static YoutubeLisener youtubeClient;
    private static ToonationLisener toonationClient;
    private static SoopLisener soopClient;
    private static MCWebSocketClient mcWebSocketClient;
    private static APIKey apiKey;
    private static NodeJSUrl nodeJSUrl;
    private static DonationList donationList;

    public DataClassManager(String playerName) {

        DataClassManager.playerName = playerName;

        instance = this;

        configManager = new ConfigManager();
        stateData = new StateData();
        loginPlatform = new LoginPlatform();
        accessToken = new AccessToken();
        socketManager = new SocketManager();
        apiKey = new APIKey();
        nodeJSUrl = new NodeJSUrl();
        donationList = new DonationList();

    }

    public static DataClassManager getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LoginPlatform getLoginPlatform() {
        return loginPlatform;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public SocketManager getSocketManager() {
        return socketManager;
    }

    public MCWebSocketClient getMcWebSocketClient() {
        return mcWebSocketClient;
    }

    public APIKey getApiKey() {
        return apiKey;
    }

    public ChzzkLisener getChzzkClient() {
        return chzzkClient;
    }

    public SoopLisener getSoopClient() {
        return soopClient;
    }

    public NodeJSUrl getNodeJSUrl() {
        return nodeJSUrl;
    }

    public StateData getStateData() {
        return stateData;
    }

    public DonationList getDonationList() {
        return donationList;
    }

    public void crateChzzkClient(String url) {
        chzzkClient = new ChzzkLisener();
        chzzkClient.run(url);
    }

    public void crateSoopClient() throws IOException, InterruptedException {
        soopClient = new SoopLisener();
        soopClient.run();
    }

    public void crateYoutubeClient(String url) throws Exception {
        url = url.replace("https://www.youtube.com/watch?v=", "");
        youtubeClient = new YoutubeLisener(url);
    }

    public void crateToonationClient(String url) {
        url = url.replace("https://toon.at/widget/alertbox/", "");
        toonationClient = new ToonationLisener(url);
    }


    public void crateMcWebSocketClient() {
        try {
            mcWebSocketClient = new MCWebSocketClient(playerName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
