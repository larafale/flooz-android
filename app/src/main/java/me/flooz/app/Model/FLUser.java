package me.flooz.app.Model;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.JSONHelper;

/**
 * Created by Flooz on 9/8/14.
 */
public class FLUser
{
    public String userId;
    public Number amount;
    public String coupon;
    public String firstname;
    public String lastname;
    public String fullname;
    public String username;
    public String password;
    public String birthdate;
    public String email;
    public String phone;
    public String avatarURL;
    public String avatarURLFull;
    public String coverURL;
    public String coverURLFull;
    public String userBio;
    public String website;
    public String location;
    public Bitmap avatarData = null;
    public String profileCompletion;
    public Number friendsCount;
    public Boolean haveStatsPending;
    public String passCode;
    public String smsCode;
    public String distinctId;
    public List<String> actions;
    public Boolean isStar;
    public Boolean isCertified;
    public Boolean isPro;

    public Boolean isComplete;
    public Boolean isFriendable;
    public Boolean isFriend;

    public Boolean isIdentified;
    public Boolean isFloozer;

    public Boolean isCactus;
    public Boolean isPot;

    public Number totalParticipations;
    public Number countParticipations;
    public JSONArray participations;


    public JSONObject floozOptions;
    public Map<String, String> address;
    public Map<String, Object> sepa;
    public Map<String, Object> notifications;
    public Map<String, Object> notificationsText;
    public Map<String, Object> checkDocuments;
    public JSONObject ux;
    public FLCreditCard creditCard;

    public List<FLUser> friends = new ArrayList<>(0);
    public List<FLUser> friendsRecent = new ArrayList<>(0);
    public List<FLUser> friendsRequest = new ArrayList<>(0);

    public List<FLUser> followers = new ArrayList<>(0);
    public List<FLUser> followings = new ArrayList<>(0);

    public Boolean isFriendWaiting;

    public FLCountry country;

    public Number record;
    public String device;
    public Map<String, Object> settings;
    public String inviteCode;
    public String hasSecureCode;
    public JSONObject json;

    public UserKind userKind;
    public FLUserSelectedCanal selectedCanal;
    public PublicMetrics publicMetrics = new PublicMetrics();

    public JSONObject metrics;

    public class PublicMetrics {
        public int nbFlooz;
        public int nbFriends;
        public int nbFollowers;
        public int nbFollowings;
        public int nbCollects;
    }

    public enum UserKind {
        FloozUser,
        PhoneUser,
        CactusUser
    }

    public enum FLUserSelectedCanal {
        RecentCanal,
        SuggestionCanal,
        FriendsCanal,
        ContactCanal,
        SearchCanal,
        TimelineCanal,
        PendingCanal
    }

    public FLUser(JSONObject data)
    {
        super();
        this.userKind = UserKind.FloozUser;
        this.isIdentified = true;
        this.isFloozer = true;
        this.setJson(data);
    }

