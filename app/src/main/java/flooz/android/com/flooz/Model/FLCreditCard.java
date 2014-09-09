package flooz.android.com.flooz.Model;

import org.json.JSONObject;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLCreditCard {

    public String cardId;
    public String owner;
    public String number;

    public FLCreditCard(JSONObject json) {
        super();
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.cardId = json.optString("id");
        this.owner = json.optString("holder");
        this.number = json.optString("number");
    }
}
