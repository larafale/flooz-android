package me.flooz.app.UI.Fragment.Start;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 6/11/15.
 */
public class StartLoginFragment extends StartBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_login_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_login_title);
        LinearLayout fbButton = (LinearLayout) view.findViewById(R.id.start_login_facebook);
        TextView orLabel = (TextView) view.findViewById(R.id.start_login_or);
        final EditText phoneTextfield = (EditText) view.findViewById(R.id.start_login_phone);
        final EditText passwordTextfield = (EditText) view.findViewById(R.id.start_login_password);
        Button loginButton = (Button) view.findViewById(R.id.start_login_next);
        Button forgetButton = (Button) view.findViewById(R.id.start_login_forget);
        ImageView facebookPicto = (ImageView) view.findViewById(R.id.start_login_facebook_picto);

        phoneTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        passwordTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        orLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        forgetButton.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        phoneTextfield.getText().clear();
        passwordTextfield.getText().clear();

        facebookPicto.setColorFilter(inflater.getContext().getResources().getColor(android.R.color.white));

        phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 10) {
                    s.delete(10, s.length());
                    passwordTextfield.requestFocus();
                }
            }
        });

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().connectFacebook();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.hideKeyboard();
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().loginWithPseudoAndPassword(phoneTextfield.getText().toString(), passwordTextfield.getText().toString(), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.floozApp.didConnected();
                        parentActivity.floozApp.displayMainView();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        forgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.changeCurrentPage(new StartForgetFragment(), android.R.animator.fade_in, android.R.animator.fade_out, true);
            }
        });

        return view;
    }
}