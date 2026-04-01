package net.teujaem.nrDonation.common.config;

public class ConfigManager {

    private String ip;
    private int port;
    private boolean donation;
    private boolean chat;
    private String youtubeUrl;
    private String toonationUrl;
    private String weflabUrl;


    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean getDonation() {
        return donation;
    }

    public boolean getChat() {
        return chat;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public String getToonationUrl() {
        return toonationUrl;
    }

    public String getWeflabUrl() {
        return weflabUrl;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDonation(boolean donation) {
        this.donation = donation;
    }

    public void setChat(boolean chat) {
        this.chat = chat;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void setToonationUrl(String toonationUrl) {
        this.toonationUrl = toonationUrl;
    }

    public void setWeflabUrl(String weflabUrl) {
        this.weflabUrl = weflabUrl;
    }
}
