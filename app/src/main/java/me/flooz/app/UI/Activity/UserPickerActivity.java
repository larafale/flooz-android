package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import me.flooz.app.Adapter.SelectUserListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 03/08/16.
 */
public class UserPickerActivity extends BaseActivity {
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private ImageView headerSearchButton;
    private EditText searchTextField;
    private View searchSeparator;

    private SelectUserListAdapter listAdapter;
    private StickyListHeadersListView resultList;

    private JSONObject triggerData;

    private List<FLTrigger> successTriggers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.setContentView(R.layout.user_picker_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.headerSearchButton = (ImageView) this.findViewById(R.id.header_item_right);
        this.searchTextField = (EditText) this.findViewById(R.id.user_picker_search);
        this.searchSeparator = this.findViewById(R.id.user_picker_separator);
        this.resultList = (StickyListHeadersListView) this.findViewById(R.id.user_picker_list);

        if (FloozRestClient.getInstance().currentTexts.friendSearch != null && !FloozRestClient.getInstance().currentTexts.friendSearch.isEmpty())
            this.searchTextField.setHint(FloozRestClient.getInstance().currentTexts.friendSearch);

        this.listAdapter = new SelectUserListAdapter(this);

        resultList.setAdapter(this.listAdapter);

        this.headerSearchButton.setColorFilter(this.getResources().getColor(R.color.blue));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.headerSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchTextField.getVisibility() == View.GONE) {
                    searchTextField.setVisibility(View.VISIBLE);
                    searchSeparator.setVisibility(View.VISIBLE);

                    searchTextField.requestFocus();

                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchTextField, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    searchTextField.setVisibility(View.GONE);
                    searchSeparator.setVisibility(View.GONE);
                }
            }
        });

        this.searchTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    listAdapter.searchUser(searchTextField.getText().toString());
                } else {
                    listAdapter.searchUser(editable.toString());
                }
            }
        });

        this.resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLUser object = listAdapter.getItem(position);
                if (object != null) {
                    if (triggerData != null) {
                        successTriggers = FLTriggerManager.convertTriggersJSONArrayToList(triggerData.optJSONArray("success"));
                        FLTrigger successTrigger = successTriggers.get(0);


                        JSONObject data = new JSONObject();

                        try {
                            if (object.userKind == FLUser.UserKind.FloozUser) {
                                data.put("to", object.username);

                                if (object.isCactus) {
                                    data.put("toFullName", object.username);
                                } else
                                    data.put("toFullName", object.fullname);

                                if (object.floozOptions != null)
                                    data.put("options", object.floozOptions);

                            } else {
                                data.put("to", object.phone);
                                data.put("toFullName", object.fullname);

                                if (object.firstname != null || object.lastname != null) {
                                    JSONObject contact = new JSONObject();

                                    try {
                                        if (!object.firstname.isEmpty())
                                            contact.put("firstName", object.firstname);

                                        if (!object.lastname.isEmpty())
                                            contact.put("lastName", object.lastname);

                                        data.put("contact", contact.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            if (triggerData.has("in") && !triggerData.optString("in").isEmpty()) {
                                JSONObject base = successTrigger.data.optJSONObject(triggerData.optString("in"));

                                if (base != null) {
                                    Iterator it = base.keys();
                                    while (it.hasNext()) {
                                        String key = (String) it.next();

                                        if (data.has(key) && data.opt(key) instanceof JSONObject && base.opt(key) instanceof JSONObject) {
                                            JSONObject merged = new JSONObject();
                                            JSONObject[] objs = new JSONObject[] { data.optJSONObject(key), base.optJSONObject(key) };
                                            for (JSONObject obj : objs) {
                                                Iterator it2 = obj.keys();
                                                while (it2.hasNext()) {
                                                    String key2 = (String)it2.next();
                                                    merged.put(key2, obj.get(key2));
                                                }
                                            }

                                            data.put(key, merged);
                                        } else {
                                            data.put(key, base.opt(key));
                                        }
                                    }
                                }

                                successTrigger.data.put(triggerData.optString("in"), data);
                            } else {
                                Iterator it = data.keys();
                                while (it.hasNext()) {
                                    String key = (String) it.next();

                                    if (successTrigger.data.has(key) && successTrigger.data.opt(key) instanceof JSONObject && data.opt(key) instanceof JSONObject) {
                                        JSONObject merged = new JSONObject();
                                        JSONObject[] objs = new JSONObject[] { successTrigger.data.optJSONObject(key), data.optJSONObject(key) };
                                        for (JSONObject obj : objs) {
                                            Iterator it2 = obj.keys();
                                            while (it2.hasNext()) {
                                                String key2 = (String)it2.next();
                                                merged.put(key2, obj.get(key2));
                                            }
                                        }

                                        successTrigger.data.put(key, merged);
                                    } else {
                                        successTrigger.data.put(key, data.opt(key));
                                    }
                                }
                            }

                            headerBackButton.performClick();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Intent currentIntent = getIntent();

                        currentIntent.removeExtra("to");
                        currentIntent.removeExtra("toFullName");
                        currentIntent.removeExtra("options");
                        currentIntent.removeExtra("contact");

                        if (object.userKind == FLUser.UserKind.FloozUser) {
                            currentIntent.putExtra("to", object.username);

                            if (object.isCactus) {
                                currentIntent.putExtra("toFullName", object.username);
                            } else
                                currentIntent.putExtra("toFullName", object.fullname);

                            if (object.floozOptions != null)
                                currentIntent.putExtra("options", object.floozOptions.toString());

                        } else {
                            currentIntent.putExtra("to", object.phone);
                            currentIntent.putExtra("toFullName", object.fullname);

                            if (object.firstname != null || object.lastname != null) {
                                JSONObject contact = new JSONObject();

                                try {
                                    if (!object.firstname.isEmpty())
                                        contact.put("firstName", object.firstname);

                                    if (!object.lastname.isEmpty())
                                        contact.put("lastName", object.lastname);


                                    currentIntent.putExtra("contact", contact.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        headerBackButton.performClick();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (successTriggers != null) {
            FLTriggerManager.getInstance().executeTriggerList(successTriggers);
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
