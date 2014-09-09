package flooz.android.com.flooz.UI.Fragment;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

public class ProfileFragment extends Fragment
{
    private LinearLayout accountUserView;
    private RoundedImageView userView;
    private TextView fullname;
    private TextView username;
    private TextView balanceValue;
    private TextView accountWalletLabel;
    private LinearLayout accountShareButton;
    private LinearLayout accountSettingsButton;
    private LinearLayout accountBankButton;
    private LinearLayout accountIdeasButton;
    private TextView accountShareLabel;
    private TextView accountSettingsLabel;
    private TextView accountBankLabel;
    private TextView accountIdeasLabel;


    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(this.getClass().getSimpleName() ,"Receive Reload User Data Notification");
            reloadUserData();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, null);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        this.accountUserView = (LinearLayout) view.findViewById(R.id.accountUserView);
        this.userView = (RoundedImageView) view.findViewById(R.id.userView);
        this.fullname = (TextView) view.findViewById(R.id.fullname);
        this.username = (TextView) view.findViewById(R.id.username);
        this.balanceValue = (TextView) view.findViewById(R.id.balanceValue);
        this.accountWalletLabel = (TextView) view.findViewById(R.id.accountWalletLabel);
        this.accountShareButton = (LinearLayout) view.findViewById(R.id.accountShareButton);
        this.accountSettingsButton = (LinearLayout) view.findViewById(R.id.accountSettingsButton);
        this.accountBankButton = (LinearLayout) view.findViewById(R.id.accountBankButton);
        this.accountIdeasButton = (LinearLayout) view.findViewById(R.id.accountIdeasButton);
        this.accountShareLabel = (TextView) view.findViewById(R.id.accountShareLabel);
        this.accountSettingsLabel = (TextView) view.findViewById(R.id.accountSettingsLabel);
        this.accountBankLabel = (TextView) view.findViewById(R.id.accountBankLabel);
        this.accountIdeasLabel = (TextView) view.findViewById(R.id.accountIdeasLabel);

        this.fullname.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.username.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.balanceValue.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.accountWalletLabel.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.accountShareLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.accountSettingsLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.accountBankLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.accountIdeasLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.accountUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

            }
        });

        this.accountShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

            }
        });

        this.accountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

            }
        });

        this.accountBankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

            }
        });

        this.accountIdeasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

            }
        });

        return view;
    }

    private void reloadUserData()
    {
        FLUser user = FloozRestClient.getInstance().currentUser;

        this.fullname.setText(user.fullname);
        this.balanceValue.setText("+ " + user.amount + " €");
        this.username.setText("@" + user.username);

        if (user.avatarURL != null)
            Picasso.with(getActivity()).load(user.avatarURL).into(this.userView);
        else
            this.userView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onDestroy()
    {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
        super.onDestroy();
    }
}
