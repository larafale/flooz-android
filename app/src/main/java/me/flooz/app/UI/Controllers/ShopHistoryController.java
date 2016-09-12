package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.Adapter.ShopHistoryAdapter;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AdvancedPopupActivity;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.Settings.PasswordSettingsActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.PasswordFragment;
import me.flooz.app.UI.View.TimelineListView;

/**
 * Created by Flooz on 09/09/16.
 */
public class ShopHistoryController extends BaseController {

    private TimelineListView contentList;
    private ShopHistoryAdapter listAdapter;

    public ShopHistoryController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public ShopHistoryController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.contentList = (TimelineListView) this.currentView.findViewById(R.id.shop_history_list);

        this.listAdapter = new ShopHistoryAdapter(this.parentActivity);
        this.contentList.setAdapter(this.listAdapter);

        this.contentList.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onShowLastItem() {
                listAdapter.loadNextPage();
            }
        });

        this.contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject currentItem = listAdapter.getItem(position);

                if (currentItem != null) {
                    JSONObject triggerData = new JSONObject();

                    String contentString = "";

                    if (currentItem.has("code")) {
                        if (currentItem.opt("code") instanceof String) {
                            contentString = "Votre code:\n\n\"" + currentItem.optString("code") + "\"";
                        } else if (currentItem.opt("code") instanceof JSONArray) {
                            if (currentItem.optJSONArray("code").length() > 1) {
                                JSONArray codes = currentItem.optJSONArray("code");

                                contentString = "Votre code:";


                                for (int i = 0; i < codes.length(); i++) {
                                    contentString += "\n\n\"" + codes.optString(i) + "\"";
                                }
                            } else {
                                contentString = "Votre code:\n\n\"" + currentItem.optJSONArray("code").optString(0) + "\"";
                            }
                        }
                    }

                    try {
                        triggerData.put("title", currentItem.optJSONObject("type").optString("name"));
                        triggerData.put("subtitle", "");
                        triggerData.put("amount", currentItem.opt("amount"));
                        triggerData.put("content", contentString);
                        triggerData.put("close", false);

                        JSONArray buttons = new JSONArray();

                        JSONArray buttonTriggers = new JSONArray();

                        JSONObject buttonTrigger = new JSONObject();
                        JSONObject buttonTriggerData = new JSONObject();

                        JSONObject button = new JSONObject();

                        button.put("title", "Fermer");

                        buttonTriggerData.put("noAnim", true);

                        buttonTrigger.put("key", "popup:advanced:hide");
                        buttonTrigger.put("data", buttonTriggerData);

                        buttonTriggers.put(buttonTrigger);

                        button.put("triggers", buttonTriggers);

                        buttons.put(button);

                        triggerData.put("buttons", buttons);

                        Intent popupIntent = new Intent(parentActivity, AdvancedPopupActivity.class);
                        popupIntent.putExtra("triggerData", triggerData.toString());

                        parentActivity.startActivity(popupIntent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
