package me.flooz.app.UI.Fragment.Signup;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/14/14.
 */
public class SignupImageFragment extends SignupBaseFragment {

    private RoundedImageView imageView;
    private Button nextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_image_fragment, null);

        this.imageView = (RoundedImageView)view.findViewById(R.id.signup_image_image);
        TextView infoLabel = (TextView) view.findViewById(R.id.signup_image_info_label);
        TextView fbButtonText = (TextView) view.findViewById(R.id.signup_image_fbButton_text);
        TextView takeButtonText = (TextView) view.findViewById(R.id.signup_image_takeButton_text);
        TextView chooseButtonText = (TextView) view.findViewById(R.id.signup_image_chooseButton_text);
        RelativeLayout fbButton = (RelativeLayout) view.findViewById(R.id.signup_image_fbButton);
        RelativeLayout takeButton = (RelativeLayout) view.findViewById(R.id.signup_image_takeButton);
        RelativeLayout chooseButton = (RelativeLayout) view.findViewById(R.id.signup_image_chooseButton);
        this.nextButton = (Button) view.findViewById(R.id.signup_image_next);

        infoLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        fbButtonText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        takeButtonText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        chooseButtonText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().connectFacebook();
            }
        });

        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.takeImageFromCam();
            }
        });

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.takeImageFromGallery();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setEnabled(false);
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signupPassStep("image", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        JSONObject responseJSON = (JSONObject) response;

                        parentActivity.gotToNextPage(responseJSON.optString("nextStep"), responseJSON.optJSONObject("nextStepData"));
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        nextButton.setEnabled(true);
                    }
                });
            }
        });

        if (parentActivity.userData.avatarURL != null)
            ImageLoader.getInstance().displayImage(parentActivity.userData.avatarURL, this.imageView);
        else if (parentActivity.userData.avatarData != null) {
            int nh = (int) ( parentActivity.userData.avatarData.getHeight() * (256.0 / parentActivity.userData.avatarData.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(parentActivity.userData.avatarData, 256, nh, true);

            this.imageView.setImageBitmap(scaled);
        } else
            this.imageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));

        return view;
    }

    @Override
    public void refreshView() {
        if (parentActivity.userData.avatarURL != null)
            ImageLoader.getInstance().displayImage(parentActivity.userData.avatarURL, this.imageView);
        else if (parentActivity.userData.avatarData != null) {
            int nh = (int) (parentActivity.userData.avatarData.getHeight() * (512.0 / parentActivity.userData.avatarData.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(parentActivity.userData.avatarData, 512, nh, true);
            this.imageView.setImageBitmap(scaled);
        } else
            this.imageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
    }


    @Override
    public void onResume() {
        super.onResume();

        nextButton.setEnabled(true);
    }
}
