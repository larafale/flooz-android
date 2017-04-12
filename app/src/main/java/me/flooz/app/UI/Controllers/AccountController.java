package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import me.flooz.app.Adapter.ProfileListAdapter;
import me.flooz.app.Adapter.ProfileListAdapterDelegate;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.CashinActivity;
import me.flooz.app.UI.Activity.CashoutActivity;
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.FriendRequestActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.Settings.BankSettingsActivity;
import me.flooz.app.UI.Activity.Settings.DocumentsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.IdentitySettingsActivity;
import me.flooz.app.UI.Activity.Settings.NotificationsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.PreferencesSettingsActivity;
import me.flooz.app.UI.Activity.Settings.PrivacySettingsActivity;
import me.flooz.app.UI.Activity.Settings.SecuritySettingsActivity;
import me.flooz.app.UI.Activity.ShopHistoryActivity;
import me.flooz.app.UI.Activity.SponsorActivity;
import me.flooz.app.UI.Activity.WebContentActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.BankFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CashoutFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CreditCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.DocumentsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.FriendRequestFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.IdentityFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotifsSettingsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.PreferencesFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.PrivacyFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.SecurityFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ShopHistoryFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.SponsorFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.WebFragment;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.ImageHelper;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 12/10/15.
 */
public class AccountController extends BaseController implements ProfileListAdapterDelegate {

    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;

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

    public AccountController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public AccountController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.fullname = (TextView) this.currentView.findViewById(R.id.header_title);
        this.username = (TextView) this.currentView.findViewById(R.id.header_subtitle);

        this.fullname.setTypeface(CustomFonts.customTitleExtraLight(parentActivity));
        this.username.setTypeface(CustomFonts.customTitleLight(parentActivity));

        this.listAdapter = new ProfileListAdapter(parentActivity, this);

        this.menuActionList = (StickyListHeadersListView) this.currentView.findViewById(R.id.account_menu_list);
        this.menuActionList.setAdapter(this.listAdapter);

        this.menuActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> item = listAdapter.getItem(position);

