package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Flooz on 3/19/15.
 */
public class FLTexts {

    public String shareCode;
    public String shareTitle;
    public String shareHeader;
    public String shareSms;
    public String shareTwitter;
    public String cardInfos;
    public JSONObject shareMail;
    public JSONObject shareFb;
    public JSONArray shareText;
    public JSONObject notificationsText;
    public JSONObject json;
    public FLSlider slider;
    public JSONObject couponButton;
    public JSONArray secretQuestions;

    public FLTexts(JSONObject json) {
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.json = json;

        this.shareCode = json.optString("code");
        this.shareText = json.optJSONArray("text");
        this.shareFb = json.optJSONObject("facebook");
        this.shareMail = json.optJSONObject("mail");
        this.shareTwitter = json.optString("twitter");
        this.shareSms = json.optString("sms");
        this.shareTitle = json.optString("title");
        this.shareHeader = json.optString("h1");
        this.notificationsText = json.optJSONObject("notificationsText");
        this.slider = new FLSlider(json.optJSONObject("slider"));
        this.couponButton = json.optJSONObject("couponButton");
        this.secretQuestions = json.optJSONArray("secretQuestions");
        this.cardInfos = json.optString("card");
    }
}
