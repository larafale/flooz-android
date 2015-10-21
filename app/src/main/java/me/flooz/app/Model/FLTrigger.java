package me.flooz.app.Model;

import org.json.JSONObject;

import me.flooz.app.UI.Activity.HomeActivity;

import static me.flooz.app.Model.FLTrigger.FLTriggerType.*;

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
        TriggerShowSignup,
        TriggerLogout,
        TriggerAppUpdate,
        TriggerShowContactInfo,
        TriggerShowUserDocuments,
        TriggerShow3DSecure,
        TriggerComplete3DSecure,
        TriggerFail3DSecure,
        TriggerSecureCodeClear,
        TriggerSecureCodeCheck,
        TriggerPresetLine,
        TriggerFeedRead,
        TriggerShowInvitation,
        TriggerHttpCall,
        TriggerShowPopup,
        TriggerShowHome,
        TriggerShowIban,
        TriggerResetTuto,
        TriggerCloseView,
        TriggerSendContacts,
        TriggerUserShow,
        TriggerInvitationSMSShow,
        TriggerSMSValidate,
        TriggerSecureCodeValidate,
        TriggerEditProfile
    }

    public FLTriggerType type;
    public JSONObject data;
    public Number delay;
    public Class handlerClass;

    public FLTrigger(JSONObject json) {
        super();

        this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.type = triggerTypeParamToEnum(json.optString("type"));
        this.delay = json.optDouble("delay", 0);
        this.handlerClass = this.getHandlerClass();
        this.data = json;
    }

    private Class getHandlerClass() {
        switch (this.type) {
            case TriggerShowLine:
                return HomeActivity.class;
            case TriggerShowAvatar:
                return HomeActivity.class;
            case TriggerShowFriend:
                return HomeActivity.class;
            default:
                return null;
        }
    }

    public static FLTriggerType triggerTypeParamToEnum(String param) {
        if (param == null || param.isEmpty())
            return TriggerNone;

        if (param.contentEquals("timeline:reload"))
            return TriggerReloadTimeline;
        else if (param.contentEquals("line:show"))
            return TriggerShowLine;
        else if (param.contentEquals("avatar:show"))
            return TriggerShowAvatar;
        else if (param.contentEquals("profile:reload"))
            return TriggerReloadProfile;
        else if (param.contentEquals("card:show"))
            return TriggerShowCard;
        else if (param.contentEquals("friend:show"))
            return TriggerShowFriend;
        else if (param.contentEquals("friend:reload"))
            return TriggerReloadFriend;
        else if (param.contentEquals("line:reload"))
            return TriggerReloadLine;
        else if (param.contentEquals("profile:show"))
            return TriggerShowProfile;
        else if (param.contentEquals("signup:show"))
            return TriggerShowSignup;
        else if (param.contentEquals("logout"))
            return TriggerLogout;
        else if (param.contentEquals("app:update"))
            return TriggerAppUpdate;
        else if (param.contentEquals("contactInfo:show"))
            return TriggerShowContactInfo;
        else if (param.contentEquals("documents:show"))
            return TriggerShowUserDocuments;
        else if (param.contentEquals("3dSecure:show"))
            return TriggerShow3DSecure;
        else if (param.contentEquals("3dSecure:complete"))
            return TriggerComplete3DSecure;
        else if (param.contentEquals("3dSecure:fail"))
            return TriggerFail3DSecure;
        else if (param.contentEquals("secureCode:clear"))
            return TriggerSecureCodeClear;
        else if (param.contentEquals("secureCode:check"))
            return TriggerSecureCodeCheck;
        else if (param.contentEquals("line:preset"))
            return TriggerPresetLine;
        else if (param.contentEquals("feed:read"))
            return TriggerFeedRead;
        else if (param.contentEquals("invitation:show"))
            return TriggerShowInvitation;
        else if (param.contentEquals("popup:show"))
            return TriggerShowPopup;
        else if (param.contentEquals("home:show"))
            return TriggerShowHome;
        else if (param.contentEquals("iban:show"))
            return TriggerShowIban;
        else if (param.contentEquals("http:call"))
            return TriggerHttpCall;
        else if (param.contentEquals("view:close"))
            return TriggerCloseView;
        else if (param.contentEquals("contacts:send"))
            return TriggerSendContacts;
        else if (param.contentEquals("user:show"))
            return TriggerUserShow;
        else if (param.contentEquals("invitation:sms:show"))
            return TriggerInvitationSMSShow;
        else if (param.contentEquals("profile:edit"))
            return TriggerEditProfile;
        else if (param.contentEquals("phone:validate"))
            return TriggerSMSValidate;
        else if (param.contentEquals("secureCode:set"))
            return TriggerSecureCodeValidate;
        else
            return TriggerNone;
    }
}
