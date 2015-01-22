package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/5/14.
 */
public class InvitationCodeActivity extends Activity {

    public FloozApplication floozApp;

    private String phoneNumber;

    private ImageView backButton;
    private TextView headerTitle;
    private TextView text;
    private TextView learn;
    private EditText codeTextfield;
    private Button joinButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.invitation_code_activity);
        floozApp = (FloozApplication)this.getApplicationContext();

        this.phoneNumber = getIntent().getStringExtra("phone");
        if (this.phoneNumber.startsWith("+33"))
            this.phoneNumber = this.phoneNumber.replace("+33", "0");

        this.backButton = (ImageView) this.findViewById(R.id.invitation_code_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.invitation_code_header_title);
        this.text = (TextView) this.findViewById(R.id.invitation_code_text);
        this.codeTextfield = (EditText) this.findViewById(R.id.invitation_code_textfield);
        this.joinButton = (Button) this.findViewById(R.id.invitation_code_join);
        this.learn = (TextView) this.findViewById(R.id.invitation_code_learn);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.text.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.learn.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.flooz.me/about/invitation"));
                startActivity(browserIntent);
            }
        });

        this.codeTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0)
                    joinButton.setEnabled(true);
                else
                    joinButton.setEnabled(false);
            }
        });

        this.codeTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    joinButton.performClick();
                }
                return false;
            }
        });

        this.joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().verifyInvitationCode(codeTextfield.getText().toString(), phoneNumber, null);
            }
        });

        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(floozApp, SignupActivity.class);

                intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupPhone.ordinal());
                intent.putExtra("phone", phoneNumber);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    protected void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        this.backButton.performClick();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

}
