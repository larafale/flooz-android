package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Flooz on 3/19/15.
 */
public class FLTexts {

    public String cardInfos;
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

        this.notificationsText = json.optJSONObject("notificationsText");
        this.slider = new FLSlider(json.optJSONObject("slider"));
        this.couponButton = json.optJSONObject("couponButton");
        this.secretQuestions = json.optJSONArray("secretQuestions");
        this.cardInfos = json.optString("card");
    }
}
