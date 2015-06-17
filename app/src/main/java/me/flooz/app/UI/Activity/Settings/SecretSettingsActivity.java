package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import info.hoang8f.android.segmented.SegmentedGroup;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.View.AutoResizeEditText;
import me.flooz.app.UI.View.AutoResizeTextView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 5/12/15.
 */
public class SecretSettingsActivity extends Activity {

    private SecretSettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private RelativeLayout editView;
    private RelativeLayout infosView;
    private TextView infosQuestion;
    private AutoResizeTextView editQuestion;
    private AutoResizeEditText editCustomQuestion;
    private AutoResizeEditText editAnswer;
    private Button editSave;
    private int currentQuestion = -1;

    private List<String> questionList;

    private Boolean needEditing = false;
    private Boolean authenticationValidate = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.instance = this;

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_secret_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_secret_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_secret_header_title);
        this.editView = (RelativeLayout) this.findViewById(R.id.settings_secret_edit_view);
        this.infosView = (RelativeLayout) this.findViewById(R.id.settings_secret_infos_view);
        this.infosQuestion = (TextView) this.findViewById(R.id.settings_secret_infos_question);
        this.editQuestion = (AutoResizeTextView) this.findViewById(R.id.settings_secret_edit_question);
        this.editCustomQuestion = (AutoResizeEditText) this.findViewById(R.id.settings_secret_edit_custom_question);
        this.editAnswer = (AutoResizeEditText) this.findViewById(R.id.settings_secret_edit_answer);
        TextView editDesc = (TextView) this.findViewById(R.id.settings_secret_edit_desc);
        TextView infosDesc = (TextView) this.findViewById(R.id.settings_secret_infos_desc);
        TextView infosText = (TextView) this.findViewById(R.id.settings_secret_infos_text);
        Button infosEdit = (Button) this.findViewById(R.id.settings_secret_edit);
        this.editSave = (Button) this.findViewById(R.id.settings_secret_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.infosQuestion.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.editQuestion.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.editCustomQuestion.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.editAnswer.setTypeface(CustomFonts.customTitleExtraLight(this));
        editDesc.setTypeface(CustomFonts.customTitleExtraLight(this));
        infosDesc.setTypeface(CustomFonts.customTitleExtraLight(this));
        infosText.setTypeface(CustomFonts.customTitleExtraLight(this));

        int maxLength = 50;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);

        this.editQuestion.setEnabled(true);
        this.editQuestion.setFocusableInTouchMode(false);
        this.editQuestion.setFocusable(false);
        this.editQuestion.setEnableSizeCache(false);
        this.editQuestion.setMovementMethod(null);

        this.editCustomQuestion.setEnabled(true);
        this.editCustomQuestion.setFocusableInTouchMode(true);
        this.editCustomQuestion.setFocusable(true);
        this.editCustomQuestion.setEnableSizeCache(false);
        this.editCustomQuestion.setMovementMethod(null);
        this.editCustomQuestion.setFilters(fArray);

        this.editAnswer.setEnabled(true);
        this.editAnswer.setFocusableInTouchMode(true);
        this.editAnswer.setFocusable(true);
        this.editAnswer.setEnableSizeCache(false);
        this.editAnswer.setMovementMethod(null);
        this.editAnswer.setFilters(fArray);

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            if (modal)
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        JSONArray questions = FloozRestClient.getInstance().currentTexts.secretQuestions;

        try {
            List tmpList = JSONHelper.toList(questions);

            questionList = new ArrayList<>();
            Object item;
            for(int i = 0; i < tmpList.size(); i++) {
                item = tmpList.get(i);
                questionList.add((String)item);
            }

            questionList.add(instance.getResources().getString(R.string.CUSTOM_SECRET_QUESTION));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        editQuestion.setOnClickListener(v -> {

            final int previousQuestion = currentQuestion;

            CharSequence[] questionArray = questionList.toArray(new CharSequence[questionList.size()]);

            new AlertDialog.Builder(instance)
                    .setTitle(R.string.SETTINGS_SECRET)
                    .setSingleChoiceItems(questionArray, currentQuestion, (dialog, which) -> {
                        currentQuestion = which;
                    })
                    .setPositiveButton(R.string.GLOBAL_OK, (dialog, which) -> {
                        if (currentQuestion >= 0) {
                            InputMethodManager imm = (InputMethodManager) instance.getSystemService(Context.INPUT_METHOD_SERVICE);
                            editQuestion.setText(questionList.get(currentQuestion));
                            if (currentQuestion == questionList.size() - 1) {
                                editCustomQuestion.setVisibility(View.VISIBLE);
                                editCustomQuestion.requestFocus();

                                imm.showSoftInput(editCustomQuestion, InputMethodManager.SHOW_IMPLICIT);
                            } else {
                                editCustomQuestion.setVisibility(View.GONE);
                                editCustomQuestion.setText("");
                                editAnswer.requestFocus();

                                if (previousQuestion != currentQuestion)
                                    editAnswer.setText("");

                                imm.showSoftInput(editAnswer, InputMethodManager.SHOW_IMPLICIT);
                            }
                        }
                        validateView();
                    })
                    .setNegativeButton(R.string.GLOBAL_CANCEL, (dialog, which) -> {
                        currentQuestion = previousQuestion;
                    })
                    .setCancelable(false)
                    .show();
        });

        editCustomQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateView();
            }
        });

        editAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateView();
            }
        });

        infosEdit.setOnClickListener(v -> {
            needEditing = true;
            Intent intentNotifs = new Intent(instance, AuthenticationActivity.class);
            instance.startActivityForResult(intentNotifs, AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY);
            instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        });

        this.editSave.setOnClickListener(v -> {
            Map<String, Object> param = new HashMap<>();
            Map<String, Object> settings = new HashMap<>();
            Map<String, Object> secret = new HashMap<>();

            if (currentQuestion == questionList.size() - 1)
                secret.put("question", editCustomQuestion.getText());
            else
                secret.put("question", editQuestion.getText());

            secret.put("answer", editAnswer.getText());

            settings.put("secret", secret);
            param.put("settings", settings);

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().updateUser(param, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    headerBackButton.performClick();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

        Map settings = FloozRestClient.getInstance().currentUser.settings;

        if (settings.containsKey("secret")
                && ((Map) settings.get("secret")).containsKey("question")
                && !((String) ((Map) settings.get("secret")).get("question")).isEmpty()) {
            if (needEditing && authenticationValidate) {
                needEditing = false;
                infosView.setVisibility(View.GONE);
                editView.setVisibility(View.VISIBLE);
            } else {
                needEditing = false;
                infosQuestion.setText("“" + (((Map) FloozRestClient.getInstance().currentUser.settings.get("secret")).get("question")) + "”");
                infosView.setVisibility(View.VISIBLE);
                editView.setVisibility(View.GONE);
            }
        } else {
            if (needEditing && authenticationValidate) {
                needEditing = false;
                infosView.setVisibility(View.GONE);
                editView.setVisibility(View.VISIBLE);
            } else if (needEditing) {
                needEditing = false;
                headerBackButton.performClick();
            } else {
                needEditing = true;
                Intent intentNotifs = new Intent(instance, AuthenticationActivity.class);
                instance.startActivityForResult(intentNotifs, AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                editView.setVisibility(View.VISIBLE);
            }
        }

        this.headerTitle.requestFocus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY) {
            authenticationValidate = resultCode == Activity.RESULT_OK;
        }
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

    private void validateView() {
        boolean valid = true;

        if (currentQuestion == -1 || editQuestion.getText().toString().isEmpty())
            valid = false;
        else if (currentQuestion == questionList.size() - 1 && editCustomQuestion.getText().toString().isEmpty())
            valid = false;
        else if (editAnswer.getText().toString().isEmpty())
            valid = false;

        this.editSave.setEnabled(valid);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
