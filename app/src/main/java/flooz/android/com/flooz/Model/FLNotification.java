package flooz.android.com.flooz.Model;

import android.text.format.DateFormat;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            this.date = dateFormatter.parse(json.optString("cAt"));
            this.dateText = DateFormat.format("dd MMM à hh:mm", this.date).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
