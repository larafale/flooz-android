package flooz.android.com.flooz.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import flooz.android.com.flooz.Utils.JSONHelper;

/**
 * Created by Flooz on 9/8/14.
 */
public class FLUser
{
    public String userId;
    public Number amount;
    public String firstname;
    public String lastname;
    public String fullname;
    public String username;
    public String email;
    public String phone;
    public String avatarURL;
    public String profileCompletion;
    public Number friendsCount;
    public Number eventsCount;
    public Number transactionsCount;
    public Boolean haveStatsPending;

    public String deviceToken;

    public Map<String, Object> address;
    public Map<String, Object> sepa;
    public Map<String, Object> notifications;
    public Map<String, Object> notificationsText;
    public Map<String, Object> checkDocuments;
    public FLCreditCard creditCard;

    public List friends;
    public List friendsRecent;
    public List friendsRequest;

    public Boolean needDocuments;

    public Boolean isFriendWaiting;

    public Number record;
    public String device;
    public Map<String, Object> settings;
    public String inviteCode;
    public String hasSecureCode;
    public JSONObject json;

    public FLUser(JSONObject data)
    {
        super();
        this.setJson(data);
    }

    public void setJson(JSONObject data)
    {
        this.json = data;

        try {
            if (this.json.has("_id")) {
                this.userId = this.json.getString("_id");
            }

            if (this.json.has("balance")){
                this.amount = this.json.optDouble("balance");
            } else {
                this.amount = this.json.optDouble("amount");
            }

            this.firstname = this.json.optString("firstName");
            this.lastname = this.json.optString("lastName");
            this.fullname = this.json.optString("name");
            this.username = this.json.optString("nick");
            this.email = this.json.optString("email");
            this.phone = this.json.optString("phone");
            this.avatarURL = this.json.optString("pic");
            this.profileCompletion = this.json.optString("profileCompletion");
            this.hasSecureCode = this.json.optString("secureCode");

            if (this.avatarURL.equals("/img/nopic.png"))
                this.avatarURL = null;

            if (this.json.has("device"))
                this.device = this.json.getString("device");

            if (this.json.has("friends"))
                this.friendsCount = this.json.getJSONArray("friends").length();

            if (this.json.has("stats")) {
                this.eventsCount = this.json.getJSONObject("stats").getJSONObject("event").getInt("created");
                this.transactionsCount = this.json.getJSONObject("stats").getJSONObject("flooz").getInt("total");
            }

            this.haveStatsPending = false;

            if (this.json.has("stats") && this.json.getJSONObject("stats").getJSONObject("flooz").has("pending")) {
                Number statsPending = this.json.getJSONObject("stats").getJSONObject("flooz").getInt("pending");
                if (statsPending.intValue() > 0)
                    this.haveStatsPending = true;
            }

            if (this.json.has("settings")) {
                this.settings = JSONHelper.toMap(this.json.getJSONObject("settings"));

                if (this.json.has("settings") && this.json.getJSONObject("settings").has("address")) {
                    this.address = JSONHelper.toMap(this.json.getJSONObject("settings").getJSONObject("address"));
                }

                if (this.json.has("settings") && this.json.getJSONObject("settings").has("sepa")) {
                    this.sepa = JSONHelper.toMap(this.json.getJSONObject("settings").getJSONObject("sepa"));
                }
            }

            this.notifications = new HashMap<String, Object>();

            if (this.json.has("notifications"))
                this.notifications = JSONHelper.toMap(this.json.getJSONObject("notifications"));

            this.notificationsText = new HashMap<String, Object>();

            if (this.json.has("notificationsText"))
                this.notificationsText = JSONHelper.toMap(this.json.getJSONObject("notificationsText"));

            if (this.json.has("friends")) {
                this.friends = new ArrayList();
                JSONArray arrayFriends = this.json.getJSONArray("friends");

                for (int i = 0; i < arrayFriends.length(); i++) {
                    friends.add(new FLUser(arrayFriends.getJSONObject(i)));
                }
            }

            if (this.json.has("recentFriends")) {
                this.friendsRecent = new ArrayList();
                JSONArray arrayFriendsRecent = this.json.getJSONArray("recentFriends");

                for (int i = 0; i < arrayFriendsRecent.length(); i++) {
                    friendsRecent.add(new FLUser(arrayFriendsRecent.getJSONObject(i)));
                }
            }

            if (this.json.has("friendsRequest")) {
                this.friendsRequest = new ArrayList();
                JSONArray arrayFriendsRequest = this.json.getJSONArray("friendsRequest");

                for (int i = 0; i < arrayFriendsRequest.length(); i++) {
                    friendsRequest.add(new FLUser(arrayFriendsRequest.getJSONObject(i)));
                }
            }

            this.checkDocuments = new HashMap<String, Object>();

            if (this.json.has("check"))
                this.checkDocuments = JSONHelper.toMap(this.json.getJSONObject("check"));

            this.isFriendWaiting = false;

            if (this.json.has("state") && this.json.getInt("state") == 1) {
                this.isFriendWaiting = true;
            }

            if (this.json.has("record"))
                this.record = this.json.getInt("record");

            if (this.json.has("settings") && this.json.getJSONObject("settings").has("device"))
                this.device = this.json.getJSONObject("settings").getString("device");

            if (this.json.has("invitation") && this.json.getJSONObject("invitation").has("code"))
                this.inviteCode = this.json.getJSONObject("invitation").getString("code");

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        if([json objectForKey:@"cards"] && [[json objectForKey:@"cards"] count] > 0){
//        _creditCard = [[FLCreditCard alloc] initWithJSON:[[json objectForKey:@"cards"] objectAtIndex:0]];
//    }
//
    }
}
