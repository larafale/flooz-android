package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLShopItem;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.Settings.SecuritySettingsActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 30/08/16.
 */
public class ShopItemActivity extends BaseActivity {
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView titleTextview;
    private ImageView headerShareButton;
    private RoundedImageView pic;
    private TextView amountTextview;
    private TextView descriptionTextview;
    private TextView tosTextview;
    private Button buyButton;

    private JSONObject triggerData;

    private FLShopItem currentItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null) {
            if (getIntent().hasExtra("triggerData")) {
                try {
                    this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));

                    this.currentItem = new FLShopItem(this.triggerData.optJSONObject("item"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (getIntent().hasExtra("item")) {
                try {
                    this.currentItem = new FLShopItem(new JSONObject(getIntent().getStringExtra("item")));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        this.setContentView(R.layout.shop_item_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.titleTextview = (TextView) this.findViewById(R.id.header_title);
        this.headerShareButton = (ImageView) this.findViewById(R.id.header_item_right);
        this.pic = (RoundedImageView) this.findViewById(R.id.shop_item_pic);
        this.amountTextview = (TextView) this.findViewById(R.id.shop_item_amount);
        this.descriptionTextview = (TextView) this.findViewById(R.id.shop_item_desc);
        this.tosTextview = (TextView) this.findViewById(R.id.shop_item_tos);
        this.buyButton = (Button) this.findViewById(R.id.shop_item_buy_button);

        this.titleTextview.setTypeface(CustomFonts.customTitleLight(this));
        this.amountTextview.setTypeface(CustomFonts.customContentBold(this));
        this.descriptionTextview.setTypeface(CustomFonts.customContentRegular(this));
        this.tosTextview.setTypeface(CustomFonts.customContentLight(this));
        this.buyButton.setTypeface(CustomFonts.customContentRegular(this));

        this.headerShareButton.setColorFilter(this.getResources().getColor(R.color.blue));

        if (this.currentItem.shareUrl == null || this.currentItem.shareUrl.isEmpty())
            this.headerShareButton.setVisibility(View.GONE);
        else
            this.headerShareButton.setVisibility(View.VISIBLE);

        this.titleTextview.setText(this.currentItem.name);

        ImageLoader.getInstance().displayImage(this.currentItem.pic, this.pic);

        if (this.currentItem.value == null || this.currentItem.value.isEmpty())
            this.amountTextview.setVisibility(View.GONE);
        else {
            this.amountTextview.setText(this.currentItem.value);
            this.amountTextview.setVisibility(View.VISIBLE);
        }

        this.descriptionTextview.setText(this.currentItem.description);

        if (this.currentItem.tosString == null || this.currentItem.tosString.isEmpty())
            this.tosTextview.setVisibility(View.GONE);
        else {
            SpannableString content = new SpannableString("Conditions d'utilisation");
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

            this.tosTextview.setText(content);
            this.tosTextview.setVisibility(View.VISIBLE);
        }

        if (this.triggerData != null) {
            if (triggerData.has("close") && !triggerData.optBoolean("close")) {
                this.headerBackButton.setVisibility(View.GONE);
            }
        }

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (triggerData != null && triggerData.has("close") && !triggerData.optBoolean("close"))
                    return;

                setResult(RESULT_OK, getIntent());
                ShopItemActivity.this.finish();
                ShopItemActivity.this.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.headerShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");

                share.putExtra(Intent.EXTRA_TEXT, currentItem.shareUrl);

                ShopItemActivity.this.startActivity(Intent.createChooser(share, null));
            }
        });

        this.tosTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(ShopItemActivity.this, WebContentActivity.class);
                webIntent.putExtra("title", currentItem.name);
                webIntent.putExtra("url", currentItem.tosString);

                ShopItemActivity.this.startActivity(webIntent);
                ShopItemActivity.this.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(currentItem.purchaseTriggers);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
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

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
