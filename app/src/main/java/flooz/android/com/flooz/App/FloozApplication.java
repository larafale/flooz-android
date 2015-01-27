package flooz.android.com.flooz.App;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLReport;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Activity.StartActivity;
import flooz.android.com.flooz.UI.Fragment.Home.NewFloozFragment;
import flooz.android.com.flooz.UI.Tools.ActionSheet;
import flooz.android.com.flooz.UI.Tools.ActionSheetItem;
import flooz.android.com.flooz.Utils.FLHelper;

/**
 * Created by Flooz on 9/8/14.
 */
public class FloozApplication extends Application
{
    private static Context context;

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static String SENDER_ID = "202597897110";
    private GoogleCloudMessaging gcm;
    public String regid;


    public static MixpanelAPI mixpanelAPI;
    private static FloozApplication instance;

    public static boolean appInForeground = false;

    private Activity currentActivity = null;

    public static FloozApplication getInstance()
    {
        return instance;
    }

    private FLUser userActionMenu;

    public static Context getAppContext() {
        return FloozApplication.context;
    }

    public void onCreate(){
        super.onCreate();

        mixpanelAPI = MixpanelAPI.getInstance(this, "86108691338079db55b5fac30140d895");

        FloozApplication.instance = this;
        FloozApplication.context = getApplicationContext();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(150 * 1024 * 1024)
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);

        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(context);

        if (regid.isEmpty()) {
            registerInBackground();
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences("FloozPrefs", Context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }


        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        };
        task.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences("FloozPrefs", Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();

        FloozRestClient.getInstance().checkDeviceToken();
    }

    public static void performLocalNotification(Intent intent) {
        LocalBroadcastManager.getInstance(FloozApplication.context).sendBroadcast(intent);
    }

    public void didConnected() {
        FloozRestClient.getInstance().initializeSockets();
        mixpanelAPI.identify(FloozRestClient.getInstance().currentUser.userId);
    }

    public void displayMainView() {
        if (!(this.getCurrentActivity() instanceof HomeActivity)) {
            Activity tmp = this.getCurrentActivity();

            Intent intent = new Intent();
            intent.setClass(context, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            if (this.getCurrentActivity() != null) {
                this.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                this.getCurrentActivity().finish();
            }
        }
    }

    public void didDisconnected() {
        this.displayStartView();
        mixpanelAPI.identify(FLHelper.generateRandomString());
    }

    public void displayStartView() {
        Intent intent = new Intent();
        intent.setClass(context, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (this.getCurrentActivity() != null) {
            this.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            this.getCurrentActivity().finish();
        }
    }

    public Activity getCurrentActivity(){
        return this.currentActivity;
    }

    public void setCurrentActivity(Activity activity){
        if (activity != null)
            appInForeground = true;
        else
            appInForeground = false;
        if (this.currentActivity == null && activity != null) {
            FloozRestClient.getInstance().initializeSockets();
        } else if (this.currentActivity != null && activity == null) {
            FloozRestClient.getInstance().closeSockets();
        }

        this.currentActivity = activity;
    }

    private ActionSheetItem.ActionSheetItemClickListener createTransaction = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            if (getCurrentActivity() instanceof HomeActivity) {
                ((NewFloozFragment) ((HomeActivity) getCurrentActivity()).contentFragments.get("create")).initWithUser(userActionMenu);
                ((HomeActivity) getCurrentActivity()).pushMainFragment("create", R.animator.slide_up, android.R.animator.fade_out);
            }
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener addFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().sendFriendRequest(userActionMenu.userId, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                }

                @Override
                public void failure(int statusCode, FLError error) { }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener removeFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(userActionMenu.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                }

                @Override
                public void failure(int statusCode, FLError error) { }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener acceptFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(userActionMenu.userId, FloozRestClient.FriendAction.Accept, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                }

                @Override
                public void failure(int statusCode, FLError error) { }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener declineFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(userActionMenu.userId, FloozRestClient.FriendAction.Decline, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                }

                @Override
                public void failure(int statusCode, FLError error) { }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener blockUser = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            new AlertDialog.Builder(getCurrentActivity())
                    .setTitle(R.string.MENU_BLOCK_USER)
                    .setMessage(R.string.BLOCK_USER_ALERT_MESSAGE)
                    .setPositiveButton(R.string.GLOBAL_YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FloozRestClient.getInstance().blockUser(userActionMenu.userId, null);
                        }
                    })
                    .setNegativeButton(R.string.GLOBAL_NO, null)
                    .show();
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener reportUser = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            new AlertDialog.Builder(getCurrentActivity())
                    .setTitle(R.string.MENU_REPORT_USER)
                    .setMessage(R.string.REPORT_USER_ALERT_MESSAGE)
                    .setPositiveButton(R.string.GLOBAL_YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FloozRestClient.getInstance().reportContent(new FLReport(FLReport.ReportType.User, userActionMenu.userId), null);
                        }
                    })
                    .setNegativeButton(R.string.GLOBAL_NO, null)
                    .show();
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showUserPicture = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            ((HomeActivity)getCurrentActivity()).showImageViewer(userActionMenu.avatarURL);
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showOtherMenu = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {

            List<ActionSheetItem> items = new ArrayList<>();

            if (userActionMenu.avatarURL != null && !userActionMenu.avatarURL.isEmpty())
                items.add(new ActionSheetItem(getAppContext(), R.string.MENU_AVATAR, showUserPicture));

            if (userActionMenu.isFriend())
                items.add(new ActionSheetItem(getAppContext(), R.string.MENU_REMOVE_FRIENDS, removeFriend));

            items.add(new ActionSheetItem(getAppContext(), R.string.MENU_BLOCK_USER, blockUser));
            items.add(new ActionSheetItem(getAppContext(), R.string.MENU_REPORT_USER, reportUser));

            ActionSheet.showWithItems(getCurrentActivity(), items);
        }
    };

    public void showUserActionMenu(FLUser user) {
        if (user == null || user.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId) || user.username == null || user.fullname == null)
            return;

        this.userActionMenu = user;

        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(this, R.string.MENU_NEW_FLOOZ, createTransaction));

        if (!this.userActionMenu.isFriend() && !this.userActionMenu.isFriendRequest())
            items.add(new ActionSheetItem(this, R.string.MENU_ADD_FRIENDS, addFriend));
        else if (this.userActionMenu.isFriendRequest()) {
            items.add(new ActionSheetItem(this, R.string.FRIEND_REQUEST_ACCEPT, acceptFriend));
            items.add(new ActionSheetItem(this, R.string.FRIEND_REQUEST_REFUSE, declineFriend));
        }

        items.add(new ActionSheetItem(this, R.string.MENU_OTHER, showOtherMenu));

        ActionSheet.showWithItems(getCurrentActivity(), items);
    }

    public void showReportActionMenu(final String transacId) {
        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(this, R.string.MENU_REPORT_LINE, new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {
                new AlertDialog.Builder(getCurrentActivity())
                        .setTitle(R.string.MENU_REPORT_USER)
                        .setMessage(R.string.REPORT_LINE_ALERT_MESSAGE)
                        .setPositiveButton(R.string.GLOBAL_YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FloozRestClient.getInstance().reportContent(new FLReport(FLReport.ReportType.Transaction, transacId), null);
                            }
                        })
                        .setNegativeButton(R.string.GLOBAL_NO, null)
                        .show();
            }
        }));
        ActionSheet.showWithItems(getCurrentActivity(), items);
    }
}
