package me.flooz.app.Model;
import android.graphics.drawable.Drawable;
import android.media.Image;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by gawenberger on 07/04/2017.
 */

public class FLScope {

    public enum FLScopeKey {
        FLScopePublic,
        FLScopeFriend,
        FLScopePrivate,
        FLScopeAll,
        FLScopeNone
    }

    public JSONObject jsonData;
    public FLScopeKey key;
    public String keyString;
    public String name;
    public String desc;
    public String shortDesc;
    public String imageURL;
    public Drawable image;
    public String imageName;

    public FLScope(JSONObject json) {
        super();

        this.setJson(json);
    }

    public void setJson(JSONObject json) {
        this.jsonData = json;
        if (this.jsonData != null) {
            if (this.jsonData.has("key"))
                this.keyString = this.jsonData.optString("key");
            this.key = FLScope.scopeKeyFromString(this.keyString);
            if (this.jsonData.has("name"))
                this.name = this.jsonData.optString("name");
            if (this.jsonData.has("desc"))
                this.desc = this.jsonData.optString("desc");
            if (this.jsonData.has("shortDesc"))
                this.shortDesc = this.jsonData.optString("shortDesc");
            String imageURL = this.jsonData.optString("imageURL");
            String imageName = this.jsonData.optString("imageName");
            if (imageURL != null && imageURL.length() > 0) {
//            [[SDWebImageDownloader sharedDownloader] downloadImageWithURL:[NSURL URLWithString:jsonData[@"imageURL"]] options:SDWebImageDownloaderHighPriority progress:nil completed:^(UIImage * _Nullable image, NSData * _Nullable data, NSError * _Nullable error, BOOL finished) {
//                    if (image)
//                        self.image = image;
//                }];
            }
            if (imageName != null && imageName.length() > 0) {
                this.image = FloozApplication.getInstance().getResources()
                        .getDrawable(FLHelper.getResourceId("drawable", imageName, FloozApplication.getInstance().getPackageName()));
            } else {
                int resID = -1;

                switch (this.key) {
                    case FLScopePublic:
                        resID = R.drawable.scope_public;
                        break;
                    case FLScopeFriend:
                        resID = R.drawable.scope_friend;
                        break;
                    case FLScopePrivate:
                        resID = R.drawable.scope_private;
                        break;
                    case FLScopeAll:
                        resID = R.drawable.scope_public;
                        break;
                    default:
                        break;
                }
                this.image = FloozApplication.getAppContext().getResources().getDrawable(resID);
            }
        }
    }

    public static FLScope defaultScope(FLScopeKey scopeKey) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", FLScope.textFromKey(scopeKey));
            json.put("desc", FLScope.descFromKey(scopeKey, false));
            json.put("shortDesc", FLScope.shortDescFromKey(scopeKey));

