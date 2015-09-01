package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.ImageHelper;

/**
 * Created by Flooz on 9/1/15.
 */
public class DocumentsController extends BaseController {

    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;

    private ImageView headerBackButton;

    private ImageView cniRectoButton;
    private ImageView cniVersoButton;
    private ImageView justificatoryButton;
    private ImageView justificatory2Button;

    private String currentDoc;
    private Uri tmpUriImage;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadDocuments();
        }
    };

    public DocumentsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        EditText cniRectoTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_cni_recto);
        EditText cniVersoTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_cni_verso);
        this.cniRectoButton = (ImageView) this.currentView.findViewById(R.id.settings_identity_recto);
        this.cniVersoButton = (ImageView) this.currentView.findViewById(R.id.settings_identity_verso);
        EditText justificatoryTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_justificatory);
        EditText justificatory2Textfield = (EditText) this.currentView.findViewById(R.id.settings_coord_justificatory2);
        this.justificatory2Button = (ImageView) this.currentView.findViewById(R.id.settings_coord_justificatory2_button);
        this.justificatoryButton = (ImageView) this.currentView.findViewById(R.id.settings_coord_justificatory_button);
        TextView infos = (TextView) this.currentView.findViewById(R.id.documents_infos);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        cniRectoTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        justificatoryTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        justificatory2Textfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        infos.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        infos.setText(FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("documents").optString("info"));

        this.reloadDocuments();

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(view -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity)this.parentActivity).popFragmentInCurrentTab(R.animator.slide_in_right, R.animator.slide_out_left);
            }
        });

        cniRectoTextfield.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("cniRecto").equals(0) || currentUser.checkDocuments.get("cniRecto").equals(3)) {
                currentDoc = "cniRecto";
                showImageActionMenu();
            }
        });

        this.cniRectoButton.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("cniRecto").equals(0) || currentUser.checkDocuments.get("cniRecto").equals(3)) {
                currentDoc = "cniRecto";
                showImageActionMenu();
            }
        });

        cniVersoTextfield.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("cniVerso").equals(0) || currentUser.checkDocuments.get("cniVerso").equals(3)) {
                currentDoc = "cniVerso";
                showImageActionMenu();
            }
        });

        this.cniVersoButton.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            if (currentUser.checkDocuments.get("cniVerso").equals(0) || currentUser.checkDocuments.get("cniVerso").equals(3)) {
                currentDoc = "cniVerso";
                showImageActionMenu();
            }
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

    @Override
    public void onResume() {
        this.reloadDocuments();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    public void showImageActionMenu() {
        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(this.parentActivity, R.string.GLOBAL_CAMERA, showCamera));
        items.add(new ActionSheetItem(this.parentActivity, R.string.GLOBAL_ALBUMS, showGallery));

        ActionSheet.showWithItems(this.parentActivity, items);
    }

    private ActionSheetItem.ActionSheetItemClickListener showCamera = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            tmpUriImage = fileUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            parentActivity.startActivityForResult(intent, TAKE_PICTURE);
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showGallery = () -> {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        try {
            parentActivity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
        } catch (ActivityNotFoundException e) {

        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
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
                        String imagePath = ImageHelper.getPath(parentActivity, selectedImageUri);
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
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
