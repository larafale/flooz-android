package me.flooz.app.Adapter;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLButton;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.JSONHelper;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


/**
 * Created by Flooz on 8/31/15.
 */
public class ProfileListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;

    private ProfileListAdapterDelegate delegate;

    private ArrayList<String> menuHeader = new ArrayList<>();
    private ArrayList<Map<String, Object>> menuData = new ArrayList<>();

    public View userView;
    public UserView userViewHolder;

    public View versionView;
    public VersionHolder versionViewHolder;

    public ProfileListAdapter(Context ctx, @NonNull ProfileListAdapterDelegate del) {

        this.delegate = del;
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.userView = inflater.inflate(R.layout.account_menu_user_view, null, false);

        this.userViewHolder = new UserView();
        this.userViewHolder.imageView = (RoundedImageView) this.userView.findViewById(R.id.account_menu_user_view_img);
        this.userViewHolder.balance = (TextView) this.userView.findViewById(R.id.account_menu_user_view_balance);

        this.userViewHolder.balance.setTypeface(CustomFonts.customContentRegular(this.context));

        this.userViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.userImageClicked();
            }
        });

        this.userViewHolder.balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.balanceClicked();
            }
        });

        this.versionView = inflater.inflate(R.layout.account_menu_version, null, false);

        this.versionViewHolder = new VersionHolder();
        this.versionViewHolder.text = (TextView) this.versionView.findViewById(R.id.account_menu_version_title);

        this.versionViewHolder.text.setTypeface(CustomFonts.customContentLight(this.context));
        this.versionViewHolder.text.setText("Flooz " + FloozApplication.getAppVersionName(this.context));

        this.reloadData();
    }

    public void reloadData() {
        int cardNotifs = 0;
        int bankNotifs = 0;
        int coordsNotifs = 0;
        int friendsNotifs;
        int docNotifs = 0;

        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.userViewHolder.balance.setText("Solde " + FLHelper.trimTrailingZeros( String.format(Locale.US, "%.2f", currentUser.amount.floatValue())) + " €");

        if (currentUser.avatarURL != null && !currentUser.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(currentUser.avatarURL, this.userViewHolder.imageView);
        else
            this.userViewHolder.imageView.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        if (currentUser.creditCard == null || TextUtils.isEmpty(currentUser.creditCard.cardId))
            ++cardNotifs;

        List missingFields;
        try {
            missingFields = JSONHelper.toList(currentUser.json.optJSONArray("missingFields"));

            if (missingFields.contains("sepa"))
                ++bankNotifs;
            if (missingFields.contains("cniRecto"))
                ++docNotifs;
            if (missingFields.contains("cniVerso"))
                ++docNotifs;
            if (missingFields.contains("justificatory"))
                ++docNotifs;
            if (missingFields.contains("address"))
                ++coordsNotifs;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        friendsNotifs = currentUser.json.optJSONObject("metrics").optInt("pendingFriend");

        menuData.clear();
        menuHeader.clear();

        menuHeader.add(this.context.getResources().getString(R.string.ACCOUNT_MENU_ACCOUNT));
        menuHeader.add(this.context.getResources().getString(R.string.ACCOUNT_MENU_BANK));
        menuHeader.add(this.context.getResources().getString(R.string.ACCOUNT_MENU_SETTINGS));
        menuHeader.add(this.context.getResources().getString(R.string.MENU_OTHER));

        {
            Map<String, Object> item0 = new HashMap<>();
            item0.put("title", this.context.getResources().getString(R.string.MENU_EDIT_PROFILE));
            item0.put("action", "profile");
            item0.put("headerID", 1);
            menuData.add(item0);

            Map<String, Object> item1 = new HashMap<>();
            item1.put("title", this.context.getResources().getString(R.string.SETTINGS_COORD));
            item1.put("action", "coords");
            item1.put("notif", coordsNotifs);
            item1.put("headerID", 1);
            menuData.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("title", this.context.getResources().getString(R.string.SETTINGS_DOCUMENTS));
            item2.put("action", "documents");
            item2.put("notif", docNotifs);
            item2.put("headerID", 1);
            menuData.add(item2);

            if (friendsNotifs > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", this.context.getResources().getString(R.string.NAV_FRIEND_REQUEST));
                item.put("action", "friends");
                item.put("notif", friendsNotifs);
                item.put("headerID", 1);
                menuData.add(item);
            }

            Boolean isShopActive = false;

            for (FLButton homeButton : FloozRestClient.getInstance().currentTexts.homeButtons) {
                if (homeButton.name.contentEquals("hop")) {
                    isShopActive = true;
                    break;
                }
            }

            if (isShopActive || (FloozRestClient.getInstance().currentUser.metrics.has("gcard")
                    && FloozRestClient.getInstance().currentUser.metrics.optJSONObject("gcard").optInt("count") > 0)) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", this.context.getResources().getString(R.string.ACCOUNT_MENU_SHOP_HISTORY));
                item.put("action", "shopHistory");
                item.put("headerID", 1);
                menuData.add(item);
            }

            if (FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").has("promo")
                    && FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("promo").has("title")
                    && !FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("promo").optString("title").isEmpty()) {
                Map<String, Object> item = new HashMap<>();
                item.put("title", FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("promo").optString("title"));
                item.put("action", "sponsor");
                item.put("headerID", 1);
                menuData.add(item);
            }
        }

        {
            Map<String, Object> item0 = new HashMap<>();
            item0.put("title", this.context.getResources().getString(R.string.SETTINGS_CARD));
            item0.put("action", "card");
            item0.put("headerID", 2);
            menuData.add(item0);

            Map<String, Object> item1 = new HashMap<>();
            item1.put("title", this.context.getResources().getString(R.string.SETTINGS_RIB));
            item1.put("action", "bank");
            item1.put("notif", bankNotifs);
            item1.put("headerID", 2);
            menuData.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("title", this.context.getResources().getString(R.string.ACCOUNT_MENU_CASHOUT));
            item2.put("action", "cashout");
            item2.put("headerID", 2);
            menuData.add(item2);
        }

        {
            Map<String, Object> item0 = new HashMap<>();
            item0.put("title", this.context.getResources().getString(R.string.SETTINGS_NOTIFICATIONS));
            item0.put("action", "notifSetting");
            item0.put("headerID", 3);
            menuData.add(item0);

            Map<String, Object> item1 = new HashMap<>();
            item1.put("title", this.context.getResources().getString(R.string.SETTINGS_PRIVACY));
            item1.put("action", "privacy");
            item1.put("headerID", 3);
            menuData.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("title", this.context.getResources().getString(R.string.SETTINGS_SECURITY));
            item2.put("action", "security");
            item2.put("headerID", 3);
            menuData.add(item2);

        }

        {
            Map<String, Object> item0 = new HashMap<>();
            item0.put("title", this.context.getResources().getString(R.string.INFORMATIONS_RATE_APP));
            item0.put("action", "rate");
            item0.put("headerID", 4);
            menuData.add(item0);

            Map<String, Object> item1 = new HashMap<>();
            item1.put("title", this.context.getResources().getString(R.string.INFORMATIONS_FAQ));
            item1.put("action", "faq");
            item1.put("headerID", 4);
            menuData.add(item1);

            Map<String, Object> item2 = new HashMap<>();
            item2.put("title", this.context.getResources().getString(R.string.INFORMATIONS_TERMS));
            item2.put("action", "cgu");
            item2.put("headerID", 4);
            menuData.add(item2);

            Map<String, Object> item3 = new HashMap<>();
            item3.put("title", this.context.getResources().getString(R.string.INFORMATIONS_CONTACT));
            item3.put("action", "contact");
            item3.put("headerID", 4);
            menuData.add(item3);

            Map<String, Object> item4 = new HashMap<>();
            item4.put("title", this.context.getResources().getString(R.string.ACCOUNT_MENU_IDEAS));
            item4.put("action", "critics");
            item4.put("headerID", 4);
            menuData.add(item4);

            Map<String, Object> item5 = new HashMap<>();
            item5.put("title", this.context.getResources().getString(R.string.SETTINGS_LOGOUT));
            item5.put("action", "logout");
            item5.put("headerID", 4);
            menuData.add(item5);
        }

        this.notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (position == 0)
            return new View(this.context);

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

        int headerId = (int) this.getHeaderId(position);

        holder.text.setText(this.menuHeader.get(headerId - 1));;

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (i == 0)
            return 0;
        if (i == this.getCount() - 1)
            return 4;

        return (int) this.getItem(i).get("headerID");
    }

    @Override
    public int getCount() {
        return menuData.size() + 2;
    }

    @Override
    public Map<String, Object> getItem(int i) {
        if (i == 0 || i == this.getCount() - 1)
            return null;

        return this.menuData.get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position == 0)
            return userView;
        if (position == this.getCount() - 1)
            return versionView;

        final ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.account_menu_row, parent, false);
            holder.title = (TextView) convertView.findViewById(R.id.account_menu_row_title);
            holder.notifs = (TextView) convertView.findViewById(R.id.account_menu_row_notif);
            holder.indicator = (ImageView) convertView.findViewById(R.id.account_menu_row_arrow);

            holder.title.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.notifs.setTypeface(CustomFonts.customContentRegular(this.context));

            holder.indicator.setColorFilter(this.context.getResources().getColor(R.color.placeholder));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String, Object> item = this.getItem(position);

        holder.title.setText((String) item.get("title"));

        if (item.containsKey("notif") && ((int) item.get("notif")) > 0) {
            holder.notifs.setText("" + (int) item.get("notif"));
            holder.notifs.setVisibility(View.VISIBLE);
        } else
            holder.notifs.setVisibility(View.GONE);

        return convertView;
    }

    public class VersionHolder {
        TextView text;
    }

    public class HeaderViewHolder {
        TextView text;
    }

    public class ViewHolder {
        TextView title;
        TextView notifs;
        ImageView indicator;
    }
}

