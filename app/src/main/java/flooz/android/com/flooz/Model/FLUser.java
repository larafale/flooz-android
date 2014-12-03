package flooz.android.com.flooz.Model;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String password;
    public String birthdate;
    public String email;
    public String phone;
    public String avatarURL;
    public Bitmap avatarData = null;
    public String profileCompletion;
    public Number friendsCount;
    public Boolean haveStatsPending;
    public String passCode;

    public String deviceToken;

    public Map<String, Object> address;
    public Map<String, Object> sepa;
    public Map<String, Object> notifications;
    public Map<String, Object> notificationsText;
    public Map<String, Object> checkDocuments;
    public FLCreditCard creditCard;

    public List<FLUser> friends = new ArrayList<FLUser>(0);
    public List<FLUser> friendsRecent = new ArrayList<FLUser>(0);
    public List<FLUser> friendsRequest = new ArrayList<FLUser>(0);

    public Boolean needDocuments;

    public Boolean isFriendWaiting;

    public Number record;
    public String device;
    public Map<String, Object> settings;
    public String inviteCode;
    public String hasSecureCode;
    public JSONObject json;

    public UserKind userKind;

    public enum UserKind {
        FloozUser,
        PhoneUser
    }

    public FLUser(JSONObject data)
    {
        super();
        this.userKind = UserKind.FloozUser;
        this.setJson(data);
    }

    public FLUser()
    {
        super();
        this.userKind = UserKind.PhoneUser;
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

            this.haveStatsPending = false;

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
                this.friends = new ArrayList<FLUser>(0);
                JSONArray arrayFriends = this.json.getJSONArray("friends");

                for (int i = 0; i < arrayFriends.length(); i++) {
                    friends.add(new FLUser(arrayFriends.getJSONObject(i)));
                }
            }

            if (this.json.has("recentFriends")) {
                this.friendsRecent = new ArrayList<FLUser>(0);
                JSONArray arrayFriendsRecent = this.json.getJSONArray("recentFriends");

                for (int i = 0; i < arrayFriendsRecent.length(); i++) {
                    friendsRecent.add(new FLUser(arrayFriendsRecent.getJSONObject(i)));
                }
            }

            if (this.json.has("friendsRequest")) {
                this.friendsRequest = new ArrayList<FLUser>(0);
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

            if (this.json.has("cards") && this.json.getJSONArray("cards").length() > 0)
                this.creditCard = new FLCreditCard(this.json.getJSONArray("cards").getJSONObject(0));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setCreditCard(FLCreditCard card) {
        this.creditCard = card;
    }

    public Boolean userIsAFriend(FLUser user) {
        for (int i = 0; i < this.friends.size(); i++) {
            if (user.userId.equals(this.friends.get(i).userId))
                return true;
        }
        return false;
    }

    public static String formattedBirthdate(String birthdate) {
        String[] tmp = birthdate.split("/");

        return tmp[2] + "-" + tmp[1] + "-" + tmp[0];
    }

    public Map<String, Object> getParamsForSignupCheck() {
        Map<String, Object> res = new HashMap<String, Object>();

        res.put("firstName", this.firstname);
        res.put("lastName", this.lastname);
        res.put("nick", this.username);
        res.put("email", this.email);
        res.put("birthdate", formattedBirthdate(this.birthdate));
        res.put("phone", this.phone);
        res.put("password", this.password);
        res.put("validate", true);

        if (this.fullname != null)
            res.put("fullName", this.fullname);

        if (this.avatarURL != null || this.avatarData != null)
            res.put("hasImage", true);
        else
            res.put("hasImage", false);

        if (this.avatarURL != null)
            res.put("avatarURL", this.avatarURL);

        if (this.json != null && this.json.has("fb")) {
            res.put("fb", this.json.optJSONObject("fb"));
            res.put("idFacebook", this.json.optJSONObject("fb").optString("id"));
        }

        return res;
    }

    public Map<String, Object> getParamsForSignup() {
        Map<String, Object> res = new HashMap<String, Object>();

        res.put("firstName", this.firstname);
        res.put("lastName", this.lastname);
        res.put("nick", this.username);
        res.put("email", this.email);
        res.put("birthdate", formattedBirthdate(this.birthdate));
        res.put("phone", this.phone);
        res.put("password", this.password);
        res.put("secureCode", this.passCode);

        if (this.fullname != null)
            res.put("fullName", this.fullname);

        if (this.avatarURL != null || this.avatarData != null)
            res.put("hasImage", true);
        else
            res.put("hasImage", false);

        if (this.avatarURL != null)
            res.put("avatarURL", this.avatarURL);

        if (this.json != null && this.json.has("fb")) {
            res.put("fb", this.json.optJSONObject("fb"));
            res.put("idFacebook", this.json.optJSONObject("fb").optString("id"));
        }

        return res;
    }
}
