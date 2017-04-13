package me.flooz.app.App;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.view.inputmethod.InputMethodManager;

import com.facebook.FacebookSdk;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.github.ajalt.reprint.core.Reprint;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.branch.referral.BranchApp;
import me.flooz.app.BuildConfig;
import me.flooz.app.Model.FLReport;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.CollectActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.StartActivity;
import me.flooz.app.UI.Activity.TransactionActivity;
import me.flooz.app.UI.Activity.UserProfileActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TransactionCardFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.SafeLooper;

/**
 * Created by Flooz on 9/8/14.
 */
public class FloozApplication extends BranchApp
{
    private static Context context;

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static String SENDER_ID = "202597897110";
    private GoogleCloudMessaging gcm;
    public String regid;
    public JSONArray pendingTriggers;
    public JSONObject pendingPopup;
    public JSONObject branchParams;

    public static MixpanelAPI mixpanelAPI;
    private static FloozApplication instance;

    public static boolean appInForeground = false;

    private Activity currentActivity = null;

    private Handler activityStateHandler;

    private Runnable backgroundRunnable = new Runnable() {
        @Override
        public void run() {
                appInForeground = false;
        }
    };

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

        if (BuildConfig.DEBUG_API) {
            mixpanelAPI = MixpanelAPI.getInstance(this, "82c134b277474d6143decdc6ae73d5c9");
        } else {
            mixpanelAPI = MixpanelAPI.getInstance(this, "81df2d3dcfb7c866f37e78f1ad8aa1c4");
        }

        FloozApplication.instance = this;
        FloozApplication.context = getApplicationContext();

        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(context);

        FacebookSdk.sdkInitialize(this);
        Fresco.initialize(this);

        if (regid.isEmpty()) {
            registerInBackground();
        }

        activityStateHandler = new Handler(Looper.getMainLooper());

        SafeLooper.install();

        Reprint.initialize(this);

        this.registerActivityLifecycleCallbacks(FLTriggerManager.getInstance());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences("FloozPrefs", Context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        assert registrationId != null;
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

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
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
        FloozRestClient.getInstance().textObjectFromApi(null);

        mixpanelAPI.getPeople().identify(FloozRestClient.getInstance().currentUser.userId);
        mixpanelAPI.getPeople().initPushHandling("202597897110");

        if (mixpanelAPI.getPeople().getSurveyIfAvailable() != null)
            mixpanelAPI.getPeople().showSurveyIfAvailable(getCurrentActivity());
    }

    public void displayMainView() {
        if (!(this.getCurrentActivity() instanceof HomeActivity)) {
            Activity tmp = this.getCurrentActivity();

            Intent intent = new Intent();
            intent.setClass(context, HomeActivity.class);
            if (tmp == null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                tmp.startActivity(intent);
                tmp.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                tmp.finish();
            }
        }
    }

    public void didDisconnected() {
        this.displayStartView();
        mixpanelAPI.getPeople().identify(FLHelper.generateRandomString());
    }

    public void displayStartView() {
        if (!(this.getCurrentActivity() instanceof StartActivity)) {
            Activity tmp = this.getCurrentActivity();

            Intent intent = new Intent();
            intent.setClass(context, StartActivity.class);
            if (tmp == null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                tmp.startActivity(intent);
                tmp.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                tmp.finish();
            }
        }
    }

    public Activity getCurrentActivity(){
        return this.currentActivity;
    }

    public void setCurrentActivity(Activity activity){

        if (activity == null) {
            activityStateHandler.postDelayed(backgroundRunnable, 100);
        } else {
            appInForeground = true;
            activityStateHandler.removeCallbacks(backgroundRunnable);
        }

        if (!BuildConfig.LOCAL_API) {
            if (this.currentActivity == null && activity != null) {
                FloozRestClient.getInstance().initializeSockets();
            } else if (this.currentActivity != null && activity == null) {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);
                FloozRestClient.getInstance().closeSockets();
            }
        }

