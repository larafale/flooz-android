package flooz.android.com.flooz.UI.Fragment.Signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.SignupActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/2/14.
 */
public class Signup3DSecureFragment extends SignupBaseFragment {

    private WebView webView;

    public String data = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_3d_secure_fragment, null);

        ((TextView)view.findViewById(R.id.signup_3ds_header_title)).setTypeface(CustomFonts.customTitleLight(parentActivity));

        view.findViewById(R.id.signup_3ds_header_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().abort3DSecure();
            }
        });

        webView = (WebView) view.findViewById(R.id.signup_3ds_webview);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setWebViewClient(new Callback());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(null, this.data, "text/html", "utf-8", null);

        return view;
    }

    public void dismiss() {
        parentActivity.showHeader();
        parentActivity.changeCurrentPage(SignupActivity.SignupPageIdentifier.SignupCard, android.R.animator.fade_in, R.animator.slide_down);
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }

}
