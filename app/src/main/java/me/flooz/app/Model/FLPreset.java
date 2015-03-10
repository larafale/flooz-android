package me.flooz.app.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import me.flooz.app.Utils.JSONHelper;

/**
 * Created by Flooz on 1/16/15.
 */
public class FLPreset {

    public FLUser to;
    public Number amount;
    public String why;
    public String title;
    public Map payload;
    public boolean blockAmount = false;
    public boolean blockTo = false;
    public boolean blockBack = false;
    public boolean blockWhy = false;
    public boolean focusAmount = false;
    public boolean focusWhy = false;
    public FLTransaction.TransactionType type = FLTransaction.TransactionType.TransactionTypeNone;

    public FLPreset(JSONObject json) {
        super();
        if (json != null)
            this.setJson(json);
    }

    private void setJson(JSONObject json) {
        if (json.has("to"))
            this.to = new FLUser(json.optJSONObject("to"));

        if (json.has("amount"))
            this.amount = json.optDouble("amount");

        if (json.has("why"))
            this.why = json.optString("why");

        if (json.has("title"))
            this.title = json.optString("title");

        if (json.has("payload"))
            try {
                this.payload = JSONHelper.toMap(json.optJSONObject("payload"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (json.has("block")) {
            JSONObject block = json.optJSONObject("block");

            if (block.has("amount"))
                this.blockAmount = block.optBoolean("amount");

            if (block.has("to"))
                this.blockTo = block.optBoolean("to");

            if (block.has("close"))
                this.blockBack = block.optBoolean("close");

            if (block.has("pay") && block.optBoolean("pay"))
                this.type = FLTransaction.TransactionType.TransactionTypeCharge;

            if (block.has("charge") && block.optBoolean("charge"))
                this.type = FLTransaction.TransactionType.TransactionTypePayment;

            if (block.has("why"))
                this.blockWhy = block.optBoolean("why");
        }

        if (json.has("focus")) {
            String focus = json.optString("focus");

            if (focus.contentEquals("amount"))
                this.focusAmount = true;
            else if (focus.contentEquals("why"))
                this.focusWhy = true;
        }
    }
}
