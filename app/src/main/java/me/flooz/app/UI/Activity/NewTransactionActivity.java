package me.flooz.app.UI.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLPreset;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.FLTransactionActionBar;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.UI.View.SlidableRelativeLayout;
import me.flooz.app.UI.View.ToolTipScopeViewDelegate;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/13/15.
 */
public class NewTransactionActivity extends BaseActivity implements FLTransactionActionBar.FLTransactionActionBarDelegate{
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int PERMISSION_CAMERA = 3;
    private static final int PICK_LOCATION = 5;
    private static final int PERMISSION_LOCATION = 6;
    private static final int PERMISSION_STORAGE = 7;

    private NewTransactionActivity instance;
    private FloozApplication floozApp;

    private FLUser currentReceiver = null;
    private FLPreset preset = null;

    private Uri tmpUriImage;
    private String random;
    private String savedAmount;
    private String savedWhy;

    public Boolean havePicture = false;
    public Bitmap currentPicture;
    public Bitmap currentPictureURL;

    FLTransaction.TransactionType currentType;
    private FLTransaction.TransactionScope currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

    private SlidableRelativeLayout baseLayout;
    private ImageView headerScope;
    private ImageView closeButton;
    private TextView toTextview;
    private EditText amountTextfield;
    private EditText contentTextfield;
    private RelativeLayout picContainer;
    private LoadingImageView pic;

    private FLTransactionActionBar actionBar;

    private Boolean transactionPending = false;

    private String imagePath;

    private JSONObject currentGeo;

    public void init() {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
        this.preset = null;
        this.currentPicture = null;
        this.havePicture = false;

        this.savedAmount = "";
        this.savedWhy = "";
    }

