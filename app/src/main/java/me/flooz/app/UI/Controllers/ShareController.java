package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
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

/**
 * Created by Flooz on 8/25/15.
 */
public class ShareController extends BaseController {

    private Boolean pendingShare = false;
    private CallbackManager fbShareCallbackManager;
    private CallbackManager fbPublishCallbackManager;

    private FloozApplication floozApp;
    private ImageView headerBackButton;
    private TextView headerTitle;

    private TextView h1;
    private TextView content;
    private ToolTip toolTip;
    private ToolTipLayout tipContainer;

    private LinearLayout mailButton;
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

    public ShareController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull BaseController.ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) currentView.findViewById(R.id.header_item_left);
        this.headerTitle = (TextView) currentView.findViewById(R.id.header_title);

        this.h1 = (TextView) currentView.findViewById(R.id.invite_h1);
        this.content = (TextView) currentView.findViewById(R.id.invite_content);
        LinearLayout smsButton = (LinearLayout) currentView.findViewById(R.id.invite_sms);
        this.mailButton = (LinearLayout) currentView.findViewById(R.id.invite_mail);
        this.fbButton = (LinearLayout) currentView.findViewById(R.id.invite_fb);
        TextView smsText = (TextView) currentView.findViewById(R.id.invite_sms_text);
        TextView fbText = (TextView) currentView.findViewById(R.id.invite_fb_text);
        TextView twitterText = (TextView) currentView.findViewById(R.id.invite_twitter_text);
        TextView mailText = (TextView) currentView.findViewById(R.id.invite_mail_text);
        ImageView smsImage = (ImageView) currentView.findViewById(R.id.invite_sms_image);
        ImageView fbImage = (ImageView) currentView.findViewById(R.id.invite_fb_image);
        ImageView twitterImage = (ImageView) currentView.findViewById(R.id.invite_twitter_image);
        ImageView mailImage = (ImageView) currentView.findViewById(R.id.invite_mail_image);
        this.tipContainer = (ToolTipLayout) currentView.findViewById(R.id.invite_tooltip_container);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.h1.setTypeface(CustomFonts.customContentBold(this.parentActivity));
        this.content.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        smsText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        fbText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        twitterText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        mailText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        smsImage.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));
        fbImage.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));
        twitterImage.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));
        mailImage.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));

        if (this.currentKind == BaseController.ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setVisibility(View.GONE);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
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
                    View contentTooltip = parentActivity.getLayoutInflater().inflate(R.layout.invite_tooltip_code, null);

                    contentTooltip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager _clipboard = (ClipboardManager) parentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            _clipboard.setPrimaryClip(ClipData.newPlainText("FloozCode", _code));
                            tipContainer.dismiss();
                            toolTip = null;
                        }
                    });

                    toolTip = new ToolTip.Builder(parentActivity)
                            .anchor(content)
                            .gravity(Gravity.TOP)
                            .color(parentActivity.getResources().getColor(R.color.black_alpha))
                            .pointerSize(25)
                            .contentView(contentTooltip)
                            .build();

                    tipContainer.addTooltip(toolTip, true);
                }
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

        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", _smsText);
                if (sendIntent.resolveActivity(parentActivity.getPackageManager()) != null) {
                    parentActivity.startActivity(sendIntent);
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
                if (sendIntent.resolveActivity(parentActivity.getPackageManager()) != null) {
                    parentActivity.startActivity(Intent.createChooser(sendIntent, "Partager par mail..."));
                }
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
        code.setSpan(new ForegroundColorSpan(parentActivity.getResources().getColor(R.color.blue)), 0, code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Spannable text2 = new SpannableString(_appText.optString(1));
        text2.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        content.setText(text);
        content.append(code);
        content.append(text2);

        if (_fbData == null || _fbData.length() == 0)
            this.fbButton.setVisibility(View.GONE);

        if (_mailData == null || _mailData.length() == 0)
            this.mailButton.setVisibility(View.GONE);
    }

    private void shareFacebook() {
        if (_fbData.optString("method").contentEquals("widget") && ShareDialog.canShow(ShareLinkContent.class)) {
            fbShareCallbackManager = CallbackManager.Factory.create();
            ShareDialog shareDialog = new ShareDialog(parentActivity);

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

                        }

                        @Override
                        public void onError(FacebookException exception) {

                        }
                    });

            LoginManager.getInstance().logInWithPublishPermissions(floozApp.getCurrentActivity(), Arrays.asList("publish_actions"));
        }
    }

    public void showCustomFbSharePopup() {
        final Dialog fbDialog = new Dialog(parentActivity);

        fbDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fbDialog.setContentView(R.layout.custom_dialog_share);

        TextView text = (TextView) fbDialog.findViewById(R.id.dialog_share_text);
        text.setText(_fbData.optString("title"));
        text.setTypeface(CustomFonts.customContentRegular(parentActivity));

        final EditText textArea = (EditText) fbDialog.findViewById(R.id.dialog_share_field);
        textArea.setHint(_fbData.optString("placeholder"));
        textArea.setTypeface(CustomFonts.customContentRegular(parentActivity));

        Button decline = (Button) fbDialog.findViewById(R.id.dialog_share_decline);
        Button accept = (Button) fbDialog.findViewById(R.id.dialog_share_accept);

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fbDialog != null)
                    fbDialog.dismiss();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().invitationFacebook(textArea.getText().toString(), null);

                if (fbDialog != null)
                    fbDialog.dismiss();
            }
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

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(facebookConnected,
                CustomNotificationIntents.filterFacebookConnect());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(facebookConnected);
    }

    @Override
    public void onBackPressed() {
        if (this.currentKind == ControllerKind.ACTIVITY_CONTROLLER)
            this.headerBackButton.performClick();
    }
}