        this.currentActivity = activity;
    }

    public void showUserProfile(FLUser user) {
        this.showUserProfile(user, null);
    }

    public void showUserProfile(FLUser user, Runnable completion) {
        if (user == null || user.isCactus) {
            return;
        }

        if (this.currentActivity instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity)this.currentActivity;
            if (homeActivity.currentFragment instanceof ProfileCardFragment) {
                ProfileCardFragment userProfileFragment = (ProfileCardFragment)homeActivity.currentFragment;
                if (userProfileFragment.user.userId.contentEquals(user.userId)) {

                } else {
                    ProfileCardFragment controller = new ProfileCardFragment();
                    controller.user = user;
                    homeActivity.pushFragmentInCurrentTab(controller, completion);
                }
            } else {
                ProfileCardFragment controller = new ProfileCardFragment();
                controller.user = user;
                homeActivity.pushFragmentInCurrentTab(controller, completion);
            }
        } else if (currentActivity != null) {
            Intent intent = new Intent(getAppContext(), UserProfileActivity.class);
            intent.putExtra("user", user.json.toString());
            currentActivity.startActivity(intent);
            currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    public void showTransactionCard(FLTransaction transaction) {
        this.showTransactionCard(transaction, false, null);
    }

    public void showTransactionCard(FLTransaction transaction, Runnable completion) {
        this.showTransactionCard(transaction, false, completion);
    }

    public void showTransactionCard(FLTransaction transaction, Boolean insertComment) {
        this.showTransactionCard(transaction, insertComment, null);
    }

    public void showTransactionCard(FLTransaction transaction, Boolean insertComment, Runnable completion) {
        Activity activity = this.getCurrentActivity();

        if (activity != null) {
            if (activity instanceof HomeActivity) {
                FloozRestClient.getInstance().readNotification(transaction.transactionId, null);
                TransactionCardFragment transactionCardFragment = new TransactionCardFragment();

                transactionCardFragment.insertComment = insertComment;
                transactionCardFragment.transaction = transaction;

                ((HomeActivity) activity).pushFragmentInCurrentTab(transactionCardFragment, completion);
            } else {
                Intent intent = new Intent(activity, TransactionActivity.class);
                intent.putExtra("insertComment", insertComment);
                intent.putExtra("transaction", transaction.json.toString());
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }
    }


    public void showCollect(FLTransaction collect) {
        this.showCollect(collect, false, null);
    }

    public void showCollect(FLTransaction collect, Runnable completion) {
        this.showCollect(collect, false, completion);
    }

    public void showCollect(FLTransaction collect, Boolean insertComment) {
        this.showCollect(collect, insertComment, null);
    }

    public void showCollect(FLTransaction collect, Boolean insertComment, Runnable completion) {
        Activity activity = this.getCurrentActivity();

        if (activity != null) {
            if (activity instanceof HomeActivity) {
                FloozRestClient.getInstance().readNotification(collect.transactionId, null);
                CollectFragment collectFragment = new CollectFragment();

                collectFragment.insertComment = insertComment;
                collectFragment.transaction = collect;

                ((HomeActivity) activity).pushFragmentInCurrentTab(collectFragment, completion);
            } else {
                Intent intent = new Intent(activity, CollectActivity.class);
                intent.putExtra("insertComment", insertComment);
                intent.putExtra("collect", collect.json.toString());
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }
    }

    public void showReportActionMenu(final FLTransaction transac) {
        List<ActionSheetItem> items = new ArrayList<>();

//        items.add(new ActionSheetItem(this, R.string.MENU_QR_CODE, new ActionSheetItem.ActionSheetItemClickListener() {
//            @Override
//            public void onClick() {
//                QRCodeWriter writer = new QRCodeWriter();
//                BitMatrix bitMatrix;
//                try {
//                    bitMatrix = writer.encode("flooz://" + transac.transactionId, BarcodeFormat.QR_CODE, 300, 300);
//                    int height = bitMatrix.getHeight();
//                    int width = bitMatrix.getWidth();
//                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//                    for (int x = 0; x < width; x++){
//                        for (int y = 0; y < height; y++){
//                            bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                    ((HomeActivity)getCurrentActivity()).showImageViewer(bmp);
//                } catch (WriterException e){
//                    e.printStackTrace();
//                }
//            }
//        }));

        items.add(new ActionSheetItem(this, R.string.MENU_REPORT_LINE, new ActionSheetItem.ActionSheetItemClickListener() {
            @Override
            public void onClick() {
                new AlertDialog.Builder(getCurrentActivity())
                        .setTitle(R.string.MENU_REPORT_USER)
                        .setMessage(R.string.REPORT_LINE_ALERT_MESSAGE)
                        .setPositiveButton(R.string.GLOBAL_YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FloozRestClient.getInstance().reportContent(new FLReport(FLReport.ReportType.Transaction, transac.transactionId), null);
                            }
                        })
                        .setNegativeButton(R.string.GLOBAL_NO, null)
                        .show();
            }
        }));

        ActionSheet.showWithItems(getCurrentActivity(), items);
    }
}
