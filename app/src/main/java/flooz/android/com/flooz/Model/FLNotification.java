package flooz.android.com.flooz.Model;

import android.os.Handler;
import android.text.format.DateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Utils.MomentDate;

/**
 * Created by Flooz on 10/15/14.
 */
public class FLNotification {

    public String notificationID;
    public FLUser user;
    public String content;
    public Boolean isRead;

    public String transactionID;
    public Boolean isFriend = false;
    public Boolean isForCompleteProfil = false;

    public Date date;
    public String dateText;
    public List<FLTrigger> triggers;

    public FLNotification (JSONObject json) {
        this.setJson(json);
    }

    private void setJson(JSONObject json) {

        this.notificationID = json.optString("id");
        this.content = json.optString("text");
        this.user = new FLUser(json.optJSONObject("emitter"));

        this.isRead = json.optInt("state") != 0;

        if (json.has("resource")) {
            if (json.optJSONObject("resource").optString("type").equals("line"))
                this.transactionID = json.optJSONObject("resource").optString("resourceId");
            else if (json.optJSONObject("resource").optString("type").equals("friend"))
                this.isFriend = true;
        }

        if (json.optString("type").equals("completeProfile"))
            this.isForCompleteProfil = true;

        try {
            MomentDate momentDate = new MomentDate(FloozApplication.getAppContext(), json.optString("cAt"));
            this.date = momentDate.getDate();
            this.dateText = momentDate.fromNow();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.triggers = new ArrayList<>();

        if (json != null && json.has("triggers")) {
            JSONArray t = json.optJSONArray("triggers");
            for (int i = 0; i < t.length(); i++) {
                FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                this.triggers.add(trigger);
            }
        }
    }
}
