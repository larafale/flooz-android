package flooz.android.com.flooz.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Formatter;
import java.util.List;

import flooz.android.com.flooz.Adapter.MenuListAdapter;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import flooz.android.com.flooz.Utils.MenuItem;

public class ProfileFragment extends Fragment
{
    public HomeActivity parentActivity;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, null);

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

        this.menuActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (parentActivity != null) {
                    switch (i) {
                        case 0:
                            parentActivity.pushMainFragment("profile_settings", R.animator.slide_up, android.R.animator.fade_out);
                            break;
                        case 1:
                            parentActivity.pushMainFragment("notifications", R.animator.slide_up, android.R.animator.fade_out);
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                        case 4:
                            FloozRestClient.getInstance().logout();
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

//            this.completeNb.setText(user.profileCompletion);
//            this.floozNb.setText(user.transactionsCount.toString());
//            this.friendsNb.setText(user.friendsCount.toString());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onDestroy()
    {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
        super.onDestroy();
    }
}
