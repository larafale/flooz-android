package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.flooz.app.Adapter.TimelineListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 9/17/14.
 */
public class StartActivity extends Activity {

    public FloozApplication floozApp;
    private StartActivity instance;

    private Button couponButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        setContentView(R.layout.start_activity);
        floozApp = (FloozApplication)this.getApplicationContext();
        instance = this;

        Button loginButton = (Button) this.findViewById(R.id.start_login_button);
        Button signupButton = (Button) this.findViewById(R.id.start_signup_button);
        this.couponButton = (Button) this.findViewById(R.id.start_coupon_button);

        loginButton.setTypeface(CustomFonts.customTitleLight(this));
        signupButton.setTypeface(CustomFonts.customTitleLight(this), Typeface.BOLD);
        this.couponButton.setTypeface(CustomFonts.customTitleLight(this), Typeface.BOLD);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLogin(null);
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLogin(null);
            }
        });

        this.couponButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLogin(FloozRestClient.getInstance().currentTexts.couponButton.optString("coupon"));
            }
        });

        this.findViewById(R.id.start_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, SliderActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

        FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (FloozRestClient.getInstance().currentTexts != null) {
                    if (!FloozRestClient.getInstance().appSettings.getBoolean("IntroSliders", false)) {
                        Intent intent = new Intent(instance, SliderActivity.class);
                        instance.startActivity(intent);
                        instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    }

                    if (FloozRestClient.getInstance().currentTexts.couponButton != null) {
                        couponButton.setText(FloozRestClient.getInstance().currentTexts.couponButton.optString("text"));
                        couponButton.getBackground().setColorFilter(Color.parseColor(FloozRestClient.getInstance().currentTexts.couponButton.optString("bgcolor")), PorterDuff.Mode.ADD);
                        couponButton.setVisibility(View.VISIBLE);
                    } else {
                        couponButton.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    private void launchLogin(String coupon) {
        Intent intent = new Intent(this, SignupActivity.class);

        if (coupon != null)
            intent.putExtra("coupon", coupon);

        intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupPhone.ordinal());

        this.startActivity(intent);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}