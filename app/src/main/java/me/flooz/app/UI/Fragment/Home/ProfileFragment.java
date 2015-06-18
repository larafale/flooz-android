package me.flooz.app.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.MenuListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
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
import me.flooz.app.UI.Activity.ScannerActivity;
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

public class ProfileFragment extends HomeBaseFragment
{
    Context context;

    public RoundedImageView userView;
    private TextView fullname;
    private TextView username;
    private TextView balanceValue;
    private ListView menuActionList;

    private List<MenuItem> menuItems;
    private MenuListAdapter listAdapter;
    public Uri tmpUriImage;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadUserData();
        }
    };

    private BroadcastReceiver reloadNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            menuItems.get(1).nbNotification = FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications;
            listAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, null);

        this.context = inflater.getContext();

        this.userView = (RoundedImageView) view.findViewById(R.id.userView);
        this.fullname = (TextView) view.findViewById(R.id.fullname);
        this.username = (TextView) view.findViewById(R.id.username);
        this.balanceValue = (TextView) view.findViewById(R.id.balanceValue);

        TextView floozNb = (TextView) view.findViewById(R.id.account_nb_flooz);
        TextView floozLabel = (TextView) view.findViewById(R.id.account_flooz_label);
        TextView friendsNb = (TextView) view.findViewById(R.id.account_nb_friends);
        TextView friendsLabel = (TextView) view.findViewById(R.id.account_friends_label);
        TextView completeNb = (TextView) view.findViewById(R.id.account_nb_complete);
        TextView completeLabel = (TextView) view.findViewById(R.id.account_complete_label);

        this.fullname.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.username.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.balanceValue.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        floozNb.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        floozLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        friendsNb.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        friendsLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        completeNb.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        completeLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.menuActionList = (ListView) view.findViewById(R.id.account_menu_list);

        this.balanceValue.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(inflater.getContext());

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog_balance);

            TextView title = (TextView) dialog.findViewById(R.id.dialog_wallet_title);
            title.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);

            TextView text = (TextView) dialog.findViewById(R.id.dialog_wallet_msg);
            text.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

            Button close = (Button) dialog.findViewById(R.id.dialog_wallet_btn);

            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });

        this.userView.setOnClickListener(v -> {
            List<ActionSheetItem> items = new ArrayList<>();

            items.add(new ActionSheetItem(inflater.getContext(), R.string.SIGNUP_IMAGE_BUTTON_TAKE, new ActionSheetItem.ActionSheetItemClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
                    tmpUriImage = fileUri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    parentActivity.startActivityForResult(intent, HomeActivity.TAKE_PICTURE);
                }
            }));

            items.add(new ActionSheetItem(inflater.getContext(), R.string.SIGNUP_IMAGE_BUTTON_CHOOSE, new ActionSheetItem.ActionSheetItemClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    parentActivity.startActivityForResult(intent, HomeActivity.SELECT_PICTURE);
                }
            }));

            ActionSheet.showWithItems(parentActivity, items);
        });

        menuActionList.setOnItemClickListener((adapterView, view1, i, l) -> {
            if (parentActivity != null) {
                switch (i) {
                    case 0:
                        FloozRestClient.getInstance().updateCurrentUser(null);
                        Intent intentSettings = new Intent(parentActivity, ProfileSettingsActivity.class);
                        parentActivity.startActivity(intentSettings);
                        parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        break;
                    case 1:
                        Intent intentNotifs = new Intent(parentActivity, NotificationActivity.class);
                        parentActivity.startActivity(intentNotifs);
                        parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        break;
                    case 2:
                        Intent intentShare = new Intent(parentActivity, ShareAppActivity.class);
                        parentActivity.startActivity(intentShare);
                        parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        break;
//                        case 3:
//                            Intent intentScan = new Intent(parentActivity, ScannerActivity.class);
//                            parentActivity.startActivity(intentScan);
//                            parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
//                            break;
                    case 3:
                        FloozRestClient.getInstance().showLoadView();
                        FloozRestClient.getInstance().cashoutValidate(new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                Intent intentCashout = new Intent(parentActivity, CashoutActivity.class);
                                parentActivity.startActivity(intentCashout);
                                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {

                            }
                        });
                        break;
                    case 4:
                        Intent intentCashout = new Intent(parentActivity, AboutActivity.class);
                        parentActivity.startActivity(intentCashout);
                        parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        break;
                    default:
                        break;
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

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadNotificationsReceiver);
    }

    private void reloadUserData()
    {
        FLUser user = FloozRestClient.getInstance().currentUser;

        if (user != null) {
            this.fullname.setText(user.fullname);

            this.balanceValue.setText(FLHelper.trimTrailingZeros(String.format("%.2f", user.amount.floatValue()).replace(',', '.')) + " €");
            this.username.setText("@" + user.username);

            if (user.avatarURL != null && !user.avatarURL.isEmpty())
                ImageLoader.getInstance().displayImage(user.avatarURL, this.userView);
            else
                this.userView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));

            int nbNotif = 0;

            if (user.creditCard == null || user.creditCard.cardId == null || user.creditCard.cardId.isEmpty())
                ++nbNotif;

            List missingFields;
            try {
                missingFields = JSONHelper.toList(user.json.optJSONArray("missingFields"));

                if (missingFields.contains("sepa"))
                    ++nbNotif;
                if (missingFields.contains("secret"))
                    ++nbNotif;
                if (missingFields.contains("cniRecto"))
                    ++nbNotif;
                if (missingFields.contains("cniVerso"))
                    ++nbNotif;
                if (missingFields.contains("justificatory"))
                    ++nbNotif;
                if (missingFields.contains("address"))
                    ++nbNotif;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            this.menuItems = new ArrayList<>();

            this.menuItems.add(new MenuItem(this.context, R.string.ACCOUNT_MENU_PROFILE, R.drawable.account_profile_button, nbNotif));
            this.menuItems.add(new MenuItem(this.context, R.string.ACCOUNT_MENU_NOTIFICATION, R.drawable.account_notification_button, FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications));
            this.menuItems.add(new MenuItem(this.context, R.string.ACCOUNT_MENU_SHARE, R.drawable.account_share_button));
//        this.menuItems.add(new MenuItem(inflater.getContext(), R.string.ACCOUNT_MENU_SCANNER, R.drawable.qrcode));
            this.menuItems.add(new MenuItem(this.context, R.string.ACCOUNT_MENU_CASHOUT, R.drawable.account_bank_button));
            this.menuItems.add(new MenuItem(this.context, R.string.ACCOUNT_MENU_OTHER, R.drawable.account_other_button));

            this.listAdapter = new MenuListAdapter(this.context, this.menuItems);
            menuActionList.setAdapter(this.listAdapter);
        }
    }
}
