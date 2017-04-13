package me.flooz.app.Adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Wapazz on 13/10/15.
 */
public class SearchListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    public Boolean isSearchActive = false;

    private Context context;
    private LayoutInflater inflater;

    public List<FLUser> suggestionList = new ArrayList<>(0);
    public List<FLUser> searchList = new ArrayList<>(0);
    private Handler searchHandler;

    private String searchData;

    private BroadcastReceiver reloadContent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FloozRestClient.getInstance().updateCurrentUser(null);
        }
    };

    private BroadcastReceiver reloadSuggest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadSuggestions();
        }
    };

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchData.isEmpty()) {
                isSearchActive = true;

                FloozRestClient.getInstance().searchUser(searchData, false, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        searchList = new ArrayList<>((List<FLUser>)response);
                        if (isSearchActive)
                            notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        searchList.clear();
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };

    public SearchListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.reloadSuggestions();
        this.searchHandler = new Handler(Looper.getMainLooper());

        notifyDataSetChanged();
    }

    public void reloadSuggestions() {
        FloozRestClient.getInstance().loadFriendSuggestions(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                suggestionList.clear();
                suggestionList.addAll((List<FLUser>) response);
                notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void reloadSuggestions(List<FLUser> suggests) {
        suggestionList.clear();
        suggestionList.addAll(suggests);
        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.account_menu_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.account_menu_header_title);

            holder.text.setTypeface(CustomFonts.customContentBold(this.context));

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (!this.isSearchActive)
            holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS_SUGGESTION));
        else
            holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PCIKER_SELECTION_CELL) + " (" + this.searchList.size() + ")");

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (!this.isSearchActive)
            return FLUser.FLUserSelectedCanal.SuggestionCanal.ordinal();
        else
            return FLUser.FLUserSelectedCanal.SearchCanal.ordinal();
    }

    @Override
    public int getCount() {
        if (!this.isSearchActive)
            return this.suggestionList.size();
        return this.searchList.size();
    }

    @Override
    public FLUser getItem(int i) {
        if (!this.isSearchActive)
            return this.suggestionList.get(i);
        else
            return this.searchList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private void setFriendButton(ImageView button, boolean isFriend) {
        if (!isFriend) {
            button.setImageResource(R.drawable.follow);
            button.setColorFilter(context.getResources().getColor(R.color.blue));
            button.setBackgroundResource(R.drawable.frame_action_button);
        }
        else {
            button.setImageResource(R.drawable.unfollow);
            button.setColorFilter(Color.WHITE);
            button.setBackgroundResource(R.drawable.background_action_button);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_row, parent, false);
            holder.container = (LinearLayout) convertView.findViewById(R.id.user_list_row_layout);
            holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
            holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
            holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);
            holder.button = (ImageView) convertView.findViewById(R.id.user_list_row_button);

            holder.button.setColorFilter(context.getResources().getColor(R.color.blue));
            holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);
            holder.fullname.setTypeface(CustomFonts.customContentRegular(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FLUser user = this.getItem(position);

        holder.fullname.setText(user.fullname);
        holder.username.setText("@" + user.username);

        holder.pic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (user.avatarURL != null && !user.avatarURL.isEmpty())
            FloozApplication.getInstance().imageFetcher.attachImage(user.avatarURL, holder.pic);

        if (!user.isFriendable)
            holder.button.setVisibility(View.GONE);
        else {
            this.setFriendButton(holder.button, user.isFriend);
            holder.button.setVisibility(View.VISIBLE);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isFriend) {
                    user.isFriend = false;
                    notifyDataSetChanged();
                    user.selectedCanal = FLUser.FLUserSelectedCanal.SuggestionCanal;
                    FloozRestClient.getInstance().sendFriendRequest(user.userId, user.getSelectedCanal(), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FloozRestClient.getInstance().updateCurrentUser(null);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            user.isFriend = true;
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    List<ActionSheetItem> items = new ArrayList<>();
                    items.add(new ActionSheetItem(context, R.string.MENU_REMOVE_FRIENDS, new ActionSheetItem.ActionSheetItemClickListener() {
                        @Override
                        public void onClick() {
                            user.isFriend = true;
                            notifyDataSetChanged();
                            FloozRestClient.getInstance().performActionOnFriend(user.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    FloozRestClient.getInstance().updateCurrentUser(null);
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {
                                    user.isFriend = false;
                                    notifyDataSetChanged();
                                }
                            });
                        }
                    }));

                    ActionSheet.showWithItems(context, items);
                }
            }
        });

        return convertView;
    }

    public void searchUser(String searchString) {
        this.searchHandler.removeCallbacks(searchRunnable);
        this.searchData = searchString;
        if (searchString.isEmpty() || searchString.length() < 3) {
            this.stopSearch();
        } else {
            this.searchHandler.postDelayed(searchRunnable, 500);
        }
    }

    public void stopSearch() {
        this.searchData = "";
        this.searchHandler.removeCallbacks(searchRunnable);
        this.isSearchActive = false;
        this.notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        LinearLayout container;
        TextView fullname;
        TextView username;
        RoundedImageView pic;
        ImageView button;
    }
}