                if (item != null && item.containsKey("action")) {
                    String action = (String) item.get("action");

                    switch (action) {
                        case "profile":
                            Intent editIntent = new Intent(parentActivity, EditProfileActivity.class);
                            parentActivity.startActivity(editIntent);
                            parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            break;
                        case "friends":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new FriendRequestFragment());
                            else {
                                Intent friendsIntent = new Intent(parentActivity, FriendRequestActivity.class);
                                parentActivity.startActivity(friendsIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }

                            break;
                        case "shopHistory":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new ShopHistoryFragment());
                            else {
                                Intent bankIntent = new Intent(parentActivity, ShopHistoryActivity.class);
                                parentActivity.startActivity(bankIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "cashout":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new CashoutFragment());
                            else {
                                Intent cashoutIntent = new Intent(parentActivity, CashoutActivity.class);
                                parentActivity.startActivity(cashoutIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "card":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new CreditCardFragment());
                            else {
                                Intent cardIntent = new Intent(parentActivity, CreditCardController.class);
                                parentActivity.startActivity(cardIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "cashin":
                                Intent cardIntent = new Intent(parentActivity, CashinActivity.class);
                                parentActivity.startActivity(cardIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            break;
                        case "bank":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new BankFragment());
                            else {
                                Intent bankIntent = new Intent(parentActivity, BankSettingsActivity.class);
                                parentActivity.startActivity(bankIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "coords":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new IdentityFragment());
                            else {
                                Intent identityIntent = new Intent(parentActivity, IdentitySettingsActivity.class);
                                parentActivity.startActivity(identityIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "documents":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new DocumentsFragment());
                            else {
                                Intent docIntent = new Intent(parentActivity, DocumentsSettingsActivity.class);
                                parentActivity.startActivity(docIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "sponsor":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new SponsorFragment());
                            else {
                                Intent sponsorIntent = new Intent(parentActivity, SponsorActivity.class);
                                parentActivity.startActivity(sponsorIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "privacy":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new PrivacyFragment());
                            else {
                                Intent prefIntent = new Intent(parentActivity, PrivacySettingsActivity.class);
                                parentActivity.startActivity(prefIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "notifSetting":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new NotifsSettingsFragment());
                            else {
                                Intent prefIntent = new Intent(parentActivity, NotificationsSettingsActivity.class);
                                parentActivity.startActivity(prefIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "security":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new SecurityFragment());
                            else {
                                Intent bankIntent = new Intent(parentActivity, SecuritySettingsActivity.class);
                                parentActivity.startActivity(bankIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "rate":
                            final String appPackageName = parentActivity.getPackageName();
                            try {
                                parentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                parentActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            break;
                        case "faq":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER) {
                                WebFragment webFragment = new WebFragment();
                                webFragment.title = (String) item.get("title");
                                webFragment.url = "https://www.flooz.me/faq?layout=webview";
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(webFragment);
                            } else {
                                Intent faqIntent = new Intent(parentActivity, WebContentActivity.class);
                                faqIntent.putExtra("title", (String) item.get("title"));
                                faqIntent.putExtra("url", "https://www.flooz.me/faq?layout=webview");
                                parentActivity.startActivity(faqIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "cgu":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER) {
                                WebFragment webFragment = new WebFragment();
                                webFragment.title = (String) item.get("title");
                                webFragment.url = "https://www.flooz.me/cgu?layout=webview";
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(webFragment);
                            } else {
                                Intent cguIntent = new Intent(parentActivity, WebContentActivity.class);
                                cguIntent.putExtra("title", (String) item.get("title"));
                                cguIntent.putExtra("url", "https://www.flooz.me/cgu?layout=webview");
                                parentActivity.startActivity(cguIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "contact":
                            if (currentKind == ControllerKind.FRAGMENT_CONTROLLER) {
                                WebFragment webFragment = new WebFragment();
                                webFragment.title = (String) item.get("title");
                                webFragment.url = "https://www.flooz.me/contact?layout=webview";
                                ((HomeActivity)parentActivity).pushFragmentInCurrentTab(webFragment);
                            } else {
                                Intent cguIntent = new Intent(parentActivity, SecuritySettingsActivity.class);
                                cguIntent.putExtra("title", (String) item.get("title"));
                                cguIntent.putExtra("url", "https://www.flooz.me/contact?layout=webview");
                                parentActivity.startActivity(cguIntent);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }
                            break;
                        case "critics":
                            Intent intent = WebController.newEmailIntent("hello@flooz.me", (String) item.get("title"), "Voici quelques idées pour améliorer l'application : ", "");
                            parentActivity.startActivity(intent);
                            break;
                        case "logout":
                            final Dialog validationDialog = new Dialog(parentActivity);

                            validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            validationDialog.setContentView(R.layout.custom_dialog_validate_transaction);

                            TextView text = (TextView) validationDialog.findViewById(R.id.dialog_validate_flooz_text);
                            text.setText(parentActivity.getResources().getString(R.string.LOGOUT_INFO));
                            text.setTypeface(CustomFonts.customContentRegular(parentActivity));

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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FloozRestClient.getInstance() != null) {
            FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    reloadUserData();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
            this.reloadUserData();
        }

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
        ((HomeActivity)parentActivity).pushFragmentInCurrentTab(controller);
    }

    @Override
    public void balanceClicked() {
        final Dialog dialog = new Dialog(parentActivity);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_balance);

        TextView title = (TextView) dialog.findViewById(R.id.dialog_wallet_title);
        title.setTypeface(CustomFonts.customContentRegular(parentActivity), Typeface.BOLD);

        if (FloozRestClient.getInstance().currentTexts.balancePopupTitle != null)
            title.setText(FloozRestClient.getInstance().currentTexts.balancePopupTitle);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_wallet_msg);
        text.setTypeface(CustomFonts.customContentRegular(parentActivity));

        if (FloozRestClient.getInstance().currentTexts.balancePopupText != null)
            text.setText(FloozRestClient.getInstance().currentTexts.balancePopupText);

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

    @Override
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
                            String path = ImageHelper.getPath(parentActivity, selectedImageUri);
                            if (path != null) {
                                File image = new File(path);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                                if (photo != null) {
                                    int rotation = ImageHelper.getRotation(parentActivity, selectedImageUri);
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
                                                FloozApplication.getInstance().imageFetcher.attachImage(user.avatarURL, listAdapter.userViewHolder.imageView);
                                            else
                                                listAdapter.userViewHolder.imageView.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.avatar_default));
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
