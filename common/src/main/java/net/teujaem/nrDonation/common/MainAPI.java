package net.teujaem.nrDonation.common;

import net.teujaem.nrDonation.common.data.PlatformType;
import net.teujaem.nrDonation.common.event.EventManager;
import net.teujaem.nrDonation.common.manager.DataClassManager;

import java.io.IOException;

public class MainAPI {

    private static MainAPI instance;

    private static DataClassManager dataClassManager;
    private static EventManager eventManager;

    private static String playerName;

    public MainAPI(String playerName) {
        MainAPI.playerName = playerName;
        instance = this;
        dataClassManager = new DataClassManager(playerName);
        eventManager = new EventManager();
    }

    public static MainAPI getInstance() {
        return instance;
    }

    public DataClassManager getDataClassManager() {
        return dataClassManager;
    }

    public void runChzzkClient(String url) {
        if (dataClassManager.getChzzkClient() != null) return;
        dataClassManager.crateChzzkClient(url);
    }

    public void runSoopClient() throws IOException, InterruptedException {
        dataClassManager.crateSoopClient();
    }

    public void runYoutubeClient() {
        dataClassManager.crateYoutubeClient(dataClassManager.getConfigManager().getYoutubeUrl());;
    }

    public void runToonationClient() {
        dataClassManager.crateToonationClient(dataClassManager.getConfigManager().getToonationUrl());
    }

    public void resetLoginPlatform() {
        dataClassManager.getLoginPlatform().setPlatformType(null);
    }

    public void eventLogin(PlatformType platformType) {
        eventManager.callEvent("login//" + platformType.toString().toLowerCase());
    }

    public void eventLoginTry(PlatformType platformType) {
        eventManager.callEvent("loginTry//" + platformType.toString().toLowerCase());
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public String getPlayerName() {
        return playerName;
    }
}
