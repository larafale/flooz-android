package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Fragment.Camera.CameraFullscreenFragment;
import flooz.android.com.flooz.UI.Fragment.Camera.CameraOverlayFragment;
import flooz.android.com.flooz.UI.Fragment.Camera.ImageGalleryFragment;
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/1/14.
 */
public class NewFloozFragment extends Fragment implements TransactionSelectReceiverFragment.TransactionSelectReceiverDelegate,
        CustomCameraHost.CustomCameraHostDelegate, CameraOverlayFragment.CameraOverlayFragmentCallbacks, SecureCodeFragment.SecureCodeDelegate {

    private NewFloozFragment instance;
    private Context context;

    public HomeActivity parentActivity;

    private FLUser currentReceiver = null;

    private CameraOverlayFragment camFragmentBack;
    private CameraOverlayFragment camFragmentFront;

    private Boolean cameraVisible = false;
    private Boolean currentCameraIsFront = false;

    private Boolean havePicture = false;
    private Bitmap currentPicture;

    FLTransaction.TransactionType currentType;
    private FLTransaction.TransactionScope currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

    private TextView headerTitle;
    private ImageView closeButton;
    private RoundedImageView toPic;
    private TextView toUsername;
    private TextView toFullname;
    private TextView toWho;
    private LinearLayout toContainer;
    private EditText amountTextfield;
    private TextView currencySymbol;
    private EditText contentTextfield;
    private RelativeLayout picContainer;
    private ImageView pic;
    private ImageView picDeleteButton;
    private LinearLayout scopeContainer;
    private ImageView scopePic;
    private TextView scopeText;
    private ImageView capturePicButton;
    private CheckBox fbCheckbox;
    private TextView chargeButton;
    private TextView payButton;
    private FrameLayout cameraFragmentContainer;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.instance = this;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.transaction_fragment, null);

        this.context = inflater.getContext();
        this.headerTitle = (TextView) view.findViewById(R.id.new_transac_header_text);
        this.closeButton = (ImageView) view.findViewById(R.id.new_transac_close_button);
        this.toPic = (RoundedImageView) view.findViewById(R.id.new_transac_to_pic);
        this.toUsername = (TextView) view.findViewById(R.id.new_transac_to_username);
        this.toFullname = (TextView) view.findViewById(R.id.new_transac_to_fullname);
        this.toWho = (TextView) view.findViewById(R.id.new_transac_to_who);
        this.toContainer = (LinearLayout) view.findViewById(R.id.new_transac_to_container);
        this.amountTextfield = (EditText) view.findViewById(R.id.new_transac_amount_textfield);
        this.currencySymbol = (TextView) view.findViewById(R.id.new_transac_currency_symbol);
        this.contentTextfield = (EditText) view.findViewById(R.id.new_transac_content_textfield);
        this.picContainer = (RelativeLayout) view.findViewById(R.id.new_transac_pic_container);
        this.pic = (ImageView) view.findViewById(R.id.new_transac_pic);
        this.picDeleteButton = (ImageView) view.findViewById(R.id.new_transac_pic_delete);
        this.scopeContainer = (LinearLayout) view.findViewById(R.id.new_transac_scope_container);
        this.scopePic = (ImageView) view.findViewById(R.id.new_transac_scope_pic);
        this.scopeText = (TextView) view.findViewById(R.id.new_transac_scope_text);
        this.capturePicButton = (ImageView) view.findViewById(R.id.new_transac_pic_button);
        this.fbCheckbox = (CheckBox) view.findViewById(R.id.new_transac_fb_checkbox);
        this.chargeButton = (TextView) view.findViewById(R.id.new_transac_collect);
        this.payButton = (TextView) view.findViewById(R.id.new_transac_paid);
        this.cameraFragmentContainer = (FrameLayout) view.findViewById(R.id.new_transac_camera_fragment_container);

        this.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                currentReceiver = null;
            }
        });

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.toUsername.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.toFullname.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.toWho.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.amountTextfield.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.currencySymbol.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.contentTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.scopeText.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.chargeButton.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.payButton.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.toContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity != null) {
                    ((TransactionSelectReceiverFragment) parentActivity.contentFragments.get("select_user")).delegate = instance;
                    parentActivity.pushMainFragment("select_user", R.animator.slide_up, android.R.animator.fade_out);
                }
            }
        });

        this.capturePicButton.setOnClickListener(toogleCamera);

        this.camFragmentBack = new CameraOverlayFragment();
        this.camFragmentBack.delegate = this;
        this.camFragmentBack.callbacks = this;
        this.camFragmentBack.isExpandable = true;

        this.camFragmentFront = new CameraOverlayFragment();
        this.camFragmentFront.delegate = this;
        this.camFragmentFront.callbacks = this;
        this.camFragmentFront.showFront = true;
        this.camFragmentFront.isExpandable = true;

        this.updateScopeField();

        this.scopeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentScope) {
                    case TransactionScopePublic:
                        currentScope = FLTransaction.TransactionScope.TransactionScopeFriend;
                        break;
                    case TransactionScopeFriend:
                        currentScope = FLTransaction.TransactionScope.TransactionScopePrivate;
                        break;
                    case TransactionScopePrivate:
                        currentScope = FLTransaction.TransactionScope.TransactionScopePublic;
                        break;
                }
                updateScopeField();
            }
        });

        if (this.currentReceiver != null) {
            this.toWho.setVisibility(View.GONE);
            this.toFullname.setVisibility(View.VISIBLE);
            this.toUsername.setVisibility(View.VISIBLE);

            this.toFullname.setText(this.currentReceiver.fullname);
            this.toUsername.setText("@" + this.currentReceiver.username);

            if (this.currentReceiver.avatarURL != null && !this.currentReceiver.avatarURL.isEmpty()) {
                if (this.currentReceiver.userKind == FLUser.UserKind.FloozUser)
                    ImageLoader.getInstance().displayImage(this.currentReceiver.avatarURL, this.toPic);
                else
                    this.toPic.setImageURI(Uri.parse(this.currentReceiver.avatarURL));
            }
            else
                this.toPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        }

        if (this.havePicture) {
            this.picContainer.setVisibility(View.VISIBLE);
            this.pic.setImageBitmap(currentPicture);
        }

        this.picDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picContainer.setVisibility(View.GONE);
                havePicture = false;
                currentPicture = null;
            }
        });

        this.chargeButton.setOnClickListener(chargeTransaction);
        this.payButton.setOnClickListener(payTransaction);

        return view;
    }

    private void updateScopeField() {
        this.scopePic.setImageDrawable(FLTransaction.transactionScopeToImage(this.currentScope));
        this.scopeText.setText(FLTransaction.transactionScopeToText(this.currentScope));
    }

    private void performTransaction(FLTransaction.TransactionType type) {
        this.currentType = type;

        String amount = this.amountTextfield.getText().toString();
        if (amount.isEmpty())
            amount = "0";

        FloozRestClient.getInstance().performTransaction(Float.parseFloat(amount),
                this.currentReceiver, this.currentType, this.currentScope, this.contentTextfield.getText().toString(), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        JSONObject jsonObject = (JSONObject) response;
                        showValidationDialog(jsonObject.optString("confirmationText"));
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
    }

    private void showValidationDialog(String content) {
        final Dialog dialog = new Dialog(this.context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_validate_transaction);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_validate_flooz_text);
        text.setText(content);
        text.setTypeface(CustomFonts.customContentRegular(this.context));

        Button decline = (Button) dialog.findViewById(R.id.dialog_validate_flooz_decline);
        Button accept = (Button) dialog.findViewById(R.id.dialog_validate_flooz_accept);

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((SecureCodeFragment)parentActivity.contentFragments.get("secure_code")).delegate = instance;
                parentActivity.pushMainFragment("secure_code", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        dialog.show();
    }

    private View.OnClickListener payTransaction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performTransaction(FLTransaction.TransactionType.TransactionTypePayment);
        }
    };

    private View.OnClickListener chargeTransaction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performTransaction(FLTransaction.TransactionType.TransactionTypeCharge);
        }
    };

    private View.OnClickListener toogleCamera = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (Camera.getNumberOfCameras() == 0) {
                ((ImageGalleryFragment)parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = instance;
                parentActivity.pushMainFragment("photo_gallery", R.animator.slide_up, android.R.animator.fade_out);
            }
            else {
                if (!cameraVisible) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

                    cameraVisible = true;
                    FragmentTransaction ft = instance.getChildFragmentManager().beginTransaction();

                    if (Camera.getNumberOfCameras() < 2) {
                        Camera.CameraInfo info = new Camera.CameraInfo();
                        Camera.getCameraInfo(0, info);
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            currentCameraIsFront = true;
                            ft.replace(R.id.new_transac_camera_fragment_container, camFragmentFront);
                        } else
                            ft.replace(R.id.new_transac_camera_fragment_container, camFragmentBack);
                    } else
                        ft.replace(R.id.new_transac_camera_fragment_container, camFragmentBack);

                    ft.addToBackStack(null).commit();

                    cameraFragmentContainer.setVisibility(View.VISIBLE);
                } else
                    dismissCamera();
            }
        }
    };

    private void dismissCamera() {
        cameraFragmentContainer.setVisibility(View.GONE);
        cameraVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void UserSelected(FLUser user) {
        this.currentReceiver = user;
    }

    @Override
    public void photoTaken(final Bitmap photo) {
        Handler mainHandler = new Handler(this.context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                havePicture = true;
                picContainer.setVisibility(View.VISIBLE);
                pic.setImageBitmap(photo);
                currentPicture = photo;

                if (cameraVisible)
                    dismissCamera();
                else
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void switchCamera(Boolean showFront) {
        CameraOverlayFragment in;

        if (showFront) {
            this.currentCameraIsFront = true;
            in = this.camFragmentFront;
        }
        else {
            this.currentCameraIsFront = false;
            in = this.camFragmentBack;
        }

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

        ft.setCustomAnimations(
                R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                R.animator.card_flip_left_in, R.animator.card_flip_left_out);

        ft.replace(R.id.new_transac_camera_fragment_container, in);
        ft.addToBackStack(null).commit();
    }

    @Override
    public void expandCamera() {
        ((CameraFullscreenFragment)this.parentActivity.contentFragments.get("camera_fullscreen")).currentCameraIsFront = this.currentCameraIsFront;
        ((CameraFullscreenFragment)this.parentActivity.contentFragments.get("camera_fullscreen")).cameraHostDelegate = this;
        parentActivity.pushMainFragment("camera_fullscreen", R.animator.slide_up, android.R.animator.fade_out);
        this.dismissCamera();
    }

    @Override
    public void closeCamera() { }

    @Override
    public void showGallery() {
        ((ImageGalleryFragment)this.parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = this;
        parentActivity.pushMainFragment("photo_gallery", R.animator.slide_up, android.R.animator.fade_out);
        this.dismissCamera();
    }

    @Override
    public void secureCodeValidated(Boolean success) {
        if (success) {
            File uploadImage = null;

            if (this.havePicture) {
                uploadImage = new File(this.context.getCacheDir(), "image.png");
                try {
                    uploadImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = this.currentPicture;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(uploadImage);
                    fos.write(bitmapdata);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FloozRestClient.getInstance().validateTransaction(Float.parseFloat(this.amountTextfield.getText().toString()),
                    this.currentReceiver, this.currentType, this.currentScope, uploadImage, this.contentTextfield.getText().toString(), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            currentReceiver = null;
                            currentScope = FLTransaction.TransactionScope.TransactionScopePublic;
                            contentTextfield.setText("");
                            amountTextfield.setText("");
                            parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
        }
    }
}
