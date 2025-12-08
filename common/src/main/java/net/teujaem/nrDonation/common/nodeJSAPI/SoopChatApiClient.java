package net.teujaem.nrDonation.common.nodeJSAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * SOOP Chat SDK Bridge API Client
 * Node.js API와 통신하는 Java 클라이언트
 */
public class SoopChatApiClient {
    private static final Logger log = LoggerFactory.getLogger(SoopChatApiClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private EventSource eventSource;

    public SoopChatApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    // ==================== Session Management ====================

    /**
     * 세션 시작 (초기화 + 연결)
     */
    public ApiResponse<SessionResponse> startSession(String sessionId, String clientId,
                                                     String clientSecret, String accessToken) throws IOException {

        SessionStartRequest requestObj =
                new SessionStartRequest(sessionId, clientId, clientSecret, accessToken);

        return post("/api/session/start", requestObj, SessionResponse.class);
    }

    /**
     * 세션 중지
     */
    public ApiResponse<SessionResponse> stopSession(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/session/stop", requestObj, SessionResponse.class);
    }

    /**
     * 세션 상태 조회
     */
    public ApiResponse<SessionStatusResponse> getSessionStatus(String sessionId) throws IOException {
        return get("/api/session/status?sessionId=" + sessionId, SessionStatusResponse.class);
    }

    /**
     * 인증 토큰 설정
     */
    public ApiResponse<SessionResponse> setAuth(String sessionId, String accessToken) throws IOException {
        AuthRequest requestObj = new AuthRequest(sessionId, accessToken);
        return post("/api/session/auth", requestObj, SessionResponse.class);
    }

    /**
     * 채팅 서버 연결
     */
    public ApiResponse<ConnectResponse> connect(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/session/connect", requestObj, ConnectResponse.class);
    }

    /**
     * 채팅 서버 연결 해제
     */
    public ApiResponse<SessionResponse> disconnect(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/session/disconnect", requestObj, SessionResponse.class);
    }

    // ==================== Room Info ====================

    /**
     * 방 정보 조회
     */
    public ApiResponse<RoomInfoResponse> getRoomInfo(String sessionId) throws IOException {
        return get("/api/room/info?sessionId=" + sessionId, RoomInfoResponse.class);
    }

    // ==================== Chat Messages ====================

    /**
     * 일반 메시지 전송
     */
    public ApiResponse<MessageResponse> sendMessage(String sessionId, String message) throws IOException {
        MessageRequest requestObj = new MessageRequest(sessionId, message);
        return post("/api/chat/send", requestObj, MessageResponse.class);
    }

    /**
     * 매니저 메시지 전송
     */
    public ApiResponse<MessageResponse> sendManagerMessage(String sessionId, String message) throws IOException {
        MessageRequest requestObj = new MessageRequest(sessionId, message);
        return post("/api/chat/manager/send", requestObj, MessageResponse.class);
    }

    // ==================== Chat Control ====================

    /**
     * 채팅 정지
     */
    public ApiResponse<ChatControlResponse> freezeChat(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/chat/freeze", requestObj, ChatControlResponse.class);
    }

    /**
     * 채팅 재개
     */
    public ApiResponse<ChatControlResponse> unfreezeChat(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/chat/unfreeze", requestObj, ChatControlResponse.class);
    }

    /**
     * 슬로우 모드 설정
     */
    public ApiResponse<ChatControlResponse> setSlowMode(String sessionId, int duration) throws IOException {
        SlowModeRequest requestObj = new SlowModeRequest(sessionId, duration);
        return post("/api/chat/slow", requestObj, ChatControlResponse.class);
    }

    /**
     * 슬로우 모드 해제
     */
    public ApiResponse<ChatControlResponse> unsetSlowMode(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/chat/slow/off", requestObj, ChatControlResponse.class);
    }

    // ==================== User Management ====================

    /**
     * 사용자 차단
     */
    public ApiResponse<UserManagementResponse> banUser(String sessionId, String userId, String chatMessage) throws IOException {
        BanUserRequest requestObj = new BanUserRequest(sessionId, userId, chatMessage);
        return post("/api/chat/ban", requestObj, UserManagementResponse.class);
    }

    /**
     * 사용자 차단 해제
     */
    public ApiResponse<UserManagementResponse> unbanUser(String sessionId, String userId) throws IOException {
        UnbanUserRequest requestObj = new UnbanUserRequest(sessionId, userId);
        return post("/api/chat/unban", requestObj, UserManagementResponse.class);
    }

    /**
     * 차단된 사용자 목록 요청
     */
    public ApiResponse<UserManagementResponse> requestBannedUserList(String sessionId) throws IOException {
        SessionIdRequest requestObj = new SessionIdRequest(sessionId);
        return post("/api/chat/request-banned", requestObj, UserManagementResponse.class);
    }

    // ==================== Event Streaming (SSE) ====================

    /**
     * SSE 이벤트 스트림 연결
     */
    public void openEventStream(String sessionId, final Consumer<String> onEvent, final Consumer<Throwable> onError) {
        String url = baseUrl + "/api/events/stream?sessionId=" + sessionId;

        Request request = new Request.Builder()
                .url(url)
                .build();

        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                log.info("SSE Connected: {}", url);
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                log.debug("SSE Event: {}", data);
                onEvent.accept(data);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                log.error("SSE Error", t);
                if (onError != null) {
                    onError.accept(t);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("SSE Closed");
            }
        };

        eventSource = EventSources.createFactory(httpClient)
                .newEventSource(request, listener);
    }

