package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

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

    private EditText sponsorTextfield;
    private Button saveButton;

    public SponsorController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public SponsorController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        if (this.titleLabel.getText().length() == 0)
            this.titleLabel.setText(FloozRestClient.getInstance().currentTexts.menu.optJSONObject("promo").optString("title"));

        this.sponsorTextfield = (EditText) this.currentView.findViewById(R.id.sponsor_field);
        this.saveButton = (Button) this.currentView.findViewById(R.id.sponsor_save);
        TextView infos = (TextView) this.currentView.findViewById(R.id.sponsor_infos);

        infos.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sponsorTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.sponsorTextfield.setHint(FloozRestClient.getInstance().currentTexts.menu.optJSONObject("promo").optString("placeholder"));
        infos.setText(FloozRestClient.getInstance().currentTexts.menu.optJSONObject("promo").optString("info"));

        if (this.triggersData != null) {
            if (this.triggersData.has("placeholder") && !this.triggersData.optString("placeholder").isEmpty()) {
                this.sponsorTextfield.setHint(this.triggersData.optString("placeholder"));
            }

            if (this.triggersData.has("info") && !this.triggersData.optString("info").isEmpty()) {
                infos.setText(this.triggersData.optString("info"));
            }

            if (this.triggersData.has("button") && !this.triggersData.optString("button").isEmpty()) {
                this.saveButton.setText(this.triggersData.optString("button"));
            }
        }

        this.sponsorTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    saveButton.performClick();
                }
                return false;
            }
        });

        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();

                FloozRestClient.getInstance().sendDiscountCode(sponsorTextfield.getText().toString(), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        SponsorController.this.onBackPressed();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }
}
