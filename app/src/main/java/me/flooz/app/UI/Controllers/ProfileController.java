package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;
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
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.UI.Fragment.Home.ProfileCardFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.TimelineListView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Wapazz on 22/09/15.
 */
public class ProfileController extends BaseController implements UserListAdapter.UserListDelegate, TimelineListAdapter.TimelineListRowDelegate {

    private ImageView cardHeaderCloseButton;
    private ImageView profileImage;
    private ImageView profileCover;
    private ImageView stickyProfileCover;
    private ImageView stickyHeader;
    private ImageView certifiedIcon;
    private String profileImageFullURL;
    private String coverURLFull;
    private TextView stickyName;
    private TextView stickyUsername;
    private TextView profileName;
    private TextView profileUsername;
    private TextView userBio;
    private LinearLayout stickyLayout;
    private FLUser flUser;
    private ImageView removeButton;
    private ImageView settingsButton;
    private ImageView addButtonImage;
    private ImageView largePendingImage;
    private ImageView addPendingImage;
    private LinearLayout largePendingButton;
    private LinearLayout addButton;
    private LinearLayout editButton;
    private TextView addButtonText;
    private TextView largePendingText;
    private TextView addPendingButtonText;
    private TextView editProfileText;
    private RadioButton settingsFloozButton;
    private RadioButton settingsFollowingButton;
    private LinearLayout buttonRequestPending;
    private RelativeLayout stickyCoverContainer;

    private TimelineListAdapter timelineAdapter;
//    private UserListAdapter followersAdapter;
    private UserListAdapter friendAdapter;

    private TimelineListView mainListContainer;

    private List<FLTransaction> transactions = new ArrayList<>(0);
    private String nextPageUrl;

    private Typeface regContent;
    private Typeface boldContent;

    private int imageSize = 0;
    private int stickyModifier = 0;
    private int modCond = 0;
    private boolean isSticky = false;
    private boolean isHeaderSticky = false;
    private boolean isBlurred = false;
    private boolean isStart = true;
    private boolean isSegmentDefault = true;

