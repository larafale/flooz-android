package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
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
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.ImageHelper;

/**
 * Created by Flooz on 3/10/15.
 */
public class IdentitySettingsActivity extends Activity {

    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;

    private IdentitySettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private TextView headerTitle;

    private EditText firstnameTextfield;
    private EditText lastnameTextfield;
    private EditText usernameTextfield;
    private EditText birthdateTextfield;
    private EditText cniRectoTextfield;
    private EditText cniVersoTextfield;

    private TextView birthdateHint;
    private ImageView cniRectoButton;
    private ImageView cniVersoButton;

    private Button saveButton;

    private String currentDoc;
    private Uri tmpUriImage;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_identity_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_identity_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_identity_header_title);
        this.firstnameTextfield = (EditText) this.findViewById(R.id.settings_identity_firstname);
        this.lastnameTextfield = (EditText) this.findViewById(R.id.settings_identity_lastname);
        this.usernameTextfield = (EditText) this.findViewById(R.id.settings_identity_username);
        this.birthdateTextfield = (EditText) this.findViewById(R.id.settings_identity_birthdate);
        this.cniRectoTextfield = (EditText) this.findViewById(R.id.settings_identity_cni_recto);
        this.cniVersoTextfield = (EditText) this.findViewById(R.id.settings_identity_cni_verso);
        this.birthdateHint = (TextView) this.findViewById(R.id.settings_identity_birthdate_hint);
        this.cniRectoButton = (ImageView) this.findViewById(R.id.settings_identity_recto);
        this.cniVersoButton = (ImageView) this.findViewById(R.id.settings_identity_verso);
        this.saveButton = (Button) this.findViewById(R.id.settings_identity_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.firstnameTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.lastnameTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.usernameTextfield.setTypeface(CustomFonts.customContentRegular(this), Typeface.BOLD);
        this.birthdateTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.cniRectoTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.cniVersoTextfield.setTypeface(CustomFonts.customContentRegular(this));

        this.reloadView();

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                if (modal)
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                else
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });

        this.birthdateTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 0 && birthdateHint.getVisibility() != View.GONE)
                    birthdateHint.setVisibility(View.GONE);
                if (charSequence.length() == 0 && birthdateHint.getVisibility() != View.VISIBLE)
                    birthdateHint.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 3 && editable.charAt(2) != '/')
                    editable.insert(2, "/");
                if (editable.length() == 6 && editable.charAt(5) != '/')
                    editable.insert(5, "/");

                if (editable.length() == 1 && editable.charAt(0) > '3')
                    editable.insert(0, "0");

                if (editable.length() == 4 && editable.charAt(3) > '1')
                    editable.insert(3, "0");

                if (editable.length() == 2 && editable.charAt(0) == '3' && editable.charAt(1) > '1')
                    editable.delete(1, 2);

                if (editable.length() == 5 && editable.charAt(3) == '1' && editable.charAt(4) > '2')
                    editable.delete(4, 5);

                if (editable.length() == 7 && editable.charAt(6) > '2') {
                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(8);
                    birthdateTextfield.setFilters(filterArray);
                }
                else if (editable.length() == 7) {
                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(10);
                    birthdateTextfield.setFilters(filterArray);
                }
            }
        });

        this.birthdateTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    saveButton.performClick();
                }
                return false;
            }
        });

        this.cniRectoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("cniRecto").equals(0) || currentUser.checkDocuments.get("cniRecto").equals(3)) {
                    currentDoc = "cniRecto";
                    showImageActionMenu();
                }
            }
        });

        this.cniVersoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("cniVerso").equals(0) || currentUser.checkDocuments.get("cniVerso").equals(3)) {
                    currentDoc = "cniVerso";
                    showImageActionMenu();
                }
            }
        });

        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                Map<String, Object> param = new HashMap<>();

                if (!firstnameTextfield.getText().toString().contentEquals(currentUser.firstname.substring(0,1).toUpperCase() + currentUser.firstname.substring(1)))
                    param.put("firstName", firstnameTextfield.getText().toString());

                if (!lastnameTextfield.getText().toString().contentEquals(currentUser.lastname.substring(0,1).toUpperCase() + currentUser.lastname.substring(1)))
                    param.put("lastName", lastnameTextfield.getText().toString());

                if (!usernameTextfield.getText().toString().contentEquals(currentUser.username))
                    param.put("nick", usernameTextfield.getText().toString());

                if (!birthdateTextfield.getText().toString().contentEquals(currentUser.birthdate)) {
                    param.put("birthdate", birthdateTextfield.getText().toString());
                }

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().updateUser(param, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {

                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    private void reloadView() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.firstnameTextfield.setText(currentUser.firstname.substring(0,1).toUpperCase() + currentUser.firstname.substring(1));
        this.lastnameTextfield.setText(currentUser.lastname.substring(0,1).toUpperCase() + currentUser.lastname.substring(1));
        this.usernameTextfield.setText(currentUser.username);
        this.birthdateTextfield.setText(currentUser.birthdate);

        if (this.birthdateTextfield.getText().length() > 0)
            this.birthdateHint.setVisibility(View.GONE);
        else
            this.birthdateHint.setVisibility(View.VISIBLE);


        this.reloadDocuments();
    }

    private void reloadDocuments() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        if (currentUser.checkDocuments.get("cniRecto").equals(0))
            cniRectoButton.setImageResource(R.drawable.document_refused);
        else if (currentUser.checkDocuments.get("cniRecto").equals(3))
            cniRectoButton.setImageResource(R.drawable.friends_add);
        else
            cniRectoButton.setImageResource(R.drawable.friends_accepted);

        if (currentUser.checkDocuments.get("cniVerso").equals(0))
            cniVersoButton.setImageResource(R.drawable.document_refused);
        else if (currentUser.checkDocuments.get("cniVerso").equals(3))
            cniVersoButton.setImageResource(R.drawable.friends_add);
        else
            cniVersoButton.setImageResource(R.drawable.friends_accepted);
    }

    public void showImageActionMenu() {
        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(this, R.string.GLOBAL_CAMERA, showCamera));
        items.add(new ActionSheetItem(this, R.string.GLOBAL_ALBUMS, showGallery));

        ActionSheet.showWithItems(this, items);
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

    private ActionSheetItem.ActionSheetItemClickListener showGallery = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_PICTURE);
        }
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            reloadDocuments();
                        }
                    });

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            String imagePath = getPath(selectedImageUri);
                            FloozRestClient.getInstance().uploadDocument(currentDoc, new File(imagePath), new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    FloozRestClient.getInstance().updateCurrentUser(null);
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {
                                    FloozRestClient.getInstance().updateCurrentUser(null);
                                }
                            });
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }
    }

    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        return uri.getPath();
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
        this.reloadView();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    @Override
    protected void onDestroy() {
        clearReferences();
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
