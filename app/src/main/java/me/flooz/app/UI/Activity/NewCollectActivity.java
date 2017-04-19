package me.flooz.app.UI.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.CustomDialog;
import me.flooz.app.UI.View.FLTransactionActionBar;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.UI.View.SlidableRelativeLayout;
import me.flooz.app.UI.View.TokenPickerLibrary.TokenCompleteTextView;
import me.flooz.app.UI.View.ToolTipScopeView;
import me.flooz.app.UI.View.ToolTipScopeViewDelegate;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 4/12/16.
 */
public class NewCollectActivity extends BaseActivity implements FLTransactionActionBar.FLTransactionActionBarDelegate {
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int PERMISSION_CAMERA = 3;
    private static final int PICK_LOCATION = 5;
    private static final int PICK_IMAGE = 6;
    private static final int PERMISSION_LOCATION = 6;
    private static final int PERMISSION_STORAGE = 7;

    private FloozApplication floozApp;

    private FLPreset preset = null;

    private Uri tmpUriImage;
    private String random;
    private String savedName;
    private String savedWhy;

    public Boolean havePicture = false;
    public Bitmap currentPicture;
    public String currentPictureURL;

    private SlidableRelativeLayout baseLayout;
    private ImageView closeButton;
    private EditText nameTextfield;
    private EditText contentTextfield;
    private RelativeLayout picContainer;
    private LoadingImageView pic;

    private FLTransactionActionBar actionBar;

    private Boolean transactionPending = false;

    private String imagePath;

    private JSONObject currentGeo;

    public void init() {
        this.random = FLHelper.generateRandomString();
        this.preset = null;
        this.currentPicture = null;
        this.havePicture = false;

        this.savedName = "";
        this.savedWhy = "";
    }

