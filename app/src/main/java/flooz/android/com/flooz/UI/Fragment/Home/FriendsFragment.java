package flooz.android.com.flooz.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Adapter.FriendsListAdapter;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Fragment.Camera.ImageViewerFragment;
import flooz.android.com.flooz.UI.Tools.ActionSheet;
import flooz.android.com.flooz.UI.Tools.ActionSheetItem;
import flooz.android.com.flooz.UI.Tools.CustomToast;
import flooz.android.com.flooz.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FriendsFragment extends Fragment
{
    public Context context;

    public HomeActivity parentActivity;

    private TextView searchTextfield;
    private ImageView clearSearchTextfieldButton;

    private StickyListHeadersListView resultList;

    private FriendsListAdapter listAdapter;

    private FLUser userAction = null;
    private  int userPosition;
    private FriendsListAdapter.FriendKind userKind;

    private ActionSheetItem.ActionSheetItemClickListener createTransaction = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            if (parentActivity != null) {
                ((NewFloozFragment)parentActivity.contentFragments.get("create")).UserSelected(userAction);
                parentActivity.pushMainFragment("create", R.animator.slide_up, android.R.animator.fade_out);
            }
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener addFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().sendFriendRequest(userAction.userId, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    if (userKind == FriendsListAdapter.FriendKind.SUGGESTION)
                        listAdapter.reloadSuggestions();
                    FloozRestClient.getInstance().updateUserProfile(null);
                }

                @Override
                public void failure(int statusCode, FLError error) {
                    CustomToast.show(context, error);
                }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener removeFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(userAction.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateUserProfile(null);
                    listAdapter.reloadSuggestions();
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
            FloozRestClient.getInstance().performActionOnFriend(userAction.userId, FloozRestClient.FriendAction.Accept, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateUserProfile(null);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener declineFriend = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            FloozRestClient.getInstance().performActionOnFriend(userAction.userId, FloozRestClient.FriendAction.Decline, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().updateUserProfile(null);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
    };

    private ActionSheetItem.ActionSheetItemClickListener showUserPicture = new ActionSheetItem.ActionSheetItemClickListener() {
        @Override
        public void onClick() {
            ((ImageViewerFragment)parentActivity.contentFragments.get("img")).setImage(userAction.avatarURL);
            parentActivity.pushMainFragment("img", R.animator.slide_up, android.R.animator.fade_out);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends_fragment, null);

        this.context = inflater.getContext();

        this.searchTextfield = (TextView) view.findViewById(R.id.friends_search_textfield);
        this.clearSearchTextfieldButton = (ImageView) view.findViewById(R.id.friends_search_clear);
        this.searchTextfield.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.searchTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0)
                    clearSearchTextfieldButton.setVisibility(View.VISIBLE);
                else
                    clearSearchTextfieldButton.setVisibility(View.GONE);
                listAdapter.searchUser(editable.toString());
            }
        });

        this.clearSearchTextfieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.stopSearch();
                searchTextfield.setText("");
                searchTextfield.clearFocus();
                clearSearchTextfieldButton.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager)inflater.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            }
        });

        this.resultList = (StickyListHeadersListView) view.findViewById(R.id.friends_result_list);
        this.listAdapter = new FriendsListAdapter(inflater.getContext());
        this.resultList.setAdapter(this.listAdapter);

        this.resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                userAction = (FLUser)listAdapter.getItem(position);
                userPosition = position;
                userKind = listAdapter.getUserKind(position);

                List<ActionSheetItem> items = new ArrayList<ActionSheetItem>();

                switch (userKind) {
                    case SUGGESTION:
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_NEW_FLOOZ, createTransaction));
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_ADD_FRIENDS, addFriend));
                        break;
                    case REQUEST:
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.FRIEND_REQUEST_ACCEPT, acceptFriend));
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.FRIEND_REQUEST_REFUSE, declineFriend));
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_NEW_FLOOZ, createTransaction));
                        break;
                    case FRIEND:
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_NEW_FLOOZ, createTransaction));
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_REMOVE_FRIENDS, removeFriend));
                        break;
                    case SEARCH:
                        items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_NEW_FLOOZ, createTransaction));
                        if (FloozRestClient.getInstance().currentUser.userIsAFriend(userAction))
                            items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_REMOVE_FRIENDS, removeFriend));
                        else
                            items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_ADD_FRIENDS, addFriend));
                        break;
                }

                if (userAction.avatarURL != null && !userAction.avatarURL.isEmpty())
                    items.add(new ActionSheetItem(inflater.getContext(), R.string.MENU_AVATAR, showUserPicture));

                ActionSheet.showWithItems(inflater.getContext(), items);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
