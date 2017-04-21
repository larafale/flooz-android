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
import android.widget.ScrollView;
import android.widget.TextView;

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
import me.flooz.app.Model.FLScope;
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
public class NewTransactionActivity extends BaseActivity implements FLTransactionActionBar.FLTransactionActionBarDelegate {
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int PERMISSION_CAMERA = 3;
    private static final int PICK_SCOPE = 4;
    private static final int PICK_LOCATION = 5;
    private static final int PICK_IMAGE = 6;
    private static final int PERMISSION_LOCATION = 6;
    private static final int PERMISSION_STORAGE = 7;

    private NewTransactionActivity instance;
    private FloozApplication floozApp;

    private FLUser currentReceiver = null;
    private FLPreset preset = null;
    private String to;
    private String toFullName;
    private JSONObject toInfos;
    private JSONObject blockUser;

    private Uri tmpUriImage;
    private String random;
    private String savedAmount;
    private String savedWhy;

    public Boolean havePicture = false;
    public Bitmap currentPicture;
    public String currentPictureURL;

    FLTransaction.TransactionType currentType;
    private FLScope currentScope = FLScope.defaultScope(FLScope.FLScopeKey.FLScopePublic);

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
        this.currentPicture = null;
        this.havePicture = false;
        this.blockUser = null;