    /**
     * SSE 이벤트 스트림 연결 종료
     */
    public void closeEventStream() {
        if (eventSource != null) {
            eventSource.cancel();
            eventSource = null;
            log.info("SSE Closed");
        }
    }

    // ==================== Private Helper Methods ====================

    private <T> ApiResponse<T> post(String path, Object body, Class<T> responseType) throws IOException {
        String json = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(baseUrl + path)
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        try {
            return parseResponse(response, responseType);
        } finally {
            response.close();
        }
    }

    private <T> ApiResponse<T> get(String path, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + path)
                .get()
                .build();

        Response response = httpClient.newCall(request).execute();
        try {
            return parseResponse(response, responseType);
        } finally {
            response.close();
        }
    }

    private <T> ApiResponse<T> parseResponse(Response response, Class<T> responseType) throws IOException {
        String body = response.body() != null ? response.body().string() : "";

        if (!response.isSuccessful()) {
            ErrorResponse error = gson.fromJson(body, ErrorResponse.class);
            return new ApiResponse<T>(false, null, error != null ? error.error : "Unknown error");
        }

        T data = gson.fromJson(body, responseType);
        return new ApiResponse<T>(true, data, null);
    }

    // ==================== Request/Response Classes ====================

    public static class SessionStartRequest {
        private String sessionId;
        private String clientId;
        private String clientSecret;
        private String accessToken;

        public SessionStartRequest(String sessionId, String clientId, String clientSecret, String accessToken) {
            this.sessionId = sessionId;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.accessToken = accessToken;
        }
    }

    public static class SessionIdRequest {
        private String sessionId;

        public SessionIdRequest(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    public static class AuthRequest {
        private String sessionId;
        private String accessToken;

        public AuthRequest(String sessionId, String accessToken) {
            this.sessionId = sessionId;
            this.accessToken = accessToken;
        }
    }

    public static class MessageRequest {
        private String sessionId;
        private String message;

        public MessageRequest(String sessionId, String message) {
            this.sessionId = sessionId;
            this.message = message;
        }
    }

    public static class SlowModeRequest {
        private String sessionId;
        private int duration;

        public SlowModeRequest(String sessionId, int duration) {
            this.sessionId = sessionId;
            this.duration = duration;
        }
    }

    public static class BanUserRequest {
        private String sessionId;
        private String userId;
        private String chatMessage;

        public BanUserRequest(String sessionId, String userId, String chatMessage) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.chatMessage = chatMessage;
        }
    }

    public static class UnbanUserRequest {
        private String sessionId;
        private String userId;

        public UnbanUserRequest(String sessionId, String userId) {
            this.sessionId = sessionId;
            this.userId = userId;
        }
    }

    public static class SessionResponse {
        private boolean success;
        private String sessionId;

        public boolean isSuccess() { return success; }
        public String getSessionId() { return sessionId; }
    }

    public static class SessionStatusResponse {
        private boolean exists;
        private String sessionId;
        private boolean connected;
        private boolean hasAccessToken;
        private String clientId;

        public boolean isExists() { return exists; }
        public String getSessionId() { return sessionId; }
        public boolean isConnected() { return connected; }
        public boolean isHasAccessToken() { return hasAccessToken; }
        public String getClientId() { return clientId; }
    }

    public static class ConnectResponse {
        private boolean success;
        private Object result;

        public boolean isSuccess() { return success; }
        public Object getResult() { return result; }
    }

    public static class RoomInfoResponse {
        private boolean success;
        private Object data;

        public boolean isSuccess() { return success; }
        public Object getData() { return data; }
    }

    public static class MessageResponse {
        private boolean success;
        private Object result;

        public boolean isSuccess() { return success; }
        public Object getResult() { return result; }
    }

    public static class ChatControlResponse {
        private boolean success;
        private Object result;

        public boolean isSuccess() { return success; }
        public Object getResult() { return result; }
    }

    public static class UserManagementResponse {
        private boolean success;
        private Object result;

        public boolean isSuccess() { return success; }
        public Object getResult() { return result; }
    }

    public static class ErrorResponse {
        private String error;

        public String getError() { return error; }
    }

    public static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String error;

        public ApiResponse(boolean success, T data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getError() { return error; }
    }
}
