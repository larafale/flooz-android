package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Flooz on 8/7/15.
 */
public class FLShareText {

    public String shareCode;
    public String shareTitle;
    public String shareHeader;
    public String shareSms;
    public String shareTwitter;
    public JSONObject shareMail;
    public String shareFb;
    public JSONArray shareText;
    public JSONObject json;

    public FLShareText(JSONObject json) {
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.json = json;

        this.shareCode = json.optString("code");
        this.shareText = json.optJSONArray("text");
        this.shareFb = json.optString("facebook");
        this.shareMail = json.optJSONObject("mail");
        this.shareTwitter = json.optString("twitter");
        this.shareSms = json.optString("sms");
        this.shareTitle = json.optString("title");
        this.shareHeader = json.optString("h1");
     }
}
