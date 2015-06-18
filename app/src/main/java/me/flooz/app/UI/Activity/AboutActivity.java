package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class AboutActivity extends Activity {

    private AboutActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        instance = this;
        floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.other_menu_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.other_menu_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.other_menu_header_title);
        ListView contentList = (ListView) this.findViewById(R.id.other_menu_list);
        TextView aboutText = (TextView) this.findViewById(R.id.other_menu_about);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        aboutText.setTypeface(CustomFonts.customTitleLight(this));

        aboutText.setText("Flooz " + FloozApplication.getAppVersionName(this));

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        });

        final List<SettingsListItem> list = new ArrayList<>();

        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_RATE_APP), (parent, view, position, id) -> {
            final String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }));

        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_FAQ), (parent, view, position, id) -> {
            Intent intent = new Intent();
            intent.putExtra("title", list.get(position).getTitle());
            intent.putExtra("url", "https://www.flooz.me/faq?layout=webview");
            intent.setClass(instance, WebContentActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_TERMS), (parent, view, position, id) -> {
            Intent intent = new Intent();
            intent.putExtra("title", list.get(position).getTitle());
            intent.putExtra("url", "https://www.flooz.me/cgu?layout=webview");
            intent.setClass(instance, WebContentActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_CONTACT), (parent, view, position, id) -> {
            Intent intent = new Intent();
            intent.putExtra("title", list.get(position).getTitle());
            intent.putExtra("url", "https://www.flooz.me/contact?layout=webview");
            intent.setClass(instance, WebContentActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_REVIEW), (parent, view, position, id) -> {
            Intent intent = WebContentActivity.newEmailIntent("hello@flooz.me", list.get(position).getTitle(), "Voici quelques idées pour améliorer l'application : ", "");
            startActivity(intent);
        }));

        new SettingsListAdapter(this, list, contentList);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
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

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
