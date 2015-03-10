package me.flooz.app.UI.Fragment.Signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import me.flooz.app.R;

/**
 * Created by Flooz on 11/18/14.
 */
public class SignupCGUFragment extends SignupBaseFragment {

    private WebView webView;
    private ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_cgu_fragment, null);

        progressBar = (ProgressBar) view.findViewById(R.id.signup_cgu_progress_bar);

        webView = (WebView) view.findViewById(R.id.signup_cgu_webview);
        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        };

        webView.setWebViewClient(client);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.flooz.me/cgu?layout=webview");

        return view;
    }
}