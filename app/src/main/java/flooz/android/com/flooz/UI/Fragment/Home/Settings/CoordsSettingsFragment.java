package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
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
public class CoordsSettingsFragment extends HomeBaseFragment  implements CustomCameraHost.CustomCameraHostDelegate {

    private ImageView headerBackButton;
    private TextView headerTitle;

    private EditText phoneTextfield;
    private EditText emailTextfield;
    private EditText addressTextfield;
    private EditText zipCodeTextfield;
    private EditText cityTextfield;
    private EditText justificatoryTextfield;

    private TextView sendVerifyPhone;
    private TextView sendVerifyMail;
    private ImageView justificatoryButton;

    private Button saveButton;

    private CoordsSettingsFragment instance;

    private String currentDoc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_coords_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_coord_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_coord_header_title);
        this.phoneTextfield = (EditText) view.findViewById(R.id.settings_coord_phone);
        this.emailTextfield = (EditText) view.findViewById(R.id.settings_coord_email);
        this.addressTextfield = (EditText) view.findViewById(R.id.settings_coord_address);
        this.zipCodeTextfield = (EditText) view.findViewById(R.id.settings_coord_zip);
        this.cityTextfield = (EditText) view.findViewById(R.id.settings_coord_city);
        this.justificatoryTextfield = (EditText) view.findViewById(R.id.settings_coord_justificatory);
        this.sendVerifyPhone = (TextView) view.findViewById(R.id.settings_coord_verify_phone);
        this.sendVerifyMail = (TextView) view.findViewById(R.id.settings_coord_verify_email);
        this.justificatoryButton = (ImageView) view.findViewById(R.id.settings_coord_justificatory_button);
        this.saveButton = (Button) view.findViewById(R.id.settings_coord_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.phoneTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.emailTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.addressTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.zipCodeTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.cityTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.sendVerifyPhone.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.sendVerifyMail.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.justificatoryTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.phoneTextfield.setText(currentUser.phone.replace("+33", "0"));
        this.emailTextfield.setText(currentUser.email);
        this.addressTextfield.setText(currentUser.address.get("address"));
        this.zipCodeTextfield.setText(currentUser.address.get("zipCode"));
        this.cityTextfield.setText(currentUser.address.get("city"));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        if (currentUser.checkDocuments.get("justificatory").equals(0))
            this.justificatoryButton.setImageResource(R.drawable.document_refused);
        if (currentUser.checkDocuments.get("justificatory").equals(3))
            this.justificatoryButton.setImageResource(R.drawable.friends_add);

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

        this.sendVerifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().sendSMSValidation();
                v.setVisibility(View.GONE);
            }
        });

        this.sendVerifyMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().sendEmailValidation();
                v.setVisibility(View.GONE);
            }
        });

        this.justificatoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("justificatory").equals(0) || currentUser.checkDocuments.get("justificatory").equals(3)) {
                    currentDoc = "justificatory";
                    showImageActionMenu();
                }
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

        this.cityTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    saveButton.performClick();
                }
                return false;
            }
        });

        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                Map<String, Object> param = new HashMap<>();
                Map<String, Object> settings = new HashMap<>();

                if (!phoneTextfield.getText().toString().contentEquals(currentUser.phone.replace("+33", "0")))
                    param.put("phone", phoneTextfield.getText().toString());

                if (!emailTextfield.getText().toString().contentEquals(currentUser.email))
                    param.put("email", emailTextfield.getText().toString());

                if (((currentUser.address.get("address") == null || (currentUser.address.get("address") != null && currentUser.address.get("address").isEmpty())) && addressTextfield.getText().length() > 0)
                        || (currentUser.address.get("address") != null && !currentUser.address.get("address").isEmpty() && !addressTextfield.getText().toString().contentEquals(currentUser.address.get("address"))))
                    settings.put("address", addressTextfield.getText().toString());

                if (((currentUser.address.get("zipCode") == null || (currentUser.address.get("zipCode") != null && currentUser.address.get("zipCode").isEmpty())) && zipCodeTextfield.getText().length() > 0)
                        || (currentUser.address.get("zipCode") != null && !currentUser.address.get("zipCode").isEmpty() && !zipCodeTextfield.getText().toString().contentEquals(currentUser.address.get("zipCode")))) {
                    settings.put("zipCode", zipCodeTextfield.getText().toString());
                }

                if (((currentUser.address.get("city") == null || (currentUser.address.get("city") != null && currentUser.address.get("city").isEmpty())) && cityTextfield.getText().length() > 0)
                        || (currentUser.address.get("city") != null && !currentUser.address.get("city").isEmpty() && !cityTextfield.getText().toString().contentEquals(currentUser.address.get("city")))) {
                    settings.put("city", cityTextfield.getText().toString());
                }

                if (settings.size() > 0)
                    param.put("settings", settings);

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
            ((CameraFullscreenFragment) parentActivity.contentFragments.get("camera_fullscreen")).cameraHostDelegate = instance;
            ((CameraFullscreenFragment) parentActivity.contentFragments.get("camera_fullscreen")).canAccessAlbum = false;
            parentActivity.pushMainFragment("camera_fullscreen", R.animator.slide_up, android.R.animator.fade_out);
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showGallery = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            ((ImageGalleryFragment) parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = instance;
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