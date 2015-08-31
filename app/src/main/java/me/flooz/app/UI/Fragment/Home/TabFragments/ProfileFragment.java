package me.flooz.app.UI.Fragment.Home.TabFragments;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import me.flooz.app.Adapter.MenuListAdapter;
import me.flooz.app.Adapter.ProfileListAdapter;
import me.flooz.app.Adapter.ProfileListAdapterDelegate;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AboutActivity;
import me.flooz.app.UI.Activity.CashoutActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NotificationActivity;
import me.flooz.app.UI.Activity.Settings.ProfileSettingsActivity;
import me.flooz.app.UI.Activity.ShareAppActivity;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.MenuItem;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ProfileFragment extends TabBarFragment implements ProfileListAdapterDelegate
{
    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;

    private Context context;

    private TextView fullname;
    private TextView username;

    public ProfileListAdapter listAdapter;
    public Uri tmpUriImage;

    private StickyListHeadersListView menuActionList;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadUserData();
        }
    };

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, null);

        this.context = inflater.getContext();

        this.fullname = (TextView) view.findViewById(R.id.header_title);
        this.username = (TextView) view.findViewById(R.id.header_subtitle);

        this.fullname.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.username.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.listAdapter = new ProfileListAdapter(this.context, this);

        this.menuActionList = (StickyListHeadersListView) view.findViewById(R.id.account_menu_list);
        this.menuActionList.setAdapter(this.listAdapter);

        this.menuActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> item = listAdapter.getItem(position);

                if (item != null && item.containsKey("action")) {
                    String action  = (String) item.get("action");

                    switch (action) {
                        case "friends":
                            tabBarActivity.pushFragmentInCurrentTab(new FriendsFragment());
                            break;
                        case "cashout":
                            FloozRestClient.getInstance().showLoadView();
                            FloozRestClient.getInstance().cashoutValidate(new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    tabBarActivity.pushFragmentInCurrentTab(new CashoutFragment());
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {

                                }
                            });

                            break;
                        case "card":
                            break;
                        case "bank":
                            break;
                        case "coords":
                            break;
                        case "documents":
                            break;
                        case "sponsor":
                            break;
                        case "preferences":
                            break;
                        case "security":
                            break;
                        case "rate":
                            break;
                        case "faq":
                            break;
                        case "terms":
                            break;
                        case "contact":
                            break;
                        case "critics":
                            break;
                        case "logout":
                            break;
                    }
                }
            }
        });

//        this.menuActionList.setOnItemClickListener((adapterView, view1, i, l) -> {
//            if (tabBarActivity != null) {
//                switch (i) {
//                    case 0:
//                        FloozRestClient.getInstance().updateCurrentUser(null);
//                        Intent intentSettings = new Intent(tabBarActivity, ProfileSettingsActivity.class);
//                        tabBarActivity.startActivity(intentSettings);
//                        tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
//                        break;
//                    case 1:
//                        Intent intentNotifs = new Intent(tabBarActivity, NotificationActivity.class);
//                        tabBarActivity.startActivity(intentNotifs);
//                        tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
//                        break;
//                    case 2:
//                        Intent intentShare = new Intent(tabBarActivity, ShareAppActivity.class);
//                        tabBarActivity.startActivity(intentShare);
//                        tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
//                        break;
////                        case 3:
////                            Intent intentScan = new Intent(tabBarActivity, ScannerActivity.class);
////                            tabBarActivity.startActivity(intentScan);
////                            tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
////                            break;
//                    case 3:
//                        FloozRestClient.getInstance().showLoadView();
//                        FloozRestClient.getInstance().cashoutValidate(new FloozHttpResponseHandler() {
//                            @Override
//                            public void success(Object response) {
//                                tabBarActivity.pushFragmentInCurrentTab(new CashoutFragment(), R.animator.slide_in_left, R.animator.slide_out_right);
////                                Intent intentCashout = new Intent(tabBarActivity, CashoutActivity.class);
////                                tabBarActivity.startActivity(intentCashout);
////                                tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
//                            }
//
//                            @Override
//                            public void failure(int statusCode, FLError error) {
//
//                            }
//                        });
//                        break;
//                    case 4:
//                        Intent intentCashout = new Intent(tabBarActivity, AboutActivity.class);
//                        tabBarActivity.startActivity(intentCashout);
//                        tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FloozRestClient.getInstance() != null)
            this.reloadUserData();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    private void reloadUserData()
    {
        FLUser user = FloozRestClient.getInstance().currentUser;

        if (user != null) {
            this.fullname.setText(user.fullname);

            this.username.setText("@" + user.username);

            this.listAdapter.reloadData();
        }
    }

    @Override
    public void userImageClicked() {
        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(this.context, R.string.SIGNUP_IMAGE_BUTTON_TAKE, () -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
            tmpUriImage = fileUri;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            try {
                tabBarActivity.startActivityForResult(intent, TAKE_PICTURE);
            } catch (ActivityNotFoundException e) {

            }
        }));

        items.add(new ActionSheetItem(this.context, R.string.SIGNUP_IMAGE_BUTTON_CHOOSE, () -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            try {
                tabBarActivity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
            } catch (ActivityNotFoundException e) {

            }
        }));

        ActionSheet.showWithItems(tabBarActivity, items);
    }

    @Override
    public void balanceClicked() {
        final Dialog dialog = new Dialog(this.context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_balance);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_wallet_title);
        title.setTypeface(CustomFonts.customContentRegular(this.context), Typeface.BOLD);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_wallet_msg);
        text.setTypeface(CustomFonts.customContentRegular(this.context));

        Button close = (Button) dialog.findViewById(R.id.dialog_wallet_btn);

        close.setOnClickListener(v1 -> dialog.dismiss());

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {
                Uri imageUri = Uri.EMPTY;
                if (data == null || data.getData() == null)
                    imageUri = this.tmpUriImage;
                else
                    imageUri = data.getData();

                this.tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        String path = ImageHelper.getPath(this.context, selectedImageUri);
                        if (path != null) {
                            File image = new File(path);
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                            if (photo != null) {
                                int rotation = ImageHelper.getRotation(this.context, selectedImageUri);
                                if (rotation != 0) {
                                    photo = ImageHelper.rotateBitmap(photo, rotation);
                                }

                                int nh = (int) (photo.getHeight() * (512.0 / photo.getWidth()));
                                Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                                this.listAdapter.userViewHolder.imageView.setImageBitmap(scaled);
                                FloozRestClient.getInstance().uploadDocument("picId", image, new FloozHttpResponseHandler() {
                                    @Override
                                    public void success(Object response) {
                                        FloozRestClient.getInstance().updateCurrentUser(null);
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        FLUser user = FloozRestClient.getInstance().currentUser;

                                        if (user.avatarURL != null)
                                            ImageLoader.getInstance().displayImage(user.avatarURL, listAdapter.userViewHolder.imageView);
                                        else
                                            listAdapter.userViewHolder.imageView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
}
