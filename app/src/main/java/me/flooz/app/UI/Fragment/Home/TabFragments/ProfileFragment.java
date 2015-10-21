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

import com.makeramen.roundedimageview.RoundedImageView;
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
import me.flooz.app.UI.Activity.WebContentActivity;
import me.flooz.app.UI.Controllers.WebController;
import me.flooz.app.UI.Fragment.Home.ProfileCardFragment;
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
                        case "profile":
                            ProfileCardFragment controller = new ProfileCardFragment();
                            controller.user = FloozRestClient.getInstance().currentUser;
                            tabBarActivity.pushFragmentInCurrentTab(controller);
                            break;
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
                            tabBarActivity.pushFragmentInCurrentTab(new CreditCardFragment());
                            break;
                        case "bank":
                            tabBarActivity.pushFragmentInCurrentTab(new BankFragment());
                            break;
                        case "coords":
                            tabBarActivity.pushFragmentInCurrentTab(new IdentityFragment());
                            break;
                        case "documents":
                            tabBarActivity.pushFragmentInCurrentTab(new DocumentsFragment());
                            break;
                        case "sponsor":
                            tabBarActivity.pushFragmentInCurrentTab(new SponsorFragment());
                            break;
                        case "preferences":
                            tabBarActivity.pushFragmentInCurrentTab(new PreferencesFragment());
                            break;
                        case "security":
                            tabBarActivity.pushFragmentInCurrentTab(new SecurityFragment());
                            break;
                        case "rate":
                            final String appPackageName = tabBarActivity.getPackageName();
                            try {
                                tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            break;
                        case "faq":
                            WebFragment webFragment = new WebFragment();
                            webFragment.title = (String) item.get("title");
                            webFragment.url = "https://www.flooz.me/faq?layout=webview";
                            tabBarActivity.pushFragmentInCurrentTab(webFragment);
                            break;
                        case "cgu":
                            WebFragment webFragment2 = new WebFragment();
                            webFragment2.title = (String) item.get("title");
                            webFragment2.url = "https://www.flooz.me/cgu?layout=webview";
                            tabBarActivity.pushFragmentInCurrentTab(webFragment2);
                            break;
                        case "contact":
                            WebFragment webFragment3 = new WebFragment();
                            webFragment3.title = (String) item.get("title");
                            webFragment3.url = "https://www.flooz.me/contact?layout=webview";
                            tabBarActivity.pushFragmentInCurrentTab(webFragment3);
                            break;
                        case "critics":
                            Intent intent = WebController.newEmailIntent("hello@flooz.me", (String) item.get("title"), "Voici quelques idées pour améliorer l'application : ", "");
                            tabBarActivity.startActivity(intent);
                            break;
                        case "logout":
                            final Dialog validationDialog = new Dialog(tabBarActivity);

                            validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            validationDialog.setContentView(R.layout.custom_dialog_validate_transaction);

                            TextView text = (TextView) validationDialog.findViewById(R.id.dialog_validate_flooz_text);
                            text.setText(tabBarActivity.getResources().getString(R.string.LOGOUT_INFO));
                            text.setTypeface(CustomFonts.customContentRegular(tabBarActivity));

                            Button decline = (Button) validationDialog.findViewById(R.id.dialog_validate_flooz_decline);
                            Button accept = (Button) validationDialog.findViewById(R.id.dialog_validate_flooz_accept);

                            decline.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    validationDialog.dismiss();
                                }
                            });

                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    validationDialog.dismiss();
                                    FloozRestClient.getInstance().logout();
                                }
                            });

                            validationDialog.setCanceledOnTouchOutside(false);
                            validationDialog.show();
                            break;
                    }
                }
            }
        });

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
        ProfileCardFragment controller = new ProfileCardFragment();
        controller.user = FloozRestClient.getInstance().currentUser;
        tabBarActivity.pushFragmentInCurrentTab(controller);
//        List<ActionSheetItem> items = new ArrayList<>();
//
//        items.add(new ActionSheetItem(this.context, R.string.SIGNUP_IMAGE_BUTTON_TAKE, () -> {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//            Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
//            tmpUriImage = fileUri;
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//
//            try {
//                tabBarActivity.startActivityForResult(intent, TAKE_PICTURE);
//            } catch (ActivityNotFoundException e) {
//
//            }
//        }));
//
//        items.add(new ActionSheetItem(this.context, R.string.SIGNUP_IMAGE_BUTTON_CHOOSE, () -> {
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            try {
//                tabBarActivity.startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
//            } catch (ActivityNotFoundException e) {
//
//            }
//        }));
//
//        ActionSheet.showWithItems(tabBarActivity, items);
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

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String path = ImageHelper.getPath(context, selectedImageUri);
                            if (path != null) {
                                File image = new File(path);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                                if (photo != null) {
                                    int rotation = ImageHelper.getRotation(context, selectedImageUri);
                                    if (rotation != 0) {
                                        photo = ImageHelper.rotateBitmap(photo, rotation);
                                    }

                                    int nh = (int) (photo.getHeight() * (512.0 / photo.getWidth()));
                                    Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                                    listAdapter.userViewHolder.imageView.setImageBitmap(scaled);
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
                        }
                    });
                }
            }
        }
    }
}
