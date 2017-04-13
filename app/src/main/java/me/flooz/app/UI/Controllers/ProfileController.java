package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import me.flooz.app.Adapter.TimelineListAdapter;
import me.flooz.app.Adapter.UserListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLReport;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AccountActivity;
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.AccountFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.UserProfileListView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Wapazz on 22/09/15.
 */
public class ProfileController extends BaseController implements TimelineListAdapter.TimelineListRowDelegate {

    private ImageView profileImage;
    private ImageView profileCover;
    private ImageView stickyHeader;
    private ImageView stickyHeaderBlur;
    private ImageView certifiedIcon;
    private String profileImageFullURL;
    private String coverURLFull;
    private TextView stickyName;
    private TextView stickyUsername;
    private TextView profileName;
    private TextView profileUsername;
    private TextView userBio;
    private LinearLayout infosContainer;
    private LinearLayout locationContainer;
    private ImageView locationPic;
    private TextView locationText;
    private LinearLayout websiteContainer;
    private ImageView websitePic;
    private TextView websiteText;
    private LinearLayout stickyLayout;
    private FLUser flUser;
    private ImageView removeButton;
    private ImageView settingsButton;
    private ImageView addButtonImage;
    private LinearLayout largePendingButton;
    private LinearLayout addButton;
    private RelativeLayout editButton;
    private RelativeLayout floozButton;
    private ImageView floozButtonImg;
    private TextView addButtonText;
    private TextView largePendingText;
    private TextView addPendingButtonText;
    private TextView editProfileText;
    private TextView editProfileBadge;
    private RadioButton settingsFloozButton;
    private RadioButton settingsFollowingButton;
    private RadioButton settingsPotsButton;
    private LinearLayout buttonRequestPending;
    private RelativeLayout stickyCoverContainer;
    private View listHeader;

    private TimelineListAdapter timelineAdapter;
    private UserListAdapter friendAdapter;
    private TimelineListAdapter collectAdapter;

    private UserProfileListView mainListContainer;

    private List<FLTransaction> transactions = new ArrayList<>(0);
    private String nextPageUrl;

    private List<FLTransaction> collects = new ArrayList<>(0);
    private String nextPotPageUrl;

    private Typeface regContent;
    private Typeface boldContent;

    private int imageSize = 0;
    private int stickyModifier = 0;
    private int modCond = 0;
    private boolean isSticky = false;
    private boolean isHeaderSticky = false;
    private boolean isSegmentDefault = true;

