package flooz.android.com.flooz.UI.Fragment.Home;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import org.json.JSONArray;
import org.json.JSONObject;


import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/15/14.
 */
public class InviteFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;

    private ImageView worldmap;
    private TextView h1;
    private TextView content;
    private LinearLayout smsButton;
    private LinearLayout fbButton;
    private LinearLayout twitterButton;
    private LinearLayout mailButton;
    private TextView smsText;
    private TextView fbText;
    private TextView twitterText;
    private TextView mailText;
    private ImageView smsImage;
    private ImageView fbImage;
    private ImageView twitterImage;
    private ImageView mailImage;
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

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invite_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.invite_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.invite_header_title);

        this.worldmap = (ImageView) view.findViewById(R.id.invite_worldmap);
        this.h1 = (TextView) view.findViewById(R.id.invite_h1);
        this.content = (TextView) view.findViewById(R.id.invite_content);
        this.smsButton = (LinearLayout) view.findViewById(R.id.invite_sms);
        this.fbButton = (LinearLayout) view.findViewById(R.id.invite_fb);
        this.twitterButton = (LinearLayout) view.findViewById(R.id.invite_twitter);
        this.mailButton = (LinearLayout) view.findViewById(R.id.invite_mail);
        this.smsText = (TextView) view.findViewById(R.id.invite_sms_text);
        this.fbText = (TextView) view.findViewById(R.id.invite_fb_text);
        this.twitterText = (TextView) view.findViewById(R.id.invite_twitter_text);
        this.mailText = (TextView) view.findViewById(R.id.invite_mail_text);
        this.smsImage = (ImageView) view.findViewById(R.id.invite_sms_image);
        this.fbImage = (ImageView) view.findViewById(R.id.invite_fb_image);
        this.twitterImage = (ImageView) view.findViewById(R.id.invite_twitter_image);
        this.mailImage = (ImageView) view.findViewById(R.id.invite_mail_image);
        this.tipContainer = (ToolTipLayout) view.findViewById(R.id.invite_tooltip_container);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.h1.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.content.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.smsText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.fbText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.twitterText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.mailText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.worldmap.setColorFilter(inflater.getContext().getResources().getColor(R.color.blue));
        this.smsImage.setColorFilter(inflater.getContext().getResources().getColor(R.color.blue));
        this.fbImage.setColorFilter(inflater.getContext().getResources().getColor(R.color.blue));
        this.twitterImage.setColorFilter(inflater.getContext().getResources().getColor(R.color.blue));
        this.mailImage.setColorFilter(inflater.getContext().getResources().getColor(R.color.blue));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
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
                } else {
                    View contentTooltip = inflater.inflate(R.layout.invite_tooltip_code, null);

                    contentTooltip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ClipboardManager _clipboard = (ClipboardManager) parentActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            _clipboard.setPrimaryClip(ClipData.newPlainText("FloozCode", _code));
                            tipContainer.dismiss();
                            toolTip = null;
                        }
                    });

                    toolTip = new ToolTip.Builder(inflater.getContext())
                            .anchor(content)
                            .gravity(Gravity.TOP)
                            .color(inflater.getContext().getResources().getColor(R.color.black_alpha))
                            .pointerSize(25)
                            .contentView(contentTooltip)
                            .build();

                    tipContainer.addTooltip(toolTip, true);
                }
            }
        });

        FloozRestClient.getInstance().invitationStrings(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseData = ((JSONObject)response).optJSONObject("item");

                _code = responseData.optString("code");
                _appText = responseData.optJSONArray("text");
                _fbData = responseData.optJSONObject("facebook");
                _mailData = responseData.optJSONObject("mail");
                _twitterText = responseData.optString("twitter");
                _smsText = responseData.optString("sms");
                _viewTitle = responseData.optString("title");
                _h1 = responseData.optString("h1");

                if (_code == null || _code.isEmpty())
                    _code = "";

                headerTitle.setText(_viewTitle);
                h1.setText(_h1);

                Spannable text = new SpannableString(_appText.optString(0));
                text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Spannable code = new SpannableString(_code);
                code.setSpan(new ForegroundColorSpan(inflater.getContext().getResources().getColor(R.color.blue)), 0, code.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

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

        this.smsButton.setOnClickListener(new View.OnClickListener() {
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

        this.mailButton.setOnClickListener(new View.OnClickListener() {
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

        return view;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}