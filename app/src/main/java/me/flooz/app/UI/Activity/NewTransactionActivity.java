package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.PointTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.flooz.app.Adapter.SelectUserListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLPreset;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.ContactPickerView;
import me.flooz.app.UI.View.TokenPickerLibrary.TokenCompleteTextView;
import me.flooz.app.UI.View.ToolTipScopeView;
import me.flooz.app.UI.View.ToolTipScopeViewDelegate;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/13/15.
 */
public class NewTransactionActivity extends Activity implements ToolTipScopeViewDelegate {
    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;

    private NewTransactionActivity instance;
    private FloozApplication floozApp;

    private FLUser currentReceiver = null;
    private FLPreset preset = null;

    private Uri tmpUriImage;
    private String random;
    private String savedAmount;
    private String savedWhy;
    private Boolean isDemo = false;

    private Boolean havePicture = false;
    private Bitmap currentPicture;

    private Boolean clearFocus;

    FLTransaction.TransactionType currentType;
    private FLTransaction.TransactionScope currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

    private Dialog validationDialog;

    private LinearLayout baseLayout;
    private TextView headerBalance;
    private ImageView headerCB;
    private ImageView closeButton;
    private ContactPickerView toPicker;
    private ListView contactList;
    private SelectUserListAdapter contactListAdapter;
    private EditText amountTextfield;
    private EditText contentTextfield;
    private RelativeLayout picContainer;
    private ImageView pic;
    private ImageView scopePic;
    private TextView chargeButton;
    private TextView payButton;

    private ToolTip toolTipScope;
    private ToolTipScopeView tooltipScopeView;

    private ToolTipLayout tipContainer;
    private ToolTipLayout demoContainer;

    private Boolean transactionPending = false;

