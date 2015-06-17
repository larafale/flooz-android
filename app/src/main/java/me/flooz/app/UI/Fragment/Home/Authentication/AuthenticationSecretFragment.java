package me.flooz.app.UI.Fragment.Home.Authentication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.AutoResizeEditText;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 5/13/15.
 */
public class AuthenticationSecretFragment extends AuthenticationBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authentication_secret_fragment, null);

        this.parentActivity.showHeader();

        TextView text = (TextView) view.findViewById(R.id.authentication_secret_text);
        TextView question = (TextView) view.findViewById(R.id.authentication_secret_question);
        AutoResizeEditText answer = (AutoResizeEditText) view.findViewById(R.id.authentication_secret_answer);
        EditText pass = (EditText) view.findViewById(R.id.authentication_secret_password);
        EditText confirm = (EditText) view.findViewById(R.id.authentication_secret_confirm);
        Button next = (Button) view.findViewById(R.id.authentication_secret_next);
        TextView forget = (TextView) view.findViewById(R.id.authentication_secret_forget);

        text.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        question.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        answer.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        pass.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        confirm.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        forget.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        answer.setEnabled(true);
        answer.setFocusableInTouchMode(true);
        answer.setFocusable(true);
        answer.setEnableSizeCache(false);
        answer.setMovementMethod(null);

        question.setText((String) ((Map) FloozRestClient.getInstance().currentUser.settings.get("secret")).get("question"));

        next.setOnClickListener(v -> {
            Map<String, Object> param = new HashMap<String, Object>();

            param.put("phone", FloozRestClient.getInstance().currentUser.phone);
            param.put("newPassword", pass.getText().toString());
            param.put("confirm", confirm.getText().toString());
            param.put("secretAnswer", answer.getText().toString());

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().updateUserPassword(param, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    parentActivity.gotToNextPage();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });

        forget.setOnClickListener(v -> {
            String[] TO = {"support@flooz.me"};

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(Uri.parse("mailto:"));
            sendIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "J'ai oublié mon mot de passe et la réponse à ma question secrète. Je souhaite être contacté au " + FloozRestClient.getInstance().currentUser.phone + ".");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Mot de passe oublié");
            if (sendIntent.resolveActivity(parentActivity.getPackageManager()) != null) {
                parentActivity.startActivity(Intent.createChooser(sendIntent, "Envoyer un mail..."));
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        this.parentActivity.backToPreviousPage();
    }
}
