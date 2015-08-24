package me.flooz.app.UI.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.R;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.UI.Fragment.Home.ProfileFragment;
import me.flooz.app.UI.Fragment.Home.TabBarFragment;
import me.flooz.app.UI.Fragment.Home.TimelineFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.UI.Fragment.Home.TransactionCardFragment;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.ViewServer;
import uk.co.senab.photoview.PhotoViewAttacher;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;

public class HomeActivity extends Activity implements TimelineFragment.TimelineFragmentDelegate
{
    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;

    private enum TabID {
        HOME_TAB,
        NOTIF_TAB,
        SHARE_TAB,
        ACCOUNT_TAB
    }

    private HomeActivity instance;

    private FragmentManager fragmentManager;

    private TimelineFragment timelineFragment;
    private ProfileFragment accountFragment;

    private TabBarFragment currentFragment;

    private LinearLayout homeTab;
    private LinearLayout notifTab;
    private LinearLayout floozTab;
    private LinearLayout shareTab;
    private LinearLayout accountTab;

    private ImageView homeTabImage;
    private ImageView notifTabImage;
    private ImageView floozTabImage;
    private ImageView shareTabImage;
    private ImageView accountTabImage;

    private TextView homeTabText;
    private TextView notifTabText;
    private TextView shareTabText;
    private TextView accountTabText;

    private TextView homeTabBadge;
    private TextView notifTabBadge;
    private TextView shareTabBadge;
    private TextView accountTabBadge;

    private TabID currentTab;

    private RelativeLayout imageViewer;
    private ImageView imageViewerClose;
    private ProgressBar imageViewerProgress;
    private ImageView imageViewerImage;
    private PhotoViewAttacher imageViewerAttacher;

    private RelativeLayout transactionCardContainer;

    private TransactionCardFragment transactionCardFragment;

    public FloozApplication floozApp;

