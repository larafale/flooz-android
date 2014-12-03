package flooz.android.com.flooz.UI.Fragment.Signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.SignupActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/7/14.
 */
public class SignupAccueil1Fragment extends SignupBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_accueil1_fragment, null);

        TextView title1 = (TextView)view.findViewById(R.id.signup_accueil_1_title_1);
        TextView title2 = (TextView)view.findViewById(R.id.signup_accueil_1_title_2);
        TextView title3 = (TextView)view.findViewById(R.id.signup_accueil_1_title_3);

        TextView subtitle1 = (TextView)view.findViewById(R.id.signup_accueil_1_subtitle_1);
        TextView subtitle2 = (TextView)view.findViewById(R.id.signup_accueil_1_subtitle_2);
        TextView subtitle3 = (TextView)view.findViewById(R.id.signup_accueil_1_subtitle_3);

        Button button = (Button)view.findViewById(R.id.signup_accueil_1_button);

        title1.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        title2.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        title3.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        subtitle1.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        subtitle2.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        subtitle3.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        button.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.gotToNextPage();
            }
        });

        return view;
    }
}