    public void initWithPreset(FLPreset data) {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
        this.preset = data;
        this.currentPicture = null;
        this.havePicture = false;

        if (!this.preset.isParticipation) {
            if (this.preset.to != null) {

            }
        }

        if (this.preset.amount != null)
            this.savedAmount = this.preset.amount.toString();
        else
            this.savedAmount = "";

        if (this.preset.why != null)
            this.savedWhy = this.preset.why;
        else
            this.savedWhy = "";

        if (this.preset.image != null) {
            ImageLoader.getInstance().loadImage(this.preset.image, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    instance.photoTaken(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.initWithPreset(new FLPreset(new JSONObject(getIntent().getStringExtra("triggerData"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        else
            this.init();

        if (FloozRestClient.getInstance().currentUser != null && FloozRestClient.getInstance().currentUser.settings != null)
            this.currentScope = FLTransaction.transactionScopeParamToEnum((String)((Map)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));

        this.setContentView(R.layout.transaction_fragment);

        this.baseLayout = (SlidableRelativeLayout) this.findViewById(R.id.new_transac_base);
        TextView headerTitle = (TextView) this.findViewById(R.id.new_transac_header_text);
        this.closeButton = (ImageView) this.findViewById(R.id.new_transac_close_button);
        this.headerScope = (ImageView) this.findViewById(R.id.new_transac_header_scope);
        this.toTextview = (TextView) this.findViewById(R.id.new_transac_to_container);
        this.amountTextfield = (EditText) this.findViewById(R.id.new_transac_amount_textfield);
        LinearLayout amountContainer = (LinearLayout) this.findViewById(R.id.new_transac_amount_container);
        TextView currencySymbol = (TextView) this.findViewById(R.id.new_transac_currency_symbol);
        this.contentTextfield = (EditText) this.findViewById(R.id.new_transac_content_textfield);
        this.picContainer = (RelativeLayout) this.findViewById(R.id.new_transac_pic_container);

        this.actionBar = (FLTransactionActionBar) this.findViewById(R.id.new_transac_action_bar);

        this.pic = (LoadingImageView) this.findViewById(R.id.new_transac_pic);
        this.pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomImageViewer.start(NewTransactionActivity.this, "file://" + imagePath);
            }
        });

        ImageView picDeleteButton = (ImageView) this.findViewById(R.id.new_transac_pic_delete);
        LinearLayout contentContainer = (LinearLayout) this.findViewById(R.id.content_background);

        if (this.preset == null || this.preset.close) {
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
        this.toTextview.setTypeface(CustomFonts.customContentRegular(this));

        this.amountTextfield.setRawInputType(Configuration.KEYBOARD_12KEY);

        this.headerScope.setColorFilter(this.getResources().getColor(R.color.blue));

        this.headerScope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.amountTextfield.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        if (this.currentReceiver != null)
            this.toTextview.setText(this.currentReceiver.fullname);
        else if (this.preset != null) {
            if (this.preset.isParticipation)
                this.toTextview.setText(this.preset.collectName);
            else
                this.toTextview.setText(this.preset.toFullname);
        }

        if (this.preset != null && (this.preset.blockTo || this.preset.isParticipation)) {
            this.toTextview.setClickable(false);
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

        if (this.preset != null && this.preset.isParticipation)
            this.actionBar.setType(FLTransactionActionBar.ActionBarType.ActionBarTypeParticipation);
        else
            this.actionBar.setType(FLTransactionActionBar.ActionBarType.ActionBarTypeBase);

        this.actionBar.setCurrentPreset(this.preset);

        this.actionBar.delegate = this;

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
            }
        });


        this.updateScopeField();

        if (getIntent() != null && getIntent().hasExtra("user"))
            try {
                this.changeUser(new FLUser(new JSONObject(getIntent().getStringExtra("user"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (this.havePicture) {
            this.picContainer.setVisibility(View.VISIBLE);
//            this.pic.setImageBitmap(currentPicture);
        }

        picDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picContainer.setVisibility(View.GONE);
                havePicture = false;
                currentPicture = null;
            }
        });

        this.contentTextfield.setFocusable(true);
        this.contentTextfield.setFocusableInTouchMode(true);

        this.contentTextfield.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                actionBar.highlightButton(0, hasFocus);
            }
        });

        this.contentTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        contentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                contentTextfield.requestFocus();
                imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_FORCED);
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
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {

                Uri imageUri;
                if (data == null || data.getData() == null) {
                    imageUri = this.tmpUriImage;
                } else
                    imageUri = data.getData();

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String path = ImageHelper.getPath(instance, selectedImageUri);
                            if (path != null) {
                                imagePath = path;
                                File image = new File(path);
                                Bitmap photo = null;
                                BitmapFactory.Options bmOptions;

                                try {
                                    photo = BitmapFactory.decodeFile(image.getAbsolutePath());
                                } catch (OutOfMemoryError e) {
                                    try {
                                        bmOptions = new BitmapFactory.Options();
                                        bmOptions.inSampleSize = 2;
                                        photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                                    } catch (Exception f) {
                                        f.printStackTrace();
                                    }
                                }

                                if (photo != null) {
                                    int rotation = ImageHelper.getRotation(instance, selectedImageUri);
                                    if (rotation != 0) {
                                        photo = ImageHelper.rotateBitmap(photo, rotation);
                                    }

                                    photoTaken(photo);
                                }
                            }
                        }
                    });
                }
            } else if (requestCode == PICK_LOCATION) {
                if (data.hasExtra("geo")) {
                    try {
                        currentGeo = new JSONObject(data.getStringExtra("geo"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        currentGeo = null;
                    }
                } else {
                    currentGeo = null;
                }

                this.actionBar.setCurrentGeo(currentGeo);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (FloozRestClient.getInstance().currentUser != null)
            this.currentScope = FLTransaction.transactionScopeParamToEnum((String) ((Map) FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));
        else
            this.currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

        if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.READ_CONTACTS)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.amountTextfield.setText(this.savedAmount);
        this.contentTextfield.setText(this.savedWhy);

        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (this.amountTextfield.getText().length() == 0 && ((this.preset != null && !this.preset.blockAmount) || this.preset == null)) {
            this.amountTextfield.requestFocus();
            imm.showSoftInput(amountTextfield, InputMethodManager.SHOW_FORCED);
        } else if (this.contentTextfield.getText().length() == 0 && ((this.preset != null && !this.preset.blockWhy) || this.preset == null)) {
            this.contentTextfield.requestFocus();
            imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_FORCED);
        } else {
            this.baseLayout.requestFocus();
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }


        this.actionBar.updateGeo();

        if (this.transactionPending)
            FloozRestClient.getInstance().showLoadView();
    }

    private void saveData() {
        this.savedAmount = this.amountTextfield.getText().toString();
        this.savedWhy = this.contentTextfield.getText().toString();
    }

    private void updateScopeField() {
//        this.scopePic.setImageDrawable(FLTransaction.transactionScopeToImage(this.currentScope));
    }

    private void performTransaction(FLTransaction.TransactionType type) {
        this.currentType = type;

        FloozRestClient.getInstance().showLoadView();
        FloozRestClient.getInstance().performTransaction(this.generateRequestParams(true), new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private View.OnClickListener showCamera = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();

            List<ActionSheetItem> items = new ArrayList<>();

            items.add(new ActionSheetItem(instance, R.string.GLOBAL_CAMERA, new ActionSheetItem.ActionSheetItemClickListener() {
                @Override
                public void onClick() {
                    if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                        ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
//                }
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        tmpUriImage = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUriImage);

                        try {
                            startActivityForResult(intent, TAKE_PICTURE);
                        } catch (ActivityNotFoundException e) {

                        }
                    }
                }
            }));

            items.add(new ActionSheetItem(instance, R.string.GLOBAL_ALBUMS, new ActionSheetItem.ActionSheetItemClickListener() {
                @Override
                public void onClick() {
                    if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                        ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
//                }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        try {
                            startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
                        } catch (ActivityNotFoundException e) {

                        }
                    }
                }
            }));

            ActionSheet.showWithItems(instance, items);

        }
    };

    private View.OnClickListener showGeo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveData();
            if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
