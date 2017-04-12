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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsDocumentAdapter;
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

    private TextView contentLabel;
    private ImageView pic;
    private TextView infoLabel;

    private ListView listView;
    private SettingsDocumentAdapter listAdapter;

    private String currentDoc;
    private Uri tmpUriImage;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            listAdapter.notifyDataSetChanged();
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

        this.contentLabel = (TextView) this.currentView.findViewById(R.id.documents_content);
        this.pic = (ImageView) this.currentView.findViewById(R.id.documents_pic);
        this.listView = (ListView) this.currentView.findViewById(R.id.documents_list);
        this.infoLabel = (TextView) this.currentView.findViewById(R.id.documents_infos);

        this.contentLabel.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.infoLabel.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        String contentString = FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("documents").optString("content");

        if (this.triggersData != null && this.triggersData.optString("content") != null && !this.triggersData.optString("content").isEmpty())
            contentString = this.triggersData.optString("content");

        if (contentString != null && !contentString.isEmpty())
            this.contentLabel.setText(contentString);
        else
            this.contentLabel.setVisibility(View.GONE);

        String picString = FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("documents").optString("pic");

        if (this.triggersData != null && this.triggersData.optString("pic") != null && !this.triggersData.optString("pic").isEmpty())
            picString = this.triggersData.optString("pic");

        if (picString != null && !picString.isEmpty())
            FloozApplication.getInstance().imageFetcher.attachImage(picString, this.pic);
        else
            this.pic.setVisibility(View.GONE);

        JSONArray items = FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("documents").optJSONArray("items");

        if (this.triggersData != null && this.triggersData.optJSONArray("items") != null && this.triggersData.optJSONArray("items").length() != 0)
            items = this.triggersData.optJSONArray("items");

        this.listAdapter = new SettingsDocumentAdapter(parentActivity, items);
        this.listView.setAdapter(this.listAdapter);

        String infoString = FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("documents").optString("info");

        if (this.triggersData != null && this.triggersData.optString("info") != null && !this.triggersData.optString("info").isEmpty())
            infoString = this.triggersData.optString("info");

        if (infoString != null && !infoString.isEmpty())
            this.infoLabel.setText(infoString);
        else
            this.infoLabel.setVisibility(View.GONE);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentDoc = listAdapter.getItemKey(position);
                showImageActionMenu();
            }
        });
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    public void showImageActionMenu() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        if (currentUser.settings.containsKey(currentDoc) && (currentUser.checkDocuments.get(currentDoc).equals(1) || currentUser.checkDocuments.get(currentDoc).equals(2)))
            return;

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
                            listAdapter.notifyDataSetChanged();
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
                                        listAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                                            @Override
                                            public void success(Object response) {
                                                listAdapter.notifyDataSetChanged();
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
