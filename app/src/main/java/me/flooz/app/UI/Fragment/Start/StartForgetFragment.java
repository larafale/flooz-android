package me.flooz.app.UI.Fragment.Start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 6/12/15.
 */
public class StartForgetFragment extends StartBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_forget_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_forget_title);
        EditText emailTextfield = (EditText) view.findViewById(R.id.start_forget_email);
        Button forgetButton = (Button) view.findViewById(R.id.start_forget_next);

        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        forgetButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().passwordForget(emailTextfield.getText().toString(), new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    parentActivity.hideKeyboard();
                    parentActivity.onBackPressed();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });

        return view;
    }
}
