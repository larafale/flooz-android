package flooz.android.com.flooz.UI.Fragment.Home;

import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/8/14.
 */
public class CustomWebViewFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;    private WebView webView;
    private ProgressBar progressBar;

    public String title;
    public String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.custom_webview_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.custom_webview_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.custom_webview_header_title);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.headerTitle.setText(this.title);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        this.progressBar = (ProgressBar) view.findViewById(R.id.custom_webview_progress_bar);

        this.webView = (WebView) view.findViewById(R.id.custom_webview_webview);
        this.webView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        this.webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                }
                else if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    startActivity(Intent.createChooser(i, "Envoyé un mail..."));
                }
                else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }

        };

        this.webView.setWebViewClient(client);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.loadUrl(this.url);

        return view;
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
