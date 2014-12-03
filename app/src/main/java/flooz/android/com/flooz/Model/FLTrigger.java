package flooz.android.com.flooz.Model;

import org.json.JSONObject;

import static flooz.android.com.flooz.Model.FLTrigger.FLTriggerType.*;

/**
 * Created by Flooz on 11/6/14.
 */
public class FLTrigger {

    public enum FLTriggerType {
        TriggerNone,
        TriggerReloadTimeline,
        TriggerShowLine,
        TriggerShowAvatar,
        TriggerReloadProfile,
        TriggerShowCard,
        TriggerReloadFriend,
        TriggerShowProfile,
        TriggerShowFriend,
        TriggerReloadLine,
        TriggerShowLogin,
        TriggerShowSignup,
        TriggerShowSignupCode,
        TriggerLogout,
        TriggerAppUpdate,
        TriggerShowContactInfo,
        TriggerShowUserIdentity,
        TriggerShow3DSecure,
        TriggerComplete3DSecure,
        TriggerResetPassword,
        TriggerFail3DSecure
    }

    public FLTriggerType type;
    public JSONObject data;
    public Number delay;

    public FLTrigger(JSONObject json) {
        super();

        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.type = triggerTypeParamToEnum(json.optString("type"));
        this.delay = json.optDouble("delay");

        if (this.delay == null)
            this.delay = 0;

        json.remove("type");
        json.remove("delay");

        this.data = json;
    }

    public static FLTriggerType triggerTypeParamToEnum(String param) {
        if (param.equals("timeline:reload"))
            return TriggerReloadTimeline;
        else if (param.equals("line:show"))
            return TriggerShowLine;
        else if (param.equals("avatar:show"))
            return TriggerShowAvatar;
        else if (param.equals("profile:reload"))
            return TriggerReloadProfile;
        else if (param.equals("card:show"))
            return TriggerShowCard;
        else if (param.equals("friend:show"))
            return TriggerShowFriend;
        else if (param.equals("friend:reload"))
            return TriggerReloadFriend;
        else if (param.equals("line:reload"))
            return TriggerReloadLine;
        else if (param.equals("profile:show"))
            return TriggerShowProfile;
        else if (param.equals("signup:show"))
            return TriggerShowSignup;
        else if (param.equals("login:show"))
            return TriggerShowLogin;
        else if (param.equals("signup:invitation"))
            return TriggerShowSignupCode;
        else if (param.equals("logout"))
            return TriggerLogout;
        else if (param.equals("app:update"))
            return TriggerAppUpdate;
        else if (param.equals("contactInfo:show"))
            return TriggerShowContactInfo;
        else if (param.equals("identity:show"))
            return TriggerShowUserIdentity;
        else if (param.equals("3dSecure:show"))
            return TriggerShow3DSecure;
        else if (param.equals("3dSecure:complete"))
            return TriggerComplete3DSecure;
        else if (param.equals("password:change"))
            return TriggerResetPassword;
        else if (param.equals("3dSecure:fail"))
            return TriggerFail3DSecure;
        else
            return TriggerNone;
    }
}