//                }
            } else {
                Intent intentGeo = new Intent(instance, LocationActivity.class);

                if (currentGeo != null)
                    intentGeo.putExtra("geo", currentGeo.toString());

                instance.startActivityForResult(intentGeo, PICK_LOCATION);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }
    };

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                tmpUriImage = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUriImage);

                try {
                    startActivityForResult(intent, TAKE_PICTURE);
                } catch (ActivityNotFoundException e) {
                }
            }
        } else if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
                } catch (ActivityNotFoundException e) {

                }
            }
        } else if (requestCode == PERMISSION_LOCATION) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                Intent intentGeo = new Intent(instance, LocationActivity.class);

                if (currentGeo != null)
                    intentGeo.putExtra("geo", currentGeo.toString());

                instance.startActivityForResult(intentGeo, PICK_LOCATION);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }
    }

    public void changeUser(FLUser user) {
//        this.currentReceiver = user;
//
//        if (this.currentReceiver != null) {
//            this.toPicker.clear();
//            this.toPicker.clearText();
//            this.toPicker.addObject(this.currentReceiver);
//            this.contactListAdapter.searchUser("");
//            if (this.contentTextfield.getText().length() > 0)
//                this.baseLayout.requestFocus();
//            else
//                this.contentTextfield.requestFocus();
//
//            this.currentReceiver = user;
//        }
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

                int nh = (int) (photo.getHeight() * (256.0 / photo.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(photo, 256, nh, true);
//                pic.setImageBitmap(scaled);

                currentPicture = photo;
            }
        };

        mainHandler.post(myRunnable);
    }

    private Map<String, Object> generateRequestParams(boolean validate) {

        Map<String, Object> params = new HashMap<>();
        params.put("amount", this.amountTextfield.getText().toString());
        if (currentReceiver != null) {
            if (currentReceiver.userKind == FLUser.UserKind.FloozUser)
                params.put("to", currentReceiver.username);
            else
                params.put("to", currentReceiver.phone);

            if (currentReceiver.userKind == FLUser.UserKind.PhoneUser) {
                currentReceiver = ContactsManager.fillUserInformations(currentReceiver);

                Map<String, Object> contact = new HashMap<>();
                if (currentReceiver.firstname != null && !currentReceiver.firstname.isEmpty())
                    contact.put("firstName", currentReceiver.firstname);

                if (currentReceiver.lastname != null && !currentReceiver.lastname.isEmpty())
                    contact.put("lastName", currentReceiver.lastname);

                if (currentReceiver.fullname != null && !currentReceiver.fullname.isEmpty())
                    contact.put("name", currentReceiver.fullname);

                if (contact.values().size() > 0)
                    params.put("contact", contact);
            }
        } else
            params.put("to", "");

        if (havePicture)
            params.put("hasImage", true);

        params.put("random", random);
        params.put("method", FLTransaction.transactionTypeToParams(this.currentType));
        params.put("scope", FLTransaction.transactionScopeToParams(this.currentScope));
        params.put("why", this.contentTextfield.getText().toString());
        params.put("validate", validate);

        if (currentGeo != null)
            try {
                params.put("geo", JSONHelper.toMap(currentGeo));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (this.preset != null && this.preset.payload != null)
            params.put("payload", this.preset.payload);

        return params;
    }

    @Override
    public void onPause() {
        saveData();
        clearReferences();

        this.hideKeyboard();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        FloozRestClient.getInstance().clearLocationData();

        super.onDestroy();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        if (this.preset == null || this.preset.close)
            this.closeButton.performClick();
    }

    public void scopeChanged(FLTransaction.TransactionScope scope) {
//        this.tipContainer.dismiss(true);
//        this.tipContainer.setVisibility(View.GONE);
//        this.toolTipScope = null;
//        this.currentScope = scope;
//        this.updateScopeField();
//
//        if (isDemo && demoCurrentStep < preset.steps.length()) {
//            NewTransactionActivity.this.showDemoStepPopover(NewTransactionActivity.this.preset.steps.optJSONObject(demoCurrentStep));
//        }
//
//        this.validateView();
    }

    @Override
    public void presentCamera() {
        if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
            ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
//                }
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            tmpUriImage = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUriImage);

            try {
                startActivityForResult(intent, TAKE_PICTURE);
            } catch (ActivityNotFoundException e) {

            }
        }
    }

    @Override
    public void presentLocation() {
        saveData();
        if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
            ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
//                }
        } else {
            Intent intentGeo = new Intent(instance, LocationActivity.class);

            if (currentGeo != null)
                intentGeo.putExtra("geo", currentGeo.toString());

            instance.startActivityForResult(intentGeo, PICK_LOCATION);
            instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    @Override
    public void presentImagePicker() {
        saveData();

        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(instance, R.string.GLOBAL_ALBUMS, new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {
                if (ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(NewTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                    ActivityCompat.requestPermissions(NewTransactionActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
//                }
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    tmpUriImage = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpUriImage);

                    try {
                        startActivityForResult(intent, TAKE_PICTURE);
                    } catch (ActivityNotFoundException e) {

                    }
                }
            }
        }));

        items.add(new ActionSheetItem(instance, "Rechercher sur le Web", new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {

            }
        }));

        ActionSheet.showWithItems(instance, items);

    }

    @Override
    public void presentGIFPicker() {

    }

    @Override
    public void focusDescription() {
        if (contentTextfield.isFocused()) {
            contentTextfield.clearFocus();
            this.hideKeyboard();
        } else {
            contentTextfield.requestFocus();

            final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public void validatePay() {
        performTransaction(FLTransaction.TransactionType.TransactionTypePayment);
    }

    @Override
    public void validateCharge() {
        performTransaction(FLTransaction.TransactionType.TransactionTypeCharge);
    }

    @Override
    public void validateParticipate() {

    }

    @Override
    public void validateCreate() {

    }
}
