package me.flooz.app.UI.Fragment.Start;

import android.app.Fragment;

import org.json.JSONObject;

import io.branch.referral.Branch;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
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

    public static void handleStepResponse(JSONObject response, StartActivity activity) {
        if (response.has("step") && response.optJSONObject("step").has("next")) {
            if (response.optJSONObject("step").optString("next").contentEquals("signup")) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signupPassStep("signup", activity.signupData, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        JSONObject responseObject = (JSONObject) response;
                        FloozApplication.getInstance().pendingTriggers = null;
                        FloozApplication.getInstance().pendingPopup = null;
                        FloozApplication.getInstance().branchParams = null;

                        FloozRestClient.getInstance().updateCurrentUserAfterSignup(responseObject);

                        if (responseObject.has("step") && responseObject.optJSONObject("step").has("next")) {
                            StartBaseFragment nextFragment = StartBaseFragment.getSignupFragmentFromId(responseObject.optJSONObject("step").optString("next"), responseObject.optJSONObject("step"));

                            if (nextFragment != null) {
                                activity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
                            }
                        }
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            } else {
                StartBaseFragment nextFragment = StartBaseFragment.getSignupFragmentFromId(response.optJSONObject("step").optString("next"), response.optJSONObject("step"));

                if (nextFragment != null) {
                    activity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
                }
            }
        }
    }
}