    public void initWithPreset(FLPreset data) {
        this.random = FLHelper.generateRandomString();
        this.preset = data;
        this.currentPicture = null;
        this.havePicture = false;

        if (this.preset.name != null)
            this.savedName = this.preset.name;
        else
            this.savedName = "";

        if (this.preset.why != null)
            this.savedWhy = this.preset.why;
        else
            this.savedWhy = "";

        if (this.preset.image != null) {
            this.havePicture = true;
            this.currentPictureURL = this.preset.image;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.initWithPreset(new FLPreset(new JSONObject(getIntent().getStringExtra("triggerData"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        else
            this.init();

        this.setContentView(R.layout.new_collect_activity);

        this.baseLayout = (SlidableRelativeLayout) this.findViewById(R.id.new_collect_base);
        TextView headerTitle = (TextView) this.findViewById(R.id.new_collect_header_text);
        this.closeButton = (ImageView) this.findViewById(R.id.new_collect_close_button);
        this.nameTextfield = (EditText) this.findViewById(R.id.new_collect_name_textfield);
        this.contentTextfield = (EditText) this.findViewById(R.id.new_collect_content_textfield);
        this.picContainer = (RelativeLayout) this.findViewById(R.id.new_collect_pic_container);

        this.actionBar = (FLTransactionActionBar) this.findViewById(R.id.new_collect_action_bar);

        this.pic = (LoadingImageView) this.findViewById(R.id.new_collect_pic);

        this.pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPicture != null)
                    CustomImageViewer.start(NewCollectActivity.this, "file://" + imagePath, FLTransaction.TransactionAttachmentType.TransactionAttachmentImage);
                else if (currentPictureURL != null)
                    CustomImageViewer.start(NewCollectActivity.this, currentPictureURL, FLTransaction.TransactionAttachmentType.TransactionAttachmentImage);
            }
        });

        ImageView picDeleteButton = (ImageView) this.findViewById(R.id.new_collect_pic_delete);
        LinearLayout contentContainer = (LinearLayout) this.findViewById(R.id.content_background);

        if (this.preset == null || this.preset.close) {
            this.closeButton.setVisibility(View.VISIBLE);
            this.closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transactionPending = false;
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                }
            });
        } else {
            this.closeButton.setVisibility(View.GONE);
        }

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.nameTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.contentTextfield.setTypeface(CustomFonts.customContentRegular(this));

        this.nameTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 30)
                    s.delete(30, s.length());
            }
        });

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
                currentPictureURL = null;
            }
        });


        this.actionBar.setType(FLTransactionActionBar.ActionBarType.ActionBarTypeCollect);

        this.actionBar.setCurrentPreset(this.preset);

        this.actionBar.delegate = this;

        this.contentTextfield.setFocusable(true);
        this.contentTextfield.setFocusableInTouchMode(true);

        this.contentTextfield.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                actionBar.highlightButton(0, hasFocus);
            }
        });

        ScrollView scrollView = (ScrollView) this.findViewById(R.id.content_scroll);

        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentTextfield.requestFocus();

                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_IMPLICIT);
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
                            String path = ImageHelper.getPath(NewCollectActivity.this, selectedImageUri);
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
                                    int rotation = ImageHelper.getRotation(NewCollectActivity.this, selectedImageUri);
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
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.nameTextfield.clearFocus();

        this.nameTextfield.setText(this.savedName);
        this.contentTextfield.setText(this.savedWhy);

        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (this.nameTextfield.getText().length() == 0) {
            this.nameTextfield.requestFocus();
            imm.showSoftInput(nameTextfield, InputMethodManager.SHOW_FORCED);
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
        this.savedName = this.nameTextfield.getText().toString();
        this.savedWhy = this.contentTextfield.getText().toString();
    }

    public void createCollect() {
        this.hideKeyboard();

        FloozRestClient.getInstance().showLoadView();
        FloozRestClient.getInstance().createCollect(this.generateRequestParams(true), new FloozHttpResponseHandler() {
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
                Intent intentGeo = new Intent(NewCollectActivity.this, LocationActivity.class);

                if (currentGeo != null)
                    intentGeo.putExtra("geo", currentGeo.toString());

                NewCollectActivity.this.startActivityForResult(intentGeo, PICK_LOCATION);
                NewCollectActivity.this.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
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
        params.put("name", this.nameTextfield.getText().toString());

        if (havePicture) {
            if (currentPicture != null)
                params.put("hasImage", true);
            else if (currentPictureURL != null)
                params.put("imageUrl", currentPictureURL);
        }

        params.put("random", random);
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
        if (ActivityCompat.checkSelfPermission(NewCollectActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(NewCollectActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
            ActivityCompat.requestPermissions(NewCollectActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
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
        if (ActivityCompat.checkSelfPermission(NewCollectActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(NewCollectActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
            ActivityCompat.requestPermissions(NewCollectActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
//                }
        } else {
            Intent intentGeo = new Intent(NewCollectActivity.this, LocationActivity.class);

            if (currentGeo != null)
                intentGeo.putExtra("geo", currentGeo.toString());

            NewCollectActivity.this.startActivityForResult(intentGeo, PICK_LOCATION);
            NewCollectActivity.this.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    @Override
    public void presentImagePicker() {
        saveData();

        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(NewCollectActivity.this, R.string.GLOBAL_ALBUMS, new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {
                if (ActivityCompat.checkSelfPermission(NewCollectActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                    ActivityCompat.requestPermissions(NewCollectActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
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

        items.add(new ActionSheetItem(NewCollectActivity.this, "Rechercher sur le Web", new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {
                Intent intentImage = new Intent(NewCollectActivity.this, ImagePickerActivity.class);

                intentImage.putExtra("type", "web");

                NewCollectActivity.this.startActivityForResult(intentImage, PICK_IMAGE);
                NewCollectActivity.this.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }));

        ActionSheet.showWithItems(NewCollectActivity.this, items);
    }

    @Override
    public void presentGIFPicker() {
        Intent intentGif = new Intent(NewCollectActivity.this, ImagePickerActivity.class);

        intentGif.putExtra("type", "gif");

        NewCollectActivity.this.startActivityForResult(intentGif, PICK_IMAGE);
        NewCollectActivity.this.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
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

    }

    @Override
    public void validateCharge() {

    }

    @Override
    public void validateParticipate() {

    }

    @Override
    public void validateCreate() {
        this.createCollect();
    }
}