    public FLUser()
    {
        super();
        this.userKind = UserKind.PhoneUser;

        this.isIdentified = false;
        this.isFloozer = false;
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
            this.website = this.json.optString("website", "");
            this.location = this.json.optString("location", "");
            this.avatarURL = this.json.optString("pic");
            this.profileCompletion = this.json.optString("profileCompletion");
            this.hasSecureCode = this.json.optString("secureCode");
            this.coverURL = this.json.optString("cover");
            this.coverURLFull = this.json.optString("coverFull");
            this.userBio = this.json.optString("bio", "");
            this.avatarURLFull = this.json.optString("picFull");
            this.isStar = this.json.optBoolean("isStar");
            this.isPro = this.json.optBoolean("isPro");
            this.isComplete = this.json.optBoolean("isComplete");
            this.isPot = this.json.optBoolean("isPot");
            this.isFriend = this.json.optBoolean("isFriend");

            this.isCactus = this.json.optBoolean("isCactus");
            this.isCertified = this.json.optBoolean("isCertified");

            if (this.json.has("isFriendable"))
                isFriendable = this.json.optBoolean("isFriendable");
            else
                isFriendable = true;

            if (this.json.has("actions")) {
                try {
                    this.actions = JSONHelper.toList(this.json.getJSONArray("actions"));
                } catch (Exception e) {
                    Log.d("JSON FAILURE", "Cannot retrieve actions");
                    this.actions = null;
                }
            }

            if (this.json.has("participations")) {
                this.totalParticipations = json.optJSONObject("participations").optDouble("amount");
                this.countParticipations = json.optJSONObject("participations").optDouble("count");
                this.participations = json.optJSONObject("participations").optJSONArray("list");
            }

            if (this.json.has("publicMetrics")) {
                Map<String, Object> pubMetrics = JSONHelper.toMap(this.json.getJSONObject("publicMetrics"));
                if (pubMetrics.containsKey("flooz"))
                    this.publicMetrics.nbFlooz = (int)pubMetrics.get("flooz");
                if (pubMetrics.containsKey("friends"))
                    this.publicMetrics.nbFriends = (int)pubMetrics.get("friends");
                if (pubMetrics.containsKey("followers"))
                    this.publicMetrics.nbFollowers = (int)pubMetrics.get("followers");
                if (pubMetrics.containsKey("followings"))
                    this.publicMetrics.nbFollowings = (int)pubMetrics.get("followings");
                if (pubMetrics.containsKey("pots"))
                    this.publicMetrics.nbCollects = (int)pubMetrics.get("pots");
            }

            if (this.json.has("floozOptions"))
                this.floozOptions = this.json.optJSONObject("floozOptions");

            if (this.json.has("birthdate")) {
                String array[]  = this.json.optString("birthdate").split("-");
                this.birthdate = String.format("%s/%s/%s", array[2].substring(0, 2), array[1], array[0]);
            }

            if (this.avatarURL.equals("/img/nopic.png")) {
                this.avatarURL = null;
                this.avatarURLFull = null;
            }

            if (this.coverURL.equals("/img/nocover.png")) {
                this.coverURL = null;
                this.coverURLFull = null;
            }

            if (this.json.has("device"))
                this.device = this.json.getString("device");

            if (this.json.has("friends"))
                this.friendsCount = this.json.getJSONArray("friends").length();

            this.metrics = this.json.optJSONObject("metrics");

            this.haveStatsPending = false;

            if (this.json.has("settings")) {
                this.settings = JSONHelper.toMap(this.json.getJSONObject("settings"));

                if (this.json.has("settings") && this.json.getJSONObject("settings").has("address")) {
                    this.address = JSONHelper.toMapOfString(this.json.getJSONObject("settings").getJSONObject("address"));
                }

                if (this.json.has("settings") && this.json.getJSONObject("settings").has("sepa")) {
                    this.sepa = JSONHelper.toMap(this.json.getJSONObject("settings").getJSONObject("sepa"));
                }

                this.country = FLCountry.countryFromIndicatif(this.json.getJSONObject("settings").getString("indicatif"));
            }

            if (this.json.has("ux")) {
                this.ux = this.json.getJSONObject("ux");
            }

            this.notifications = new HashMap<>();

            if (this.json.has("notifications"))
                this.notifications = JSONHelper.toMap(this.json.getJSONObject("notifications"));

            this.notificationsText = new HashMap<>();

            if (this.json.has("notificationsText"))
                this.notificationsText = JSONHelper.toMap(this.json.getJSONObject("notificationsText"));

            if (this.json.has("friends")) {
                this.friends = new ArrayList<>(0);
                JSONArray arrayFriends = this.json.getJSONArray("friends");

                for (int i = 0; i < arrayFriends.length(); i++) {
                    friends.add(new FLUser(arrayFriends.getJSONObject(i)));
                }
            }

            if (this.json.has("followers")) {
                this.followers = new ArrayList<>(0);
                JSONArray arrayFollowers = this.json.getJSONArray("followers");

                for (int i = 0; i < arrayFollowers.length(); i++) {
                    followers.add(new FLUser(arrayFollowers.getJSONObject(i)));
                }
            }

            if (this.json.has("followings")) {
                this.followings = new ArrayList<>(0);
                JSONArray arrayFollowings = this.json.getJSONArray("followings");

                for (int i = 0; i < arrayFollowings.length(); i++) {
                    followings.add(new FLUser(arrayFollowings.getJSONObject(i)));
                }
            }

            if (this.json.has("recentFriends")) {
                this.friendsRecent = new ArrayList<>(0);
                JSONArray arrayFriendsRecent = this.json.getJSONArray("recentFriends");

                for (int i = 0; i < arrayFriendsRecent.length(); i++) {
                    friendsRecent.add(new FLUser(arrayFriendsRecent.getJSONObject(i)));
                }
            }

            if (this.json.has("friendsRequest")) {
                this.friendsRequest = new ArrayList<>(0);
                JSONArray arrayFriendsRequest = this.json.getJSONArray("friendsRequest");

                for (int i = 0; i < arrayFriendsRequest.length(); i++) {
                    friendsRequest.add(new FLUser(arrayFriendsRequest.getJSONObject(i)));
                }
            }

            this.checkDocuments = new HashMap<>();

            if (this.json.has("check"))
                this.checkDocuments = JSONHelper.toMap(this.json.getJSONObject("check"));

            this.isFriendWaiting = this.json.has("state") && this.json.getInt("state") == 1;

            if (this.json.has("record"))
                this.record = this.json.getInt("record");

            if (this.json.has("settings") && this.json.getJSONObject("settings").has("device"))
                this.device = this.json.getJSONObject("settings").getString("device");

            if (this.json.has("invitation") && this.json.getJSONObject("invitation").has("code"))
                this.inviteCode = this.json.getJSONObject("invitation").getString("code");

            if (this.json.has("card"))
                this.creditCard = new FLCreditCard(this.json.getJSONObject("card"));
            else
                this.creditCard = null;

            if (this.json.has("cactus") || (this.json.has("isCactus") && this.json.optBoolean("isCactus"))) {
                this.username = null;
                this.userKind = UserKind.CactusUser;
            }

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

        if (tmp.length == 3)
            return tmp[2] + "-" + tmp[1] + "-" + tmp[0];
        else
            return birthdate;
    }

