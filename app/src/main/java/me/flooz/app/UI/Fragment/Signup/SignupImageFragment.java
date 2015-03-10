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

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.SignupActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/14/14.
 */
public class SignupImageFragment extends SignupBaseFragment {

    private RoundedImageView imageView;
    private TextView infoLabel;
    private RelativeLayout fbButton;
    private RelativeLayout takeButton;
    private RelativeLayout chooseButton;
    private TextView fbButtonText;
    private TextView takeButtonText;
    private TextView chooseButtonText;
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
        this.infoLabel = (TextView)view.findViewById(R.id.signup_image_info_label);
        this.fbButtonText = (TextView)view.findViewById(R.id.signup_image_fbButton_text);
        this.takeButtonText = (TextView)view.findViewById(R.id.signup_image_takeButton_text);
        this.chooseButtonText = (TextView)view.findViewById(R.id.signup_image_chooseButton_text);
        this.fbButton = (RelativeLayout)view.findViewById(R.id.signup_image_fbButton);
        this.takeButton = (RelativeLayout)view.findViewById(R.id.signup_image_takeButton);
        this.chooseButton = (RelativeLayout)view.findViewById(R.id.signup_image_chooseButton);
        this.nextButton = (Button)view.findViewById(R.id.signup_image_next);

        this.infoLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.fbButtonText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.takeButtonText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.chooseButtonText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().connectFacebook();
            }
        });

        this.takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.changeCurrentPage(SignupActivity.SignupPageIdentifier.SignupImageCapture, R.animator.slide_in_left, R.animator.slide_out_right);
                parentActivity.hideHeader();
            }
        });

        this.chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.changeCurrentPage(SignupActivity.SignupPageIdentifier.SignupImagePicker, R.animator.slide_in_left, R.animator.slide_out_right);
            }
        });

        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().signupPassStep("image", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.gotToNextPage();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {}
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
        else if (parentActivity.userData.avatarData != null)
            this.imageView.setImageBitmap(parentActivity.userData.avatarData);
        else
            this.imageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
    }
}
