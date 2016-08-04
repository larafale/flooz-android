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
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
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
public class NewCollectActivity extends BaseActivity implements ToolTipScopeViewDelegate {
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int PERMISSION_CAMERA = 3;
    private static final int PERMISSION_CONTACTS = 4;
    private static final int PICK_LOCATION = 5;
    private static final int PERMISSION_LOCATION = 6;
    private static final int PERMISSION_STORAGE = 7;

    private FloozApplication floozApp;

    private FLUser currentReceiver = null;
    private FLPreset preset = null;

    private Uri tmpUriImage;
    private String random;
    private String savedName;
    private String savedWhy;
    private Boolean isDemo = false;
    private int demoCurrentStep = 0;

    public Boolean havePicture = false;
    public Bitmap currentPicture;

    FLTransaction.TransactionType currentType;
    private FLTransaction.TransactionScope currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

    private LinearLayout baseLayout;
    private ImageView closeButton;
    private EditText nameTextfield;
    private EditText contentTextfield;
    private RelativeLayout picContainer;
    private ImageView pic;
    private ImageView scopePic;
    private TextView createButton;
    private ImageView capturePicButton;
    private ImageView locationButton;

    private LinearLayout locationContainer;
    private ImageView locationPic;
    private TextView locationText;

    private ToolTip toolTipScope;
    private ToolTipScopeView tooltipScopeView;

    private ToolTipLayout tipContainer;
    private ToolTipLayout demoContainer;
    private View demoPassthroughView = null;
    private ToolTip demoToolTip;

    private Boolean transactionPending = false;

    private Dialog popupDialog;

    private String imagePath;

    private JSONObject currentGeo;

    public void init() {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
        this.preset = null;
        this.currentPicture = null;
        this.havePicture = false;

        this.savedName = "";
        this.savedWhy = "";
    }

    public void initWithPreset(FLPreset data) {
        this.random = FLHelper.generateRandomString();
        this.currentReceiver = null;
        this.preset = data;
        this.currentPicture = null;
        this.havePicture = false;

//        if (this.preset.to != null)
//            this.currentReceiver = this.preset.to;

        if (this.preset.name != null)
            this.savedName = this.preset.name;
        else
            this.savedName = "";

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
                    NewCollectActivity.this.photoTaken(loadedImage);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
        }

        this.isDemo = preset.popup != null || preset.steps != null;
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