        this.savedAmount = "";
        this.savedWhy = "";
        this.preset = new FLPreset(new JSONObject());
    }

    public void initWithPreset(FLPreset data) {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
        this.preset = data;
        this.currentPicture = null;
        this.havePicture = false;
        this.blockUser = null;

        if (!this.preset.isParticipation) {
            if (this.preset.to != null) {
                this.to = this.preset.to;
                this.toFullName = this.preset.toFullname;
                this.toInfos = this.preset.toInfos;
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
            havePicture = true;
            currentPictureURL = this.preset.image;
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
            this.currentScope = FLScope.scopeFromObject(((Map)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));

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

        ScrollView scrollView = (ScrollView) this.findViewById(R.id.content_scroll);

        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentTextfield.requestFocus();

                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        this.actionBar = (FLTransactionActionBar) this.findViewById(R.id.new_transac_action_bar);

        this.pic = (LoadingImageView) this.findViewById(R.id.new_transac_pic);

        this.pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPicture != null)
                    CustomImageViewer.start(NewTransactionActivity.this, "file://" + imagePath, FLTransaction.TransactionAttachmentType.TransactionAttachmentImage);
                else if (currentPictureURL != null)
                    CustomImageViewer.start(NewTransactionActivity.this, currentPictureURL, FLTransaction.TransactionAttachmentType.TransactionAttachmentImage);
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
                Intent scopeIntent = new Intent(NewTransactionActivity.this, ScopePickerActivity.class);

                scopeIntent.putExtra("scope", currentScope.keyString);

                if (preset != null && preset.options.scopes != null)
                    scopeIntent.putExtra("preset", preset.jsonData.toString());

                instance.startActivityForResult(scopeIntent, PICK_SCOPE);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        if (preset != null && preset.options.scopes != null && preset.options.scopes.length() < 2)
            this.headerScope.setVisibility(View.INVISIBLE);

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

        if (this.preset != null && (!this.preset.options.allowTo || this.preset.isParticipation)) {
            this.toTextview.setClickable(false);
        }

        if (this.preset == null || this.preset.options.allowAmount) {
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

            if (this.currentPictureURL != null) {
                pic.setImageFromUrl(this.currentPictureURL);
            } else if (this.currentPicture != null) {
                pic.setImageFromUrl("file://" + imagePath);
            }
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

            if (!this.preset.options.allowWhy) {
                this.contentTextfield.setFocusable(false);
                this.contentTextfield.setFocusableInTouchMode(false);
                this.contentTextfield.setHint("");
            }
        }

        if (preset != null && preset.options.scope != null)
            this.currentScope = this.preset.options.scope;
        else {
            if (FloozRestClient.getInstance().currentUser != null)
                this.currentScope = FLScope.scopeFromObject(((Map)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));
            else
                this.currentScope = FLScope.defaultScope(FLScope.FLScopeKey.FLScopePublic);
        }

        this.updateScopeField();
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
            } else if (requestCode == PICK_IMAGE) {
                if (data.hasExtra("imageUrl")) {
                    currentPicture = null;
                    imagePath = null;
                    currentPictureURL = data.getStringExtra("imageUrl");

                    Handler mainHandler = new Handler(this.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (currentPicture != null)
                                currentPicture.recycle();
                            havePicture = true;
                            picContainer.setVisibility(View.VISIBLE);

                            pic.setImageFromUrl(currentPictureURL);
                        }
                    };

                    mainHandler.post(myRunnable);
                }
            } else if (requestCode == PICK_SCOPE) {
                if (data.hasExtra("scope")) {
                    currentScope = FLScope.scopeFromObject(data.getStringExtra("scope"));

                    Handler mainHandler = new Handler(this.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            updateScopeField();
                        }
                    };

                    mainHandler.post(myRunnable);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

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
        if (this.amountTextfield.getText().length() == 0 && ((this.preset != null && this.preset.options.allowAmount) || this.preset == null)) {
            this.amountTextfield.requestFocus();
            imm.showSoftInput(amountTextfield, InputMethodManager.SHOW_FORCED);
        } else if (this.contentTextfield.getText().length() == 0 && ((this.preset != null && this.preset.options.allowWhy) || this.preset == null)) {
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
        this.currentScope.displayImage(this.headerScope);
    }

    private void performTransaction(FLTransaction.TransactionType type) {
        this.currentType = type;

        this.hideKeyboard();

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
        this.currentReceiver = user;

        if (this.currentReceiver != null) {
            this.to = this.currentReceiver.username;
            this.toFullName = this.currentReceiver.fullname;
            this.blockUser = this.currentReceiver.floozOptions;

            this.toTextview.setText(this.toFullName);

            if (this.contentTextfield.getText().length() > 0)
                this.baseLayout.requestFocus();
            else
                this.contentTextfield.requestFocus();
        }
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

                pic.setImageFromUrl("file://" + imagePath);

                currentPicture = photo;
            }
        };

        mainHandler.post(myRunnable);
    }

    private Map<String, Object> generateRequestParams(boolean validate) {

        Map<String, Object> params = new HashMap<>();
        params.put("amount", this.amountTextfield.getText().toString());
        if (to != null) {
            params.put("to", this.to);

            if (this.toInfos != null)
                params.put("contact", this.toInfos);
        } else
            params.put("to", "");

        if (havePicture) {
            if (currentPicture != null)
                params.put("hasImage", true);
            else if (currentPictureURL != null)
                params.put("imageUrl", currentPictureURL);
        }

        params.put("random", random);
        params.put("method", FLTransaction.transactionTypeToParams(this.currentType));
        params.put("scope", this.currentScope.keyString);
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

        items.add(new ActionSheetItem(instance, "Rechercher sur le Web", new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {
                Intent intentImage = new Intent(instance, ImagePickerActivity.class);

                intentImage.putExtra("type", "web");

                instance.startActivityForResult(intentImage, PICK_IMAGE);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }));

        ActionSheet.showWithItems(instance, items);
    }

    @Override
    public void presentGIFPicker() {
        Intent intentGif = new Intent(instance, ImagePickerActivity.class);

        intentGif.putExtra("type", "gif");

        instance.startActivityForResult(intentGif, PICK_IMAGE);
        instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
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
        this.hideKeyboard();

        FloozRestClient.getInstance().showLoadView();
        FloozRestClient.getInstance().createParticipationValidate(preset.presetId, generateRequestParams(true), null);
    }

    @Override
    public void validateCreate() {

    }
}
