package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class CoordsSettingsActivity extends Activity {

    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;

    private CoordsSettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;

    private EditText phoneTextfield;
    private EditText emailTextfield;
    private EditText addressTextfield;
    private EditText zipCodeTextfield;
    private EditText cityTextfield;

    private TextView sendVerifyPhone;
    private TextView sendVerifyMail;
    private ImageView justificatoryButton;
    private ImageView justificatory2Button;

    private Button saveButton;

    private String currentDoc;

    private Uri tmpUriImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_coords_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_coord_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.settings_coord_header_title);
        this.phoneTextfield = (EditText) this.findViewById(R.id.settings_coord_phone);
        this.emailTextfield = (EditText) this.findViewById(R.id.settings_coord_email);
        this.addressTextfield = (EditText) this.findViewById(R.id.settings_coord_address);
        this.zipCodeTextfield = (EditText) this.findViewById(R.id.settings_coord_zip);
        this.cityTextfield = (EditText) this.findViewById(R.id.settings_coord_city);
        EditText justificatoryTextfield = (EditText) this.findViewById(R.id.settings_coord_justificatory);
        EditText justificatory2Textfield = (EditText) this.findViewById(R.id.settings_coord_justificatory2);
        this.sendVerifyPhone = (TextView) this.findViewById(R.id.settings_coord_verify_phone);
        this.sendVerifyMail = (TextView) this.findViewById(R.id.settings_coord_verify_email);
        this.justificatory2Button = (ImageView) this.findViewById(R.id.settings_coord_justificatory2_button);
        this.justificatoryButton = (ImageView) this.findViewById(R.id.settings_coord_justificatory_button);
        this.saveButton = (Button) this.findViewById(R.id.settings_coord_save);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.phoneTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.emailTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.addressTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.zipCodeTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.cityTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.sendVerifyPhone.setTypeface(CustomFonts.customContentRegular(this));
        this.sendVerifyMail.setTypeface(CustomFonts.customContentRegular(this));
        justificatoryTextfield.setTypeface(CustomFonts.customContentRegular(this));
        justificatory2Textfield.setTypeface(CustomFonts.customContentRegular(this));

        this.reloadView();

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            if (modal)
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        this.sendVerifyPhone.setOnClickListener(v -> {
            FloozRestClient.getInstance().sendSMSValidation();
            v.setVisibility(View.GONE);
        });

        this.sendVerifyMail.setOnClickListener(v -> {
            FloozRestClient.getInstance().sendEmailValidation();
            v.setVisibility(View.GONE);
        });

        justificatoryTextfield.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("justificatory").equals(0) || currentUser.checkDocuments.get("justificatory").equals(3)) {
                currentDoc = "justificatory";
                showImageActionMenu();
            }
        });

        this.justificatoryButton.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("justificatory").equals(0) || currentUser.checkDocuments.get("justificatory").equals(3)) {
                currentDoc = "justificatory";
                showImageActionMenu();
            }
        });

        justificatory2Textfield.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("justificatory2").equals(0) || currentUser.checkDocuments.get("justificatory2").equals(3)) {
                currentDoc = "justificatory2";
                showImageActionMenu();
            }
        });

        this.justificatory2Button.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("justificatory2").equals(0) || currentUser.checkDocuments.get("justificatory2").equals(3)) {
                currentDoc = "justificatory2";
                showImageActionMenu();
            }
        });

        this.phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 10)
                    s.delete(10, s.length());
            }
        });

        this.zipCodeTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 5)
                    s.delete(5, s.length());
            }
        });

        this.cityTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                saveButton.performClick();
            }
            return false;
        });

        this.saveButton.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            Map<String, Object> param = new HashMap<>();
            Map<String, Object> settings = new HashMap<>();
            Map<String, Object> address = new HashMap<>();

            if (!phoneTextfield.getText().toString().contentEquals(currentUser.phone.replace("+33", "0")))
                param.put("phone", phoneTextfield.getText().toString());

            if (!emailTextfield.getText().toString().contentEquals(currentUser.email))
                param.put("email", emailTextfield.getText().toString());

            if (((currentUser.address.get("address") == null || (currentUser.address.get("address") != null && currentUser.address.get("address").isEmpty())) && addressTextfield.getText().length() > 0)
                    || (currentUser.address.get("address") != null && !currentUser.address.get("address").isEmpty() && !addressTextfield.getText().toString().contentEquals(currentUser.address.get("address"))))
                address.put("address", addressTextfield.getText().toString());

            if (((currentUser.address.get("zipCode") == null || (currentUser.address.get("zipCode") != null && currentUser.address.get("zipCode").isEmpty())) && zipCodeTextfield.getText().length() > 0)
                    || (currentUser.address.get("zipCode") != null && !currentUser.address.get("zipCode").isEmpty() && !zipCodeTextfield.getText().toString().contentEquals(currentUser.address.get("zipCode")))) {
                address.put("zipCode", zipCodeTextfield.getText().toString());
            }

            if (((currentUser.address.get("city") == null || (currentUser.address.get("city") != null && currentUser.address.get("city").isEmpty())) && cityTextfield.getText().length() > 0)
                    || (currentUser.address.get("city") != null && !currentUser.address.get("city").isEmpty() && !cityTextfield.getText().toString().contentEquals(currentUser.address.get("city")))) {
                address.put("city", cityTextfield.getText().toString());
            }

            if (address.size() > 0) {
                settings.put("address", address);
                param.put("settings", settings);
            }

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().updateUser(param, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    headerBackButton.performClick();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });
    }

    public void showImageActionMenu() {
        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(this, R.string.GLOBAL_CAMERA, showCamera));
        items.add(new ActionSheetItem(this, R.string.GLOBAL_ALBUMS, showGallery));

        ActionSheet.showWithItems(this, items);
    }

    private void reloadView() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.phoneTextfield.setText(currentUser.phone.replace("+33", "0"));
        this.emailTextfield.setText(currentUser.email);
        this.addressTextfield.setText(currentUser.address.get("address"));
        this.zipCodeTextfield.setText(currentUser.address.get("zipCode"));
        this.cityTextfield.setText(currentUser.address.get("city"));

        this.reloadDocuments();

        if (currentUser.checkDocuments.get("phone").equals(3)) {
            this.sendVerifyPhone.setVisibility(View.VISIBLE);
            this.phoneTextfield.setFocusable(false);
            this.phoneTextfield.setFocusableInTouchMode(false);
            this.phoneTextfield.setCursorVisible(false);
            this.phoneTextfield.setClickable(false);
        }
        else {
            this.sendVerifyPhone.setVisibility(View.GONE);
            this.phoneTextfield.setFocusable(true);
            this.phoneTextfield.setFocusableInTouchMode(true);
            this.phoneTextfield.setCursorVisible(true);
            this.phoneTextfield.setClickable(true);
        }

        if (currentUser.checkDocuments.get("email").equals(3)) {
            this.sendVerifyMail.setVisibility(View.VISIBLE);
            this.emailTextfield.setFocusable(false);
            this.emailTextfield.setFocusableInTouchMode(false);
            this.emailTextfield.setCursorVisible(false);
            this.emailTextfield.setClickable(false);
        }
        else {
            this.sendVerifyMail.setVisibility(View.GONE);
            this.emailTextfield.setFocusable(true);
            this.emailTextfield.setFocusableInTouchMode(true);
            this.emailTextfield.setCursorVisible(true);
            this.emailTextfield.setClickable(true);
        }
    }

    private void reloadDocuments() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        if (currentUser.checkDocuments.get("justificatory").equals(0))
            this.justificatoryButton.setImageResource(R.drawable.document_refused);
        else if (currentUser.checkDocuments.get("justificatory").equals(3))
            this.justificatoryButton.setImageResource(R.drawable.friends_add);
        else
            this.justificatoryButton.setImageResource(R.drawable.friends_accepted);

        if (currentUser.checkDocuments.get("justificatory2").equals(0))
            this.justificatory2Button.setImageResource(R.drawable.document_refused);
        else if (currentUser.checkDocuments.get("justificatory2").equals(3))
            this.justificatory2Button.setImageResource(R.drawable.friends_add);
        else
            this.justificatory2Button.setImageResource(R.drawable.friends_accepted);
    }

    private ActionSheetItem.ActionSheetItemClickListener showCamera = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            tmpUriImage = fileUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(intent, TAKE_PICTURE);
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showGallery = () -> {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PICTURE);
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {

                Uri imageUri;
                if (data == null || data.getData() == null)
                    imageUri = tmpUriImage;
                else
                    imageUri = data.getData();

                tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null && currentDoc != null) {
                    FLUser currentUser = FloozRestClient.getInstance().currentUser;
                    currentUser.checkDocuments.put(currentDoc, 1);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(this::reloadDocuments);

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = () -> {
                        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                        String imagePath = ImageHelper.getPath(instance, selectedImageUri);
                        if (imagePath != null) {
                            FloozRestClient.getInstance().uploadDocument(currentDoc, new File(imagePath), new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    reloadDocuments();
                                }
                                @Override
                                public void failure(int statusCode, FLError error) {
                                    FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                                        @Override
                                        public void success(Object response) {
                                            reloadDocuments();
                                        }

                                        @Override
                                        public void failure(int statusCode, FLError error) {

                                        }
                                    });
                                }
                            });
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.reloadView();
    }

    @Override
    public void onPause() {
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
        this.headerBackButton.performClick();
    }
}
