package flooz.android.com.flooz.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Adapter.MenuListAdapter;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.CameraFullscreenFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Camera.ImageGalleryFragment;
import flooz.android.com.flooz.UI.Tools.ActionSheet;
import flooz.android.com.flooz.UI.Tools.ActionSheetItem;
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import flooz.android.com.flooz.Utils.ImageHelper;
import flooz.android.com.flooz.Utils.MenuItem;

public class ProfileFragment extends HomeBaseFragment implements CustomCameraHost.CustomCameraHostDelegate
{
    private ProfileFragment instance;
    private LinearLayout accountUserView;
    private RoundedImageView userView;
    private TextView fullname;
    private TextView username;
    private TextView balanceValue;
    private TextView floozNb;
    private TextView floozLabel;
    private TextView friendsNb;
    private TextView friendsLabel;
    private TextView completeNb;
    private TextView completeLabel;

    private ListView menuActionList;
    private List<MenuItem> menuItems;
    private MenuListAdapter listAdapter;

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

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, null);

        this.instance = this;
        this.accountUserView = (LinearLayout) view.findViewById(R.id.accountUserView);
        this.userView = (RoundedImageView) view.findViewById(R.id.userView);
        this.fullname = (TextView) view.findViewById(R.id.fullname);
        this.username = (TextView) view.findViewById(R.id.username);
        this.balanceValue = (TextView) view.findViewById(R.id.balanceValue);

        this.floozNb = (TextView) view.findViewById(R.id.account_nb_flooz);
        this.floozLabel = (TextView) view.findViewById(R.id.account_flooz_label);
        this.friendsNb = (TextView) view.findViewById(R.id.account_nb_friends);
        this.friendsLabel = (TextView) view.findViewById(R.id.account_friends_label);
        this.completeNb = (TextView) view.findViewById(R.id.account_nb_complete);
        this.completeLabel = (TextView) view.findViewById(R.id.account_complete_label);

        this.fullname.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.username.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.balanceValue.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.floozNb.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.floozLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.friendsNb.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.friendsLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.completeNb.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.completeLabel.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.menuActionList = (ListView)view.findViewById(R.id.account_menu_list);

        this.menuItems = new ArrayList<MenuItem>();

        this.menuItems.add(new MenuItem(inflater.getContext(), R.string.ACCOUNT_MENU_PROFILE, R.drawable.account_profile_button));
        this.menuItems.add(new MenuItem(inflater.getContext(), R.string.ACCOUNT_MENU_NOTIFICATION, R.drawable.account_notification_button, FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications));
        this.menuItems.add(new MenuItem(inflater.getContext(), R.string.ACCOUNT_MENU_SHARE, R.drawable.account_share_button));
        this.menuItems.add(new MenuItem(inflater.getContext(), R.string.ACCOUNT_MENU_CASHOUT, R.drawable.account_bank_button));
        this.menuItems.add(new MenuItem(inflater.getContext(), R.string.ACCOUNT_MENU_OTHER, R.drawable.account_other_button));

        this.listAdapter = new MenuListAdapter(inflater.getContext(), this.menuItems);
        this.menuActionList.setAdapter(this.listAdapter);

        this.userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<ActionSheetItem> items = new ArrayList<>();

                items.add(new ActionSheetItem(inflater.getContext(), R.string.SIGNUP_IMAGE_BUTTON_TAKE, new ActionSheetItem.ActionSheetItemClickListener() {
                    @Override
                    public void onClick() {
                        ((CameraFullscreenFragment)parentActivity.contentFragments.get("camera_fullscreen")).cameraHostDelegate = instance;
                        ((CameraFullscreenFragment)parentActivity.contentFragments.get("camera_fullscreen")).canAccessAlbum = false;
                        parentActivity.pushMainFragment("camera_fullscreen", R.animator.slide_up, android.R.animator.fade_out);
                    }
                }));

                items.add(new ActionSheetItem(inflater.getContext(), R.string.SIGNUP_IMAGE_BUTTON_CHOOSE, new ActionSheetItem.ActionSheetItemClickListener() {
                    @Override
                    public void onClick() {
                        ((ImageGalleryFragment)parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = instance;
                        parentActivity.pushMainFragment("photo_gallery", R.animator.slide_up, android.R.animator.fade_out);
                    }
                }));

                ActionSheet.showWithItems(parentActivity, items);
            }
        });

        this.menuActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (parentActivity != null) {
                    switch (i) {
                        case 0:
                            FloozRestClient.getInstance().updateCurrentUser(null);
                            parentActivity.pushMainFragment("profile_settings", R.animator.slide_up, android.R.animator.fade_out);
                            break;
                        case 1:
                            ((NotificationFragment)parentActivity.contentFragments.get("notifications")).showCross = false;
                            parentActivity.pushMainFragment("notifications", R.animator.slide_up, android.R.animator.fade_out);
                            break;
                        case 2:
                            parentActivity.pushMainFragment("invite", R.animator.slide_up, android.R.animator.fade_out);
                            break;
                        case 3:
                            FloozRestClient.getInstance().showLoadView();
                            FloozRestClient.getInstance().cashoutValidate(new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    parentActivity.pushMainFragment("cashout", R.animator.slide_up, android.R.animator.fade_out);
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {

                                }
                            });
                            break;
                        case 4:
                            parentActivity.pushMainFragment("other_menu", R.animator.slide_up, android.R.animator.fade_out);
                            break;
                        default:
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
    }

    private void reloadUserData()
    {
        FLUser user = FloozRestClient.getInstance().currentUser;

        if (user != null) {
            this.fullname.setText(user.fullname);

            this.balanceValue.setText(String.format("%.2f", user.amount.floatValue()).replace(',', '.') + " €");
            this.username.setText("@" + user.username);

            if (user.avatarURL != null)
                ImageLoader.getInstance().displayImage(user.avatarURL, this.userView);
            else
                this.userView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
        }
    }

    @Override
    public void onDestroy()
    {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
        super.onDestroy();
    }

    @Override
    public void photoTaken(final Bitmap photo) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                FloozRestClient.getInstance().uploadDocument("picId", ImageHelper.convertBitmapInFile(photo), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FloozRestClient.getInstance().updateCurrentUser(null);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
                parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });
    }
}
