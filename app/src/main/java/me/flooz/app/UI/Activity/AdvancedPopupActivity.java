package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLPreset;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 10/08/16.
 */
public class AdvancedPopupActivity extends BaseActivity {

    private FloozApplication floozApp;

    private ImageView closeButton;

    private List<String> buttonsString = new ArrayList<>();
    private List<JSONArray> buttonsAction = new ArrayList<>();

    private ImageView cover;
    private TextView title;
    private TextView subtitle;
    private RoundedImageView pic;
    private TextView amount;
    private TextView content;

    private LinearLayout btnContainer1;
    private LinearLayout btnContainer2;

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;

    private View spacer1;
    private View spacer2;

    private JSONObject triggerData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (this.triggerData.has("button")) {
            buttonsString.add(this.triggerData.optString("button"));

            if (this.triggerData.has("triggers")) {
                buttonsAction.add(this.triggerData.optJSONArray("triggers"));
            } else {
                buttonsAction.add(new JSONArray());
            }
        }

        if (this.triggerData.has("buttons")) {
            for (int i = 0; i < this.triggerData.optJSONArray("buttons").length(); i++) {
                JSONObject button = this.triggerData.optJSONArray("buttons").optJSONObject(i);
                if (button.has("title")) {
                    buttonsString.add(button.optString("title"));

                    if (button.has("triggers"))
                        buttonsAction.add(button.optJSONArray("triggers"));
                    else
                        buttonsAction.add(new JSONArray());
                }
            }
        }

        if (buttonsString.size() == 0) {
            buttonsString.add(this.getResources().getString(R.string.GLOBAL_OK));

            if (this.triggerData.has("triggers")) {
                buttonsAction.add(this.triggerData.optJSONArray("triggers"));
            } else {
                buttonsAction.add(new JSONArray());
            }
        }

        this.setContentView(R.layout.advanced_popup_activity);

        this.closeButton = (ImageView) this.findViewById(R.id.dialog_trigger_close);
        this.title = (TextView) this.findViewById(R.id.dialog_trigger_title);
        this.subtitle = (TextView) this.findViewById(R.id.dialog_trigger_subtitle);
        this.cover = (ImageView) this.findViewById(R.id.dialog_trigger_cover);
        this.pic = (RoundedImageView) this.findViewById(R.id.dialog_trigger_pic);
        this.amount = (TextView) this.findViewById(R.id.dialog_trigger_amount);
        this.content = (TextView) this.findViewById(R.id.dialog_trigger_content);

        this.btnContainer1 = (LinearLayout) this.findViewById(R.id.dialog_trigger_container1);
        this.btnContainer2 = (LinearLayout) this.findViewById(R.id.dialog_trigger_container2);

        this.btn1 = (Button) this.findViewById(R.id.dialog_trigger_btn1);
        this.btn2 = (Button) this.findViewById(R.id.dialog_trigger_btn2);
        this.btn3 = (Button) this.findViewById(R.id.dialog_trigger_btn3);
        this.btn4 = (Button) this.findViewById(R.id.dialog_trigger_btn4);

        this.spacer1 = this.findViewById(R.id.dialog_trigger_spacer1);
        this.spacer2 = this.findViewById(R.id.dialog_trigger_spacer2);

        this.title.setTypeface(CustomFonts.customContentBold(this));
        this.subtitle.setTypeface(CustomFonts.customContentRegular(this));
        this.amount.setTypeface(CustomFonts.customContentBold(this));
        this.content.setTypeface(CustomFonts.customContentRegular(this));
        this.btn1.setTypeface(CustomFonts.customContentRegular(this));
        this.btn2.setTypeface(CustomFonts.customContentRegular(this));
        this.btn3.setTypeface(CustomFonts.customContentRegular(this));
        this.btn4.setTypeface(CustomFonts.customContentRegular(this));

        if (this.triggerData.has("title") && !this.triggerData.optString("title").isEmpty()) {
            title.setText(this.triggerData.optString("title"));
            title.setVisibility(View.VISIBLE);
        } else {
            title.setVisibility(View.GONE);
        }

        if (this.triggerData.has("subtitle") && !this.triggerData.optString("subtitle").isEmpty()) {
            subtitle.setText(this.triggerData.optString("subtitle"));
            subtitle.setVisibility(View.VISIBLE);
        } else {
            subtitle.setVisibility(View.GONE);
        }

        if (this.triggerData.has("cover") && !this.triggerData.optString("cover").isEmpty()) {
            ImageLoader.getInstance().displayImage(this.triggerData.optString("cover"), this.cover);
        } else {
            this.cover.setImageDrawable(this.getResources().getDrawable(R.drawable.cover));
        }

        if (this.triggerData.has("pic") && !this.triggerData.optString("pic").isEmpty()) {
            ImageLoader.getInstance().displayImage(this.triggerData.optString("pic"), this.pic);
            this.pic.setVisibility(View.VISIBLE);
        } else {
            this.pic.setVisibility(View.INVISIBLE);
        }

        if (this.triggerData.has("amount")) {
            amount.setText(FLHelper.trimTrailingZeros(String.format(Locale.US, "%.2f", this.triggerData.optDouble("amount"))));
            amount.setVisibility(View.VISIBLE);
        } else {
            amount.setVisibility(View.GONE);
        }

        if (this.triggerData.has("content")) {
            content.setText(this.triggerData.optString("content"));
            content.setVisibility(View.VISIBLE);
        } else {
            content.setVisibility(View.GONE);
        }

        if (this.triggerData.has("close") && this.triggerData.optBoolean("close")) {
            closeButton.setVisibility(View.VISIBLE);
        } else {
            closeButton.setVisibility(View.GONE);
        }

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        if (this.triggerData.has("content") && !this.triggerData.optString("content").isEmpty()) {
            content.setText(this.triggerData.optString("content"));
            content.setVisibility(View.VISIBLE);
        } else {
            content.setVisibility(View.GONE);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = 0;

                if (v == btn1)
                    index = 0;
                else if (v == btn2)
                    index = 1;
                else if (v == btn3)
                    index = 2;
                else if (v == btn4)
                    index = 3;

                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(buttonsAction.get(index)));
            }
        };

        btn1.setOnClickListener(clickListener);
        btn2.setOnClickListener(clickListener);
        btn3.setOnClickListener(clickListener);
        btn4.setOnClickListener(clickListener);

        btn1.setText(buttonsString.get(0));

        if (buttonsString.size() < 2) {
            btn2.setVisibility(View.GONE);
            spacer1.setVisibility(View.GONE);
        } else {
            btn2.setVisibility(View.VISIBLE);
            spacer1.setVisibility(View.VISIBLE);
            btn2.setText(buttonsString.get(1));
        }

        if (buttonsString.size() < 3) {
            btnContainer2.setVisibility(View.GONE);
        } else {
            btnContainer2.setVisibility(View.VISIBLE);

            btn3.setText(buttonsString.get(2));

            if (buttonsString.size() < 4) {
                btn4.setVisibility(View.GONE);
                spacer2.setVisibility(View.GONE);
            } else {
                btn4.setVisibility(View.VISIBLE);
                spacer2.setVisibility(View.VISIBLE);
                btn4.setText(buttonsString.get(3));
            }
        }

        this.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
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

    @Override
    public void onBackPressed() {
        this.closeButton.performClick();
    }

}
