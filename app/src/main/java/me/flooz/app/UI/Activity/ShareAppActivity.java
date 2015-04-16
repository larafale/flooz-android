package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTexts;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class ShareAppActivity extends Activity {

    private ShareAppActivity instance;
    private FloozApplication floozApp;
    private ImageView headerBackButton;
    private TextView headerTitle;

    private TextView h1;
    private TextView content;
    private ToolTip toolTip;
    private ToolTipLayout tipContainer;

    private String _code;
    private JSONArray _appText;
    private JSONObject _fbData;
    private JSONObject _mailData;
    private String _twitterText;
    private String _smsText;
    private String _h1;
    private String _viewTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        instance = this;
        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.invite_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.invite_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.invite_header_title);

        ImageView worldmap = (ImageView) this.findViewById(R.id.invite_worldmap);
        this.h1 = (TextView) this.findViewById(R.id.invite_h1);
        this.content = (TextView) this.findViewById(R.id.invite_content);
        LinearLayout smsButton = (LinearLayout) this.findViewById(R.id.invite_sms);
        LinearLayout fbButton = (LinearLayout) this.findViewById(R.id.invite_fb);
        LinearLayout twitterButton = (LinearLayout) this.findViewById(R.id.invite_twitter);
        LinearLayout mailButton = (LinearLayout) this.findViewById(R.id.invite_mail);
        TextView smsText = (TextView) this.findViewById(R.id.invite_sms_text);
        TextView fbText = (TextView) this.findViewById(R.id.invite_fb_text);
        TextView twitterText = (TextView) this.findViewById(R.id.invite_twitter_text);
        TextView mailText = (TextView) this.findViewById(R.id.invite_mail_text);
        ImageView smsImage = (ImageView) this.findViewById(R.id.invite_sms_image);
        ImageView fbImage = (ImageView) this.findViewById(R.id.invite_fb_image);
        ImageView twitterImage = (ImageView) this.findViewById(R.id.invite_twitter_image);
        ImageView mailImage = (ImageView) this.findViewById(R.id.invite_mail_image);
        this.tipContainer = (ToolTipLayout) this.findViewById(R.id.invite_tooltip_container);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.h1.setTypeface(CustomFonts.customContentBold(this));
        this.content.setTypeface(CustomFonts.customContentRegular(this));
        smsText.setTypeface(CustomFonts.customTitleExtraLight(this));
        fbText.setTypeface(CustomFonts.customTitleExtraLight(this));
        twitterText.setTypeface(CustomFonts.customTitleExtraLight(this));
        mailText.setTypeface(CustomFonts.customTitleExtraLight(this));

        worldmap.setColorFilter(this.getResources().getColor(R.color.blue));
        smsImage.setColorFilter(this.getResources().getColor(R.color.blue));
        fbImage.setColorFilter(this.getResources().getColor(R.color.blue));
        twitterImage.setColorFilter(this.getResources().getColor(R.color.blue));
        mailImage.setColorFilter(this.getResources().getColor(R.color.blue));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.tipContainer.dismiss();
        this.toolTip = null;

        this.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (toolTip != null) {
                    tipContainer.dismiss();
                    toolTip = null;
                } else if (_code != null && !_code.isEmpty() && !_code.replace(" ", "").isEmpty()) {
                    View contentTooltip = getLayoutInflater().inflate(R.layout.invite_tooltip_code, null);

                    contentTooltip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager _clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            _clipboard.setPrimaryClip(ClipData.newPlainText("FloozCode", _code));
                            tipContainer.dismiss();
                            toolTip = null;
                        }
                    });

                    toolTip = new ToolTip.Builder(instance)
                            .anchor(content)
                            .gravity(Gravity.TOP)
                            .color(instance.getResources().getColor(R.color.black_alpha))
                            .pointerSize(25)
                            .contentView(contentTooltip)
                            .build();

                    tipContainer.addTooltip(toolTip, true);
                }
            }
        });

        if (FloozRestClient.getInstance().currentTexts == null) {
            FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLTexts texts = FloozRestClient.getInstance().currentTexts;

                    _code = texts.shareCode;
                    _appText = texts.shareText;
                    _fbData = texts.shareFb;
                    _mailData = texts.shareMail;
                    _twitterText = texts.shareTwitter;
                    _smsText = texts.shareSms;
                    _viewTitle = texts.shareTitle;
                    _h1 = texts.shareHeader;

                    if (_code == null || _code.isEmpty())
                        _code = "";

                    headerTitle.setText(_viewTitle);
                    h1.setText(_h1);

                    Spannable text = new SpannableString(_appText.optString(0));
                    text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    Spannable code = new SpannableString(_code);
                    code.setSpan(new ForegroundColorSpan(instance.getResources().getColor(R.color.blue)), 0, code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    Spannable text2 = new SpannableString(_appText.optString(1));
                    text2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    content.setText(text);
                    content.append(code);
                    content.append(text2);

                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        } else {
            FLTexts texts = FloozRestClient.getInstance().currentTexts;

            _code = texts.shareCode;
            _appText = texts.shareText;
            _fbData = texts.shareFb;
            _mailData = texts.shareMail;
            _twitterText = texts.shareTwitter;
            _smsText = texts.shareSms;
            _viewTitle = texts.shareTitle;
            _h1 = texts.shareHeader;

            if (_code == null || _code.isEmpty())
                _code = "";

            headerTitle.setText(_viewTitle);
            h1.setText(_h1);

            Spannable text = new SpannableString(_appText.optString(0));
            text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            Spannable code = new SpannableString(_code);
            code.setSpan(new ForegroundColorSpan(instance.getResources().getColor(R.color.blue)), 0, code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            Spannable text2 = new SpannableString(_appText.optString(1));
            text2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            content.setText(text);
            content.append(code);
            content.append(text2);
        }

        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", _smsText);
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
            }
        });

        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(Uri.parse("mailto:"));
                sendIntent.putExtra(Intent.EXTRA_TEXT, _mailData.optString("content"));
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, _mailData.optString("title"));
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(sendIntent, "Partager par mail..."));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        FloozRestClient.getInstance().updateNotificationFeed(null);
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
        this.headerBackButton.performClick();
    }
}
