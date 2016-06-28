package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLSocial {

    public enum SocialScope {
        SocialScopeNone,
        SocialScopePublic,
        SocialScopeFriend,
        SocialScopePrivate
    }

    public Number commentsCount;
    public Number likesCount;
    public Boolean isCommented;
    public Boolean isLiked;
    public String likeText;
    public String commentText;
    public SocialScope scope;
    public ArrayList<FLUser> likes;
    public ArrayList<FLComment> comments;

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
            this.likeText = json.optString("likesString");
            this.commentText = json.optString("commentsString");

            this.isCommented = false;
            this.isLiked = false;

            this.comments = new ArrayList<>();
            this.likes = new ArrayList<>();

            if (FloozRestClient.getInstance().currentUser != null) {
                for (int i = 0; i < comments.length(); i++) {

                    this.comments.add(new FLComment(comments.optJSONObject(i)));

                    String userId = FloozRestClient.getInstance().currentUser.userId;
                    if (comments.optJSONObject(i).optString("userId").equals(userId)) {
                        this.isCommented = true;
                        break;
                    }
                }

                for (int i = 0; i < likes.length(); i++) {
                    FLUser tmp = new FLUser(likes.optJSONObject(i));
                    tmp.userId = likes.optJSONObject(i).optString("userId");

                    this.likes.add(tmp);

                    if (tmp.userId.equals(FloozRestClient.getInstance().currentUser.userId)) {
                        this.isLiked = true;
                        break;
                    }
                }
            }

            this.scope = socialScopeParamToEnum(json.optString("scope"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static SocialScope socialScopeParamToEnum(String param) {
        switch (param) {
            case "0":
                return SocialScope.SocialScopePublic;
            case "1":
                return SocialScope.SocialScopeFriend;
            case "2":
                return SocialScope.SocialScopePrivate;
            default:
                return SocialScope.SocialScopeNone;
        }
    }
}
