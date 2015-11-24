package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Flooz on 3/19/15.
 */
public class FLTexts {

    public String cardInfos;
    public JSONObject notificationsText;
    public JSONObject json;
    public FLSlider slider;
    public JSONObject couponButton;
    public JSONObject menu;
    public List<FLCountry> avalaibleCountries;

    public FLTexts(JSONObject json) {
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.json = json;

        this.notificationsText = json.optJSONObject("notificationsText");
        this.slider = new FLSlider(json.optJSONObject("slider"));
        this.couponButton = json.optJSONObject("couponButton");
        this.cardInfos = json.optString("card");
        this.menu = json.optJSONObject("menu");

        JSONArray countries = json.optJSONArray("countries");

        this.avalaibleCountries = new ArrayList();

        if (countries != null) {
            for (int i = 0; i < countries.length(); i++) {
                this.avalaibleCountries.add(new FLCountry(countries.optJSONObject(i)));
            }
        }

        if (this.avalaibleCountries.size() == 0)
            this.avalaibleCountries.add(FLCountry.defaultCountry());
    }
}
