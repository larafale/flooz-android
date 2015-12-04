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
        Context context = FloozApplication.getAppContext();

        if (json != null) {
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
        Context context = FloozApplication.getAppContext();

        FLCountry defaultCountry = new FLCountry(null);

        defaultCountry.indicatif = "+33";
        defaultCountry.code = "FR";
        defaultCountry.name = "France";
        defaultCountry.imageID = context.getResources().getIdentifier(defaultCountry.code.toLowerCase(), "drawable", context.getPackageName());
        defaultCountry.maxLength = 9;

        return defaultCountry;
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
