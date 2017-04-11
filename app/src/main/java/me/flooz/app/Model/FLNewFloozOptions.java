package me.flooz.app.Model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Network.FloozRestClient;

/**
 * Created by omouren on 11/04/2017.
 */

public class FLNewFloozOptions {

    public Boolean allowTo = true;
    public Boolean allowPic = true;
    public Boolean allowGif = true;
    public Boolean allowGeo = true;
    public Boolean allowWhy = true;
    public Boolean allowAmount = true;
    public Boolean allowBalance = true;
    public Boolean scopeDefined = false;

    public FLTransaction.TransactionType type = FLTransaction.TransactionType.TransactionTypeNone;
    public FLScope scope;
    public List<FLScope> scopes;

    public FLNewFloozOptions(JSONObject jsonData) {
        super();

        this.setJSON(jsonData);
    }

    public static FLNewFloozOptions defaultInstance() {
        if (FloozRestClient.getInstance().currentTexts != null && FloozRestClient.getInstance().currentTexts.newFloozOptions != null)
            return FloozRestClient.getInstance().currentTexts.newFloozOptions;

        return new FLNewFloozOptions(null);
    }

    public static FLNewFloozOptions defaultWithJSON(JSONObject jsonData) {
        FLNewFloozOptions options = FLNewFloozOptions.defaultInstance();

        if (jsonData != null) {
            options.setJSON(jsonData);
        }

        return options;
    }

    public void setJSON(JSONObject jsonData) {

        if (jsonData != null && jsonData.has("scope")) {
            this.scopeDefined = true;
            this.scope = FLScope.scopeFromObject(jsonData.opt("scope"));
        }

        if (jsonData != null && jsonData.has("scopes")) {
            this.scopes = new ArrayList<>(jsonData.optJSONArray("scopes").length());
            for (int i = 0; i < jsonData.optJSONArray("scopes").length(); i++) {
                this.scopes.add(FLScope.scopeFromObject(jsonData.optJSONArray("scopes").opt(i)));
            }
        }

        if (jsonData != null && jsonData.has("amount")) {
            this.allowAmount = jsonData.optBoolean("amount");
        }

        if (jsonData != null && jsonData.has("balance")) {
            this.allowBalance = jsonData.optBoolean("balance");
        }

        if (jsonData != null && jsonData.has("to")) {
            this.allowTo = jsonData.optBoolean("to");
        }

        if (jsonData != null && jsonData.has("pic")) {
            this.allowPic = jsonData.optBoolean("pic");
        }

        if (jsonData != null && jsonData.has("gif")) {
            this.allowGif = jsonData.optBoolean("gif");
        }

        if (jsonData != null && jsonData.has("geo")) {
            this.allowGeo = jsonData.optBoolean("geo");
        }

        if (jsonData != null && jsonData.has("why")) {
            this.allowWhy = jsonData.optBoolean("why");
        }

        if (jsonData != null) {
            if (jsonData.optBoolean("pay") && jsonData.optBoolean("charge"))
                this.type = FLTransaction.TransactionType.TransactionTypeNone;
            else if (jsonData.optBoolean("pay") || !jsonData.optBoolean("charge"))
                this.type = FLTransaction.TransactionType.TransactionTypePayment;
            else if (jsonData.optBoolean("charge") || !jsonData.optBoolean("pay"))
                this.type = FLTransaction.TransactionType.TransactionTypeCharge;
        }
    }
}
