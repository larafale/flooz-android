package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLSocial {

    public enum SocialScope {
        SocialScopeNone, // Pour desactiver pour les cagnottes
        SocialScopePublic,
        SocialScopeFriend,
        SocialScopePrivate
    }

    public Number commentsCount;
    public Number likesCount;
    public Boolean isCommented;
    public Boolean isLiked;
    public String likeText;
    public SocialScope scope;

    public FLSocial(JSONObject json) {
        super();
        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        try {
            JSONArray comments = json.getJSONArray("comments");
            JSONArray likes = json.getJSONArray("likes");

            this.commentsCount = comments.length();
            this.likesCount = likes.length();
            this.likeText = json.getString("likesString");

            this.isCommented = false;
            this.isLiked = false;

            if (FloozRestClient.getInstance().currentUser != null) {
                for (int i = 0; i < comments.length(); i++) {
                    String userId = FloozRestClient.getInstance().currentUser.userId;
                    if (comments.getJSONObject(i).getString("userId").equals(userId)) {
                        this.isCommented = true;
                        break;
                    }
                }

                for (int i = 0; i < likes.length(); i++) {
                    if (likes.getJSONObject(i).getString("userId").equals(FloozRestClient.getInstance().currentUser.userId)) {
                        this.isLiked = true;
                        break;
                    }
                }
            }


            this.scope = socialScopeParamToEnum(json.getString("scope"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static SocialScope socialScopeParamToEnum(String param) {
        if (param.equals("0"))
            return SocialScope.SocialScopePublic;
        else if (param.equals("1"))
            return SocialScope.SocialScopeFriend;
        else if (param.equals("2"))
            return SocialScope.SocialScopePrivate;
        else
            return SocialScope.SocialScopeNone;
    }
}
