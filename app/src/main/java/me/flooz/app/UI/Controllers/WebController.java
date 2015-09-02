package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 9/1/15.
 */
public class WebController extends BaseController {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ProgressBar progressBar;
    private WebView webView;

    public String title;
    public String url;

    public WebController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        this.headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(view -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity)this.parentActivity).popFragmentInCurrentTab();
            }
        });

        this.progressBar = (ProgressBar) this.currentView.findViewById(R.id.custom_webview_progress_bar);

        this.webView = (WebView) this.currentView.findViewById(R.id.custom_webview_webview);
        webView.setBackgroundColor(this.parentActivity.getResources().getColor(android.R.color.transparent));
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    parentActivity.startActivity(intent);
                }
                else if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    parentActivity.startActivity(Intent.createChooser(i, "Envoyer un mail..."));
                }
                else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }

        };

        webView.setWebViewClient(client);
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    public void onResume() {
        headerTitle.setText(this.title);
        webView.loadUrl(this.url);
    }

    public static Intent newEmailIntent(String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        return intent;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