    private BroadcastReceiver reloadNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeTabBadgeValue(TabID.NOTIF_TAB, FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.instance = this;

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.fragmentManager = this.getFragmentManager();

        this.setContentView(R.layout.activity_home);

        this.homeTab = (LinearLayout) this.findViewById(R.id.home_tab);
        this.notifTab = (LinearLayout) this.findViewById(R.id.notif_tab);
        this.floozTab = (LinearLayout) this.findViewById(R.id.flooz_tab);
        this.shareTab = (LinearLayout) this.findViewById(R.id.share_tab);
        this.accountTab = (LinearLayout) this.findViewById(R.id.account_tab);

        this.homeTabImage = (ImageView) this.findViewById(R.id.home_tab_image);
        this.notifTabImage = (ImageView) this.findViewById(R.id.notif_tab_image);
        this.floozTabImage = (ImageView) this.findViewById(R.id.flooz_tab_image);
        this.shareTabImage = (ImageView) this.findViewById(R.id.share_tab_image);
        this.accountTabImage = (ImageView) this.findViewById(R.id.account_tab_image);

        this.homeTabText = (TextView) this.findViewById(R.id.home_tab_text);
        this.notifTabText = (TextView) this.findViewById(R.id.notif_tab_text);
        this.shareTabText = (TextView) this.findViewById(R.id.share_tab_text);
        this.accountTabText = (TextView) this.findViewById(R.id.account_tab_text);

        this.homeTabBadge = (TextView) this.findViewById(R.id.home_tab_badge);
        this.notifTabBadge = (TextView) this.findViewById(R.id.notif_tab_badge);
        this.shareTabBadge = (TextView) this.findViewById(R.id.share_tab_badge);
        this.accountTabBadge = (TextView) this.findViewById(R.id.account_tab_badge);

        int clearColor = this.getResources().getColor(R.color.placeholder);
        Typeface titleTypeface = CustomFonts.customContentLight(this);
        Typeface badgeTypeface = CustomFonts.customContentRegular(this);

        this.homeTabText.setTypeface(titleTypeface);
        this.notifTabText.setTypeface(titleTypeface);
        this.shareTabText.setTypeface(titleTypeface);
        this.accountTabText.setTypeface(titleTypeface);

        this.homeTabBadge.setTypeface(badgeTypeface);
        this.notifTabBadge.setTypeface(badgeTypeface);
        this.shareTabBadge.setTypeface(badgeTypeface);
        this.accountTabBadge.setTypeface(badgeTypeface);

        this.homeTabText.setTextColor(clearColor);
        this.notifTabText.setTextColor(clearColor);
        this.shareTabText.setTextColor(clearColor);
        this.accountTabText.setTextColor(clearColor);

        this.homeTabImage.setColorFilter(clearColor);
        this.notifTabImage.setColorFilter(clearColor);
        this.shareTabImage.setColorFilter(clearColor);
        this.accountTabImage.setColorFilter(clearColor);

        this.imageViewer = (RelativeLayout) this.findViewById(R.id.main_image_container);
        this.imageViewerClose = (ImageView) this.findViewById(R.id.main_image_close);
        this.imageViewerProgress = (ProgressBar) this.findViewById(R.id.main_image_progress);
        this.imageViewerImage = (ImageView) this.findViewById(R.id.main_image_image);

        this.transactionCardContainer = (RelativeLayout) this.findViewById(R.id.main_transac_container);

        this.imageViewerAttacher = new PhotoViewAttacher(this.imageViewerImage);

        this.imageViewer.setClickable(true);

        this.homeTab.setOnClickListener(v -> changeCurrentTab(TabID.HOME_TAB));
        this.notifTab.setOnClickListener(v -> changeCurrentTab(TabID.NOTIF_TAB));
        this.shareTab.setOnClickListener(v -> changeCurrentTab(TabID.SHARE_TAB));
        this.accountTab.setOnClickListener(v -> changeCurrentTab(TabID.ACCOUNT_TAB));

        this.timelineFragment = new TimelineFragment();
        this.accountFragment = new ProfileFragment();

        this.timelineFragment.tabBarActivity = this;
        this.accountFragment.tabBarActivity = this;

        this.floozTabImage.setOnClickListener(v -> {
            if (FloozRestClient.getInstance().currentUser.ux != null
                    && FloozRestClient.getInstance().currentUser.ux.has("homeButton")
                    && FloozRestClient.getInstance().currentUser.ux.optJSONArray("homeButton").length() > 0) {

                JSONArray t = FloozRestClient.getInstance().currentUser.ux.optJSONArray("homeButton");

                for (int i = 0; i < t.length(); i++) {
                    final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                    if (trigger.delay.doubleValue() > 0) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> FloozRestClient.getInstance().handleTrigger(trigger), (int) (trigger.delay.doubleValue() * 1000));
                    } else {
                        FloozRestClient.getInstance().handleTrigger(trigger);
                    }
                }
            } else {
                Intent intent = new Intent(instance, NewTransactionActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.imageViewerAttacher.setOnLongClickListener(v -> {
            List<ActionSheetItem> items = new ArrayList<>();
            items.add(new ActionSheetItem(instance, R.string.MENU_SAVE_PICTURE, () -> {
                ImageHelper.saveImageOnPhone(instance, ((BitmapDrawable)imageViewerAttacher.getImageView().getDrawable()).getBitmap());
            }));
            ActionSheet.showWithItems(instance, items);

            return false;
        });

        this.imageViewerClose.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageViewer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            imageViewer.startAnimation(anim);
        });

        this.transactionCardFragment = new TransactionCardFragment();
        this.transactionCardFragment.parentActivity = this;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();
        ft.replace(R.id.main_transac_view, this.transactionCardFragment);
        ft.addToBackStack(null).commit();
     }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY) {
                transactionCardFragment.authenticationValidated();
            }

            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {
                Uri imageUri = Uri.EMPTY;
//                if (data == null || data.getData() == null)
//                    imageUri = this.leftMenu.tmpUriImage;
//                else
//                    imageUri = data.getData();
//
//                this.leftMenu.tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        String path = ImageHelper.getPath(instance, selectedImageUri);
                        if (path != null) {
                            File image = new File(path);
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                            if (photo != null) {
                                int rotation = ImageHelper.getRotation(instance, selectedImageUri);
                                if (rotation != 0) {
                                    photo = ImageHelper.rotateBitmap(photo, rotation);
                                }

                                int nh = (int) (photo.getHeight() * (512.0 / photo.getWidth()));
                                Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
//                                leftMenu.userView.setImageBitmap(scaled);
                                FloozRestClient.getInstance().uploadDocument("picId", image, new FloozHttpResponseHandler() {
                                    @Override
                                    public void success(Object response) {
                                        FloozRestClient.getInstance().updateCurrentUser(null);
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        FLUser user = FloozRestClient.getInstance().currentUser;

//                                        if (user.avatarURL != null)
//                                            ImageLoader.getInstance().displayImage(user.avatarURL, leftMenu.userView);
//                                        else
//                                            leftMenu.userView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    public void onStart() {
        super.onStart();

        this.changeCurrentTab(TabID.HOME_TAB);
    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());

        if (FloozApplication.getInstance().pendingTriggers != null) {
            Handler handlerIntent = new Handler(Looper.getMainLooper());
            final boolean b = handlerIntent.postDelayed(() -> {
                FloozRestClient.getInstance().handleTriggerArray(FloozApplication.getInstance().pendingTriggers);
                FloozApplication.getInstance().pendingTriggers = null;
            }, 100);
        }

        if (FloozApplication.getInstance().pendingPopup != null) {
            Handler handlerIntent = new Handler(Looper.getMainLooper());
            final boolean b = handlerIntent.postDelayed(() -> {
                CustomToast.show(this, new FLError(FloozApplication.getInstance().pendingPopup));
                FloozApplication.getInstance().pendingPopup = null;
            }, 100);
        }

    }

    protected void onPause() {
        clearReferences();
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadNotificationsReceiver);
    }

    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    private void changeTabBadgeValue(@NonNull TabID tabID, int value) {
        switch (tabID) {
            case HOME_TAB:
                if (value > 0) {
                    this.homeTabBadge.setText("" + value);
                    this.homeTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.homeTabBadge.setVisibility(View.GONE);
                break;
            case NOTIF_TAB:
                if (value > 0) {
                    this.notifTabBadge.setText("" + value);
                    this.notifTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.notifTabBadge.setVisibility(View.GONE);
                break;
            case SHARE_TAB:
                if (value > 0) {
                    this.shareTabBadge.setText("" + value);
                    this.shareTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.shareTabBadge.setVisibility(View.GONE);
                break;
            case ACCOUNT_TAB:
                if (value > 0) {
                    this.accountTabBadge.setText("" + value);
                    this.accountTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.accountTabBadge.setVisibility(View.GONE);
                break;
        }
    }

    private void changeCurrentTab(@NonNull TabID tabID) {
        int clearColor = this.getResources().getColor(R.color.placeholder);
        int selectedColor = this.getResources().getColor(R.color.blue);

        if (tabID != this.currentTab) {
            if (this.currentTab != null) {
                switch (this.currentTab) {
                    case HOME_TAB:
                        this.homeTabText.setTextColor(clearColor);
                        this.homeTabImage.setColorFilter(clearColor);
                        break;
                    case NOTIF_TAB:
                        this.notifTabText.setTextColor(clearColor);
                        this.notifTabImage.setColorFilter(clearColor);
                        break;
                    case SHARE_TAB:
                        this.shareTabText.setTextColor(clearColor);
                        this.shareTabImage.setColorFilter(clearColor);
                        break;
                    case ACCOUNT_TAB:
                        this.accountTabText.setTextColor(clearColor);
                        this.accountTabImage.setColorFilter(clearColor);
                        break;
                }
            }

            this.currentTab = tabID;

            TabBarFragment fragment = null;

            switch (this.currentTab) {
                case HOME_TAB:
                    this.homeTabText.setTextColor(selectedColor);
                    this.homeTabImage.setColorFilter(selectedColor);
                    fragment = timelineFragment;
                    break;
                case NOTIF_TAB:
                    this.notifTabText.setTextColor(selectedColor);
                    this.notifTabImage.setColorFilter(selectedColor);
                    break;
                case SHARE_TAB:
                    this.shareTabText.setTextColor(selectedColor);
                    this.shareTabImage.setColorFilter(selectedColor);
                    break;
                case ACCOUNT_TAB:
                    this.accountTabText.setTextColor(selectedColor);
                    this.accountTabImage.setColorFilter(selectedColor);
                    fragment = accountFragment;
                    break;
            }

            if (fragment != null) {
                FragmentTransaction ft = this.fragmentManager.beginTransaction();
                ft.replace(R.id.home_fragment_container, fragment);
                ft.addToBackStack(null).commit();
            }
        }
    }

    public void showTransactionCard(FLTransaction transaction) {
        this.showTransactionCard(transaction, false);
    }

    public void showTransactionCard(FLTransaction transaction, Boolean insertComment) {
        FloozRestClient.getInstance().readNotification(transaction.transactionId, null);

        this.transactionCardFragment.insertComment = insertComment;
        this.transactionCardFragment.setTransaction(transaction);
        this.transactionCardFragment.cardScroll.scrollTo(0, 0);

        if (this.transactionCardContainer.getVisibility() == View.GONE) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    transactionCardContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    transactionCardFragment.updateComment();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.transactionCardContainer.startAnimation(anim);
        }
    }

    public void hideTransactionCard() {
        if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    transactionCardContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.transactionCardContainer.startAnimation(anim);
        }
    }

    public void showImageViewer(final Bitmap image) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageViewer.setVisibility(View.VISIBLE);
                imageViewerProgress.setMax(100);
                imageViewerProgress.setProgress(0);
                imageViewerImage.setVisibility(View.GONE);
                imageViewerProgress.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageViewerImage.setImageBitmap(image);
                imageViewerImage.setVisibility(View.VISIBLE);
                imageViewerProgress.setVisibility(View.GONE);
                imageViewerAttacher.update();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.imageViewer.startAnimation(anim);
    }

    public void showImageViewer(final String url) {
        if (!url.contentEquals("/img/fake")) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    imageViewer.setVisibility(View.VISIBLE);
                    imageViewerProgress.setMax(100);
                    imageViewerProgress.setProgress(0);
                    imageViewerImage.setVisibility(View.GONE);
                    imageViewerProgress.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageViewerProgress.setVisibility(View.VISIBLE);

                    ImageLoader.getInstance().displayImage(url, imageViewerImage, null, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            imageViewerImage.setVisibility(View.GONE);
                            imageViewerProgress.setVisibility(View.VISIBLE);
                            imageViewerProgress.setProgress(0);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            imageViewerImage.setVisibility(View.VISIBLE);
                            imageViewerProgress.setVisibility(View.GONE);
                            imageViewerAttacher.update();
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    }, (imageUri, view, current, total) -> {
                        float tmp = current;
                        tmp /= total;
                        tmp *= 100;

                        imageViewerProgress.setProgress((int) tmp);
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.imageViewer.startAnimation(anim);
        }
    }

    public void hideImageViewer() {
        this.imageViewerClose.performClick();
    }

    @Override
    public void onItemSelected(FLTransaction transac) {
        this.showTransactionCard(transac);
    }

    @Override
    public void onItemCommentSelected(FLTransaction transac) {
        this.showTransactionCard(transac, true);
    }

    @Override
    public void onItemImageSelected(String imgUrl) {
        this.showImageViewer(imgUrl);
    }

    @Override
    public void onBackPressed() {
        if (this.imageViewer.getVisibility() == View.VISIBLE) {
            this.imageViewerClose.performClick();
        } else if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
            this.hideTransactionCard();
        } else {
            this.finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