    public void init() {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
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

        this.isDemo = preset.isDemo;

        this.currentScope = FLTransaction.transactionScopeParamToEnum((String)((Map)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("preset"))
            try {
                this.initWithPreset(new FLPreset(new JSONObject(getIntent().getStringExtra("preset"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        else
            this.init();

        this.setContentView(R.layout.transaction_fragment);

        this.baseLayout = (LinearLayout) this.findViewById(R.id.new_transac_base);
        TextView headerTitle = (TextView) this.findViewById(R.id.new_transac_header_text);
        this.closeButton = (ImageView) this.findViewById(R.id.new_transac_close_button);
        this.headerBalance = (TextView) this.findViewById(R.id.new_transac_header_balance);
        this.headerCB = (ImageView) this.findViewById(R.id.new_transac_header_card);
        this.toPicker = (ContactPickerView) this.findViewById(R.id.new_transac_to_container);
        this.contactList = (ListView) this.findViewById(R.id.new_transac_contact_list);
        this.amountTextfield = (EditText) this.findViewById(R.id.new_transac_amount_textfield);
        LinearLayout amountContainer = (LinearLayout) this.findViewById(R.id.new_transac_amount_container);
        TextView currencySymbol = (TextView) this.findViewById(R.id.new_transac_currency_symbol);
        this.contentTextfield = (EditText) this.findViewById(R.id.new_transac_content_textfield);
        this.picContainer = (RelativeLayout) this.findViewById(R.id.new_transac_pic_container);
        this.pic = (ImageView) this.findViewById(R.id.new_transac_pic);
        ImageView picDeleteButton = (ImageView) this.findViewById(R.id.new_transac_pic_delete);
        this.scopePic = (ImageView) this.findViewById(R.id.new_transac_scope_pic);
        ImageView capturePicButton = (ImageView) this.findViewById(R.id.new_transac_pic_button);
        ImageView captureAlbumButton = (ImageView) this.findViewById(R.id.new_transac_album_button);
        this.chargeButton = (TextView) this.findViewById(R.id.new_transac_collect);
        this.payButton = (TextView) this.findViewById(R.id.new_transac_paid);
        this.tipContainer = (ToolTipLayout) this.findViewById(R.id.new_transac_tooltip_container);
        this.demoContainer = (ToolTipLayout) this.findViewById(R.id.new_transac_demo_container);

        if (this.preset == null || !this.preset.blockBack) {
            this.closeButton.setVisibility(View.VISIBLE);
            this.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transactionPending = false;
                    currentReceiver = null;
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                }
            });
        } else {
            this.closeButton.setVisibility(View.GONE);
        }

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.amountTextfield.setTypeface(CustomFonts.customTitleExtraLight(this));
        currencySymbol.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.contentTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.chargeButton.setTypeface(CustomFonts.customTitleLight(this));
        this.payButton.setTypeface(CustomFonts.customTitleLight(this));
        this.headerBalance.setTypeface(CustomFonts.customTitleLight(this));
        this.toPicker.setTypeface(CustomFonts.customContentRegular(this));

        this.headerCB.setColorFilter(this.getResources().getColor(R.color.blue));
        capturePicButton.setColorFilter(this.getResources().getColor(android.R.color.white));

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

        this.toPicker.setPrefix(this.getResources().getString(R.string.TRANSACTION_CONTACT_PICKER_PREFIX));
        this.toPicker.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        this.toPicker.setDeletionStyle(TokenCompleteTextView.TokenDeleteStyle.Clear);
        this.toPicker.allowCollapse(true);
        this.toPicker.setTokenLimit(1);
        this.toPicker.setAdapter(new ArrayAdapter<FLUser>(this, 0));

        this.toPicker.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchString = s.toString();
                searchString = searchString.replace(getResources().getString(R.string.TRANSACTION_CONTACT_PICKER_PREFIX), "");
                searchString = searchString.replace(",, ", "");
                searchString = searchString.replace(toPicker.getHint(), "");
                contactListAdapter.searchUser(searchString);
            }
        });

        this.contactListAdapter = new SelectUserListAdapter(this);
        this.contactList.setAdapter(this.contactListAdapter);

        this.contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (contactListAdapter.getItem(position) != null) {
                    changeUser(contactListAdapter.getItem(position));
                }
            }
        });

        this.toPicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    contactList.setVisibility(View.VISIBLE);
                else {
                    if (currentReceiver == null) {
                        toPicker.clear();
                        toPicker.clearComposingText();
                        toPicker.setText(getResources().getString(R.string.TRANSACTION_CONTACT_PICKER_PREFIX));
                        contactListAdapter.searchUser("");
                    }
                    contactList.setVisibility(View.GONE);
                }
            }
        });

        this.toPicker.setTokenListener(new TokenCompleteTextView.TokenListener() {
            @Override
            public void onTokenAdded(Object token) {

            }

            @Override
            public void onTokenRemoved(Object token) {
                currentReceiver = null;
                validateView();
            }
        });

        if (this.currentReceiver != null)
            this.toPicker.addObject(this.currentReceiver);

        if (this.preset != null && this.preset.blockTo) {
            this.toPicker.setFocusable(false);
            this.toPicker.setFocusableInTouchMode(false);
        }

        if (this.preset == null || !this.preset.blockAmount) {
            this.amountTextfield.setFocusable(true);
            this.amountTextfield.setFocusableInTouchMode(true);
            amountContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amountTextfield.requestFocus();
                    InputMethodManager imm = (InputMethodManager) instance.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                if (s.length() > 7)
                    s.delete(7, s.length());

                updateBalanceIndicator();
                validateView();
            }
        });

        capturePicButton.setOnClickListener(showCamera);
        captureAlbumButton.setOnClickListener(showAlbum);

        this.tooltipScopeView = new ToolTipScopeView(this);
        this.tooltipScopeView.changeScope(this.currentScope);
        this.tooltipScopeView.delegate = this;

        this.updateScopeField();

        this.scopePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipScope != null) {
                    tipContainer.dismiss(true);
                    toolTipScope = null;
                } else {
                    if (tooltipScopeView.view.getParent() != null)
                        ((ViewGroup)tooltipScopeView.view.getParent()).removeView(tooltipScopeView.view);

                    toolTipScope = new ToolTip.Builder(instance)
                            .anchor(scopePic)
                            .gravity(Gravity.TOP)
                            .color(instance.getResources().getColor(android.R.color.white))
                            .pointerSize(25)
                            .contentView(tooltipScopeView.view)
                            .dismissOnTouch(false)
                            .build();

                    tipContainer.addTooltip(toolTipScope, true);
                }
            }
        });

        if (getIntent() != null && getIntent().hasExtra("user"))
            try {
                this.changeUser(new FLUser(new JSONObject(getIntent().getStringExtra("user"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (this.havePicture) {
            this.picContainer.setVisibility(View.VISIBLE);
            this.pic.setImageBitmap(currentPicture);
        }

        picDeleteButton.setOnClickListener(new View.OnClickListener() {
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

        this.contentTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateView();
            }
        });

        if (this.preset != null) {
            if (this.preset.title != null)
                headerTitle.setText(this.preset.title);

            if (this.preset.whyPlaceholder != null && !this.preset.whyPlaceholder.isEmpty())
                this.contentTextfield.setHint(this.preset.whyPlaceholder);

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
        this.validateView();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK)
                authenticationValidated();
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {

                Uri imageUri;
                if (data == null || data.getData() == null)
                    imageUri = this.tmpUriImage;
                else
                    imageUri = data.getData();

                this.tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String path = ImageHelper.getPath(instance, selectedImageUri);
                            if (path != null) {
                                File image = new File(path);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                                int rotation = ImageHelper.getRotation(instance, selectedImageUri);
                                if (rotation != 0) {
                                    photo = ImageHelper.rotateBitmap(photo, rotation);
                                }

                                photoTaken(photo);
                            }
                        }
                    });
                }
            }
        }
    }

    public void validateView() {
        Boolean valid = true;

        if (this.currentReceiver == null)
            valid = false;
        if (this.amountTextfield.getText().length() == 0)
            valid = false;
        if (this.contentTextfield.getText().length() == 0)
            valid = false;

        this.payButton.setEnabled(valid);
        this.chargeButton.setEnabled(valid);
    }

    @Override
    public void onStart() {
        super.onStart();

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.amountTextfield, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.toPicker.clearFocus();

        this.amountTextfield.setText(this.savedAmount);
        this.contentTextfield.setText(this.savedWhy);

        if (this.amountTextfield.getText().length() == 0)
            this.amountTextfield.requestFocus();
        else if (this.contentTextfield.getText().length() == 0)
            this.contentTextfield.requestFocus();
        else
            this.baseLayout.requestFocus();

        this.updateBalanceIndicator();

        if (this.transactionPending)
            FloozRestClient.getInstance().showLoadView();
    }

    private void updateBalanceIndicator() {
        float amount = FloozRestClient.getInstance().currentUser.amount.floatValue();

        if (!this.amountTextfield.getText().toString().isEmpty()) {
            float tmp = 0;

            try {
                tmp = Float.parseFloat(this.amountTextfield.getText().toString());
            } catch (NumberFormatException e) {

            }

            amount = amount - tmp;
        }

        if (this.preset != null && this.preset.blockBalance) {
            this.headerBalance.setVisibility(View.GONE);
            this.headerCB.setVisibility(View.GONE);
        } else {
            if (amount >= 0) {
                this.headerCB.setVisibility(View.GONE);
                this.headerBalance.setVisibility(View.VISIBLE);
                this.headerBalance.setText(FLHelper.trimTrailingZeros(String.format("%.2f", amount)) + " €");
            } else {
                this.headerCB.setVisibility(View.VISIBLE);
                this.headerBalance.setVisibility(View.GONE);
            }
        }
    }

    private void showBalanceDialog() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_balance);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_wallet_title);
        title.setTypeface(CustomFonts.customContentRegular(this), Typeface.BOLD);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_wallet_msg);
        text.setTypeface(CustomFonts.customContentRegular(this));

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
        if (this.validationDialog != null || !FloozApplication.appInForeground)
            return;

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        this.validationDialog = new Dialog(this);

        this.validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.validationDialog.setContentView(R.layout.custom_dialog_validate_transaction);

        TextView text = (TextView) this.validationDialog.findViewById(R.id.dialog_validate_flooz_text);
        text.setText(content);
        text.setTypeface(CustomFonts.customContentRegular(this));

        Button decline = (Button) this.validationDialog.findViewById(R.id.dialog_validate_flooz_decline);
        Button accept = (Button) this.validationDialog.findViewById(R.id.dialog_validate_flooz_accept);

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationDialog != null)
                    validationDialog.dismiss();
                validationDialog = null;
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                if (validationDialog != null)
                    validationDialog.dismiss();
                validationDialog = null;
                Intent intentNotifs = new Intent(instance, AuthenticationActivity.class);
                instance.startActivityForResult(intentNotifs, AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.validationDialog.setCanceledOnTouchOutside(false);
        this.validationDialog.setCancelable(false);
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
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            tmpUriImage = fileUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(intent, TAKE_PICTURE);
        }
    };

    private View.OnClickListener showAlbum = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PICTURE);
        }
    };

    public void changeUser(FLUser user) {
        this.currentReceiver = user;

        if (this.currentReceiver != null) {
            this.toPicker.clear();
            this.toPicker.clearComposingText();
            this.toPicker.setText(getResources().getString(R.string.TRANSACTION_CONTACT_PICKER_PREFIX));
            this.toPicker.addObject(this.currentReceiver);
            this.contactListAdapter.searchUser("");
            if (this.contentTextfield.getText().length() > 0)
                this.baseLayout.requestFocus();
            else
                this.contentTextfield.requestFocus();
        }
        this.validateView();
    }

    public void photoTaken(final Bitmap photo) {
        Handler mainHandler = new Handler(this.getMainLooper());
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

            }
        };
        mainHandler.post(myRunnable);
    }

    public void authenticationValidated() {
        this.transactionPending = true;
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
                    closeButton.performClick();
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
        if (sendIntent.resolveActivity(this.getPackageManager()) != null) {
            this.startActivity(sendIntent);
        }
        this.closeButton.performClick();
    }

    private Map<String, Object> generateRequestParams(boolean validate) {


        Map<String, Object> params = new HashMap<>();
        params.put("amount", this.amountTextfield.getText().toString());
        if (currentReceiver != null) {
            if (currentReceiver.userKind == FLUser.UserKind.FloozUser)
                params.put("to", currentReceiver.username.replace("+33", "0"));
            else
                params.put("to", currentReceiver.phone.replace("+33", "0"));

            if (currentReceiver.userKind == FLUser.UserKind.PhoneUser) {
                Map<String, Object> contact = new HashMap<>();
                if (currentReceiver.firstname != null)
                    contact.put("firstname", currentReceiver.firstname);

                if (currentReceiver.lastname != null)
                    contact.put("lastName", currentReceiver.lastname);

                if (contact.values().size() > 0)
                    params.put("contact", contact);
            }
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
    public void onPause() {
        saveData();
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }


    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        if (this.preset == null || !this.preset.blockBack)
            this.closeButton.performClick();
    }

    @Override
    public void scopeChanged(FLTransaction.TransactionScope scope) {
        this.tipContainer.dismiss(true);
        this.toolTipScope = null;
        this.currentScope = scope;
        this.updateScopeField();
    }
}
