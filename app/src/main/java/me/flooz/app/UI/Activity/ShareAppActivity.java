package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLShareText;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class ShareAppActivity extends Activity {

    private Boolean pendingShare = false;
    private CallbackManager fbShareCallbackManager;
    private CallbackManager fbPublishCallbackManager;

    private ShareAppActivity instance;
    private FloozApplication floozApp;
    private ImageView headerBackButton;
    private TextView headerTitle;

    private TextView h1;
    private TextView content;
    private ToolTip toolTip;
    private ToolTipLayout tipContainer;

    private LinearLayout fbButton;

    private String _code;
    private JSONArray _appText;
    private JSONObject _fbData;
    private JSONObject _mailData;
    private String _twitterText;
    private String _smsText;
    private String _h1;
    private String _viewTitle;

    private BroadcastReceiver facebookConnected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            internalFbShareCheckPublishPermissions();
        }
    };

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

        this.h1 = (TextView) this.findViewById(R.id.invite_h1);
        this.content = (TextView) this.findViewById(R.id.invite_content);
        LinearLayout smsButton = (LinearLayout) this.findViewById(R.id.invite_sms);
        LinearLayout mailButton = (LinearLayout) this.findViewById(R.id.invite_mail);
        this.fbButton = (LinearLayout) this.findViewById(R.id.invite_fb);
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

        smsImage.setColorFilter(this.getResources().getColor(R.color.blue));
        fbImage.setColorFilter(this.getResources().getColor(R.color.blue));
        twitterImage.setColorFilter(this.getResources().getColor(R.color.blue));
        mailImage.setColorFilter(this.getResources().getColor(R.color.blue));

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        });

        this.tipContainer.dismiss();
        this.toolTip = null;

        this.content.setOnClickListener(v -> {

            if (toolTip != null) {
                tipContainer.dismiss();
                toolTip = null;
            } else if (_code != null && !_code.isEmpty() && !_code.replace(" ", "").isEmpty()) {
                View contentTooltip = getLayoutInflater().inflate(R.layout.invite_tooltip_code, null);

                contentTooltip.setOnClickListener(v1 -> {
                    ClipboardManager _clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    _clipboard.setPrimaryClip(ClipData.newPlainText("FloozCode", _code));
                    tipContainer.dismiss();
                    toolTip = null;
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
        });

        if (FloozRestClient.getInstance().currentShareText == null) {
            FloozRestClient.getInstance().getInvitationText(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLShareText texts = FloozRestClient.getInstance().currentShareText;

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

                    reloadView();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        } else {
            FLShareText texts = FloozRestClient.getInstance().currentShareText;

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

            reloadView();
        }

        smsButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:"));
            sendIntent.putExtra("sms_body", _smsText);
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(sendIntent);
            }
        });

        mailButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(Uri.parse("mailto:"));
            sendIntent.putExtra(Intent.EXTRA_TEXT, _mailData.optString("content"));
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, _mailData.optString("title"));
            if (sendIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sendIntent, "Partager par mail..."));
            }
        });

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloozRestClient.getInstance().isConnectedToFacebook())
                    shareFacebook();
                else
                    FloozRestClient.getInstance().connectFacebook();
            }
        });
    }

    private void reloadView() {
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

        if (_fbData == null || _fbData.length() == 0)
            this.fbButton.setVisibility(View.GONE);
    }

    private void shareFacebook() {
        if (_fbData.optString("method").contentEquals("widget") && ShareDialog.canShow(ShareLinkContent.class)) {
            fbShareCallbackManager = CallbackManager.Factory.create();
            ShareDialog shareDialog = new ShareDialog(this);

            shareDialog.registerCallback(fbShareCallbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    FloozRestClient.getInstance().sendInvitationMetric("facebook");
                    pendingShare = false;
                }

                @Override
                public void onCancel() {
                    pendingShare = false;
                }

                @Override
                public void onError(FacebookException e) {
                    pendingShare = false;
                }
            });

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(_fbData.optString("name"))
                    .setContentDescription(_fbData.optString("description"))
                    .setContentUrl(Uri.parse(_fbData.optString("link")))
                    .build();

            shareDialog.show(linkContent);
            pendingShare = true;
        } else {
            if (FloozRestClient.getInstance().isConnectedToFacebook()) {
                this.internalFbShareCheckPublishPermissions();
            } else {
                FloozRestClient.getInstance().connectFacebook();
            }
        }
    }

    public void internalFbShareCheckPublishPermissions() {
        if (AccessToken.getCurrentAccessToken().getPermissions().contains("publish_actions")) {
            showCustomFbSharePopup();
        } else {
            fbPublishCallbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(fbPublishCallbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            showCustomFbSharePopup();
                        }

                        @Override
                        public void onCancel() {
                            // App code
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            // App code
                        }
                    });

            LoginManager.getInstance().logInWithPublishPermissions(floozApp.getCurrentActivity(), Arrays.asList("publish_actions"));
        }
    }

    public void showCustomFbSharePopup() {
        final Dialog fbDialog = new Dialog(this);

        fbDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fbDialog.setContentView(R.layout.custom_dialog_share);

        TextView text = (TextView) fbDialog.findViewById(R.id.dialog_share_text);
        text.setText(_fbData.optString("title"));
        text.setTypeface(CustomFonts.customContentRegular(this));

        EditText textArea = (EditText) fbDialog.findViewById(R.id.dialog_share_field);
        textArea.setHint(_fbData.optString("placeholder"));
        textArea.setTypeface(CustomFonts.customContentRegular(this));

        Button decline = (Button) fbDialog.findViewById(R.id.dialog_share_decline);
        Button accept = (Button) fbDialog.findViewById(R.id.dialog_share_accept);

        decline.setOnClickListener(v -> {
            if (fbDialog != null)
                fbDialog.dismiss();
        });

        accept.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().invitationFacebook(textArea.getText().toString(), null);

            if (fbDialog != null)
                fbDialog.dismiss();
        });

        fbDialog.setCanceledOnTouchOutside(true);
        fbDialog.setCancelable(true);
        fbDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (pendingShare) {
            fbShareCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            if (FloozRestClient.getInstance().isConnectedToFacebook()) {
                fbPublishCallbackManager.onActivityResult(requestCode, resultCode, data);
            } else {
                FloozRestClient.getInstance().fbLoginCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        FloozRestClient.getInstance().updateNotificationFeed(null);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(facebookConnected,
                CustomNotificationIntents.filterFacebookConnect());
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(facebookConnected);
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