    private BroadcastReceiver reloadUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadUserInfos();
        }
    };

    public TimelineListAdapter.TimelineListRowDelegate delegate;

    public ProfileController(@NonNull FLUser user, @NonNull View view, @NonNull Activity parentActivity, @NonNull BaseController.ControllerKind kind) {
        super(view, parentActivity, kind);

        this.mainListContainer = (TimelineListView) view.findViewById(R.id.profile_list_container);

        View listHeader =  LayoutInflater.from(this.parentActivity).inflate(R.layout.profile_list_header, null, false);
        this.mainListContainer.addHeaderView(listHeader);
        this.mainListContainer.setAdapter(new UserListAdapter(this.parentActivity, this.flUser, UserListAdapter.ListType.FOLLOWERS));
        this.stickyLayout = (LinearLayout) view.findViewById(R.id.profile_sticky_layout);
        this.stickyCoverContainer = (RelativeLayout) view.findViewById(R.id.sticky_cover_container);
        this.stickyProfileCover = (ImageView) view.findViewById(R.id.header_cover_sticky);
        this.stickyHeader = (ImageView) view.findViewById(R.id.header_cover_sticky);
        this.cardHeaderCloseButton = (ImageView) view.findViewById(R.id.transac_card_header_close);
        this.stickyName = (TextView) view.findViewById(R.id.profile_card_username_sticky);
        this.stickyUsername = (TextView) view.findViewById(R.id.profile_card_subname_sticky);

        this.largePendingButton = (LinearLayout) listHeader.findViewById(R.id.profile_largebutton_pending);
        this.addButton = (LinearLayout) listHeader.findViewById(R.id.profile_button_add);
        this.editButton = (LinearLayout) listHeader.findViewById(R.id.profile_button_edit);
        this.buttonRequestPending = (LinearLayout) listHeader.findViewById(R.id.profile_button_add_pending);
        this.settingsButton = (ImageView) listHeader.findViewById(R.id.settings_profile_button);
        this.addButtonImage = (ImageView) listHeader.findViewById(R.id.add_profile_image);
        this.removeButton = (ImageView) listHeader.findViewById(R.id.unfollow_profile_button);
        this.addPendingImage = (ImageView) listHeader.findViewById(R.id.add_pending_profile_image);
        this.largePendingImage = (ImageView) listHeader.findViewById(R.id.pending_largebutton_image);
        this.profileImage = (ImageView) listHeader.findViewById(R.id.profile_card_pic);
        this.profileCover = (ImageView) listHeader.findViewById(R.id.header_cover);
        this.certifiedIcon = (ImageView) listHeader.findViewById(R.id.profile_icon_certified);
        this.profileName = (TextView) listHeader.findViewById(R.id.profile_card_name);
        this.profileUsername = (TextView) listHeader.findViewById(R.id.profile_card_username);
        this.userBio = (TextView) listHeader.findViewById(R.id.profile_card_bio);
        this.addButtonText = (TextView) listHeader.findViewById(R.id.add_profile_text);
        this.largePendingText = (TextView) listHeader.findViewById(R.id.pending_largebutton_text);
        this.editProfileText = (TextView) listHeader.findViewById(R.id.edit_profile_text);
        this.addPendingButtonText = (TextView) listHeader.findViewById(R.id.add_pending_profile_text);
        this.settingsFloozButton = (RadioButton) listHeader.findViewById(R.id.settings_segment_flooz);
        this.settingsFollowingButton = (RadioButton) listHeader.findViewById(R.id.settings_segment_following);

        this.imageSize = this.profileImage.getLayoutParams().height;
        this.flUser = user;

        this.settingsClearVision();
        this.stickyHeader.setVisibility(View.INVISIBLE);
        this.certifiedIcon.setVisibility(View.INVISIBLE);
        this.userBio.setVisibility(View.GONE);
        this.settingsButton.setVisibility(View.GONE);
        this.editButton.setVisibility(View.GONE);
        this.settingsButton.setColorFilter(Color.GRAY);
        this.addButtonImage.setColorFilter(parentActivity.getResources().getColor(R.color.blue));
        this.largePendingImage.setColorFilter(Color.WHITE);
        this.largePendingText.setTextColor(Color.WHITE);
        this.removeButton.setColorFilter(Color.WHITE);
        this.addPendingImage.setColorFilter(parentActivity.getResources().getColor(R.color.blue));
        this.stickyName.setTextColor(Color.WHITE);
        this.profileName.setTextColor(Color.WHITE);
        this.userBio.setTextColor(Color.WHITE);

        if (timelineAdapter == null) {
            timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), transactions);
            this.timelineAdapter.delegate = this;
        }
        if (friendAdapter == null) {
            friendAdapter = new UserListAdapter(FloozApplication.getAppContext(), null, UserListAdapter.ListType.FOLLOWERS);
            this.friendAdapter.delegate = this;
        }
