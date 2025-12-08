package net.teujaem.nrDonation.common.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.data.AccessToken;
import net.teujaem.nrDonation.common.data.LoginPlatform;
import net.teujaem.nrDonation.common.data.PlatformType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class CallbackServer {

    private static final LoginPlatform loginPlatform = MainAPI.getInstance().getDataClassManager().getLoginPlatform();
    private static final AccessToken accessToken = MainAPI.getInstance().getDataClassManager().getAccessToken();

    public CallbackServer() throws Exception {
        run();
    }

    private void run() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/callback", exchange -> {

            URI requestUri = exchange.getRequestURI();
            String query = requestUri.getQuery();

            if (query != null) {

                if (loginPlatform.getPlatformType() != null) {
                    PlatformType platformType = loginPlatform.getPlatformType();
                    MainAPI.getInstance().resetLoginPlatform();

                    openHTML(exchange);

                    String[] code = query.split("&");
                    code[0] = code[0].replace("code=", "");

                    accessToken.setAccessToken(platformType, code[0]);

                    MainAPI.getInstance().eventLogin(platformType);

                    if (PlatformType.CHZZK.equals(platformType)) {
                        try {
                            MainAPI.getInstance().getDataClassManager().getSocketManager().setUrl(platformType);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (PlatformType.SOOP.equals(platformType)) {
                        try {
                            MainAPI.getInstance().runSoopClient();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }

        });

        server.start();
    }

    private void openHTML(HttpExchange exchange) {
        try (InputStream is = CallbackServer.class.getClassLoader().getResourceAsStream("assets/nr-donation/index.html")) {
            if (is == null) {
                byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound);
                }
                return;
            }

            byte[] bytes = readAllBytes(is);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, n);
        }
        return buffer.toByteArray();
    }
}
