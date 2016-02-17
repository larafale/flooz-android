package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

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

    private ProgressBar progressBar;
    private WebView webView;

    public String title;
    public String url;

    public WebController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public WebController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

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
        if (this.title != null && !this.title.isEmpty())
            this.titleLabel.setText(this.title);

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
}
