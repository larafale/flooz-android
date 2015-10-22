package me.flooz.app.UI.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 10/22/15.
 */
public class CustomDialog {

    public static void show(Context ctx, final JSONObject data, final View.OnClickListener dismissListener) {
        final FloozApplication floozApp = FloozApplication.getInstance();

        final List<String> buttonsString = new ArrayList<>();
        final List<JSONArray> buttonsAction = new ArrayList<>();

        if (data.has("button")) {
            buttonsString.add(data.optString("button"));

            if (data.has("triggers")) {
                buttonsAction.add(data.optJSONArray("triggers"));
            } else {
                buttonsAction.add(new JSONArray());
            }
        }

        if (data.has("buttons")) {
            for (int i = 0; i < data.optJSONArray("buttons").length(); i++) {
                JSONObject button = data.optJSONArray("buttons").optJSONObject(i);
                if (button.has("title")) {
                    buttonsString.add(button.optString("title"));

                    if (button.has("triggers"))
                        buttonsAction.add(button.optJSONArray("triggers"));
                    else
                        buttonsAction.add(new JSONArray());
                }
            }
        }

        if (buttonsString.size() == 0) {
            buttonsString.add(ctx.getResources().getString(R.string.GLOBAL_OK));

            if (data.has("triggers")) {
                buttonsAction.add(data.optJSONArray("triggers"));
            } else {
                buttonsAction.add(new JSONArray());
            }
        }

        Handler handler = new Handler(ctx.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(floozApp.getCurrentActivity());

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_trigger);

                TextView title = (TextView) dialog.findViewById(R.id.dialog_trigger_title);
                title.setTypeface(CustomFonts.customContentRegular(floozApp.getCurrentActivity()), Typeface.BOLD);

                if (data.has("title") && !data.optString("title").isEmpty()) {
                    title.setText(data.optString("title"));
                    title.setVisibility(View.VISIBLE);
                } else {
                    title.setVisibility(View.GONE);
                }

                TextView text = (TextView) dialog.findViewById(R.id.dialog_trigger_msg);
                text.setTypeface(CustomFonts.customContentRegular(floozApp.getCurrentActivity()));
                text.setText(data.optString("content"));

                if (data.has("content") && !data.optString("content").isEmpty()) {
                    text.setText(data.optString("content"));
                    text.setVisibility(View.VISIBLE);
                } else {
                    text.setVisibility(View.GONE);
                }

                LinearLayout btnContainer1 = (LinearLayout) dialog.findViewById(R.id.dialog_trigger_container1);
                LinearLayout btnContainer2 = (LinearLayout) dialog.findViewById(R.id.dialog_trigger_container2);

                final Button btn1 = (Button) dialog.findViewById(R.id.dialog_trigger_btn1);
                final Button btn2 = (Button) dialog.findViewById(R.id.dialog_trigger_btn2);
                final Button btn3 = (Button) dialog.findViewById(R.id.dialog_trigger_btn3);
                final Button btn4 = (Button) dialog.findViewById(R.id.dialog_trigger_btn4);

                View spacer1 = dialog.findViewById(R.id.dialog_trigger_spacer1);
                View spacer2 = dialog.findViewById(R.id.dialog_trigger_spacer2);

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = 0;

                        if (v == btn1)
                            index = 0;
                        else if (v == btn2)
                            index = 1;
                        else if (v == btn3)
                            index = 2;
                        else if (v == btn4)
                            index = 3;

                        JSONArray triggers = buttonsAction.get(index);
                        FloozRestClient.getInstance().handleTriggerArray(triggers);

                        dialog.dismiss();

                        if (dismissListener != null)
                            dismissListener.onClick(v);
                    }
                };

                btn1.setOnClickListener(clickListener);
                btn2.setOnClickListener(clickListener);
                btn3.setOnClickListener(clickListener);
                btn4.setOnClickListener(clickListener);

                btn1.setText(buttonsString.get(0));

                if (buttonsString.size() < 2) {
                    btn2.setVisibility(View.GONE);
                    spacer1.setVisibility(View.GONE);
                } else {
                    btn2.setVisibility(View.VISIBLE);
                    spacer1.setVisibility(View.VISIBLE);
                    btn2.setText(buttonsString.get(1));
                }

                if (buttonsString.size() < 3) {
                    btnContainer2.setVisibility(View.GONE);
                } else {
                    btnContainer2.setVisibility(View.VISIBLE);

                    btn3.setText(buttonsString.get(2));

                    if (buttonsString.size() < 4) {
                        btn4.setVisibility(View.GONE);
                        spacer2.setVisibility(View.GONE);
                    } else {
                        btn4.setVisibility(View.VISIBLE);
                        spacer2.setVisibility(View.VISIBLE);
                        btn4.setText(buttonsString.get(3));
                    }
                }

                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        });
    }
}
