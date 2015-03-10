package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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

/**
 * Created by Flooz on 3/9/15.
 */
public class AboutActivity extends Activity {

    private AboutActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.other_menu_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.other_menu_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.other_menu_header_title);
        this.contentList = (ListView) this.findViewById(R.id.other_menu_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        final List<SettingsListItem> list = new ArrayList<>();
        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_FAQ), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("title", list.get(position).getTitle());
                intent.putExtra("url", "https://www.flooz.me/faq?layout=webview");
                intent.setClass(instance, WebContentActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));
        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_TERMS), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("title", list.get(position).getTitle());
                intent.putExtra("url", "https://www.flooz.me/cgu?layout=webview");
                intent.setClass(instance, WebContentActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));
        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_CONTACT), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("title", list.get(position).getTitle());
                intent.putExtra("url", "https://www.flooz.me/contact?layout=webview");
                intent.setClass(instance, WebContentActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));
        list.add(new SettingsListItem(this.getResources().getString(R.string.INFORMATIONS_REVIEW), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = WebContentActivity.newEmailIntent("hello@flooz.me", list.get(position).getTitle(), "Voici quelques idées pour améliorer l'application : ", "");
                startActivity(intent);
            }
        }));
        list.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_LOGOUT), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog validationDialog = new Dialog(instance);

                validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                validationDialog.setContentView(R.layout.custom_dialog_validate_transaction);

                TextView text = (TextView) validationDialog.findViewById(R.id.dialog_validate_flooz_text);
                text.setText(instance.getResources().getString(R.string.LOGOUT_INFO));
                text.setTypeface(CustomFonts.customContentRegular(instance));

                Button decline = (Button) validationDialog.findViewById(R.id.dialog_validate_flooz_decline);
                Button accept = (Button) validationDialog.findViewById(R.id.dialog_validate_flooz_accept);

                decline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validationDialog.dismiss();
                    }
                });

                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validationDialog.dismiss();
                        FloozRestClient.getInstance().logout();
                    }
                });

                validationDialog.setCanceledOnTouchOutside(false);
                validationDialog.show();
            }
        }));

        this.listAdapter = new SettingsListAdapter(this, list, this.contentList);
    }

    @Override
    public void onResume() {
        super.onResume();
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
