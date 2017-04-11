package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Flooz on 3/19/15.
 */
public class  FLTexts {

    public String friendSearch;
    public String cardInfos;
    public JSONObject notificationsText;
    public JSONObject json;
    public FLSlider slider;
    public JSONObject couponButton;
    public JSONObject menu;
    public List<FLCountry> avalaibleCountries;
    public List<FLButton> homeButtons;
    public List<FLButton> cashinButtons;
    public List<FLButton> paymentSources;
    public String audiotelNumber;
    public String audiotelImage;
    public String audiotelInfos;
    public String cardHolder = null;
    public String balancePopupTitle = null;
    public String balancePopupText = null;
    public JSONArray suggestWeb = new JSONArray();
    public JSONArray suggestGifs = new JSONArray();
    public List<FLScope> homeScopes;
    public FLTransactionOptions floozOptions;
    public FLNewFloozOptions newFloozOptions;


    public FLTexts(JSONObject json) {
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.json = json;

        if (json.has("balance")) {
            this.balancePopupTitle = json.optJSONObject("balance").optString("title");
            this.balancePopupText = json.optJSONObject("balance").optString("text");
        }

        if (json.has("audiotel")) {
            this.audiotelNumber = json.optJSONObject("audiotel").optString("number");
            this.audiotelImage = json.optJSONObject("audiotel").optString("image");
            this.audiotelInfos = json.optJSONObject("audiotel").optString("info");
        }

        if (json.has("cardHolder"))
            this.cardHolder = json.optBoolean("cardHolder") ? "true" : "false";
        else
            this.cardHolder = null;

        this.notificationsText = json.optJSONObject("notificationsText");
        this.slider = new FLSlider(json.optJSONObject("slider"));
        this.couponButton = json.optJSONObject("couponButton");
        this.cardInfos = json.optString("card");
        this.friendSearch = json.optString("friendSearch");
        this.menu = json.optJSONObject("menu");

        JSONArray countries = json.optJSONArray("countries");

        this.avalaibleCountries = new ArrayList<>();

        if (countries != null) {
            for (int i = 0; i < countries.length(); i++) {
                this.avalaibleCountries.add(new FLCountry(countries.optJSONObject(i)));
            }
        }

        this.cashinButtons = new ArrayList<>();

        JSONArray cashins = json.optJSONArray("cashins");

        if (cashins != null) {
            for (int i = 0; i < cashins.length(); i++) {
                this.cashinButtons.add(new FLButton(cashins.optJSONObject(i)));
            }
        }

        this.homeButtons = new ArrayList<>();

        JSONArray buttons = json.optJSONArray("homeButtons");

        if (buttons != null) {
            for (int i = 0; i < buttons.length(); i++) {
                this.homeButtons.add(new FLButton(buttons.optJSONObject(i)));
            }
        }

        this.paymentSources = new ArrayList<>();

        JSONArray sources = json.optJSONArray("sources");

        if (sources != null) {
            for (int i = 0; i < sources.length(); i++) {
                this.paymentSources.add(new FLButton(sources.optJSONObject(i)));
            }
        }

        if (this.avalaibleCountries.size() == 0)
            this.avalaibleCountries.add(FLCountry.defaultCountry());

        if (this.json.has("suggest")) {
            this.suggestGifs = this.json.optJSONObject("suggest").optJSONArray("gifs");
            this.suggestWeb = this.json.optJSONObject("suggest").optJSONArray("webs");
        }
    }
}
