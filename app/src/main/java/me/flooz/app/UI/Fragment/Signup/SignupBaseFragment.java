package me.flooz.app.UI.Fragment.Signup;

import android.app.Fragment;

import org.json.JSONObject;

import me.flooz.app.UI.Activity.SignupActivity;

/**
 * Created by Flooz on 11/7/14.
 */
public class SignupBaseFragment extends Fragment {

    public SignupActivity parentActivity;

    public SignupActivity.SignupPageIdentifier pageId;

    protected JSONObject initData;

    public void refreshView() {

    }

    public void setInitData(JSONObject data) {
        this.initData = data;
    }
}
