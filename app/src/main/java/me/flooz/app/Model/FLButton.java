package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.MomentDate;

/**
 * Created by Flooz on 03/05/16.
 */
public class FLButton {

    public String defaultImg;
    public String imgUrl;
    public String title;
    public String subtitle;
    public Boolean avalaible;
    public JSONArray triggers;

    public FLButton(JSONObject json) {
        super();
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.title = json.optString("title");
        this.subtitle = json.optString("subtitle");
        this.defaultImg = json.optString("defaultPic");
        this.imgUrl = json.optString("urlPic");

        if (json.has("soon")) {
            this.avalaible = !json.optBoolean("soon");
        } else {
            this.avalaible = true;
        }

        this.triggers = json.optJSONArray("triggers");
    }
}
