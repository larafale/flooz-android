package me.flooz.app.Model;

import org.json.JSONObject;

public class FLMangopayOptions {

    public String baseURL;
    public String clientId;

    public FLMangopayOptions(JSONObject json) {
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        if (json != null) {
            this.baseURL = json.optString("baseUrl");
            this.clientId = json.optString("clientId");
        }
    }
}