    public static String formattedBirthdateFromFacebook(String birthdate) {
        if (birthdate != null) {
            String[] tmp = birthdate.split("/");

            if (tmp.length == 3)
                return tmp[1] + "/" + tmp[0] + "/" + tmp[2];
            else
                return birthdate;
        } else
            return "";
    }

    public String getSelectedCanal() {
        if (this.selectedCanal != null) {
            switch (this.selectedCanal) {
                case RecentCanal:
                    return "recent";
                case SuggestionCanal:
                    return "suggestion";
                case FriendsCanal:
                    return "friends";
                case SearchCanal:
                    return "search";
                case TimelineCanal:
                    return "timeline";
                case ContactCanal:
                    return "contact";
                case PendingCanal:
                    return "pending";
                default:
                    return "";
            }
        }
        return "";
    }

    public Map<String, Object> getParamsForStep(Boolean last) {
        Map<String, Object> res = new HashMap<>();

        if (this.firstname != null)
            res.put("firstName", this.firstname);

        if (this.lastname != null)
            res.put("lastName", this.lastname);

        if (this.username != null)
            res.put("nick", this.username);

        if (this.email != null)
            res.put("email", this.email);

        if (this.birthdate != null)
            res.put("birthdate", formattedBirthdate(this.birthdate));

        if (this.phone != null)
            res.put("phone", this.phone);

        if (this.password != null)
            res.put("password", this.password);

        if (this.fullname != null)
            res.put("fullName", this.fullname);

        if (this.smsCode != null)
            res.put("smscode", this.smsCode);

        if (this.distinctId != null)
            res.put("distinctId", this.distinctId);

        if (this.coupon != null)
            res.put("coupon", this.coupon);

        if (this.avatarURL != null || this.avatarData != null)
            res.put("hasImage", true);
        else
            res.put("hasImage", false);

        if (this.avatarURL != null)
            res.put("avatarURL", this.avatarURL);

        if (this.passCode != null)
            res.put("secureCode", this.passCode);

        if (this.json != null && this.json.has("fb")) {
            Map<String, Object> fbData = new HashMap<>();

            fbData.put("email", this.json.optJSONObject("fb").optString("email"));
            fbData.put("id", this.json.optJSONObject("fb").optString("id"));
            fbData.put("name", this.json.optJSONObject("fb").optString("name"));
            fbData.put("token", this.json.optJSONObject("fb").optString("token"));

            res.put("fb", fbData);
            res.put("idFacebook", this.json.optJSONObject("fb").optString("id"));
        }

        if (!last)
            res.put("validate", true);

        return res;
    }

    public Boolean isFriend() {
        Boolean res = false;

        if (FloozRestClient.getInstance().currentUser.userId.contentEquals(this.userId)) {
            res = true;
        }
        else {
            for (int i = 0; i < FloozRestClient.getInstance().currentUser.friends.size(); i++) {
                if (this.userId.contentEquals(FloozRestClient.getInstance().currentUser.friends.get(i).userId)) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    public Boolean isFriendRequest() {
        Boolean res = false;

        if (FloozRestClient.getInstance().currentUser.userId.contentEquals(this.userId)) {
            res = false;
        }
        else {
            for (int i = 0; i < FloozRestClient.getInstance().currentUser.friendsRequest.size(); i++) {
                if (this.userId.contentEquals(FloozRestClient.getInstance().currentUser.friendsRequest.get(i).userId)) {
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        if (userKind == UserKind.FloozUser) {
            if (fullname != null && !fullname.isEmpty()) {
                return fullname;
            } else {
                return username;
            }
        } else if (userKind == UserKind.PhoneUser) {
            return fullname;
        } else
            return phone;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        FLUser other = (FLUser ) obj;

        if (this.userId == null)
            return false;
        if (other.userId == null)
            return false;

        return this.userId.contentEquals(other.userId);
    }
}