    private BroadcastReceiver reloadUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadUserInfos();
        }
    };

    public TimelineListAdapter.TimelineListRowDelegate delegate;

    public ProfileController(@NonNull FLUser user,@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.flUser = user;

        this.init();
    }

    public ProfileController(@NonNull FLUser user,@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.flUser = user;

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.mainListContainer = (UserProfileListView) this.currentView.findViewById(R.id.profile_list_container);

        this.listHeader =  LayoutInflater.from(this.parentActivity).inflate(R.layout.profile_list_header, null);
        this.stickyLayout = (LinearLayout) this.currentView.findViewById(R.id.profile_sticky_layout);
        this.stickyCoverContainer = (RelativeLayout) this.currentView.findViewById(R.id.sticky_cover_container);
        this.stickyHeader = (ImageView) this.currentView.findViewById(R.id.header_cover_sticky);
        this.stickyHeaderBlur = (ImageView) this.currentView.findViewById(R.id.header_cover_blur);
        this.stickyName = (TextView) this.currentView.findViewById(R.id.profile_card_username_sticky);
        this.stickyUsername = (TextView) this.currentView.findViewById(R.id.profile_card_subname_sticky);

        this.largePendingButton = (LinearLayout) listHeader.findViewById(R.id.profile_largebutton_pending);
        this.addButton = (LinearLayout) listHeader.findViewById(R.id.profile_button_add);
        this.editButton = (RelativeLayout) listHeader.findViewById(R.id.profile_button_edit);
        this.editProfileBadge = (TextView) listHeader.findViewById(R.id.edit_profile_badge);
        this.buttonRequestPending = (LinearLayout) listHeader.findViewById(R.id.profile_button_add_pending);
        this.settingsButton = (ImageView) listHeader.findViewById(R.id.settings_profile_button);
        this.addButtonImage = (ImageView) listHeader.findViewById(R.id.add_profile_image);
        this.removeButton = (ImageView) listHeader.findViewById(R.id.unfollow_profile_button);
        this.profileImage = (ImageView) listHeader.findViewById(R.id.profile_card_pic);
        this.profileCover = (ImageView) listHeader.findViewById(R.id.header_cover);
        this.certifiedIcon = (ImageView) listHeader.findViewById(R.id.profile_icon_certified);
        this.profileName = (TextView) listHeader.findViewById(R.id.profile_card_name);
        this.profileUsername = (TextView) listHeader.findViewById(R.id.profile_card_username);
        this.userBio = (TextView) listHeader.findViewById(R.id.profile_card_bio);
        this.floozButton = (RelativeLayout) listHeader.findViewById(R.id.profile_button_flooz);
        this.floozButtonImg = (ImageView) listHeader.findViewById(R.id.profile_button_flooz_img);

        this.infosContainer = (LinearLayout) listHeader.findViewById(R.id.profile_card_infos);
        this.locationContainer = (LinearLayout) listHeader.findViewById(R.id.profile_card_infos_location);
        this.locationPic = (ImageView) listHeader.findViewById(R.id.profile_card_infos_location_pic);
        this.locationText = (TextView) listHeader.findViewById(R.id.profile_card_infos_location_text);
        this.websiteContainer = (LinearLayout) listHeader.findViewById(R.id.profile_card_infos_website);
        this.websitePic = (ImageView) listHeader.findViewById(R.id.profile_card_infos_website_pic);
        this.websiteText = (TextView) listHeader.findViewById(R.id.profile_card_infos_website_text);

        this.addButtonText = (TextView) listHeader.findViewById(R.id.add_profile_text);
        this.largePendingText = (TextView) listHeader.findViewById(R.id.pending_largebutton_text);
        this.editProfileText = (TextView) listHeader.findViewById(R.id.edit_profile_text);
        this.addPendingButtonText = (TextView) listHeader.findViewById(R.id.add_pending_profile_text);
        this.settingsFloozButton = (RadioButton) listHeader.findViewById(R.id.settings_segment_flooz);
        this.settingsFollowingButton = (RadioButton) listHeader.findViewById(R.id.settings_segment_following);
        this.settingsPotsButton = (RadioButton) listHeader.findViewById(R.id.settings_segment_pots);

        this.imageSize = this.profileImage.getLayoutParams().height;

        this.settingsClearVision();
        this.stickyHeader.setVisibility(View.INVISIBLE);
        this.certifiedIcon.setVisibility(View.INVISIBLE);
        this.userBio.setVisibility(View.GONE);
        this.settingsButton.setVisibility(View.GONE);
        this.editButton.setVisibility(View.GONE);
        this.settingsButton.setColorFilter(Color.GRAY);
        this.addButtonImage.setColorFilter(parentActivity.getResources().getColor(R.color.blue));
        this.largePendingText.setTextColor(Color.WHITE);
        this.removeButton.setColorFilter(Color.WHITE);
        this.locationPic.setColorFilter(Color.WHITE);
        this.websitePic.setColorFilter(Color.WHITE);
        this.stickyName.setTextColor(Color.WHITE);
        this.profileName.setTextColor(Color.WHITE);
        this.userBio.setTextColor(Color.WHITE);

        if (timelineAdapter == null) {
            timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), transactions);
            this.timelineAdapter.delegate = this;
            this.timelineAdapter.loading = true;
            this.timelineAdapter.showEmpty = true;
        }

        if (friendAdapter == null) {
            friendAdapter = new UserListAdapter(FloozApplication.getAppContext(), this.flUser);
        }

        if (collectAdapter == null) {
            collectAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), collects);
            this.collectAdapter.delegate = this;
            this.collectAdapter.loading = true;
            this.collectAdapter.showEmpty = true;
            this.collectAdapter.isCollectTimeline = true;
        }

        this.regContent = CustomFonts.customContentRegular(parentActivity);
        this.boldContent = CustomFonts.customContentBold(parentActivity);
        this.profileName.setTypeface(boldContent);
        this.stickyName.setTypeface(boldContent);
        this.addButtonText.setTypeface(regContent);
        this.largePendingText.setTypeface(regContent);
        this.editProfileText.setTypeface(regContent);
        this.addPendingButtonText.setTypeface(regContent);
        this.stickyUsername.setTypeface(regContent);
        this.locationText.setTypeface(regContent);
        this.websiteText.setTypeface(regContent);
        this.editProfileBadge.setTypeface(regContent);
        this.profileUsername.setTypeface(regContent);
        this.userBio.setTypeface(regContent);

        this.websiteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.stickyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSegmentDefault)
                    settingsFloozButton.performClick();
                else
                    settingsFollowingButton.performClick();
            }
        });

        this.settingsFloozButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainListContainer.preLast = 0;
                mainListContainer.setAdapter(timelineAdapter);
                isSegmentDefault = true;

                timelineAdapter.notifyDataSetChanged();
            }
        });

        this.settingsFollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainListContainer.preLast = 0;
                mainListContainer.setAdapter(friendAdapter);
                isSegmentDefault = false;

                friendAdapter.notifyDataSetChanged();
            }
        });

        this.settingsPotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainListContainer.preLast = 0;
                mainListContainer.setAdapter(collectAdapter);
                isSegmentDefault = false;

                collectAdapter.notifyDataSetChanged();
            }
        });

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER) {
            HomeActivity activity = (HomeActivity) parentActivity;

            if (activity.currentTabHistory.size() == 1)
                this.closeButton.setVisibility(View.GONE);
        }

        this.reloadWithUser();

        this.websiteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flUser.website.isEmpty()) {
                    String url = flUser.website;

                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;

                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        parentActivity.startActivity(browserIntent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        this.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flUser.avatarURL != null && !flUser.avatarURL.isEmpty()) {
                    CustomImageViewer.start(parentActivity, profileImageFullURL);
                }
            }
        });

        this.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.FRAGMENT_CONTROLLER) {
                    AccountFragment controller = new AccountFragment();
                    ((HomeActivity) parentActivity).pushFragmentInCurrentTab(controller);
                } else {
                    Intent editIntent = new Intent(parentActivity, AccountActivity.class);
                    parentActivity.startActivity(editIntent);
                    parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                }
            }
        });

        this.mainListContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (settingsFollowingButton.isChecked()) {
                    if (friendAdapter.userList.size() != 0) {
                        FloozApplication.getInstance().showUserProfile(friendAdapter.getItem(position - 1));
                    }
                }
            }
        });

        this.mainListContainer.setOnUserProfileListViewListener(new UserProfileListView.OnUserProfileListViewListener() {
            @Override
            public void onShowLastItem() {
                loadNextPage();
            }
        });

        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserActionMenu(flUser);
            }
        });

        this.largePendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showActivePendingFriendActionMenu(flUser);
            }
        });

        this.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveFriendActionMenu(flUser);
            }
        });

        this.buttonRequestPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPendingFriendActionMenu(flUser);
            }
        });

        this.profileCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flUser.coverURL != null && !flUser.coverURL.isEmpty()) {
                    CustomImageViewer.start(parentActivity, coverURLFull);
                }
            }
        });

        this.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().sendFriendRequest(flUser.userId, flUser.getSelectedCanal(), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        settingsClearVision();
                        largePendingButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        this.floozButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentActivity, NewTransactionActivity.class);
                intent.putExtra("user", flUser.json.toString());
                parentActivity.startActivity(intent);
                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.mainListContainer.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int firstVisiblePosition = mainListContainer.getFirstVisiblePosition();
                int scrollY = (int) FLHelper.convertPixelsToDp(getScroll(), parentActivity);
                int modifier = (scrollY / 2);

                if (scrollY <= 100) {
                    profileImage.getLayoutParams().height = imageSize - (int) FLHelper.convertDpToPixel(modifier, parentActivity);
                    profileImage.getLayoutParams().width = imageSize - (int) FLHelper.convertDpToPixel(modifier, parentActivity);
                    profileImage.requestLayout();
                }

                if (scrollY >= 120 && (scrollY <= 200 || modCond > 1) && firstVisiblePosition == 0) {
                    if (stickyModifier == 0)
                        stickyModifier = modifier;
                    modCond = 120 - (modifier - stickyModifier) * 2;
                    if (modCond < 0)
                        modCond = 1;
                    stickyLayout.setPadding(0, modCond, 0, 0);
                }

                if (scrollY >= 60 && !isSticky && firstVisiblePosition == 0) {
                    stickyHeader.setVisibility(View.VISIBLE);
                    isSticky = !isSticky;
                }
                if (scrollY < 60 && isSticky && firstVisiblePosition == 0) {
                    stickyHeader.setVisibility(View.INVISIBLE);
                    isSticky = !isSticky;
                }
                if (scrollY < 100 && isHeaderSticky && firstVisiblePosition == 0) {
                    stickyLayout.setVisibility(View.INVISIBLE);
                    stickyHeaderBlur.setVisibility(View.INVISIBLE);
                    isHeaderSticky = !isHeaderSticky;
                }
                if (scrollY >= 100 && !isHeaderSticky && firstVisiblePosition == 0) {
                    stickyLayout.setVisibility(View.VISIBLE);
                    stickyHeaderBlur.setVisibility(View.VISIBLE);

                    isHeaderSticky = !isHeaderSticky;
                }
            }
        });

        this.mainListContainer.addHeaderView(listHeader);
        this.reloadWithUser();
        this.settingsFloozButton.performClick();
    }

    private Dictionary<Integer, Integer> listViewItemHeights = new Hashtable<Integer, Integer>();

    private int getScroll() {
        View c = mainListContainer.getChildAt(0); //this is the first visible row
        int scrollY = -c.getTop();
        listViewItemHeights.put(mainListContainer.getFirstVisiblePosition(), c.getHeight());
        for (int i = 0; i < mainListContainer.getFirstVisiblePosition(); ++i) {
            if (listViewItemHeights.get(i) != null) // (this is a sanity check)
                scrollY += listViewItemHeights.get(i); //add all heights of the views that are gone
        }
        return scrollY;
    }

    @Override
    public void onStart() {
        this.requestUserInfos();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadUserReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause () {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
    }

    private void requestUserInfos() {
        FloozRestClient.getInstance().getFullUser(this.flUser.userId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                flUser = (FLUser) response;
                friendAdapter.setCurrentUser(flUser);
                reloadWithUser();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        FloozRestClient.getInstance().getUserTransactions(this.flUser.userId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                HashMap<String, Object> resp = (HashMap<String, Object>) response;
                transactions.clear();
                transactions.addAll((List<FLTransaction>) resp.get("transactions"));
                nextPageUrl = (String) resp.get("nextUrl");
                timelineAdapter.setTransactions(transactions);
                timelineAdapter.loading = false;

                if (nextPageUrl == null || nextPageUrl.isEmpty())
                    timelineAdapter.hasNextURL = false;
                else
                    timelineAdapter.hasNextURL = true;

                if (settingsFloozButton.isChecked())
                    timelineAdapter.notifyDataSetChanged();

                mainListContainer.preLast = 0;
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        FloozRestClient.getInstance().getUserCollects(this.flUser.userId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                HashMap<String, Object> resp = (HashMap<String, Object>) response;
                collects.clear();
                collects.addAll((List<FLTransaction>) resp.get("transactions"));
                nextPotPageUrl = (String) resp.get("nextUrl");
                collectAdapter.setTransactions(collects);
                collectAdapter.loading = false;

                if (nextPotPageUrl == null || nextPotPageUrl.isEmpty())
                    collectAdapter.hasNextURL = false;
                else
                    collectAdapter.hasNextURL = true;

                if (settingsPotsButton.isChecked())
                    collectAdapter.notifyDataSetChanged();

                mainListContainer.preLast = 0;
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void reloadUserInfos() {
        if (this.flUser.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId)) {
            this.flUser = FloozRestClient.getInstance().currentUser;
            this.reloadWithUser();
        }
        else {
            requestUserInfos();
        }
    }

    private void reloadWithUser() {
        this.stickyName.setText(this.flUser.fullname);
        this.stickyUsername.setText("@" + this.flUser.username);
        this.profileName.setText(this.flUser.fullname);
        this.profileUsername.setText("@" + this.flUser.username);

        this.profileImageFullURL = flUser.avatarURLFull;
        this.coverURLFull = flUser.coverURL;

        if (flUser.coverURL != null && !flUser.coverURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(flUser.coverURL, this.stickyHeaderBlur);
            ImageLoader.getInstance().displayImage(flUser.coverURL, this.stickyHeader);
            ImageLoader.getInstance().displayImage(flUser.coverURL, this.profileCover);
        }  else {
            this.stickyHeaderBlur.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.cover));

            this.stickyHeader.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.cover));
            this.profileCover.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.cover));
        }

        if (flUser.avatarURL != null && !flUser.avatarURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(flUser.avatarURL, this.profileImage);
        } else {
            this.profileImage.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));
        }

        if (flUser.isStar || flUser.isPro) {
            this.certifiedIcon.setVisibility(View.VISIBLE);
        }

        this.settingsClearVision();

        if (flUser.actions != null && flUser.actions.contains("flooz"))
            this.floozButton.setVisibility(View.VISIBLE);

        if (flUser.actions != null && flUser.actions.contains("friend:add"))
            this.addButton.setVisibility(View.VISIBLE);

        if (flUser.actions != null && flUser.actions.contains("friend:remove"))
            this.removeButton.setVisibility(View.VISIBLE);

        if (flUser.actions != null && flUser.actions.contains("friend:pending"))
            this.largePendingButton.setVisibility(View.VISIBLE);

        if (flUser.actions != null && flUser.actions.contains("friend:request"))
            this.buttonRequestPending.setVisibility(View.VISIBLE);

        if (flUser.actions != null && flUser.actions.contains("settings"))
            this.settingsButton.setVisibility(View.VISIBLE);

        if (flUser.actions != null && flUser.actions.contains("self")) {
            this.editButton.setVisibility(View.VISIBLE);
            this.editProfileText.setText(parentActivity.getResources().getText(R.string.GLOBAL_ACCOUNT) + " " +  FLHelper.trimTrailingZeros(String.format("%.2f", FloozRestClient.getInstance().currentUser.amount.floatValue()).replace(',', '.')) + " €");

            int badgeValue = FloozRestClient.getInstance().currentUser.json.optJSONObject("metrics").optInt("accountMissing");
            if (badgeValue > 0) {
                this.editProfileBadge.setVisibility(View.VISIBLE);
                this.editProfileBadge.setText("" + badgeValue);
            } else
                this.editProfileBadge.setVisibility(View.GONE);
        }

        if (!flUser.userBio.isEmpty()) {
            this.userBio.setText(flUser.userBio);
            this.userBio.setVisibility(View.VISIBLE);
        } else {
            this.userBio.setVisibility(View.GONE);
        }

        if (!flUser.location.isEmpty() || !flUser.website.isEmpty()) {
            this.infosContainer.setVisibility(View.VISIBLE);

            if (!flUser.location.isEmpty()) {
                this.locationContainer.setVisibility(View.VISIBLE);
                this.locationText.setText(flUser.location);
            } else
                this.locationContainer.setVisibility(View.GONE);

            if (!flUser.website.isEmpty()) {
                this.websiteContainer.setVisibility(View.VISIBLE);
                this.websiteText.setText(flUser.website);
            } else
                this.websiteContainer.setVisibility(View.GONE);
        } else {
            this.infosContainer.setVisibility(View.GONE);
        }

        this.settingsFloozButton.setTypeface(regContent);
        this.settingsFollowingButton.setTypeface(regContent);
        this.settingsPotsButton.setTypeface(regContent);

        SpannableString floozString = new SpannableString(flUser.publicMetrics.nbFlooz + "\nFlooz");
        floozString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFlooz), 0);

        SpannableString friendsString = new SpannableString(flUser.publicMetrics.nbFriends + "\nAmis");
        friendsString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFriends), 0);

        String potsSuffix = "Cagnottes";
        if (flUser.publicMetrics.nbCollects < 2)
            potsSuffix = "Cagnotte";

        SpannableString potsString = new SpannableString(flUser.publicMetrics.nbCollects + "\n" + potsSuffix);
        friendsString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFriends), 0);

        this.settingsFloozButton.setText(floozString);
        this.settingsFollowingButton.setText(friendsString);
        this.settingsPotsButton.setText(potsString);
    }

    private void loadNextPage() {
        if (this.settingsFloozButton.isChecked()) {
            if (nextPageUrl == null || nextPageUrl.isEmpty())
                return;

            FloozRestClient.getInstance().timelineNextPage(this.nextPageUrl, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = (Map<String, Object>) response;

                    transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                    nextPageUrl = (String) responseMap.get("nextUrl");

                    if (nextPageUrl == null || nextPageUrl.isEmpty())
                        timelineAdapter.hasNextURL = false;
                    else
                        timelineAdapter.hasNextURL = true;

                    timelineAdapter.notifyDataSetChanged();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        } else if (this.settingsPotsButton.isChecked()) {
            if (nextPotPageUrl == null || nextPotPageUrl.isEmpty())
                return;

            FloozRestClient.getInstance().timelineNextPage(this.nextPotPageUrl, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = (Map<String, Object>) response;

                    collects.addAll((List<FLTransaction>) responseMap.get("transactions"));
                    nextPotPageUrl = (String) responseMap.get("nextUrl");

                    if (nextPotPageUrl == null || nextPotPageUrl.isEmpty())
                        collectAdapter.hasNextURL = false;
                    else
                        collectAdapter.hasNextURL = true;

                    if (settingsPotsButton.isChecked())
                        collectAdapter.notifyDataSetChanged();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
    }

    private void settingsClearVision() {
        this.floozButton.setVisibility(View.GONE);
        this.addButton.setVisibility(View.GONE);
        this.removeButton.setVisibility(View.GONE);
        this.largePendingButton.setVisibility(View.GONE);
        this.buttonRequestPending.setVisibility(View.GONE);
        this.editButton.setVisibility(View.GONE);
        this.settingsButton.setVisibility(View.GONE);
    }

    @Override
    public void ListItemClick(FLTransaction transac) {
        if (transac.isCollect)
            FloozApplication.getInstance().showCollect(transac);
        else
            FloozApplication.getInstance().showTransactionCard(transac);
    }

    @Override
    public void ListItemCommentClick(FLTransaction transac) {
        if (transac.isCollect)
            FloozApplication.getInstance().showCollect(transac, true);
        else
            FloozApplication.getInstance().showTransactionCard(transac, true);
    }

    @Override
    public void ListItemImageClick(String imgUrl) {
        CustomImageViewer.start(this.parentActivity, imgUrl);

    }

    @Override
    public void ListItemUserClick(FLUser user) {
        FloozApplication.getInstance().showUserProfile(user);
    }

    @Override
    public void ListItemShareClick(FLTransaction transac) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/flooz/" + transac.transactionId);

        parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_FLOOZ)));
    }

    private ActionSheetItem.ActionSheetItemClickListener removeFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(flUser.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                    settingsClearVision();
                    addButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(int statusCode, FLError error) {
                }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener acceptFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(flUser.userId, FloozRestClient.FriendAction.Accept, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                    settingsClearVision();
                    removeButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(int statusCode, FLError error) { }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener deletePendingRequest = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(flUser.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                    settingsClearVision();
                    addButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(int statusCode, FLError error) { }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener declineFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(flUser.userId, FloozRestClient.FriendAction.Decline, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateCurrentUser(null);
                    settingsClearVision();
                    addButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void failure(int statusCode, FLError error) {
                }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener blockUser = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            new AlertDialog.Builder(parentActivity)
                    .setTitle(R.string.MENU_BLOCK_USER)
                    .setMessage(R.string.BLOCK_USER_ALERT_MESSAGE)
                    .setPositiveButton(R.string.GLOBAL_YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FloozRestClient.getInstance().blockUser(flUser.userId, null);
                        }
                    })
                    .setNegativeButton(R.string.GLOBAL_NO, null)
                    .show();
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener reportUser = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            new AlertDialog.Builder(parentActivity)
                    .setTitle(R.string.MENU_REPORT_USER)
                    .setMessage(R.string.REPORT_USER_ALERT_MESSAGE)
                    .setPositiveButton(R.string.GLOBAL_YES, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FloozRestClient.getInstance().reportContent(new FLReport(FLReport.ReportType.User, flUser.userId), null);
                        }
                    })
                    .setNegativeButton(R.string.GLOBAL_NO, null)
                    .show();
        }
    };

    public void showUserActionMenu(FLUser user) {
        if (user == null || user.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId) || user.username == null || user.fullname == null)
            return;

        List<ActionSheetItem> items = new ArrayList<>();

        if (this.flUser.actions.contains("report"))
            items.add(new ActionSheetItem(parentActivity, R.string.MENU_REPORT_USER, reportUser));
        if (this.flUser.actions.contains("block"))
            items.add(new ActionSheetItem(parentActivity, R.string.MENU_BLOCK_USER, blockUser));

        ActionSheet.showWithItems(parentActivity, items);
    }

    public void showActivePendingFriendActionMenu(FLUser user) {
        if (user == null || user.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId) || user.username == null || user.fullname == null)
            return;

        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(parentActivity, R.string.MENU_STOP_PENDING_FRIENDS, deletePendingRequest));

        ActionSheet.showWithItems(parentActivity, items);
    }

    public void showPendingFriendActionMenu(FLUser user) {
        if (user == null || user.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId) || user.username == null || user.fullname == null)
            return;

        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(parentActivity, R.string.MENU_ACCEPT_FRIENDS, acceptFriend));
        items.add(new ActionSheetItem(parentActivity, R.string.MENU_DECLINE_FRIENDS, declineFriend));

        ActionSheet.showWithItems(parentActivity, items);
    }

    public void showRemoveFriendActionMenu(FLUser user) {
        if (user == null || user.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId) || user.username == null || user.fullname == null)
            return;

        List<ActionSheetItem> items = new ArrayList<>();

        items.add(new ActionSheetItem(parentActivity, R.string.MENU_REMOVE_FRIENDS, removeFriend));

        ActionSheet.showWithItems(parentActivity, items);
    }
}