            switch (scopeKey) {
                case FLScopeFriend:
                    json.put("key", "friend");
                    return FLScope.scopeFromJSON(json);
                case FLScopePrivate:
                    json.put("key", "private");
                    return FLScope.scopeFromJSON(json);
                case FLScopePublic:
                    json.put("key", "public");
                    return FLScope.scopeFromJSON(json);
                case FLScopeAll:
                    json.put("key", "all");
                    return FLScope.scopeFromJSON(json);
                default:
                    return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<FLScope> defaultScopeList() {
        List<FLScope> defaultScopeList = new ArrayList<FLScope>();
        defaultScopeList.add(FLScope.defaultScope(FLScopeKey.FLScopePublic));
        defaultScopeList.add(FLScope.defaultScope(FLScopeKey.FLScopeFriend));
        defaultScopeList.add(FLScope.defaultScope(FLScopeKey.FLScopePrivate));
        return defaultScopeList;
    }

    public static FLScope scopeFromKey(String scopeKey) {
        if (FloozRestClient.getInstance().currentTexts != null &&
                FloozRestClient.getInstance().currentTexts.homeScopes != null &&
                FloozRestClient.getInstance().currentTexts.homeScopes.size() != 0) {
            for (FLScope scope : FloozRestClient.getInstance().currentTexts.homeScopes) {
                if (scopeKey == scope.keyString)
                    return scope;
            }
        }
        return FLScope.defaultScope(FLScope.scopeKeyFromString(scopeKey));
    }

    public static FLScopeKey scopeKeyFromString(String keyString) {
        if (keyString != null) {
            if (keyString.equals("public"))
                return FLScopeKey.FLScopePublic;
            if (keyString.equals("friend"))
                return FLScopeKey.FLScopeFriend;
            if (keyString.equals("private"))
                return FLScopeKey.FLScopePrivate;
            if (keyString.equals("all"))
                return FLScopeKey.FLScopeAll;
        }
        return FLScopeKey.FLScopeNone;
    }

    public static FLScope scopeFromID(Integer scopeID) {
        return FLScope.defaultScope(FLScope.scopeKeyFromID(scopeID));
    }

    public static FLScopeKey scopeKeyFromID(Integer scopeID) {
        if (scopeID != null) {
            if (scopeID == 0)
                return FLScopeKey.FLScopePublic;
            if (scopeID == 1)
                return FLScopeKey.FLScopeFriend;
            if (scopeID == 2)
                return FLScopeKey.FLScopePrivate;
            if (scopeID == 3)
                return FLScopeKey.FLScopeAll;
        }
        return FLScopeKey.FLScopeNone;
    }

    public static FLScope scopeFromObject(Object object) {
        if (object instanceof Integer)
            return FLScope.scopeFromID((Integer)object);
        else if (object instanceof String)
            return FLScope.scopeFromKey((String)object);
        else if (object instanceof JSONObject)
            return FLScope.scopeFromJSON((JSONObject)object);
        return null;
    }

    public static FLScope scopeFromJSON(JSONObject json) {
        return new FLScope(json);
    }

    public static String textFromKey(FLScopeKey scopeKey) {
        int resId;
        switch (scopeKey) {
            case FLScopeFriend:
                resId = R.string.TRANSACTION_SCOPE_FRIEND;
                break;
            case FLScopePrivate:
                resId = R.string.TRANSACTION_SCOPE_PRIVATE;
                break;
            case FLScopePublic:
                resId = R.string.TRANSACTION_SCOPE_PUBLIC;
                break;
            default:
                return "";
        }
        return FloozApplication.getInstance().getString(resId);
    }

    public static String shortDescFromKey(FLScopeKey scopeKey) {
        int resId;
        switch (scopeKey) {
            case FLScopeFriend:
                resId = R.string.TRANSACTION_SCOPE_SHORT_DESC_FRIEND;
                break;
            case FLScopePrivate:
                resId = R.string.TRANSACTION_SCOPE_SHORT_DESC_PRIVATE;
                break;
            case FLScopePublic:
                resId = R.string.TRANSACTION_SCOPE_SHORT_DESC_PUBLIC;
                break;
            default:
                return "";
        }
        return FloozApplication.getInstance().getString(resId);
    }

    public static String descFromKey(FLScopeKey scopeKey, Boolean isPot) {
        int resId;
        switch (scopeKey) {
            case FLScopeFriend:
                resId = isPot ? R.string.TRANSACTION_SCOPE_SUB_POT_FRIEND : R.string.TRANSACTION_SCOPE_SUB_FRIEND;
                break;
            case FLScopePrivate:
                resId = isPot ? R.string.TRANSACTION_SCOPE_SUB_POT_PRIVATE : R.string.TRANSACTION_SCOPE_SUB_PRIVATE;
                break;
            case FLScopePublic:
                resId = isPot ? R.string.TRANSACTION_SCOPE_SUB_POT_PUBLIC : R.string.TRANSACTION_SCOPE_SUB_PUBLIC;
                break;
            default:
                return "";
        }
        return FloozApplication.getInstance().getString(resId);
    }

}

