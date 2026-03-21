package net.teujaem.nrDonation.client.config;

import net.minecraft.client.Minecraft;
import net.teujaem.nrDonation.NrDonation;
import net.teujaem.nrDonation.client.NrDonationClient;
import org.apache.logging.log4j.Level;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ConfigManager {
    private Map<String, Object> configValues;
    private String ip;
    private int port;
    private boolean sendDonation;
    private boolean sendChat;
    private String youtubeUrl;
    private String toonationUrl;
    private String weflabUrl;
    private static final String FILE_NAME = "NRDonationConfig.yml";

    public ConfigManager() {
        this.load();
    }

    private void load() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        File configFile = new File(configDir, FILE_NAME);

        if (!configFile.exists()) {
            try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
                if (in == null) {
                    throw new RuntimeException("리소스에 NRDonationConfig.yml 가 없습니다!");
                }

                if (!configDir.exists()) {
                    configDir.mkdirs();
                }
                Files.copy(in, configFile.toPath());
                System.out.println("[NRDonation] 기본 설정 파일 생성 완료");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        Yaml yaml = new Yaml();

        try (InputStream input = Files.newInputStream(configFile.toPath())) {
            this.configValues = yaml.load(input);
            if (this.configValues == null) {
                throw new RuntimeException("NRDonationConfig.yml 내용이 비어 있습니다!");
            }

            Map<String, Object> serverConfig = (Map<String, Object>) this.configValues.get("Server");
            if (serverConfig != null) {
                this.ip = serverConfig.getOrDefault("ip", "0.0.0.0").toString();
                Object portObj = serverConfig.getOrDefault("port", 8888);
                if (portObj instanceof Number) {
                    this.port = ((Number) portObj).intValue();
                } else {
                    try {
                        this.port = Integer.parseInt(portObj.toString());
                    } catch (NumberFormatException var9) {
                        this.port = 8888;
                        System.err.println("[NRDonation] 잘못된 포트 값, 기본값(8888)으로 설정합니다.");
                    }
                }
            } else {
                this.ip = "0.0.0.0";
                this.port = 8888;
            }

            Map<String, Object> sendEventConfig = (Map<String, Object>) this.configValues.get("SendEvent");
            if (sendEventConfig != null) {
                this.sendDonation = Boolean.parseBoolean(sendEventConfig.getOrDefault("donation", true).toString());
                this.sendChat = Boolean.parseBoolean(sendEventConfig.getOrDefault("chat", true).toString());
            } else {
                this.sendDonation = true;
                this.sendChat = true;
            }

            Map<String, Object> urlConfig = (Map)this.configValues.get("Url");
            if (urlConfig != null) {
                this.youtubeUrl = urlConfig.getOrDefault("youtube", "").toString();
                this.toonationUrl = urlConfig.getOrDefault("toonation", "").toString();
                this.weflabUrl = urlConfig.getOrDefault("weflab", "").toString();
            } else {
                this.youtubeUrl = "";
                this.toonationUrl = "";
                this.weflabUrl = "";
            }

        } catch (IOException e) {
            NrDonation.getLogger().log(Level.WARN, "Error while reading NRDonationConfig.yml!");
        }

        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setIp(this.ip);
        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setPort(this.port);
        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setDonation(this.sendDonation);
        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setChat(this.sendChat);
        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setYoutubeUrl(this.youtubeUrl);
        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setToonationUrl(this.toonationUrl);
        NrDonationClient.getInstance().getMainAPI().getDataClassManager().getConfigManager().setWeflabUrl(this.weflabUrl);

    }
}
