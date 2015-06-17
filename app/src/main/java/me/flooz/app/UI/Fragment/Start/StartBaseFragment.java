package me.flooz.app.UI.Fragment.Start;

import android.app.Fragment;

import org.json.JSONObject;

import me.flooz.app.UI.Activity.StartActivity;

/**
 * Created by Flooz on 6/10/15.
 */
public class StartBaseFragment extends Fragment {

    public StartActivity parentActivity;

    protected JSONObject initData;

    public void refreshView() {

    }

    public void setInitData(JSONObject data) {
        this.initData = data;
    }

    public static StartBaseFragment getSignupFragmentFromId(String id, JSONObject data) {
        StartBaseFragment ret = null;

        if (id.contentEquals("sms"))
            ret = new StartSMSFragment();
        else if (id.contentEquals("card"))
            ret = new StartCardFragment();
        else if (id.contentEquals("invitation"))
            ret = new StartInvitationFragment();
        else if (id.contentEquals("secureCode"))
            ret = new StartSecureCodeFragment();

        if (ret != null && data != null && data.length() > 0)
            ret.setInitData(data);

        return ret;
    }
}
