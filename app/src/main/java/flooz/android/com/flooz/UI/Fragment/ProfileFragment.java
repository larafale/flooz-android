package flooz.android.com.flooz.UI.Fragment;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, null);

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }
}
