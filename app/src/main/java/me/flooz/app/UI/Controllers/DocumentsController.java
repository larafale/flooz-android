package me.flooz.app.UI.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

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
    private static final int PERMISSION_CAMERA = 3;
    private static final int PERMISSION_STORAGE = 4;

    private ImageView cniRectoButton;
    private ImageView cniVersoButton;
    private ImageView justificatoryButton;
    private ImageView justificatory2Button;
    private ImageView cardButton;

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

        this.init();
    }

    public DocumentsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        EditText cniRectoTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_cni_recto);
        EditText cniVersoTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_cni_verso);
        this.cniRectoButton = (ImageView) this.currentView.findViewById(R.id.settings_identity_recto);
        this.cniVersoButton = (ImageView) this.currentView.findViewById(R.id.settings_identity_verso);
        EditText justificatoryTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_justificatory);
        EditText justificatory2Textfield = (EditText) this.currentView.findViewById(R.id.settings_coord_justificatory2);
        EditText cardTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_card);
        this.justificatory2Button = (ImageView) this.currentView.findViewById(R.id.settings_coord_justificatory2_button);
        this.justificatoryButton = (ImageView) this.currentView.findViewById(R.id.settings_coord_justificatory_button);
        this.cardButton = (ImageView) this.currentView.findViewById(R.id.settings_coord_card_button);
        TextView infos = (TextView) this.currentView.findViewById(R.id.documents_infos);

        cniRectoTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        justificatoryTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        justificatory2Textfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        cardTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        infos.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        infos.setText(FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("documents").optString("info"));

        this.reloadDocuments();

        cniRectoTextfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("cniRecto").equals(0) || currentUser.checkDocuments.get("cniRecto").equals(3)) {
                    currentDoc = "cniRecto";
                    showImageActionMenu();
                }
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

        cniVersoTextfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("cniVerso").equals(0) || currentUser.checkDocuments.get("cniVerso").equals(3)) {
                    currentDoc = "cniVerso";
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

        justificatoryTextfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("justificatory").equals(0) || currentUser.checkDocuments.get("justificatory").equals(3)) {
                    currentDoc = "justificatory";
                    showImageActionMenu();
                }
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

        justificatory2Textfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("justificatory2").equals(0) || currentUser.checkDocuments.get("justificatory2").equals(3)) {
                    currentDoc = "justificatory2";
                    showImageActionMenu();
                }
            }
        });

        this.justificatory2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("justificatory2").equals(0) || currentUser.checkDocuments.get("justificatory2").equals(3)) {
                    currentDoc = "justificatory2";
                    showImageActionMenu();
                }
            }
        });

        cardTextfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("card").equals(0) || currentUser.checkDocuments.get("card").equals(3)) {
                    currentDoc = "card";
                    showImageActionMenu();
                }
            }
        });

        this.cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                if (currentUser.checkDocuments.get("card").equals(0) || currentUser.checkDocuments.get("card").equals(3)) {
                    currentDoc = "card";
                    showImageActionMenu();
                }
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

        if (currentUser.checkDocuments.get("card").equals(0))
            this.cardButton.setImageResource(R.drawable.document_refused);
        else if (currentUser.checkDocuments.get("card").equals(3))
            this.cardButton.setImageResource(R.drawable.friends_add);
        else
            this.cardButton.setImageResource(R.drawable.friends_accepted);
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
            if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
//                }
            } else {
                if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                    ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
//                }
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
                    tmpUriImage = fileUri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    try {
                        parentActivity.startActivityForResult(intent, TAKE_PICTURE);
                    } catch (ActivityNotFoundException e) {

                    }
                }
            }
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showGallery = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
//                }
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                try {
                    parentActivity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
                } catch (ActivityNotFoundException e) {

                }
            }
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
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                    ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
//                }
                } else {
                    if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                        ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
//                }
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
                        tmpUriImage = fileUri;
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                        try {
                            parentActivity.startActivityForResult(intent, TAKE_PICTURE);
                        } catch (ActivityNotFoundException e) {

                        }
                    }
                }
            }
        } else if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                try {
                    parentActivity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
                } catch (ActivityNotFoundException e) {

                }
            }
        }
    }
}
