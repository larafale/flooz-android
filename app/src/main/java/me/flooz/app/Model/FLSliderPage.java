package me.flooz.app.Model;

import org.json.JSONObject;

/**
 * Created by Flooz on 4/7/15.
 */
public class FLSliderPage {

    public String text;
    public String skipText;
    public String imgURL;

    public FLSliderPage(JSONObject json) {
        this.text = json.optString("text");
        this.imgURL = json.optString("image");
        this.skipText = json.optString("skip");
    }
}
