package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.CameraFullscreenFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.ImageGalleryFragment;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.UI.Tools.ActionSheet;
import flooz.android.com.flooz.UI.Tools.ActionSheetItem;
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.ImageHelper;

/**
 * Created by Flooz on 12/15/14.
 */
public class IdentitySettingsFragment extends HomeBaseFragment implements CustomCameraHost.CustomCameraHostDelegate {

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

    private IdentitySettingsFragment instance;

    private String currentDoc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_identity_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_identity_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_identity_header_title);
        this.firstnameTextfield = (EditText) view.findViewById(R.id.settings_identity_firstname);
        this.lastnameTextfield = (EditText) view.findViewById(R.id.settings_identity_lastname);
        this.usernameTextfield = (EditText) view.findViewById(R.id.settings_identity_username);
        this.birthdateTextfield = (EditText) view.findViewById(R.id.settings_identity_birthdate);
        this.cniRectoTextfield = (EditText) view.findViewById(R.id.settings_identity_cni_recto);
        this.cniVersoTextfield = (EditText) view.findViewById(R.id.settings_identity_cni_verso);
        this.birthdateHint = (TextView) view.findViewById(R.id.settings_identity_birthdate_hint);
        this.cniRectoButton = (ImageView) view.findViewById(R.id.settings_identity_recto);
        this.cniVersoButton = (ImageView) view.findViewById(R.id.settings_identity_verso);
        this.saveButton = (Button) view.findViewById(R.id.settings_identity_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.firstnameTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.lastnameTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.usernameTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
        this.birthdateTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.cniRectoTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.cniVersoTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.firstnameTextfield.setText(currentUser.firstname.substring(0,1).toUpperCase() + currentUser.firstname.substring(1));
        this.lastnameTextfield.setText(currentUser.lastname.substring(0,1).toUpperCase() + currentUser.lastname.substring(1));
        this.usernameTextfield.setText(currentUser.username);
        this.birthdateTextfield.setText(currentUser.birthdate);

        if (this.birthdateTextfield.getText().length() > 0)
            this.birthdateHint.setVisibility(View.GONE);
        else
            this.birthdateHint.setVisibility(View.VISIBLE);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        if (currentUser.checkDocuments.get("cniRecto").equals(0))
            cniRectoButton.setImageResource(R.drawable.document_refused);
        if (currentUser.checkDocuments.get("cniRecto").equals(3))
            cniRectoButton.setImageResource(R.drawable.friends_add);

        if (currentUser.checkDocuments.get("cniVerso").equals(0))
            cniVersoButton.setImageResource(R.drawable.document_refused);
        if (currentUser.checkDocuments.get("cniVerso").equals(3))
            cniVersoButton.setImageResource(R.drawable.friends_add);

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
                        parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        return view;
    }

    public void showImageActionMenu() {
        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(parentActivity, R.string.GLOBAL_CAMERA, showCamera));
        items.add(new ActionSheetItem(parentActivity, R.string.GLOBAL_ALBUMS, showGallery));

        ActionSheet.showWithItems(parentActivity, items);

    }

    private ActionSheetItem.ActionSheetItemClickListener showCamera = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            ((CameraFullscreenFragment)parentActivity.contentFragments.get("camera_fullscreen")).cameraHostDelegate = instance;
            ((CameraFullscreenFragment)parentActivity.contentFragments.get("camera_fullscreen")).canAccessAlbum = false;
            parentActivity.pushMainFragment("camera_fullscreen", R.animator.slide_up, android.R.animator.fade_out);
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showGallery = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            ((ImageGalleryFragment)parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = instance;
            parentActivity.pushMainFragment("photo_gallery", R.animator.slide_up, android.R.animator.fade_out);
        }
    };

    @Override
    public void photoTaken(final Bitmap photo) {
        parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
        if (currentDoc != null) {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;
            currentUser.checkDocuments.put(currentDoc, 1);

            Handler mainHandler = new Handler();
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    FloozRestClient.getInstance().uploadDocument(currentDoc, ImageHelper.convertBitmapInFile(photo), new FloozHttpResponseHandler() {
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

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}