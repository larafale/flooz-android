package me.flooz.app.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Utils.MomentDate;
import me.flooz.app.App.FloozApplication;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLComment {

    public FLUser user;
    public String content;
    public MomentDate date;
    public String when;
    public String dateText;

    public FLComment(JSONObject json) {
        super();
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        try {
            this.content = json.getString("comment");
            this.user = new FLUser(json);
            this.user.userId = json.getString("userId");

            this.date = new MomentDate(FloozApplication.getAppContext(), json.getString("cAt"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
