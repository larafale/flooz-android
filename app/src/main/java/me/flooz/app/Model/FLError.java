package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Utils.FLTriggerManager;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLError {

    public enum ErrorType {
        ErrorTypeSuccess,
        ErrorTypeWarning,
        ErrorTypeError
    }

    public Number delay;
    public Number time;
    public String text;
    public String title;
    public Number code;
    public Boolean isVisible = true;
    public ErrorType type;
    public List<FLTrigger> triggers;

    public FLError() {
        super();
        this.triggers = new ArrayList<>();
    }

    public FLError(String title, String content, Number time, ErrorType type) {
        super();
        this.triggers = new ArrayList<>();
        this.title = title;
        this.text = content;
        this.time = time;
        this.type = type;
    }

    public FLError(JSONObject json) {
        super();
        if (json != null)
            this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.delay = json.optInt("delay");
        this.time = json.optInt("time");
        this.text = json.optString("message");
        this.title = json.optString("title");
        this.code = json.optInt("code");
        this.isVisible = json.optBoolean("visible");
        this.type = errorTypeParamToEnum(json.optString("type"));
        this.triggers = new ArrayList<>();

        if (this.time == null || this.time.intValue() == 0)
            this.time = 3;

        if (json.has("triggers")) {
            if (json.opt("triggers") instanceof JSONArray) {
                this.triggers = FLTriggerManager.convertTriggersJSONArrayToList(json.optJSONArray("triggers"));
            } else if (json.opt("triggers") instanceof JSONObject) {
                FLTrigger tmp = new FLTrigger(json.optJSONObject("triggers"));

                if (tmp.valid)
                    this.triggers.add(tmp);
            }
        }
    }

    public static ErrorType errorTypeParamToEnum(String param) {
        switch (param) {
            case "red":
                return ErrorType.ErrorTypeError;
            case "blue":
                return ErrorType.ErrorTypeWarning;
            case "green":
                return ErrorType.ErrorTypeSuccess;
        }

        return null;
    }
}
