package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

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
 * Created by Wapazz on 06/10/15.
 */

public class EditProfileActivity extends Activity {

    private FloozApplication floozApp;
    private EditProfileActivity activity;
    private FLUser currentUser;

    private ImageView backButton;
    private ImageView profileCover;
    private ImageView profileAvatar;
    private ImageView saveProfile;
    private ImageView cameraPictureAvatar;
    private ImageView cameraPictureCover;
    private EditText bioField;
    private TextView bioHeader;
    private TextView headerName;

    private Uri tmpUriImage;

    private boolean isEdited = false;

    public static final int SELECT_PICTURE_AVATAR = 1;
    public static final int TAKE_PICTURE_AVATAR = 2;
    public static final int SELECT_PICTURE_COVER = 3;
    public static final int TAKE_PICTURE_COVER = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication) this.getApplicationContext();
        activity = this;
        this.setContentView(R.layout.edit_profile_activity);

        this.currentUser = FloozRestClient.getInstance().currentUser;

        this.backButton = (ImageView) this.findViewById(R.id.edit_profile_back);
        this.profileAvatar = (ImageView) this.findViewById(R.id.edit_profile_avatar);
        this.profileCover = (ImageView) this.findViewById(R.id.edit_profile_cover);
        this.bioField = (EditText) this.findViewById(R.id.edit_profile_bio);
        this.headerName = (TextView) this.findViewById(R.id.edit_profile_headername);
        this.saveProfile = (ImageView) this.findViewById(R.id.edit_profile_save);
        this.bioHeader = (TextView) this.findViewById(R.id.edit_profile_bio_header);
        this.cameraPictureAvatar = (ImageView) this.findViewById(R.id.edit_profile_camera2);
        this.cameraPictureCover = (ImageView) this.findViewById(R.id.edit_profile_camera1);
        this.cameraPictureAvatar.bringToFront();

        this.bioHeader.setTypeface(CustomFonts.customContentRegular(this));
        this.bioField.setTypeface(CustomFonts.customContentRegular(this));
        this.headerName.setTypeface(CustomFonts.customContentLight(this));
        this.headerName.setTextColor(getResources().getColor(R.color.blue));
        this.saveProfile.setColorFilter(getResources().getColor(R.color.grey_pseudo));
        this.cameraPictureAvatar.setColorFilter(getResources().getColor(R.color.light_white_alpha));
        this.cameraPictureCover.setColorFilter(getResources().getColor(R.color.light_white_alpha));

        this.bioField.setText(this.currentUser.userBio);
        this.bioField.setSelection(this.bioField.getText().length() - 1);

        if (this.currentUser.avatarURL != null && !this.currentUser.avatarURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(this.currentUser.avatarURL, this.profileAvatar);
        }
        if (this.currentUser.coverURL != null && !this.currentUser.coverURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(this.currentUser.coverURL, this.profileCover);
        }

        this.profileAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureActionMenu(true);
            }
        });

        this.profileCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureActionMenu(false);
            }
        });

        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.bioField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().contentEquals(FloozRestClient.getInstance().currentUser.userBio)) {
                    saveProfile.setColorFilter(getResources().getColor(R.color.grey_pseudo));
                    saveProfile.setEnabled(false);
                } else {
                    saveProfile.setColorFilter(getResources().getColor(R.color.blue));
                    saveProfile.setEnabled(true);
                }
            }
        });
        this.saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map < String, Object > data = new HashMap<>(1);
                String bioContent = bioField.getText().toString();

                data.put("bio", bioContent.isEmpty() ? "" : bioContent);

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().updateUser(data, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        saveProfile.setColorFilter(getResources().getColor(R.color.grey_pseudo));
                        saveProfile.setEnabled(false);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    // Origin = true = click from avatar
    // Origin = false = click from cover
    private void showPictureActionMenu(boolean origin) {
        List<ActionSheetItem> items = new ArrayList<>();

        if (origin) {
            items.add(new ActionSheetItem(this, R.string.MENU_TAKE_PICTURE, takePictureAvatar));
            items.add(new ActionSheetItem(this, R.string.MENU_UPLOAD_PICTURE, uploadPictureAvatar));
        }
        else {
            items.add(new ActionSheetItem(this, R.string.MENU_TAKE_PICTURE, takePictureCover));
            items.add(new ActionSheetItem(this, R.string.MENU_UPLOAD_PICTURE, uploadPictureCover));
        }

        ActionSheet.showWithItems(this, items);
    }

    private ActionSheetItem.ActionSheetItemClickListener takePictureAvatar = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            tmpUriImage = fileUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            try {
                activity.startActivityForResult(intent, TAKE_PICTURE_AVATAR);
            } catch (ActivityNotFoundException e) {

            }
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener takePictureCover = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            tmpUriImage = fileUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            try {
                activity.startActivityForResult(intent, TAKE_PICTURE_COVER);
            } catch (ActivityNotFoundException e) {

            }
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener uploadPictureAvatar = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            try {
                activity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE_AVATAR);
            } catch (ActivityNotFoundException e) {

            }
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener uploadPictureCover = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            try {
                activity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE_COVER);
            } catch (ActivityNotFoundException e) {

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
        floozApp.setCurrentActivity(this);
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

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.backButton.performClick();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int containerAvatar = 0;
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == SELECT_PICTURE_AVATAR || requestCode == TAKE_PICTURE_AVATAR)
                containerAvatar = 1;
            else if (requestCode == SELECT_PICTURE_COVER || requestCode == TAKE_PICTURE_COVER)
                containerAvatar = 2;

            if (containerAvatar != 0) {
                Uri imageUri = Uri.EMPTY;
                if (data == null || data.getData() == null)
                    imageUri = this.tmpUriImage;
                else
                    imageUri = data.getData();

                this.tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    final int finalContainerAvatar = containerAvatar;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String path = ImageHelper.getPath(EditProfileActivity.this, selectedImageUri);
                            if (path != null) {
                                File image = new File(path);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                                if (photo != null) {
                                    int rotation = ImageHelper.getRotation(EditProfileActivity.this, selectedImageUri);
                                    if (rotation != 0) {
                                        photo = ImageHelper.rotateBitmap(photo, rotation);
                                    }
                                    int nh = (int) (photo.getHeight() * (512.0 / photo.getWidth()));
                                    Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);

                                    if (finalContainerAvatar == 1) {
                                        profileAvatar.setImageBitmap(scaled);
                                        FloozRestClient.getInstance().showLoadView();
                                        FloozRestClient.getInstance().uploadDocument("picId", image, new FloozHttpResponseHandler() {
                                            @Override
                                            public void success(Object response) {
                                                FloozRestClient.getInstance().updateCurrentUser(null);
                                            }

                                            @Override
                                            public void failure(int statusCode, FLError error) {
                                                FLUser user = FloozRestClient.getInstance().currentUser;

                                                if (user.avatarURL != null)
                                                    ImageLoader.getInstance().displayImage(user.avatarURL, profileAvatar);
                                                else
                                                    profileAvatar.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
                                            }
                                        });
                                    } else if (finalContainerAvatar == 2) {
                                        profileCover.setImageBitmap(scaled);
                                        FloozRestClient.getInstance().showLoadView();
                                        FloozRestClient.getInstance().uploadDocument("coverId", image, new FloozHttpResponseHandler() {
                                            @Override
                                            public void success(Object response) {
                                                FloozRestClient.getInstance().updateCurrentUser(null);
                                            }

                                            @Override
                                            public void failure(int statusCode, FLError error) {
                                                FLUser user = FloozRestClient.getInstance().currentUser;

                                                if (user.coverURL != null)
                                                    ImageLoader.getInstance().displayImage(user.coverURL, profileCover);
                                                else
                                                    profileCover.setImageDrawable(getResources().getDrawable(R.drawable.launch_background));
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}

