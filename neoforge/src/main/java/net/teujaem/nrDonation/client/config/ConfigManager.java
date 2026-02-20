package net.teujaem.nrDonation.client.config;

import net.neoforged.fml.loading.FMLPaths;
import net.teujaem.nrDonation.NrDonation;
import net.teujaem.nrDonation.client.NrDonationClient;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

/**
 * NeoForge 전용 ConfigManager
 * - FMLPaths.GAMEDIR 를 통해 .minecraft/config 디렉터리에 설정 파일 생성
 * - SnakeYAML 을 사용하여 NRDonationConfig.yml 파싱
 */
@SuppressWarnings("unchecked")
public class ConfigManager {

    private static final String FILE_NAME = "NRDonationConfig.yml";

    private String ip = "0.0.0.0";
    private int port = 8888;
    private boolean sendDonation = true;
    private boolean sendChat = true;
    private String youtubeUrl;
    private String toonationUrl;
    private String weflabUrl;
    private String youtubeAPI;

    public ConfigManager() {
        load();
    }

    private void load() {
        // NeoForge: .minecraft/config/NRDonationConfig.yml
        File configDir = FMLPaths.GAMEDIR.get().resolve("config").toFile();
        File configFile = new File(configDir, FILE_NAME);

        if (!configFile.exists()) {
            try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
                if (in == null) {
                    throw new RuntimeException("[NR-Donation] 리소스에 " + FILE_NAME + " 가 없습니다!");
                }
                if (!configDir.exists()) {
                    configDir.mkdirs();
                }
                Files.copy(in, configFile.toPath());
                NrDonation.getLogger().info("[NR-Donation] 기본 설정 파일 생성 완료: {}", configFile.getAbsolutePath());
            } catch (IOException e) {
                NrDonation.getLogger().error("[NR-Donation] 설정 파일 생성 실패", e);
                return;
            }
        }

        Yaml yaml = new Yaml();

        try (InputStream input = Files.newInputStream(configFile.toPath())) {
            Map<String, Object> configValues = yaml.load(input);
            if (configValues == null) {
                NrDonation.getLogger().error("[NR-Donation] NRDonationConfig.yml 내용이 비어 있습니다!");
                return;
            }

            // Server 설정 파싱
            Map<String, Object> serverConfig = (Map<String, Object>) configValues.get("Server");
            if (serverConfig != null) {
                this.ip = serverConfig.getOrDefault("ip", "0.0.0.0").toString();
                Object portObj = serverConfig.getOrDefault("port", 8888);
                if (portObj instanceof Number) {
                    this.port = ((Number) portObj).intValue();
                } else {
                    try {
                        this.port = Integer.parseInt(portObj.toString());
                    } catch (NumberFormatException e) {
                        this.port = 8888;
                        NrDonation.getLogger().warn("[NR-Donation] 잘못된 포트 값, 기본값(8888)으로 설정합니다.");
                    }
                }
            }

            // SendEvent 설정 파싱
            Map<String, Object> sendEventConfig = (Map<String, Object>) configValues.get("SendEvent");
            if (sendEventConfig != null) {
                this.sendDonation = Boolean.parseBoolean(sendEventConfig.getOrDefault("donation", true).toString());
                this.sendChat = Boolean.parseBoolean(sendEventConfig.getOrDefault("chat", true).toString());
            }

            Map<String, Object> urlConfig = (Map<String, Object>) configValues.get("Url");
            if (urlConfig != null) {
                this.youtubeUrl = urlConfig.getOrDefault("youtube", "").toString();
                this.toonationUrl = urlConfig.getOrDefault("toonation", "").toString();
                this.weflabUrl = urlConfig.getOrDefault("weflab", "").toString();
            } else {
                this.youtubeUrl = "";
                this.toonationUrl = "";
                this.weflabUrl = "";
            }

            Map<String, Object> apiConfig = (Map<String, Object>) configValues.get("API");
            if (apiConfig != null) {
                this.youtubeAPI = apiConfig.getOrDefault("youtube", "").toString();
            } else {
                this.youtubeAPI = "";
            }

        } catch (IOException e) {
            NrDonation.getLogger().error("[NR-Donation] 설정 파일 로딩 실패", e);
            return;
        }

        // Common 모듈의 ConfigManager에 값 전달
        NrDonationClient client = NrDonationClient.getInstance();
        if (client == null || client.getMainAPI() == null) return;

        net.teujaem.nrDonation.common.config.ConfigManager commonConfig =
            client.getMainAPI().getDataClassManager().getConfigManager();

        commonConfig.setIp(this.ip);
        commonConfig.setPort(this.port);
        commonConfig.setDonation(this.sendDonation);
        commonConfig.setChat(this.sendChat);
        commonConfig.setYoutubeUrl(this.youtubeUrl);
        commonConfig.setToonationUrl(this.toonationUrl);
        commonConfig.setWeflabUrl(this.weflabUrl);
        commonConfig.setYoutubeAPI(this.youtubeAPI);

        NrDonation.getLogger().info("[NR-Donation] 설정 로딩 완료 - IP: {}, Port: {}", this.ip, this.port);
    }
}