        if (FloozRestClient.getInstance().currentUser != null && FloozRestClient.getInstance().currentUser.settings != null)
            this.currentScope = FLTransaction.transactionScopeParamToEnum((String)((Map)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope"));

        this.setContentView(R.layout.new_collect_activity);

        this.baseLayout = (LinearLayout) this.findViewById(R.id.new_collect_base);
        TextView headerTitle = (TextView) this.findViewById(R.id.new_collect_header_text);
        this.closeButton = (ImageView) this.findViewById(R.id.new_collect_close_button);
        this.nameTextfield = (EditText) this.findViewById(R.id.new_collect_name_textfield);
        this.contentTextfield = (EditText) this.findViewById(R.id.new_collect_content_textfield);
        this.picContainer = (RelativeLayout) this.findViewById(R.id.new_collect_pic_container);
        this.locationContainer = (LinearLayout) this.findViewById(R.id.new_collect_geo);
        this.locationPic = (ImageView) this.findViewById(R.id.new_collect_geo_pic);
        this.locationText = (TextView) this.findViewById(R.id.new_collect_geo_text);

        this.pic = (ImageView) this.findViewById(R.id.new_collect_pic);
        this.pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomImageViewer.start(NewCollectActivity.this, "file://" + imagePath);
            }
        });

        ImageView picDeleteButton = (ImageView) this.findViewById(R.id.new_collect_pic_delete);
        this.scopePic = (ImageView) this.findViewById(R.id.new_collect_scope_pic);
        this.capturePicButton = (ImageView) this.findViewById(R.id.new_collect_pic_button);
        this.locationButton = (ImageView) this.findViewById(R.id.new_collect_geo_button);
        this.createButton = (TextView) this.findViewById(R.id.new_collect_collect);
        this.tipContainer = (ToolTipLayout) this.findViewById(R.id.new_collect_tooltip_container);
        this.demoContainer = (ToolTipLayout) this.findViewById(R.id.new_collect_demo_container);
        RelativeLayout contentContainer = (RelativeLayout) this.findViewById(R.id.new_collect_content);

        this.scopePic.setColorFilter(this.getResources().getColor(android.R.color.white));
        this.locationButton.setColorFilter(this.getResources().getColor(android.R.color.white));
        this.capturePicButton.setColorFilter(this.getResources().getColor(android.R.color.white));
        this.locationPic.setColorFilter(this.getResources().getColor(android.R.color.white));

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
        this.nameTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.contentTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.createButton.setTypeface(CustomFonts.customTitleLight(this));
        this.locationText.setTypeface(CustomFonts.customContentRegular(this));

        capturePicButton.setColorFilter(this.getResources().getColor(android.R.color.white));

        this.nameTextfield.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && isDemo && demoCurrentStep < preset.steps.length()) {
                    NewCollectActivity.this.showDemoStepPopover(NewCollectActivity.this.preset.steps.optJSONObject(demoCurrentStep));
                }
            }
        });

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

        capturePicButton.setOnClickListener(showCamera);
        locationButton.setOnClickListener(showGeo);

        this.tooltipScopeView = new ToolTipScopeView(this);
        this.tooltipScopeView.changeScope(this.currentScope);
        this.tooltipScopeView.delegate = this;

        this.updateScopeField();

        this.scopePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toolTipScope != null) {
                    scopeChanged(currentScope);
                } else {
                    if (tooltipScopeView.view.getParent() != null)
                        ((ViewGroup) tooltipScopeView.view.getParent()).removeView(tooltipScopeView.view);

                    toolTipScope = new ToolTip.Builder(NewCollectActivity.this)
                            .anchor(scopePic)
                            .gravity(Gravity.TOP)
                            .color(NewCollectActivity.this.getResources().getColor(android.R.color.white))
                            .pointerSize(25)
                            .contentView(tooltipScopeView.view)
                            .dismissOnTouch(false)
                            .build();

                    tipContainer.setVisibility(View.VISIBLE);
                    tipContainer.addTooltip(toolTipScope, true);
                }
            }
        });

        this.tipContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scopeChanged(currentScope);
            }
        });

        if (this.havePicture) {
            this.picContainer.setVisibility(View.VISIBLE);
            this.pic.setImageBitmap(currentPicture);
        }

        picDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picContainer.setVisibility(View.GONE);
                havePicture = false;
                currentPicture = null;
            }
        });

        this.createButton.setOnClickListener(createCollect);

        this.contentTextfield.setFocusable(true);
        this.contentTextfield.setFocusableInTouchMode(true);

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

        this.demoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (demoPassthroughView != null) {
                        Rect viewRect = new Rect();
                        Rect containerRect = new Rect();
                        if (demoPassthroughView.getGlobalVisibleRect(viewRect) && demoContainer.getGlobalVisibleRect(containerRect)) {
                            int eventX = (int) event.getX() + containerRect.left;
                            int eventY = (int) event.getY() + containerRect.top;
                            if (viewRect.contains(eventX, eventY)) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                String focus = preset.steps.optJSONObject(demoCurrentStep - 1).optString("focus");
                                NewCollectActivity.this.demoContainer.dismiss(true);
                                NewCollectActivity.this.demoContainer.setVisibility(View.GONE);

                                if (focus.contentEquals("why") && !preset.blockWhy) {
                                    contentTextfield.requestFocus();
                                    imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_FORCED);
                                } else if (focus.contentEquals("name")) {
                                    nameTextfield.requestFocus();
                                    imm.showSoftInput(nameTextfield, InputMethodManager.SHOW_FORCED);
                                } else if (focus.contentEquals("scope")) {
                                    scopePic.performClick();
                                }
                            }
                        }
                    }
                }
                return true;
            }
        });

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

                this.updateGeo();
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

        if (ActivityCompat.checkSelfPermission(NewCollectActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(NewCollectActivity.this,  Manifest.permission.READ_CONTACTS)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(NewCollectActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_CAMERA);
            }
        }
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
        if (!this.isDemo) {
            if (this.nameTextfield.getText().length() == 0) {
                this.nameTextfield.requestFocus();
                imm.showSoftInput(nameTextfield, InputMethodManager.SHOW_FORCED);
            } else if (this.contentTextfield.getText().length() == 0 && ((this.preset != null && !this.preset.blockWhy) || this.preset == null)) {
                this.contentTextfield.requestFocus();
                imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_FORCED);
            } else {
                this.baseLayout.requestFocus();
                imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        } else {
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            if (this.preset != null && this.preset.popup != null && popupDialog == null) {
                CustomDialog.show(this, this.preset.popup, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (preset.steps != null)
                            showDemoStepPopover(preset.steps.optJSONObject(demoCurrentStep));
                    }
                });
            } else if (this.preset != null && this.preset.steps != null && popupDialog == null)
                this.showDemoStepPopover(this.preset.steps.optJSONObject(demoCurrentStep));
        }

        this.updateGeo();

        if (this.transactionPending)
            FloozRestClient.getInstance().showLoadView();
    }

    private void showDemoStepPopover(final JSONObject stepData) {
        if (stepData.optString("focus").contentEquals("fb")) {
            this.demoCurrentStep++;
            if (this.demoCurrentStep < preset.steps.length()) {
                this.showDemoStepPopover(this.preset.steps.optJSONObject(demoCurrentStep));
            }
            return;
        }
        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

        View popoverView = LayoutInflater.from(this).inflate(R.layout.demo_tooltip, null);

        LinearLayout container = (LinearLayout) popoverView.findViewById(R.id.demo_tooltip_container);
        final TextView step = (TextView) popoverView.findViewById(R.id.demo_tooltip_step);
        TextView title = (TextView) popoverView.findViewById(R.id.demo_tooltip_title);
        TextView content = (TextView) popoverView.findViewById(R.id.demo_tooltip_content);
        Button button = (Button) popoverView.findViewById(R.id.demo_tooltip_btn);

        step.setTypeface(CustomFonts.customContentBold(this));
        title.setTypeface(CustomFonts.customContentBold(this));
        content.setTypeface(CustomFonts.customContentRegular(this));
        button.setTypeface(CustomFonts.customContentBold(this));

        container.setGravity(this.getDemoStepPopoverGravity(stepData.optString("focus")));
        step.setText("" + (demoCurrentStep + 1));
        title.setText(stepData.optString("title"));
        content.setText(stepData.optString("desc"));
        button.setText(stepData.optString("btn"));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewCollectActivity.this.demoContainer.dismiss(true);
                NewCollectActivity.this.demoContainer.setVisibility(View.GONE);

                if (stepData.optString("focus").contentEquals("why") && !preset.blockWhy) {
                    contentTextfield.requestFocus();
                    imm.showSoftInput(contentTextfield, InputMethodManager.SHOW_FORCED);
                } else if (stepData.optString("focus").contentEquals("name") && !preset.blockTo) {
                    nameTextfield.requestFocus();
                    imm.showSoftInput(nameTextfield, InputMethodManager.SHOW_FORCED);
                } else if (stepData.optString("focus").contentEquals("scope")) {
                    scopePic.performClick();
                } else if (demoCurrentStep < preset.steps.length()) {
                    NewCollectActivity.this.showDemoStepPopover(NewCollectActivity.this.preset.steps.optJSONObject(demoCurrentStep));
                }
            }
        });

        this.setDemoStepPopoverPassthroughView(stepData.optString("focus"));

        this.demoToolTip = new ToolTip.Builder(this)
                .anchor(this.getDemoStepPopoverView(stepData.optString("focus")))
                .gravity(this.getDemoStepPopoverArrow(stepData.optString("focus")))
                .color(this.getResources().getColor(R.color.blue))
                .pointerSize(25)
                .contentView(popoverView)
                .build();

        this.demoContainer.setVisibility(View.VISIBLE);
        this.demoContainer.addTooltip(this.demoToolTip);

        this.demoCurrentStep++;

        if (demoCurrentStep == preset.steps.length())
            isDemo = false;
    }

    private View getDemoStepPopoverView(String focus) {
        if (focus.contentEquals("name")) {
            return nameTextfield;
        }
        if (focus.contentEquals("image")) {
            return capturePicButton;
        }
        if (focus.contentEquals("scope")) {
            return scopePic;
        }
        if (focus.contentEquals("why")) {
            return contentTextfield;
        }
        if (focus.contentEquals("collect")) {
            return createButton;
        }
        if (focus.contentEquals("geo")) {
            return locationButton;
        }
        return null;
    }

    private int getDemoStepPopoverArrow(String focus) {
        if (focus.contentEquals("name")) {
            return Gravity.BOTTOM;
        }
        if (focus.contentEquals("image")) {
            return Gravity.TOP;
        }
        if (focus.contentEquals("scope")) {
            return Gravity.TOP;
        }
        if (focus.contentEquals("why")) {
            return Gravity.BOTTOM;
        }
        if (focus.contentEquals("pay")) {
            return Gravity.TOP;
        }
        if (focus.contentEquals("geo")) {
            return Gravity.TOP;
        }
        return 0;
    }

    private int getDemoStepPopoverGravity(String focus) {
        if (focus.contentEquals("name")) {
            return Gravity.LEFT;
        }
        if (focus.contentEquals("image")) {
            return Gravity.LEFT;
        }
        if (focus.contentEquals("scope")) {
            return Gravity.LEFT;
        }
        if (focus.contentEquals("why")) {
            return Gravity.LEFT;
        }
        if (focus.contentEquals("pay")) {
            return Gravity.RIGHT;
        }
        if (focus.contentEquals("geo")) {
            return Gravity.LEFT;
        }
        return 0;
    }

    private void setDemoStepPopoverPassthroughView(String focus) {
        if (focus.contentEquals("name")) {
            this.demoPassthroughView = this.nameTextfield;
        }
        else if (focus.contentEquals("scope")) {
            this.demoPassthroughView = this.scopePic;
        }
        else if (focus.contentEquals("why") && !this.preset.blockWhy) {
            this.demoPassthroughView = this.contentTextfield;
        } else
            this.demoPassthroughView = null;
    }

    private void saveData() {
        this.savedName = this.nameTextfield.getText().toString();
        this.savedWhy = this.contentTextfield.getText().toString();
    }

    private void updateScopeField() {
        this.scopePic.setImageDrawable(FLTransaction.transactionScopeToImage(this.currentScope));
    }

    public void createCollect() {
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

    private View.OnClickListener createCollect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createCollect();
        }
    };

    private View.OnClickListener showCamera = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();

            List<ActionSheetItem> items = new ArrayList<>();

            items.add(new ActionSheetItem(NewCollectActivity.this, R.string.GLOBAL_CAMERA, new ActionSheetItem.ActionSheetItemClickListener() {
                @Override
                public void onClick() {
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
            }));

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

            ActionSheet.showWithItems(NewCollectActivity.this, items);

        }
    };

    private View.OnClickListener showGeo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
                Intent intentGeo = new Intent(NewCollectActivity.this, LocationActivity.class);

                if (currentGeo != null)
                    intentGeo.putExtra("geo", currentGeo.toString());

                NewCollectActivity.this.startActivityForResult(intentGeo, PICK_LOCATION);
                NewCollectActivity.this.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }
    }

    public void updateGeo() {
        if (this.currentGeo != null) {
            this.locationButton.setColorFilter(this.getResources().getColor(R.color.blue));
            this.locationContainer.setVisibility(View.VISIBLE);
            this.locationText.setText(this.currentGeo.optString("name"));
        } else {
            this.locationButton.setColorFilter(this.getResources().getColor(android.R.color.white));
            this.locationContainer.setVisibility(View.GONE);
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

                int nh = (int) (photo.getHeight() * (256.0 / photo.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(photo, 256, nh, true);
                pic.setImageBitmap(scaled);

                currentPicture = photo;
            }
        };

        mainHandler.post(myRunnable);
    }

    private Map<String, Object> generateRequestParams(boolean validate) {

        Map<String, Object> params = new HashMap<>();
        params.put("name", this.nameTextfield.getText().toString());

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
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
    public void scopeChanged(FLTransaction.TransactionScope scope) {
        this.tipContainer.dismiss(true);
        this.tipContainer.setVisibility(View.GONE);
        this.toolTipScope = null;
        this.currentScope = scope;
        this.updateScopeField();

        if (isDemo && demoCurrentStep < preset.steps.length()) {
            NewCollectActivity.this.showDemoStepPopover(NewCollectActivity.this.preset.steps.optJSONObject(demoCurrentStep));
        }
    }
}
