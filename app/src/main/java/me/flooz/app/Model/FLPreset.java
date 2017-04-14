package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import me.flooz.app.Utils.JSONHelper;

/**
 * Created by Flooz on 1/16/15.
 */
public class FLPreset {

    public JSONObject jsonData;
    public String to;
    public String toFullname;
    public JSONObject toInfos;
    public Number amount;
    public String presetId;
    public String why;
    public String name;
    public String whyPlaceholder;
    public String title;
    public Map payload;
    public String image;
    public String collectName;
    public boolean isParticipation = false;
    public boolean close = true;
    public boolean focusAmount = false;
    public boolean focusWhy = false;
    public JSONArray steps;
    public JSONObject popup;
    public FLNewFloozOptions options = FLNewFloozOptions.defaultInstance();

    public FLPreset(JSONObject json) {
        super();
        if (json != null)
            this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.jsonData = json;

        if (json.has("isParticipation"))
            this.isParticipation = json.optBoolean("isParticipation");

        if (this.isParticipation) {
            this.collectName = json.optString("to");
        } else {
            if (json.has("to"))
                this.to = json.optString("to");

            if (json.has("toFullName"))
                this.toFullname = json.optString("toFullName");

            if (json.has("contact"))
                this.toInfos = json.optJSONObject("contact");
        }

        this.presetId = json.optString("_id");

        if (json.has("options"))
            this.options.setJSON(json.optJSONObject("options"));

        if (json.has("amount"))
            this.amount = json.optDouble("amount");

        if (json.has("name"))
            this.name = json.optString("name");

        if (json.has("whyPlaceholder"))
            this.whyPlaceholder = json.optString("whyPlaceholder");

        if (json.has("why"))
            this.why = json.optString("why");

        if (json.has("title"))
            this.title = json.optString("title");

        if (json.has("image"))
            this.image = json.optString("image");

        if (json.has("popup"))
            this.popup = json.optJSONObject("popup");
        else
            this.popup = null;

        if (json.has("steps"))
            this.steps = json.optJSONArray("steps");
        else
            this.steps = null;

        if (json.has("payload"))
            try {
                this.payload = JSONHelper.toMap(json.optJSONObject("payload"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (json.has("close"))
            this.close = json.optBoolean("close");

        if (json.has("focus")) {
            String focus = json.optString("focus");

            if (focus.contentEquals("amount"))
                this.focusAmount = true;
            else if (focus.contentEquals("why"))
                this.focusWhy = true;
        }
    }
}
