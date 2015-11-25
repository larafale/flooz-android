package me.flooz.app.Model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 11/23/15.
 */
public class FLCountry {

    public String code;
    public String name;
    public String indicatif;
    public int imageID;
    public int maxLength;

    public FLCountry(JSONObject json) {
        if (json != null) {
            Context context = FloozApplication.getAppContext();

            this.name = json.optString("country");
            this.code = json.optString("code");
            this.indicatif = "+" + json.optString("indicatif");
            this.imageID = context.getResources().getIdentifier(this.code.toLowerCase(), "drawable", context.getPackageName());

            JSONArray lengths = json.optJSONArray("lengths");

            this.maxLength = 0;
            if (lengths != null) {
                for (int i = 0; i < lengths.length(); i++) {
                    this.maxLength = Math.max(this.maxLength, lengths.optInt(i));
                }
            }
        }
    }

    public static FLCountry defaultCountry() {
        try {
            return new FLCountry(new JSONObject("'country':'France', 'code':'FR', 'indicatif':'+33', 'lengths':[9]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FLCountry countryFromCode(String code) {
        if (FloozRestClient.getInstance().currentTexts != null) {
            for (FLCountry country : FloozRestClient.getInstance().currentTexts.avalaibleCountries) {
                if (code.contentEquals(country.code))
                    return country;
            }
        } else if (code.contentEquals(FLCountry.defaultCountry().code))
            return FLCountry.defaultCountry();

        return null;
    }

    public static FLCountry countryFromIndicatif(String indicatif) {
        if (FloozRestClient.getInstance().currentTexts != null) {
            for (FLCountry country : FloozRestClient.getInstance().currentTexts.avalaibleCountries) {
                if (indicatif.contentEquals(country.indicatif))
                    return country;
            }
        } else if (indicatif.contentEquals(FLCountry.defaultCountry().indicatif))
            return FLCountry.defaultCountry();

        return null;
    }
}
