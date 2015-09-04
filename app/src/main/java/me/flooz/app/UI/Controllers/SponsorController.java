package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/2/15.
 */
public class SponsorController extends BaseController {

    private ImageView headerBackButton;
    private EditText sponsorTextfield;
    private Button saveButton;
    
    public SponsorController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.sponsorTextfield = (EditText) this.currentView.findViewById(R.id.sponsor_field);
        this.saveButton = (Button) this.currentView.findViewById(R.id.sponsor_save);
        TextView infos = (TextView) this.currentView.findViewById(R.id.sponsor_infos);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        infos.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sponsorTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        headerTitle.setText(FloozRestClient.getInstance().currentTexts.menu.optJSONObject("promo").optString("title"));
        this.sponsorTextfield.setHint(FloozRestClient.getInstance().currentTexts.menu.optJSONObject("promo").optString("placeholder"));
        infos.setText(FloozRestClient.getInstance().currentTexts.menu.optJSONObject("promo").optString("info"));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(view -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity) this.parentActivity).popFragmentInCurrentTab();
            }
        });

        this.sponsorTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                saveButton.performClick();
            }
            return false;
        });

        this.saveButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();

            FloozRestClient.getInstance().sendDiscountCode(sponsorTextfield.getText().toString(), new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    headerBackButton.performClick();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
