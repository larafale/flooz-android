package me.flooz.app.Model;

import org.json.JSONObject;

import me.flooz.app.Network.FloozRestClient;

/**
 * Created by omouren on 11/04/2017.
 */

public class FLTransactionOptions {

    public Boolean likeEnabled = true;
    public Boolean commentEnabled = true;
    public Boolean shareEnabled = true;

    public static FLTransactionOptions defaultInstance() {
        if (FloozRestClient.getInstance().currentTexts != null && FloozRestClient.getInstance().currentTexts.floozOptions != null)
            return FloozRestClient.getInstance().currentTexts.floozOptions;

        return new FLTransactionOptions(null);
    }

    public static FLTransactionOptions defaultWithJSON(JSONObject jsonData) {
        FLTransactionOptions options = FLTransactionOptions.defaultInstance();

        if (jsonData != null) {
            options.setJson(jsonData);
        }

        return options;
    }

    public static FLTransactionOptions newWithJSON(JSONObject jsonData) {
        FLTransactionOptions options = new FLTransactionOptions(jsonData);
        return options;
    }

    public FLTransactionOptions(JSONObject jsonData) {
        super();

        this.setJson(jsonData);
    }

    public void setJson(JSONObject json) {
        if (json != null) {
            if (json.has("like"))
                this.likeEnabled = json.optBoolean("like", true);

            if (json.has("comment"))
                this.commentEnabled = json.optBoolean("comment", true);

            if (json.has("share"))
                this.shareEnabled = json.optBoolean("share", true);
        }
    }
}