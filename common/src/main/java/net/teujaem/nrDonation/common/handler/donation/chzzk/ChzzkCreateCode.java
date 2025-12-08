package net.teujaem.nrDonation.common.handler.donation.chzzk;

import net.teujaem.nrDonation.common.MainAPI;
import net.teujaem.nrDonation.common.util.UrlEncoding;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ChzzkCreateCode {

    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    private static final String BASE_URL = "https://chzzk.naver.com/account-interlock?";
    private static final String state = MainAPI.getInstance().getDataClassManager().getStateData().getState();

    private static String clientId;

    public ChzzkCreateCode(String clientId) {
        ChzzkCreateCode.clientId = clientId;
    }

    public URI getLoginUrl() {

        Map<String, String> map = new HashMap<String, String>();
        map.put("clientId", clientId);
        map.put("redirectUri", REDIRECT_URI);
        map.put("state", state);

        return URI.create(BASE_URL + UrlEncoding.toXWwwFormUrl(map));
    }
}