//        if (friendAdapter == null) {
//            friendAdapter = new UserListAdapter(FloozApplication.getAppContext(), null, UserListAdapter.ListType.FOLLOWINGS);
//            this.friendAdapter.delegate = this;
//        }

        this.regContent = CustomFonts.customContentRegular(parentActivity);
        this.boldContent = CustomFonts.customContentBold(parentActivity);
        this.profileName.setTypeface(boldContent);
        this.stickyName.setTypeface(boldContent);
        this.addButtonText.setTypeface(regContent);
        this.largePendingText.setTypeface(regContent);
        this.editProfileText.setTypeface(regContent);
        this.addPendingButtonText.setTypeface(regContent);
        this.stickyUsername.setTypeface(regContent);

        this.profileUsername.setTypeface(regContent);
        this.userBio.setTypeface(regContent);

        this.stickyName.setText(this.flUser.fullname);
        this.stickyUsername.setText("@" + this.flUser.username);
        this.profileName.setText(this.flUser.fullname);
        this.profileUsername.setText("@" + this.flUser.username);

        if (user.avatarURL != null && !user.avatarURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(user.avatarURL, this.profileImage);
        }

        this.stickyLayout.setOnClickListener(v -> {
            if (this.isSegmentDefault)
                this.settingsFloozButton.performClick();
            else
                this.settingsFollowingButton.performClick();
        });
        this.settingsFloozButton.setOnClickListener(v -> {
            mainListContainer.setAdapter(timelineAdapter);
            this.isSegmentDefault = true;
        });
        this.settingsFollowingButton.setOnClickListener(v -> {
            mainListContainer.setAdapter(friendAdapter);
            this.isSegmentDefault = false;
        });
        this.cardHeaderCloseButton.setOnClickListener(v -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity) this.parentActivity).popFragmentInCurrentTab();
            }
        });

        this.profileImage.setOnClickListener(v -> {
            if (user.avatarURL != null && !user.avatarURL.isEmpty()) {
                CustomImageViewer.start(parentActivity, profileImageFullURL);
            }
        });
        // TODO Sur photo ouvrir profile, si current -> shake
        this.mainListContainer.setOnItemClickListener((adapterView, view1, i, l) -> ListUserClick((FLUser) adapterView.getAdapter().getItem(i)));
        this.editButton.setOnClickListener(view1 -> EditProfileActivity.start(parentActivity, flUser.json.toString()));

        this.mainListContainer.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onPositionChanged(TimelineListView listView, int position, View scrollBarPanel) {

            }

            @Override
            public void onShowLastItem() {
                loadNextPage();
            }
        });

        this.mainListContainer.getViewTreeObserver().addOnScrollChangedListener(() -> {
            View c = mainListContainer.getChildAt(0);
            if (c != null) {
                int firstVisiblePosition = mainListContainer.getFirstVisiblePosition();
                int scrollY = -c.getTop() + firstVisiblePosition * c.getHeight();
                int modifier = (scrollY / 2);
//                if (scrollY != 0)
//                    this.currentScrollOffset = scrollY;

                if (scrollY <= 180) {
                    this.profileImage.getLayoutParams().height = this.imageSize - modifier;
                    this.profileImage.getLayoutParams().width = this.imageSize - modifier;
                    this.profileImage.requestLayout();

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.profileImage.getLayoutParams();
                    params.setMargins(30 + (modifier / 2), modifier, 0, -150);
                    this.profileImage.setLayoutParams(params);
                }
                if (scrollY >= 370 && (scrollY <= 580 || modCond > 1) && firstVisiblePosition == 0) {
                    if (this.stickyModifier == 0)
                        this.stickyModifier = modifier;
                    modCond = 195 - (modifier - this.stickyModifier) * 2;
                    if (modCond < 0)
                        modCond = 1;
                    this.stickyLayout.setPadding(0, modCond, 0, 0);
                }
                if (scrollY >= 165 && !this.isSticky && firstVisiblePosition == 0) {
                    this.stickyHeader.setVisibility(View.VISIBLE);
                    this.isSticky = !this.isSticky;
                }
                if (scrollY < 165 && this.isSticky && firstVisiblePosition == 0) {
                    this.stickyHeader.setVisibility(View.INVISIBLE);
                    this.isSticky = !this.isSticky;
                }
                if (scrollY < 370 && this.isHeaderSticky && firstVisiblePosition == 0) {
                    this.stickyLayout.setVisibility(View.INVISIBLE);
                    this.isHeaderSticky = !this.isHeaderSticky;
                }
                if (scrollY >= 370 && !this.isHeaderSticky && firstVisiblePosition == 0) {
                    this.stickyLayout.setVisibility(View.VISIBLE);
                    this.isHeaderSticky = !this.isHeaderSticky;

                    if (!isBlurred) {
//                        Blurry.with(this.parentActivity)
//                            .radius(20)
//                            .sampling(1)
//                            .async()
//                            .animate(2000)
//                            .capture(this.stickyHeader)
//                            .into(this.stickyHeader);
                        this.isBlurred = !isBlurred;
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.cardHeaderCloseButton.performClick();
    }

    @Override
    public void onStart() {
        requestUserInfos();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadUserReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause () {
//        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
    }

    private void requestUserInfos() {
        FloozRestClient.getInstance().getFullUser(this.flUser.userId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                flUser = (FLUser) response;
                friendAdapter.setCurrentUser(flUser);
//                followersAdapter.setCurrentUser(flUser);
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
                if (settingsFloozButton.isChecked())
                    mainListContainer.setAdapter(timelineAdapter);
                if (isStart) {
                    settingsFloozButton.performClick();
                    isStart = false;
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void reloadUserInfos() {
        if (this.flUser.username.contentEquals(FloozRestClient.getInstance().currentUser.username)) {
            this.flUser = FloozRestClient.getInstance().currentUser;

            if (flUser.coverURL != null && !flUser.coverURL.isEmpty() && !flUser.coverURL.contentEquals("/img/nocover.png")) {
                ImageLoader.getInstance().displayImage(flUser.coverURL, this.stickyHeader);
                ImageLoader.getInstance().displayImage(flUser.coverURL, this.profileCover);
            }
            if (flUser.avatarURL != null && !flUser.avatarURL.isEmpty()) {
                ImageLoader.getInstance().displayImage(flUser.avatarURL, this.profileImage);
            }
            if (!flUser.userBio.isEmpty()) {
                this.userBio.setText(flUser.userBio);
                this.userBio.setVisibility(View.VISIBLE);
            } else {
                this.userBio.setVisibility(View.GONE);
            }

            SpannableString floozString = new SpannableString(flUser.publicMetrics.nbFlooz + "\nFlooz");
            floozString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFlooz), 0);
            SpannableString friendsString = new SpannableString(flUser.publicMetrics.nbFriends + "\nAmis");
            friendsString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFriends), 0);
            this.settingsFloozButton.setText(floozString);
            this.settingsFollowingButton.setText(friendsString);
        }
        else {
            requestUserInfos();
        }
    }

    private void reloadWithUser() {
        this.stickyName.setText(flUser.fullname);
        this.profileName.setText(flUser.fullname);
        this.profileImageFullURL = flUser.avatarURLFull;
        this.coverURLFull = flUser.coverURL;

        if (flUser.coverURL != null && !flUser.coverURL.isEmpty() && !flUser.coverURL.contentEquals("/img/nocover.png")) {
            ImageLoader.getInstance().displayImage(flUser.coverURL, this.stickyHeader);
            ImageLoader.getInstance().displayImage(flUser.coverURL, this.profileCover);
        }
        if (flUser.isStar || flUser.isPro) {
            this.certifiedIcon.setVisibility(View.VISIBLE);
        }
        if (flUser.actions != null && flUser.actions.contains("friend:add")) {
            this.settingsClearVision();
            this.addButton.setVisibility(View.VISIBLE);
        }
        if (flUser.actions != null && flUser.actions.contains("friend:remove")) {
            this.settingsClearVision();
            this.removeButton.setVisibility(View.VISIBLE);
        }
        if (flUser.actions != null && flUser.actions.contains("friend:pending")) {
            this.settingsClearVision();
            this.largePendingButton.setVisibility(View.VISIBLE);
        }
        if (flUser.actions != null && flUser.actions.contains("friend:request")) {
            this.settingsClearVision();
            this.buttonRequestPending.setVisibility(View.VISIBLE);
        }
        if (flUser.actions != null && flUser.actions.contains("settings")) {
            this.settingsButton.setVisibility(View.VISIBLE);
        }
        if (flUser.actions != null && flUser.actions.contains("self")) {
            this.editButton.setVisibility(View.VISIBLE);
        }
        if (!flUser.userBio.isEmpty()) {
            this.userBio.setText(flUser.userBio);
            this.userBio.setVisibility(View.VISIBLE);
        }

        this.settingsFloozButton.setTypeface(regContent);
        this.settingsFollowingButton.setTypeface(regContent);
        SpannableString floozString = new SpannableString(flUser.publicMetrics.nbFlooz + "\nFlooz");
        floozString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFlooz), 0);
        SpannableString friendsString = new SpannableString(flUser.publicMetrics.nbFriends + "\nAmis");
        friendsString.setSpan(new StyleSpan(Typeface.BOLD), 0, FLHelper.numLength(flUser.publicMetrics.nbFriends), 0);
        this.settingsFloozButton.setText(floozString);
        this.settingsFollowingButton.setText(friendsString);

        this.settingsButton.setOnClickListener(v -> this.showUserActionMenu(flUser));
        this.largePendingButton.setOnClickListener(v -> this.showActivePendingFriendActionMenu(flUser));
        this.removeButton.setOnClickListener(v -> this.showRemoveFriendActionMenu(flUser));
        this.buttonRequestPending.setOnClickListener(v -> this.showPendingFriendActionMenu(flUser));
        this.profileCover.setOnClickListener(v -> {
            if (flUser.coverURL != null && !flUser.coverURL.isEmpty() && !flUser.coverURL.contentEquals("/img/nocover.png")) {
                CustomImageViewer.start(parentActivity, coverURLFull);
            }
        });

        this.addButton.setOnClickListener(v -> FloozRestClient.getInstance().sendFriendRequest(flUser.userId, flUser.getSelectedCanal(), new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                settingsClearVision();
                largePendingButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        }));
    }

    private void loadNextPage() {
        if (nextPageUrl == null || nextPageUrl.isEmpty())
            return;

        FloozRestClient.getInstance().timelineNextPage(this.nextPageUrl, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;

                transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                nextPageUrl = (String) responseMap.get("nextUrl");
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void settingsClearVision() {
        this.addButton.setVisibility(View.GONE);
        this.removeButton.setVisibility(View.GONE);
        this.largePendingButton.setVisibility(View.GONE);
        this.buttonRequestPending.setVisibility(View.GONE);
    }

    @Override
    public void ListUserClick(FLUser user) {
        ProfileCardFragment profileCardFragment = new ProfileCardFragment();
        profileCardFragment.user = user;
        ((HomeActivity)parentActivity).pushFragmentInCurrentTab(profileCardFragment);
    }

    @Override
    public void ListItemClick(FLTransaction transac) {
        ((HomeActivity)parentActivity).onItemSelected(transac);
    }

    @Override
    public void ListItemCommentClick(FLTransaction transac) {
        ((HomeActivity)parentActivity).onItemCommentSelected(transac);
    }

    @Override
    public void ListItemImageClick(String imgUrl) {
        ((HomeActivity)parentActivity).onItemImageSelected(imgUrl);
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

    private ActionSheetItem.ActionSheetItemClickListener createTransaction = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            Intent intent = new Intent(parentActivity, NewTransactionActivity.class);
            intent.putExtra("user", flUser.json.toString());
            parentActivity.startActivity(intent);
            parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    };

    public void showUserActionMenu(FLUser user) {
        if (user == null || user.userId.contentEquals(FloozRestClient.getInstance().currentUser.userId) || user.username == null || user.fullname == null)
            return;

        List<ActionSheetItem> items = new ArrayList<>();

        if (this.flUser.actions.contains("flooz"))
            items.add(new ActionSheetItem(parentActivity, String.format(parentActivity.getResources().getString(R.string.MENU_NEW_FLOOZ), user.username), createTransaction));
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
