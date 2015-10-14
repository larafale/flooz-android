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

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
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

        this.userViewHolder.imageView.setOnClickListener(v -> {
            if (delegate != null)
                delegate.userImageClicked();
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

        this.userViewHolder.balance.setText("Solde " + FLHelper.trimTrailingZeros(String.format("%.2f", currentUser.amount.floatValue()).replace(',', '.')) + " €");

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
                ++coordsNotifs;
            if (missingFields.contains("address"))
                ++coordsNotifs;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        friendsNotifs = currentUser.json.optJSONObject("metrics").optInt("pendingFriend");

        menuData.clear();
        menuHeader.clear();

        menuHeader.add(this.context.getResources().getString(R.string.MENU_ACCOUNT));
        menuHeader.add(this.context.getResources().getString(R.string.ACCOUNT_MENU_SETTINGS));
        menuHeader.add(this.context.getResources().getString(R.string.MENU_OTHER));

        Map<String, Object> item0 = new HashMap<>();
        item0.put("title", this.context.getResources().getString(R.string.NAV_PROFILE));
        item0.put("action", "profile");
        item0.put("headerID", 1);
        menuData.add(item0);

        // TODO Changer la vue pour passer sur demande en attente
        if (friendsNotifs > 0) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("title", this.context.getResources().getString(R.string.NAV_FRIENDS));
            item1.put("action", "friends");
            item1.put("notif", friendsNotifs);
            item1.put("headerID", 1);
            menuData.add(item1);
        }

        Map<String, Object> item2 = new HashMap<>();
        item2.put("title", this.context.getResources().getString(R.string.ACCOUNT_MENU_CASHOUT));
        item2.put("action", "cashout");
        item2.put("headerID", 1);
        menuData.add(item2);

        Map<String, Object> item5 = new HashMap<>();
        item5.put("title", this.context.getResources().getString(R.string.SETTINGS_COORD));
        item5.put("action", "coords");
        item5.put("notif", coordsNotifs);
        item5.put("headerID", 1);
        menuData.add(item5);

        Map<String, Object> item6 = new HashMap<>();
        item6.put("title", this.context.getResources().getString(R.string.SETTINGS_DOCUMENTS));
        item6.put("action", "documents");
        item6.put("notif", docNotifs);
        item6.put("headerID", 1);
        menuData.add(item6);

        Map<String, Object> item7 = new HashMap<>();
        item7.put("title", FloozRestClient.getInstance().currentTexts.json.optJSONObject("menu").optJSONObject("promo").optString("title"));
        item7.put("action", "sponsor");
        item7.put("headerID", 1);
        menuData.add(item7);

        Map<String, Object> item3 = new HashMap<>();
        item3.put("title", this.context.getResources().getString(R.string.SETTINGS_CARD));
        item3.put("action", "card");
        item3.put("notif", cardNotifs);
        item3.put("headerID", 2);
        menuData.add(item3);

        Map<String, Object> item4 = new HashMap<>();
        item4.put("title", this.context.getResources().getString(R.string.SETTINGS_RIB));
        item4.put("action", "bank");
        item4.put("notif", bankNotifs);
        item4.put("headerID", 2);
        menuData.add(item4);

        Map<String, Object> item8 = new HashMap<>();
        item8.put("title", this.context.getResources().getString(R.string.SETTINGS_PREFERENCES));
        item8.put("action", "preferences");
        item8.put("headerID", 2);
        menuData.add(item8);

        Map<String, Object> item9 = new HashMap<>();
        item9.put("title", this.context.getResources().getString(R.string.SETTINGS_SECURITY));
        item9.put("action", "security");
        item9.put("headerID", 2);
        menuData.add(item9);

        Map<String, Object> item10 = new HashMap<>();
        item10.put("title", this.context.getResources().getString(R.string.INFORMATIONS_RATE_APP));
        item10.put("action", "rate");
        item10.put("headerID", 3);
        menuData.add(item10);

        Map<String, Object> item11 = new HashMap<>();
        item11.put("title", this.context.getResources().getString(R.string.INFORMATIONS_FAQ));
        item11.put("action", "faq");
        item11.put("headerID", 3);
        menuData.add(item11);

        Map<String, Object> item12 = new HashMap<>();
        item12.put("title", this.context.getResources().getString(R.string.INFORMATIONS_TERMS));
        item12.put("action", "cgu");
        item12.put("headerID", 3);
        menuData.add(item12);

        Map<String, Object> item13 = new HashMap<>();
        item13.put("title", this.context.getResources().getString(R.string.INFORMATIONS_CONTACT));
        item13.put("action", "contact");
        item13.put("headerID", 3);
        menuData.add(item13);

        Map<String, Object> item14 = new HashMap<>();
        item14.put("title", this.context.getResources().getString(R.string.ACCOUNT_MENU_IDEAS));
        item14.put("action", "critics");
        item14.put("headerID", 3);
        menuData.add(item14);

        Map<String, Object> item15 = new HashMap<>();
        item15.put("title", this.context.getResources().getString(R.string.SETTINGS_LOGOUT));
        item15.put("action", "logout");
        item15.put("headerID", 3);
        menuData.add(item15);

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

        int headerId = (int) this.getItem(position).get("headerID");

        holder.text.setText(this.menuHeader.get(headerId - 1));;

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (i == 0)
            return 0;
        if (i == this.getCount() - 1)
            return 3;

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

