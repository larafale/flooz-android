package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLTriggerManager;

import static me.flooz.app.Model.FLTrigger.FLTriggerAction.*;

/**
 * Created by Flooz on 11/6/14.
 */
public class FLTrigger {

    public enum FLTriggerAction {
        FLTriggerActionAsk,
        FLTriggerActionCall,
        FLTriggerActionClear,
        FLTriggerActionHide,
        FLTriggerActionLogin,
        FLTriggerActionLogout,
        FLTriggerActionOpen,
        FLTriggerActionNone,
        FLTriggerActionPicker,
        FLTriggerActionSend,
        FLTriggerActionShow,
        FLTriggerActionSync
    }

    public FLTriggerAction action;
    public String key;
    public String categoryView;
    public String view;
    public String category;
    public JSONObject data;
    public Number delay;
    public ArrayList<FLTrigger> triggers;
    public Boolean valid;

    public FLTrigger(JSONObject json) {
        super();

        this.valid = this.setJson(json);
    }

    private boolean setJson(JSONObject json) {
        if (json == null)
            return false;

        this.key = json.optString("key");

        if (this.key == null)
            return false;

        String[] splitKey = this.key.split(":");

        if (splitKey.length != 3)
            return false;

        if (!this.convertActionToEnum(splitKey[2]))
            return false;

        this.category = splitKey[0];
        this.view = splitKey[1];
        this.categoryView = this.category + ":" + this.view;

        this.delay = json.optDouble("delay", 0);
        this.data = json.optJSONObject("data");

        if (json.has("triggers")) {
            if (json.opt("triggers") instanceof JSONArray) {
                this.triggers = FLTriggerManager.convertTriggersJSONArrayToList(json.optJSONArray("triggers"));
            } else if (json.opt("triggers") instanceof JSONObject) {
                this.triggers = new ArrayList<>();
                FLTrigger tmp = new FLTrigger(json.optJSONObject("triggers"));

                if (tmp.valid)
                    this.triggers.add(tmp);
            } else
                this.triggers = new ArrayList<>();
        } else
            this.triggers = new ArrayList<>();

        return true;
    }

    private boolean convertActionToEnum(String actionData) {

        this.action = FLTriggerActionNone;

        switch (actionData) {
            case "ask":
                this.action = FLTriggerActionAsk;
                break;
            case "call":
                this.action = FLTriggerActionCall;
                break;
            case "clear":
                this.action = FLTriggerActionClear;
                break;
            case "hide":
                this.action = FLTriggerActionHide;
                break;
            case "login":
                this.action = FLTriggerActionLogin;
                break;
            case "logout":
                this.action = FLTriggerActionLogout;
                break;
            case "open":
                this.action = FLTriggerActionOpen;
                break;
            case "send":
                this.action = FLTriggerActionSend;
                break;
            case "show":
                this.action = FLTriggerActionShow;
                break;
            case "sync":
                this.action = FLTriggerActionSync;
                break;
            case "picker":
                this.action = FLTriggerActionPicker;
                break;
        }

        if (this.action == FLTriggerActionNone)
            return false;

        return true;
    }

}
