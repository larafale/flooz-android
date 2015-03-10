package me.flooz.app.UI.Fragment.Home;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLPreset;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationFragment;
import me.flooz.app.UI.Fragment.Home.Camera.CameraFullscreenFragment;
import me.flooz.app.UI.Fragment.Home.Camera.ImageGalleryFragment;
import me.flooz.app.Utils.CustomCameraHost;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;

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

    private Dialog validationDialog;

    private TextView headerBalance;
    private ImageView headerCB;
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
        this.headerBalance = (TextView) view.findViewById(R.id.new_transac_header_balance);
        this.headerCB = (ImageView) view.findViewById(R.id.new_transac_header_card);
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

        ((ImageView) view.findViewById(R.id.new_transac_scope_arrow)).setColorFilter(inflater.getContext().getResources().getColor(android.R.color.white));

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
        this.toUsername.setTypeface(CustomFonts.customTitleLight(inflater.getContext()), Typeface.BOLD);
        this.toFullname.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.toWho.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.amountTextfield.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.currencySymbol.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.contentTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.scopeText.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.chargeButton.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.payButton.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.headerBalance.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.headerCB.setColorFilter(inflater.getContext().getResources().getColor(R.color.blue));
        this.capturePicButton.setColorFilter(inflater.getContext().getResources().getColor(android.R.color.white));

        this.headerCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBalanceDialog();
            }
        });

        this.headerBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBalanceDialog();
            }
        });

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
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(amountTextfield, InputMethodManager.SHOW_FORCED);
                }
            });
        } else if (preset != null) {
            this.amountTextfield.setFocusable(false);
            this.amountTextfield.setFocusableInTouchMode(false);
        }

        this.amountTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateBalanceIndicator();
            }
        });

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
            if (this.currentReceiver.userKind == FLUser.UserKind.FloozUser)
                this.toUsername.setText("@" + this.currentReceiver.username);
            else
                this.toUsername.setText(this.currentReceiver.username);

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

            if (this.preset.type == FLTransaction.TransactionType.TransactionTypeCharge) {
                this.payButton.setVisibility(View.GONE);
            }
            else if (this.preset.type == FLTransaction.TransactionType.TransactionTypePayment) {
                this.chargeButton.setVisibility(View.GONE);
            }
        }

        return view;
    }

    public void onDestroyView() {
        super.onDestroyView();
        this.saveData();
    }

    public void onPause() {
        super.onPause();
        this.saveData();
    }

    public void onStart() {
        super.onStart();
        Handler handler = new Handler(this.context.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!clearFocus) {
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
                        amountContainer.performClick();
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

        this.updateBalanceIndicator();
    }

    public void onResume() {
        super.onResume();

        if (this.currentReceiver != null) {
            this.toWho.setVisibility(View.GONE);
            this.toFullname.setVisibility(View.VISIBLE);
            this.toUsername.setVisibility(View.VISIBLE);

            this.toFullname.setText(this.currentReceiver.fullname);
            if (this.currentReceiver.userKind == FLUser.UserKind.FloozUser)
                this.toUsername.setText("@" + this.currentReceiver.username);
            else
                this.toUsername.setText(this.currentReceiver.username);
            if (this.currentReceiver.avatarURL != null && !this.currentReceiver.avatarURL.isEmpty()) {
                if (this.currentReceiver.userKind == FLUser.UserKind.FloozUser)
                    ImageLoader.getInstance().displayImage(this.currentReceiver.avatarURL, this.toPic);
                else
                    this.toPic.setImageURI(Uri.parse(this.currentReceiver.avatarURL));
            }
            else
                this.toPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        }

        this.amountTextfield.setText(this.savedAmount);
        this.contentTextfield.setText(this.savedWhy);

        this.updateBalanceIndicator();
    }

    private void updateBalanceIndicator() {
        float amount = FloozRestClient.getInstance().currentUser.amount.floatValue();

        if (!this.amountTextfield.getText().toString().isEmpty()) {
            amount = amount - Float.parseFloat(this.amountTextfield.getText().toString());
        }

        if (amount >= 0) {
            this.headerCB.setVisibility(View.GONE);
            this.headerBalance.setVisibility(View.VISIBLE);
            this.headerBalance.setText(FLHelper.trimTrailingZeros(String.format("%.2f", amount)) + " €");
        } else {
            this.headerCB.setVisibility(View.VISIBLE);
            this.headerBalance.setVisibility(View.GONE);
        }
    }

    private void showBalanceDialog() {
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        final Dialog dialog = new Dialog(this.context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_balance);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_wallet_title);
        title.setTypeface(CustomFonts.customContentRegular(this.context), Typeface.BOLD);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_wallet_msg);
        text.setTypeface(CustomFonts.customContentRegular(this.context));

        Button close = (Button) dialog.findViewById(R.id.dialog_wallet_btn);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
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
        if (this.validationDialog != null)
            return;

        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        this.validationDialog = new Dialog(this.context);

        this.validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.validationDialog.setContentView(R.layout.custom_dialog_validate_transaction);

        TextView text = (TextView) this.validationDialog.findViewById(R.id.dialog_validate_flooz_text);
        text.setText(content);
        text.setTypeface(CustomFonts.customContentRegular(this.context));

        Button decline = (Button) this.validationDialog.findViewById(R.id.dialog_validate_flooz_decline);
        Button accept = (Button) this.validationDialog.findViewById(R.id.dialog_validate_flooz_accept);

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validationDialog.dismiss();
                validationDialog = null;
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                validationDialog.dismiss();
                validationDialog = null;
                ((AuthenticationFragment)parentActivity.contentFragments.get("authentication")).delegate = instance;
                parentActivity.pushMainFragment("authentication", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        this.validationDialog.setCanceledOnTouchOutside(false);
        this.validationDialog.show();
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

        if (this.currentReceiver != null && this.toContainer != null) {
            this.toWho.setVisibility(View.GONE);
            this.toFullname.setVisibility(View.VISIBLE);
            this.toUsername.setVisibility(View.VISIBLE);

            this.toFullname.setText(this.currentReceiver.fullname);
            if (this.currentReceiver.userKind == FLUser.UserKind.FloozUser)
                this.toUsername.setText("@" + this.currentReceiver.username);
            else
                this.toUsername.setText(this.currentReceiver.username);

            if (this.currentReceiver.avatarURL != null && !this.currentReceiver.avatarURL.isEmpty()) {
                if (this.currentReceiver.userKind == FLUser.UserKind.FloozUser)
                    ImageLoader.getInstance().displayImage(this.currentReceiver.avatarURL, this.toPic);
                else
                    this.toPic.setImageURI(Uri.parse(this.currentReceiver.avatarURL));
            }
            else
                this.toPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        }
    }

    @Override
    public void photoTaken(final Bitmap photo) {
        Handler mainHandler = new Handler(this.context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentPicture != null)
                    currentPicture.recycle();
                havePicture = true;
                picContainer.setVisibility(View.VISIBLE);

                int nh = (int) ( photo.getHeight() * (256.0 / photo.getWidth()) );
                Bitmap scaled = Bitmap.createScaledBitmap(photo, 256, nh, true);
                pic.setImageBitmap(scaled);

                currentPicture = photo;

                parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        };
        mainHandler.post(myRunnable);
    }


    @Override
    public void authenticationValidated() {
        FloozRestClient.getInstance().showLoadView();
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
                    sendSMSIntent(smsObject.optString("phone"), smsObject.optString("message"));
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
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(Uri.parse("sms:" + phone));
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

        Map<String, String> metrics = new HashMap<>(1);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", this.amountTextfield.getText().toString());
        if (currentReceiver != null) {
            params.put("to", currentReceiver.username.replace("+33", "0"));
            metrics.put("selectedFrom", currentReceiver.getSelectedCanal());
            params.put("metrics", metrics);
        } else
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
