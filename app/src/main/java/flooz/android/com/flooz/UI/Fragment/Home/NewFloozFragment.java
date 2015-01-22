package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLPreset;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.Authentication.AuthenticationFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.CameraFullscreenFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.CameraOverlayFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.ImageGalleryFragment;
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import flooz.android.com.flooz.Utils.FLHelper;
import flooz.android.com.flooz.Utils.ImageHelper;

/**
 * Created by Flooz on 10/1/14.
 */
public class NewFloozFragment extends HomeBaseFragment implements TransactionSelectReceiverFragment.TransactionSelectReceiverDelegate,
        CustomCameraHost.CustomCameraHostDelegate, AuthenticationFragment.AuthenticationDelegate {

    private NewFloozFragment instance;
    private Context context;

    private FLUser currentReceiver = null;
    private FLPreset preset = null;

    private String random;
    private String savedAmount;
    private String savedWhy;

    private Boolean havePicture = false;
    private Bitmap currentPicture;

    private Boolean clearFocus;

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
    private LinearLayout amountContainer;
    private TextView currencySymbol;
    private EditText contentTextfield;
    private RelativeLayout picContainer;
    private ImageView pic;
    private ImageView captureAlbumButton;
    private ImageView picDeleteButton;
    private LinearLayout scopeContainer;
    private ImageView scopePic;
    private TextView scopeText;
    private ImageView capturePicButton;
    private CheckBox fbCheckbox;
    private TextView chargeButton;
    private TextView payButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public void initWithUser(FLUser user) {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = user;
        this.preset = null;
        this.currentPicture = null;
        this.havePicture = false;

        this.savedAmount = "";
        this.savedWhy = "";
        this.clearFocus = false;
        this.currentScope = FLTransaction.transactionScopeParamToEnum((String) ((Map) FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));
    }

    public void initWithPreset(FLPreset data) {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
        this.preset = data;
        this.currentPicture = null;
        this.havePicture = false;
        this.clearFocus = false;

        if (this.preset.to != null)
            this.currentReceiver = this.preset.to;

        if (this.preset.amount != null)
            this.savedAmount = this.preset.amount.toString();
        else
            this.savedAmount = "";

        if (this.preset.why != null)
            this.savedWhy = this.preset.why;
        else
            this.savedWhy = "";

        this.currentScope = FLTransaction.transactionScopeParamToEnum((String)((Map)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.transaction_fragment, null);

        this.instance = this;

        this.context = inflater.getContext();
        this.headerTitle = (TextView) view.findViewById(R.id.new_transac_header_text);
        this.closeButton = (ImageView) view.findViewById(R.id.new_transac_close_button);
        this.toPic = (RoundedImageView) view.findViewById(R.id.new_transac_to_pic);
        this.toUsername = (TextView) view.findViewById(R.id.new_transac_to_username);
        this.toFullname = (TextView) view.findViewById(R.id.new_transac_to_fullname);
        this.toWho = (TextView) view.findViewById(R.id.new_transac_to_who);
        this.toContainer = (LinearLayout) view.findViewById(R.id.new_transac_to_container);
        this.amountTextfield = (EditText) view.findViewById(R.id.new_transac_amount_textfield);
        this.amountContainer = (LinearLayout) view.findViewById(R.id.new_transac_amount_container);
        this.currencySymbol = (TextView) view.findViewById(R.id.new_transac_currency_symbol);
        this.contentTextfield = (EditText) view.findViewById(R.id.new_transac_content_textfield);
        this.picContainer = (RelativeLayout) view.findViewById(R.id.new_transac_pic_container);
        this.pic = (ImageView) view.findViewById(R.id.new_transac_pic);
        this.picDeleteButton = (ImageView) view.findViewById(R.id.new_transac_pic_delete);
        this.scopeContainer = (LinearLayout) view.findViewById(R.id.new_transac_scope_container);
        this.scopePic = (ImageView) view.findViewById(R.id.new_transac_scope_pic);
        this.scopeText = (TextView) view.findViewById(R.id.new_transac_scope_text);
        this.capturePicButton = (ImageView) view.findViewById(R.id.new_transac_pic_button);
        this.captureAlbumButton = (ImageView) view.findViewById(R.id.new_transac_album_button);
        this.fbCheckbox = (CheckBox) view.findViewById(R.id.new_transac_fb_checkbox);
        this.chargeButton = (TextView) view.findViewById(R.id.new_transac_collect);
        this.payButton = (TextView) view.findViewById(R.id.new_transac_paid);

        if (this.preset == null || !this.preset.blockBack) {
            this.closeButton.setVisibility(View.VISIBLE);
            this.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                    currentReceiver = null;
                }
            });
        } else {
            this.closeButton.setVisibility(View.GONE);
        }

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

        if (this.preset == null || !this.preset.blockTo) {
            this.toContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveData();
                    ((TransactionSelectReceiverFragment) parentActivity.contentFragments.get("select_user")).delegate = instance;
                    ((TransactionSelectReceiverFragment) parentActivity.contentFragments.get("select_user")).showCross = false;
                    parentActivity.pushMainFragment("select_user", R.animator.slide_up, android.R.animator.fade_out);
                }
            });
        }

        if (this.preset == null || !this.preset.blockAmount) {
            this.amountTextfield.setFocusable(true);
            this.amountTextfield.setFocusableInTouchMode(true);
            this.amountContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountTextfield.requestFocus();
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(amountTextfield, 0);
                }
            });
        } else if (preset != null) {
            this.amountTextfield.setFocusable(false);
            this.amountTextfield.setFocusableInTouchMode(false);
        }

        if (Camera.getNumberOfCameras() == 0)
            this.capturePicButton.setVisibility(View.GONE);
        else
            this.capturePicButton.setVisibility(View.VISIBLE);

        this.capturePicButton.setOnClickListener(showCamera);
        this.captureAlbumButton.setOnClickListener(showAlbum);

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
        this.payButton.setVisibility(View.VISIBLE);
        this.chargeButton.setVisibility(View.VISIBLE);

        this.chargeButton.setOnClickListener(chargeTransaction);
        this.payButton.setOnClickListener(payTransaction);

        this.contentTextfield.setFocusable(true);
        this.contentTextfield.setFocusableInTouchMode(true);

        if (this.preset != null) {
            if (this.preset.title != null)
                this.headerTitle.setText(this.preset.title);

            if (this.preset.blockWhy) {
                this.contentTextfield.setFocusable(false);
                this.contentTextfield.setFocusableInTouchMode(false);
            }

            if (this.preset.focusAmount) {
                this.amountTextfield.requestFocus();
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(this.amountTextfield, 0);
            } else if (this.preset.focusWhy) {
                this.contentTextfield.requestFocus();
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.showSoftInput(this.contentTextfield, 0);
            }

            if (this.preset.type == FLTransaction.TransactionType.TransactionTypeCharge) {
                this.payButton.setVisibility(View.GONE);
            }
            else if (this.preset.type == FLTransaction.TransactionType.TransactionTypePayment) {
                this.chargeButton.setVisibility(View.GONE);
            }

        } else {
            this.amountTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this.amountTextfield, 0);
        }

        return view;
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onStart() {
        super.onStart();
        Handler handler = new Handler(this.context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (clearFocus == false) {
                    if (preset != null) {
                        if (preset.focusWhy) {
                            contentTextfield.setSelection(contentTextfield.getText().length());
                            contentTextfield.requestFocus();
                            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(contentTextfield, 0);
                        } else if (preset.focusAmount) {
                            amountTextfield.setSelection(amountTextfield.getText().length());
                            amountTextfield.requestFocus();
                            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(amountTextfield, 0);
                        }
                    } else {
                        amountTextfield.setSelection(amountTextfield.getText().length());
                        amountTextfield.requestFocus();
                        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Service.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(amountTextfield, 0);
                    }
                } else {
                    contentTextfield.clearFocus();
                    amountTextfield.clearFocus();
                }
                clearFocus = true;
            }
        }, 500);

        this.amountTextfield.setText(this.savedAmount);
        this.contentTextfield.setText(this.savedWhy);
    }

    private void saveData() {
        this.savedAmount = this.amountTextfield.getText().toString();
        this.savedWhy = this.contentTextfield.getText().toString();
    }

    private void updateScopeField() {
        this.scopePic.setImageDrawable(FLTransaction.transactionScopeToImage(this.currentScope));
        this.scopeText.setText(FLTransaction.transactionScopeToText(this.currentScope));
    }

    private void performTransaction(FLTransaction.TransactionType type) {
        this.currentType = type;

        FloozRestClient.getInstance().performTransaction(this.generateRequestParams(true), new FloozHttpResponseHandler() {
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
                saveData();
                dialog.dismiss();
                ((AuthenticationFragment)parentActivity.contentFragments.get("authentication")).delegate = instance;
                parentActivity.pushMainFragment("authentication", R.animator.slide_up, android.R.animator.fade_out);
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

    private View.OnClickListener showCamera = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();
            ((CameraFullscreenFragment)parentActivity.contentFragments.get("camera_fullscreen")).cameraHostDelegate = instance;
            ((CameraFullscreenFragment)parentActivity.contentFragments.get("camera_fullscreen")).canAccessAlbum = false;
            parentActivity.pushMainFragment("camera_fullscreen", R.animator.slide_up, android.R.animator.fade_out);
        }
    };

    private View.OnClickListener showAlbum = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();
            ((ImageGalleryFragment)parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = instance;
            parentActivity.pushMainFragment("photo_gallery", R.animator.slide_up, android.R.animator.fade_out);
        }
    };

    @Override
    public void changeUser(FLUser user) {
        this.currentReceiver = user;
    }

    @Override
    public void photoTaken(final Bitmap photo) {
        Handler mainHandler = new Handler(this.context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                havePicture = true;
                currentPicture = photo;

                parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        };
        mainHandler.post(myRunnable);
    }


    @Override
    public void authenticationValidated() {
        FloozRestClient.getInstance().performTransaction(this.generateRequestParams(false), new FloozHttpResponseHandler() {
            @Override
            public void success(final Object response) {
                final JSONObject jsonObject = (JSONObject) response;
                if (havePicture) {
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String transacId = jsonObject.optJSONObject("item").optString("_id");
                            FloozRestClient.getInstance().uploadTransactionPic(transacId, ImageHelper.convertBitmapInFile(currentPicture), new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {

                                }
                            });
                        }
                    });
                }
                if (jsonObject.has("sms")) {
                    final JSONObject smsObject = jsonObject.optJSONObject("sms");

                    PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                            new Intent("SMS_SENT"), 0);

                    context.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            switch (getResultCode()) {
                                case Activity.RESULT_OK:
                                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                                    break;
                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                    sendSMSIntent(smsObject.optString("phone"), smsObject.optString("message"));
                                    break;
                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                    sendSMSIntent(smsObject.optString("phone"), smsObject.optString("message"));
                                    break;
                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                    sendSMSIntent(smsObject.optString("phone"), smsObject.optString("message"));
                                    break;
                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                    sendSMSIntent(smsObject.optString("phone"), smsObject.optString("message"));
                                    break;
                            }
                        }
                    }, new IntentFilter("SMS_SENT"));

                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(smsObject.optString("phone"), null, smsObject.optString("message"), sentPI, null);
                } else {
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                }
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void sendSMSIntent(String phone, String content) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("address", phone);
        sendIntent.putExtra("sms_body", content);
        if (sendIntent.resolveActivity(parentActivity.getPackageManager()) != null) {
            parentActivity.startActivity(sendIntent);
        }
        parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
    }

    @Override
    public void authenticationFailed() {

    }

    private Map<String, Object> generateRequestParams(boolean validate) {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", this.amountTextfield.getText().toString());
        if (currentReceiver != null)
            params.put("to", currentReceiver.username);
        else
            params.put("to", "");

        params.put("random", random);
        params.put("method", FLTransaction.transactionTypeToParams(this.currentType));
        params.put("scope", FLTransaction.transactionScopeToParams(this.currentScope));
        params.put("why", this.contentTextfield.getText().toString());
        params.put("validate", validate);

        if (this.preset != null && this.preset.payload != null)
            params.put("payload", this.preset.payload);

        return params;
    }

    @Override
    public void onBackPressed() {
        if (this.preset == null || !this.preset.blockBack)
            this.closeButton.performClick();
    }
}
