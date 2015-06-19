package me.flooz.app.Network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.*;

import com.facebook.HttpMethod;
import com.facebook.Session;
import com.facebook.SessionState;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.BuildConfig;
import me.flooz.app.Model.FLComment;
import me.flooz.app.Model.FLCreditCard;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLNotification;
import me.flooz.app.Model.FLReport;
import me.flooz.app.Model.FLTexts;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.UI.Activity.Settings.BankSettingsActivity;
import me.flooz.app.UI.Activity.Settings.CoordsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.CreditCardSettingsActivity;
import me.flooz.app.UI.Activity.Settings.IdentitySettingsActivity;
import me.flooz.app.UI.Activity.Settings.ProfileSettingsActivity;
import me.flooz.app.UI.Activity.ShareAppActivity;
import me.flooz.app.UI.Activity.StartActivity;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.NotificationsManager;

/**
 * Created by Flooz on 9/1/14.
 */
public class FloozRestClient
{
    public static String kUserData = "userData";
    public static String kPublicTimelineData = "publicTimelineData";
    public static String kFriendTimelineData = "friendTimelineData";
    public static String kPrivateTimelineData = "privateTimelineData";
    public static String kTextData = "textData";
    public static String kNotificationsData = "notifData";

    public enum FriendAction {
        Accept,
        Decline,
        Delete,
        Request
    }

    public enum VerifyAction {
        InvitationCode,
        Username
    }

    public FloozApplication floozApp;

    public FLTexts currentTexts = null;
    public FLUser currentUser = null;
    private String secureCode = null;
    private String accessToken = null;
    private String fbAccessToken= null;

    public static String customIpAdress = "http://dev.flooz.me";

    public SharedPreferences appSettings;

    private String BASE_URL;

    public NotificationsManager notificationsManager;

    private static FloozRestClient instance;

    private Socket socket;

    private AsyncHttpClient aHttpClient = new AsyncHttpClient();
    private AsyncHttpClient sHttpClient = new SyncHttpClient();

    public static FloozRestClient getInstance()
    {
        if (instance == null)
            instance = new FloozRestClient();
        return instance;
    }

    private ProgressDialog loadDialog;

    public FloozRestClient() {
        super();

        this.appSettings = FloozApplication.getAppContext().getSharedPreferences("FloozPrefs", Context.MODE_PRIVATE);

        this.secureCode =  this.appSettings.getString("secure_code", null);
        this.accessToken =  this.appSettings.getString("access_token", null);

        this.floozApp = (FloozApplication)FloozApplication.getAppContext().getApplicationContext();

        if (BuildConfig.LOCAL_API)
            this.BASE_URL = FloozRestClient.customIpAdress;
        else if (BuildConfig.DEBUG_API)
            this.BASE_URL = "http://dev.flooz.me";
        else
            this.BASE_URL = "https://api.flooz.me";

        this.notificationsManager = new NotificationsManager();

        this.aHttpClient.addHeader("Accept", "*/*");
        this.sHttpClient.addHeader("Accept", "*/*");

        if (!this.aHttpClient.isUrlEncodingEnabled())
            this.aHttpClient.setURLEncodingEnabled(true);

        if (!this.sHttpClient.isUrlEncodingEnabled())
            this.sHttpClient.setURLEncodingEnabled(true);
    }

    public enum HttpRequestType {
        GET,
        POST,
        DELETE,
        PUT
    }

    /***************************/
    /****  LOGIN / LOGOUT  *****/
    /***************************/

    public Boolean autologin() {
        if (this.accessToken == null || this.accessToken.isEmpty())
            return false;

        this.updateCurrentUser(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                updateNotificationFeed(null);
                FloozApplication.getInstance().didConnected();
                FloozApplication.getInstance().displayMainView();
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (statusCode != 442 && statusCode != 426) {
                    logout();
                } else if (statusCode != 426) {
                    loadUserData();
                    if (currentUser != null) {
                        FloozApplication.getInstance().didConnected();
                        FloozApplication.getInstance().displayMainView();
                    } else
                        logout();
                }
            }
        });
        return true;
    }

    public void loginForSecureCode(String pseudo, String password, final FloozHttpResponseHandler responseHandler) {
        final Map<String, Object> params = new HashMap<>();
        params.put("login", pseudo);
        params.put("password", password);
        params.put("codeReset", true);

        this.request("/login/basic", HttpRequestType.POST, params, responseHandler);
    }

    public void loginWithPseudoAndPassword(String pseudo, String password, final FloozHttpResponseHandler responseHandler) {
        final Map<String, Object> params = new HashMap<>();
        params.put("login", pseudo);
        params.put("password", password);

        this.request("/login/basic", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

                if (currentUser == null) {
                    currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                    appSettings.edit().putString("userId", currentUser.userId).apply();
                    initializeSockets();
                }
                else {
                    currentUser.setJson(responseObject.optJSONArray("items").optJSONObject(1));
                    appSettings.edit().putString("userId", currentUser.userId).apply();
                }

                saveUserData();

                checkDeviceToken();

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void loginWithFacebook(String fbToken) {
        Map<String, Object> param = new HashMap<>();

        param.put("accessToken", fbToken);

        this.request("/login/facebook", HttpRequestType.POST, param, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

                if (currentUser == null) {
                    currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                    appSettings.edit().putString("userId", currentUser.userId).apply();
                    initializeSockets();
                }
                else {
                    currentUser.setJson(responseObject.optJSONArray("items").optJSONObject(1));
                    appSettings.edit().putString("userId", currentUser.userId).apply();
                }

                saveUserData();

                checkDeviceToken();
                floozApp.didConnected();
                floozApp.displayMainView();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void loginWithPhone(String phone) {
        this.loginWithPhone(phone, null);
    }

    public void loginWithPhone(String phone, String coupon) {
        this.loginWithPhone(phone, coupon, null);
    }

    public void loginWithPhone(String phone, String coupon, FloozHttpResponseHandler responseHandler) {
        String formattedPhone = phone.replace(" ", "").replace(".", "").replace("-", "");

        if (formattedPhone.startsWith("+33"))
            formattedPhone = formattedPhone.replace("+33", "0");

        final Map<String, Object> params = new HashMap<>();
        params.put("q", formattedPhone);
        params.put("distinctId", FloozApplication.mixpanelAPI.getDistinctId());

        if (coupon != null && !coupon.isEmpty())
            params.put("coupon", coupon);

        this.showLoadView();
        this.request("/login", HttpRequestType.POST, params, responseHandler);
    }

    public void logout() {
        Map<String, Object> param = new HashMap<>();

        if (this.currentUser != null && this.currentUser.device != null) {
            param.put("device", this.currentUser.device);
            this.request("/users/logout", HttpRequestType.GET, param, null);
        }

        this.closeSockets();
        this.clearLogin();
        FloozApplication.getInstance().didDisconnected();
    }

    public void clearLogin() {
        this.currentUser = null;
        this.accessToken = null;

        SharedPreferences.Editor paramEditor = this.appSettings.edit();

        paramEditor.remove("userId");
        paramEditor.remove("access_token");
        paramEditor.apply();

        this.clearSecureCode();
        this.clearSaveData();
    }

    public void checkDeviceToken() {
        if (this.currentUser == null || floozApp.regid == null || floozApp.regid.isEmpty())
            return;
        if (this.currentUser.device != null && this.currentUser.device.contentEquals(floozApp.regid))
            return;

        Map<String, Object> params = new HashMap<>();
        params.put("device", floozApp.regid);

        this.updateUser(params, null);
    }

    /***************************/
    /******  CACHE DATA  *******/
    /***************************/

    public void loadUserData() {
        String userData = this.appSettings.getString(kUserData, null);
        if (userData != null) {
            try {
                JSONObject userJson = new JSONObject(userData);
                this.currentUser = new FLUser(userJson);
                if (userJson.has("fb"))
                    this.fbAccessToken = userJson.optJSONObject("fb").optString("token");

                this.checkDeviceToken();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadTextData() {
        String textData = this.appSettings.getString(kTextData, null);
        if (textData != null) {
            try {
                JSONObject textJson = new JSONObject(textData);
                this.currentTexts = new FLTexts(textJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public List<FLTransaction> loadTimelineData(FLTransaction.TransactionScope scope) {
        String dataKey = "";

        switch (scope) {
            case TransactionScopePublic:
                dataKey = kPublicTimelineData;
                break;
            case TransactionScopePrivate:
                dataKey = kPrivateTimelineData;
                break;
            case TransactionScopeFriend:
                dataKey = kFriendTimelineData;
                break;
        }

        String timelineData = this.appSettings.getString(dataKey, null);
        if (timelineData != null) {
            try {
                JSONArray timelineJson = new JSONArray(timelineData);
                return this.createTransactionArrayFromSaveData(timelineJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<FLNotification> loadNotificationData() {
        String notificationsData = this.appSettings.getString(kNotificationsData, null);
        if (notificationsData != null) {
            try {
                JSONArray notificationsJson = new JSONArray(notificationsData);
                return this.createNotificationArrayFromSaveData(notificationsJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void saveUserData() {
        this.appSettings.edit().putString(kUserData, currentUser.json.toString()).apply();
    }

    public void saveTextsData() {
        this.appSettings.edit().putString(kTextData, currentTexts.json.toString()).apply();
    }

    public void saveNotificationData(JSONArray notifs) {
        if (notifs != null)
            this.appSettings.edit().putString(kNotificationsData, notifs.toString()).apply();
    }

    public void saveTimelineData(FLTransaction.TransactionScope scope, JSONArray timeline) {
        if (timeline != null) {
            String dataKey = "";

            switch (scope) {
                case TransactionScopePublic:
                    dataKey = kPublicTimelineData;
                    break;
                case TransactionScopePrivate:
                    dataKey = kPrivateTimelineData;
                    break;
                case TransactionScopeFriend:
                    dataKey = kFriendTimelineData;
                    break;
            }

            this.appSettings.edit().putString(dataKey, timeline.toString()).apply();
        }
    }

    public void clearSaveData() {
        SharedPreferences.Editor tmpEditor = this.appSettings.edit();

        tmpEditor.remove(kUserData);
        tmpEditor.remove(kPublicTimelineData);
        tmpEditor.remove(kPrivateTimelineData);
        tmpEditor.remove(kFriendTimelineData);
        tmpEditor.remove(kTextData);
        tmpEditor.remove(kNotificationsData);
        tmpEditor.apply();
    }

    /***************************/
    /********  SIGNUP  *********/
    /***************************/

    public void signupPassStep(String step, Map<String, Object> params, FloozHttpResponseHandler responseHandler) {
        this.request("/signup/" + step, HttpRequestType.POST, params, responseHandler);
    }

    public void signup(final Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

        this.showLoadView();
        this.request("/users/signup", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                updateCurrentUserAfterSignup(responseObject);
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void verifyInvitationCode(String code, String phone, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>(1);

        param.put("phone", phone);
        param.put("distinctId", FloozApplication.mixpanelAPI.getDistinctId());

        this.globalVerify(VerifyAction.InvitationCode, code, param, responseHandler);
    }

    public void globalVerify(VerifyAction field, String value, Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {
        this.request("/utils/exists/" + this.verifyActionToParams(field) + "/" + value, HttpRequestType.GET, params, responseHandler);
    }

    public void sendSignupSMS(String phone) {

        Map<String, Object> param = new HashMap<>(1);
        param.put("phone", phone);

        this.request("/utils/smstoken", HttpRequestType.POST, param, null);
    }

    /***************************/
    /******  SECURE CODE  ******/
    /***************************/

    public String getSecureCode() {
        this.secureCode = this.appSettings.getString("secure_code", "");

        if (this.secureCode.length() < 4)
            return null;

        return this.secureCode;
    }

    public void setSecureCode(String code) {
        this.secureCode = code;

        this.appSettings.edit().putString("secure_code", code).apply();
    }

    public Boolean secureCodeIsValid(String code) {
        return code.contentEquals(this.getSecureCode());
    }

    public void clearSecureCode() {
        this.appSettings.edit().remove("secure_code").apply();
        this.secureCode = null;
    }

    public void checkSecureCodeForUser(final String code, final FloozHttpResponseHandler responseHandler) {
        this.request("/utils/asserts/secureCode/" + code, HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                setSecureCode(code);

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (getSecureCode() != null)
                    clearSecureCode();

                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    /***************************/
    /********  USERS  **********/
    /***************************/

    public void textObjectFromApi(final FloozHttpResponseHandler responseHandler) {
        this.request("/utils/texts", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject res = (JSONObject)response;
                currentTexts = new FLTexts(res.optJSONObject("item"));
                saveTextsData();

                if (responseHandler != null)
                    responseHandler.success(currentTexts);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (statusCode != 442) {
                    if (responseHandler != null)
                        responseHandler.failure(statusCode, error);
                } else {
                    loadTextData();
                    if (currentTexts != null) {
                        if (responseHandler != null)
                            responseHandler.success(currentTexts);
                    } else {
                        if (responseHandler != null)
                            responseHandler.failure(statusCode, error);
                    }
                }
            }
        });
    }

    public void updateUserPassword(Map<String, Object> passData, final FloozHttpResponseHandler responseHandler) {

        this.request("/users/password/change", HttpRequestType.POST, passData, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                checkDeviceToken();
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void passwordForget(String email, FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>();
        param.put("email", email);

        this.request("/users/password/lost", HttpRequestType.POST, param, responseHandler);
    }

    public void updateCurrentUserAfterSignup(JSONObject responseObject) {
        setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

        this.currentUser = new FLUser((responseObject.optJSONArray("items").optJSONObject(1)));
        this.appSettings.edit().putString("userId", currentUser.userId).commit();

        if (responseObject.optJSONArray("items").optJSONObject(1).has("fb"))
            fbAccessToken = responseObject.optJSONArray("items").optJSONObject(1).optJSONObject("fb").optString("token");

        checkDeviceToken();

        this.initializeSockets();
        floozApp.didConnected();
        this.sendUserContacts();
    }

    public void updateCurrentUser(final FloozHttpResponseHandler responseHandler) {
        this.request("/users/profile", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                if (responseObject.optJSONObject("item").has("fb")
                        && responseObject.optJSONObject("item").optJSONObject("fb") != null
                        && responseObject.optJSONObject("item").optJSONObject("fb").optString("token") != null)
                    fbAccessToken = responseObject.optJSONObject("item").optJSONObject("fb").optString("token");
                else
                    fbAccessToken = null;

                if (currentUser == null) {
                    currentUser = new FLUser(responseObject.optJSONObject("item"));
                    appSettings.edit().putString("userId", currentUser.userId).apply();
                    initializeSockets();
                }
                else {
                    currentUser.setJson(responseObject.optJSONObject("item"));
                    appSettings.edit().putString("userId", currentUser.userId).apply();
                    initializeSockets();
                }

                saveUserData();

                checkDeviceToken();

                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadCurrentUser());
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void updateUser(Map<String, Object> user, final FloozHttpResponseHandler responseHandler) {

        if (user.containsKey("birthdate"))
            user.put("birthdate", FLUser.formattedBirthdate((String)user.get("birthdate")));

        this.request("/users/profile", HttpRequestType.PUT, user, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                if (responseObject.optJSONObject("item").has("fb")
                        && responseObject.optJSONObject("item").optJSONObject("fb") != null
                        && responseObject.optJSONObject("item").optJSONObject("fb").optString("token") != null)
                    fbAccessToken = responseObject.optJSONObject("item").optJSONObject("fb").optString("token");
                else
                    fbAccessToken = null;

                currentUser.setJson(responseObject.optJSONObject("item"));
                appSettings.edit().putString("userId", currentUser.userId).apply();
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadCurrentUser());
                saveUserData();

                checkDeviceToken();

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void updateNotificationSettings(String canal, String type, Boolean value, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>();

        param.put("canal", canal);
        param.put("type", type);
        param.put("value", value);

        this.request("/users/alerts", HttpRequestType.PUT, param, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                updateCurrentUser(null);
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void uploadDocument(String field, File file, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>(2);

        params.put("field", field);
        params.put(field, file);

        this.request("/users/profile/upload", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                if (responseObject.optJSONObject("item").has("fb")
                        && responseObject.optJSONObject("item").optJSONObject("fb") != null
                        && responseObject.optJSONObject("item").optJSONObject("fb").optString("token") != null)
                    fbAccessToken = responseObject.optJSONObject("item").optJSONObject("fb").optString("token");

                currentUser.setJson(responseObject.optJSONObject("item"));
                appSettings.edit().putString("userId", currentUser.userId).apply();
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadCurrentUser());
                saveUserData();

                checkDeviceToken();

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });

    }

    public void blockUser(String userId, FloozHttpResponseHandler responseHandler) {
        this.request("/users/" + userId + "/block", HttpRequestType.GET, null, responseHandler);
    }

    public void sendSMSValidation() {
        this.request("/tokens/generate/phone", HttpRequestType.POST, null, null);
    }

    public void sendEmailValidation() {
        this.request("/tokens/generate/email", HttpRequestType.POST, null, null);
    }

    public void sendUserContacts() {
        class UploadContactTask extends AsyncTask<URL, Integer, Long> {

            protected Long doInBackground(URL... urls) {
                List<FLUser> list = ContactsManager.getContactsList();
                List<String> phones = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    phones.add(list.get(i).phone);
                }

                Map<String, Object> params = new HashMap<>(1);
                params.put("phones", phones);

                request("/users/contacts", HttpRequestType.POST, params, null);
                return null;
            }

        }

        new UploadContactTask().execute();
    }

    /***************************/
    /*********  OTHER  *********/
    /***************************/

    public void reportContent(FLReport report, FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>(3);
        param.put("type", report.convertReportTypeToParam());
        param.put("resourceId", report.resourceId);
        param.put("message", report.comment);

        this.request("/reports", HttpRequestType.POST, param, responseHandler);
    }

    public void invitationStrings(FloozHttpResponseHandler responseHandler) {
        this.request("/invitations/texts", HttpRequestType.GET, null, responseHandler);
    }

    /***************************/
    /******  CREDIT CARD  ******/
    /***************************/

    public void createCreditCard(String cardOwner, String cardNumber, String cardExpires, String cardCVV, Boolean signup, final FloozHttpResponseHandler responseHandler) {

        Map<String, Object> params = new HashMap<>(4);

        params.put("holder", cardOwner);
        params.put("number", cardNumber.replace(" ", ""));
        params.put("expires", cardExpires);
        params.put("cvv", cardCVV);

        String path = "/cards";

        if (signup)
            path += "?context=signup";

        this.request(path, HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

                JSONObject responseObject = (JSONObject)response;

                currentUser.setCreditCard(new FLCreditCard(responseObject.optJSONObject("item")));

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });

    }

    public void removeCreditCard(String creditCardId, FloozHttpResponseHandler responseHandler) {
        this.request("/cards/" + creditCardId, HttpRequestType.DELETE, null, responseHandler);
    }

    public void abort3DSecure() {
        this.request("/cards/3ds/abort", HttpRequestType.GET, null, null);
    }

    /***************************/
    /********  CASHOUT  ********/
    /***************************/

    public void cashoutValidate(FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>();

        param.put("validate", true);

        if (this.getSecureCode() != null)
            param.put("secureCode", this.getSecureCode());

        this.request("/cashouts", HttpRequestType.POST, param, responseHandler);
    }

    public void cashout(float amount, FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>();

        param.put("amount", amount);

        this.request("/cashouts", HttpRequestType.POST, param, responseHandler);
    }

    /***************************/
    /*****  TRANSACTIONS  ******/
    /***************************/

    public void transactionWithId(String transacId, FloozHttpResponseHandler responseHandler) {
        this.request("/flooz/" + transacId, HttpRequestType.GET, null, responseHandler);
    }

    public void timeline(final FLTransaction.TransactionScope scope, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", FLTransaction.transactionScopeToParams(scope));

        this.request("/flooz", HttpRequestType.GET, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject jsonResponse = (JSONObject) response;

                List<FLTransaction> transactions = createTransactionArrayFromResult(jsonResponse);
                saveTimelineData(scope, ((JSONObject)response).optJSONArray("items"));
                if (responseHandler != null) {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("transactions", transactions);
                    ret.put("nextUrl", jsonResponse.optString("next"));
                    responseHandler.success(ret);
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (statusCode != 442) {
                    if (responseHandler != null)
                        responseHandler.failure(statusCode, error);
                } else {
                    List<FLTransaction> transactions = loadTimelineData(scope);
                    if (transactions != null) {
                        if (responseHandler != null) {
                            Map<String, Object> ret = new HashMap<>();
                            ret.put("transactions", transactions);
                            ret.put("nextUrl", null);
                            responseHandler.success(ret);
                        }
                    } else {
                        if (responseHandler != null)
                            responseHandler.failure(statusCode, error);
                    }
                }
            }
        });
    }

    public void timelineNextPage(String nextPageUrl, final FloozHttpResponseHandler responseHandler) {

        this.request(nextPageUrl, HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject jsonResponse = (JSONObject)response;

                List<FLTransaction> transactions = createTransactionArrayFromResult(jsonResponse);

                if (responseHandler != null) {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("transactions", transactions);
                    ret.put("nextUrl", jsonResponse.optString("next"));
                    responseHandler.success(ret);
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void performTransaction(Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {
        this.request("/flooz", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(response);
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void updateTransactionValidate(FLTransaction transaction, FLTransaction.TransactionStatus status, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>();

        param.put("validate", true);
        param.put("state", FLTransaction.transactionStatusToParams(status));

        if (getSecureCode() != null) {
            param.put("secureCode", getSecureCode());
        }

        this.request("/flooz/" + transaction.transactionId, HttpRequestType.POST, param, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                updateCurrentUser(null);

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void uploadTransactionPic(String id, File image, FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("image", image);

        this.request("/flooz/" + id + "/pic", HttpRequestType.POST, params, responseHandler);
    }

    public void updateTransaction(FLTransaction transaction, FLTransaction.TransactionStatus status, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>(1);

        param.put("state", FLTransaction.transactionStatusToParams(status));

        this.request("/flooz/" + transaction.transactionId, HttpRequestType.POST, param, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                updateCurrentUser(null);

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    /***************************/
    /*******  SOCIAL  **********/
    /***************************/

    public void likeTransaction(String idTransaction, final FloozHttpResponseHandler responseHandler)
    {
        this.request("/social/likes/" + idTransaction, HttpRequestType.POST, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(((JSONObject) response).optString("item"));
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void commentTransaction(String idTransaction, String comment, final FloozHttpResponseHandler responseHandler)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("comment", comment);

        this.request("/social/comments/" + idTransaction, HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(new FLComment(((JSONObject) response).optJSONObject("item")));
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void loadFriendSuggestions(final FloozHttpResponseHandler responseHandler) {
        this.request("/friends/suggestion", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(createUserArrayFromResult((JSONObject) response));
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void sendFriendRequest(String userID, final String canal, final FloozHttpResponseHandler responseHandler) {

        Map<String, String> metrics = new HashMap<>(1);
        metrics.put("selectedFrom", canal);

        Map<String, Object> param = new HashMap<>(1);
        param.put("metrics", metrics);

        this.request("/friends/" + userID + "/" + this.friendActionToParams(FriendAction.Request), HttpRequestType.GET, param, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (canal.contentEquals("suggestion"))
                    FloozApplication.performLocalNotification(CustomNotificationIntents.reloadFriends());

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });

    }

    public void performActionOnFriend(String friendID, FriendAction action, final FloozHttpResponseHandler responseHandler) {
        this.request("/friends/" + friendID + "/" + this.friendActionToParams(action), HttpRequestType.GET, null, responseHandler);
    }

    public void searchUser(String searchString, Boolean newFLooz, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("q", searchString);

        String url = "/friends/search";

        if (newFLooz)
            url += "?context=newFlooz";

        this.request(url, HttpRequestType.GET, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(createUserArrayFromResult((JSONObject) response));
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    /***************************/
    /*****  NOTIFICATIONS  *****/
    /***************************/

    public void updateNotificationFeed(final FloozHttpResponseHandler responseHandler) {
        this.request("/feeds", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                saveNotificationData(((JSONObject)response).optJSONArray("items"));
                notificationsManager.setNotifications(createNotificationArrayFromResult((JSONObject)response));
                notificationsManager.setNextURL(((JSONObject)response).optString("next"));
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (statusCode != 442) {
                    if (responseHandler != null)
                        responseHandler.failure(statusCode, error);
                } else {
                    List<FLNotification> res = loadNotificationData();
                    if (res != null) {
                        notificationsManager.setNotifications(res);
                        notificationsManager.setNextURL("");
                        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
                        if (responseHandler != null)
                            responseHandler.success(res);
                    } else {
                        if (responseHandler != null)
                            responseHandler.failure(statusCode, error);
                    }
                }
            }
        });
    }

    public void loadNextNotfificationFeed(final FloozHttpResponseHandler responseHandler) {
        if (this.notificationsManager.nextURL == null || this.notificationsManager.nextURL.isEmpty())
            return;

        this.request(this.notificationsManager.nextURL, HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                notificationsManager.addNotifications(createNotificationArrayFromResult((JSONObject)response));
                notificationsManager.setNextURL(((JSONObject)response).optString("next"));
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void readAllNotifications(final FloozHttpResponseHandler responseHandler) {
        this.request("/feeds/read/all", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                updateNotificationFeed(null);
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void readNotification(String notifID, final FloozHttpResponseHandler responseHandler) {
        this.request("/feeds/read/" + notifID, HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                updateNotificationFeed(null);
                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void readFriendNotifications(final FloozHttpResponseHandler responseHandler) {
        this.request("/feeds/read/friend", HttpRequestType.GET, null, responseHandler);
    }

    /***************************/
    /********  TOOLS  **********/
    /***************************/

    private List<FLUser> createUserArrayFromResult(JSONObject jsonObject) {
        List<FLUser> ret = new ArrayList<>();
        JSONArray users = jsonObject.optJSONArray("items");

        if (users != null && users.length() > 0) {
            for (int i = 0; i < users.length(); i++)
                ret.add(new FLUser(users.optJSONObject(i)));
        }
        return ret;
    }

    private List<FLTransaction> createTransactionArrayFromResult(JSONObject jsonObject) {
        List<FLTransaction> ret = new ArrayList<>();
        JSONArray transactions = jsonObject.optJSONArray("items");

        if (transactions != null && transactions.length() > 0) {
            for (int i = 0; i < transactions.length(); i++) {
                FLTransaction tmp = new FLTransaction(transactions.optJSONObject(i));
                if (tmp.transactionId != null && tmp.text3d != null)
                    ret.add(tmp);
            }
        }

        return ret;
    }

    private List<FLTransaction> createTransactionArrayFromSaveData(JSONArray transactions) {
        List<FLTransaction> ret = new ArrayList<>();

        if (transactions != null && transactions.length() > 0) {
            for (int i = 0; i < transactions.length(); i++) {
                FLTransaction tmp = new FLTransaction(transactions.optJSONObject(i));
                if (tmp.transactionId != null && tmp.text3d != null)
                    ret.add(tmp);
            }
        }

        return ret;
    }

    private List<FLNotification> createNotificationArrayFromResult(JSONObject jsonObject) {
        List<FLNotification> ret = new ArrayList<>();
        JSONArray notifications = jsonObject.optJSONArray("items");

        if (notifications != null && notifications.length() > 0) {
            for (int i = 0; i < notifications.length(); i++)
                ret.add(new FLNotification(notifications.optJSONObject(i)));
        }

        return ret;
    }

    private List<FLNotification> createNotificationArrayFromSaveData(JSONArray notifications) {
        List<FLNotification> ret = new ArrayList<>();

        if (notifications != null && notifications.length() > 0) {
            for (int i = 0; i < notifications.length(); i++)
                ret.add(new FLNotification(notifications.optJSONObject(i)));
        }

        return ret;
    }

    private String friendActionToParams(FriendAction action) {
        String ret = null;

        switch (action) {
            case Accept:
                ret = "accept";
                break;
            case Decline:
                ret = "decline";
                break;
            case Delete:
                ret = "delete";
                break;
            case Request:
                ret = "request";
                break;
        }
        return ret;
    }

    private String verifyActionToParams(VerifyAction action) {
        String ret = null;

        switch (action) {
            case InvitationCode:
                ret = "coupon";
                break;
            case Username:
                ret = "nick";
                break;
        }
        return ret;
    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }

    private void setNewAccessToken(String token) {
        this.accessToken = token;

        this.appSettings.edit().putString("access_token", token).apply();
    }

    public void showLoadView() {
        if (this.loadDialog == null || !this.loadDialog.isShowing()) {
            this.loadDialog = new ProgressDialog(this.floozApp.getCurrentActivity());
            this.loadDialog.setCancelable(false);
            this.loadDialog.setCanceledOnTouchOutside(false);
            this.loadDialog.setMessage(this.floozApp.getResources().getString(R.string.GLOBAL_WAIT));
            this.loadDialog.show();
        }
    }

    public void hideLoadView() {
        if (this.loadDialog != null && this.loadDialog.isShowing())
            this.loadDialog.dismiss();
    }

    /***************************/
    /*******  REQUEST  *********/
    /***************************/

    private void request(String path, HttpRequestType type, Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

        ConnectivityManager conMgr = (ConnectivityManager) floozApp.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            if (this.accessToken != null && !this.accessToken.isEmpty())
            {
                if (path.indexOf('?') == -1) {
                    path = path + "?token=" + this.accessToken;
                }
                else if (!path.contains("token=")) {
                    path = path + "&token=" + this.accessToken;
                }
            }

            if (!path.contains("?via=android") && !path.contains("&via=android")) {
                if (path.indexOf('?') == -1) {
                    path = path + "?via=android";
                } else {
                    path = path + "&via=android";
                }
            }

            if (FloozApplication.getAppVersionName(floozApp.getApplicationContext()) != null && !path.contains("?version=") && !path.contains("&version=")) {
                if (path.indexOf('?') == -1) {
                    path = path + "?version=" + FloozApplication.getAppVersionName(floozApp.getApplicationContext());
                }
                else {
                    path = path + "&version=" + FloozApplication.getAppVersionName(floozApp.getApplicationContext());
                }
            }

            path += "&api=v2";

            final JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadView();

                            if (responseHandler != null)
                                responseHandler.success(response);

                            if(response.has("popup"))

                            {
                                FLError errorContent = new FLError(response.optJSONObject("popup"));
                                CustomToast.show(FloozApplication.getAppContext(), errorContent);
                            }

                            handleRequestTriggers(response);
                        }
                    });
                }

                @Override
                public void onFailure(final int statusCode, Header[] headers, Throwable throwable, final JSONObject errorResponse) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadView();

                            if (errorResponse != null) {
                                handleRequestTriggers(errorResponse);

                                if (statusCode != 426) {
                                    if (errorResponse.has("popup")) {
                                        FLError errorContent = new FLError(errorResponse.optJSONObject("popup"));
                                        CustomToast.show(FloozApplication.getAppContext(), errorContent);

                                        if (responseHandler != null)
                                            responseHandler.failure(statusCode, errorContent);
                                    } else if (responseHandler != null)
                                        responseHandler.failure(statusCode, null);
                                } else {
                                    handleRequestTriggers(errorResponse);

                                    if (errorResponse.has("popup")) {
                                        FLError errorContent = new FLError(errorResponse.optJSONObject("popup"));
                                        CustomToast.show(FloozApplication.getAppContext(), errorContent);
                                    }
                                }
                            }
                        }
                    });
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String errorMsg, Throwable throwable) {
                    if (responseHandler != null)
                        responseHandler.failure(statusCode, null);
                }
            };

            RequestParams requestParams = new RequestParams();
            Boolean fileUpload = false;

            if (params != null && !params.isEmpty()) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof File)
                        try {
                            requestParams.put(entry.getKey(), (File)entry.getValue(), "image/jpeg");
                            fileUpload = true;
                        } catch(FileNotFoundException ignored) {

                        }
                    else
                        requestParams.put(entry.getKey(), entry.getValue());
                }
            }

            HttpEntity entity = null;
            try {
                entity = new StringEntity("");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if (params != null) {
                if (!fileUpload) {
                    JSONObject jsonParams;
                    try {
                        jsonParams = (JSONObject) JSONHelper.toJSON(params);
                        StringEntity sEntity = null;
                        try {
                            sEntity = new StringEntity(jsonParams.toString(), HTTP.UTF_8);
                            sEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        entity = sEntity;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        entity = requestParams.getEntity(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            AsyncHttpClient httpClient;

            if (Looper.myLooper() == null) {
                httpClient = this.sHttpClient;
            } else {
                httpClient = this.aHttpClient;
            }

            switch (type) {
                case GET:
                    httpClient.get(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                    break;
                case POST:
                    if (fileUpload)
                        httpClient.post(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                    else {
                        assert entity != null;
                        httpClient.post(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), entity, entity.getContentType().getValue(), jsonHttpResponseHandler);
                    }
                    break;
                case DELETE:
                    httpClient.delete(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), jsonHttpResponseHandler);
                    break;
                case PUT:
                    if (fileUpload)
                        httpClient.put(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                    else {
                        assert entity != null;
                        httpClient.put(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), entity, entity.getContentType().getValue(), jsonHttpResponseHandler);
                    }
                    break;
            }
        } else {
            this.hideLoadView();
            if (type != HttpRequestType.GET) {
                FLError error = new FLError();
                error.title = floozApp.getApplicationContext().getResources().getString(R.string.NETWORK_UNAVAILABLE_TITLE);
                error.text = floozApp.getApplicationContext().getResources().getString(R.string.NETWORK_UNAVAILABLE_TEXT);
                error.type = FLError.ErrorType.ErrorTypeError;
                error.time = 5;

                CustomToast.show(floozApp.getApplicationContext(), error);
            }

            if (responseHandler != null)
                responseHandler.failure(442, null);
        }
    }

    /***************************/
    /*******  FACEBOOK  ********/
    /***************************/

    public Boolean isConnectedToFacebook() {
        return this.fbAccessToken != null && !this.fbAccessToken.isEmpty();
    }

    private void facebookSessionStateChanged(Session session, SessionState state, Exception exception) {
        this.hideLoadView();

        if (exception != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }

        if (exception == null && state == SessionState.OPENED) {
            this.didConnectFacebook();
        }
    }

    public void connectFacebook() {
        if (Session.getActiveSession() != null)
            Session.getActiveSession().closeAndClearTokenInformation();

        Session.openActiveSession(floozApp.getCurrentActivity(), true, Arrays.asList("public_profile", "email", "user_friends"), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                facebookSessionStateChanged(session, state, exception);
            }
        });
    }

    public void disconnectFacebook() {
        if (Session.getActiveSession() != null)
            Session.getActiveSession().closeAndClearTokenInformation();

        this.fbAccessToken = null;

        Map<String, Object> data = new HashMap<>(1);
        data.put("fb", "");

        this.updateUser(data, null);
    }

    public void didConnectFacebook() {
        this.fbAccessToken =  Session.getActiveSession().getAccessToken();

        if (this.currentUser != null) {
            Bundle params = new Bundle();
            params.putString("fields", "id,email,first_name,last_name,name");
            com.facebook.Request request = new com.facebook.Request(Session.getActiveSession(), "me", params, HttpMethod.GET, new com.facebook.Request.Callback() {
                @Override
                public void onCompleted(com.facebook.Response response) {
                    hideLoadView();

                    if (response.getError() == null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("email", response.getGraphObject().getProperty("email"));
                        data.put("id", response.getGraphObject().getProperty("id"));
                        data.put("firstName", response.getGraphObject().getProperty("first_name"));
                        data.put("lastName", response.getGraphObject().getProperty("last_name"));
                        data.put("name", response.getGraphObject().getProperty("name"));
                        data.put("token", (fbAccessToken != null ? fbAccessToken : ""));

                        Map<String, Object> tmp = new HashMap<>();
                        tmp.put("fb", data);

                        updateUser(tmp, null);
                    }
                }
            });
            request.executeAsync();
        }
        else {
            Bundle params = new Bundle();
            params.putString("fields", "id,email,first_name,last_name,name");
            com.facebook.Request request = new com.facebook.Request(Session.getActiveSession(), "me", params, HttpMethod.GET, response -> {
                hideLoadView();

                if (response.getError() == null) {
                    loginWithFacebook(fbAccessToken);
                }
            });
            request.executeAsync();
        }
    }

    /***************************/
    /*******  TRIGGERS  ********/
    /***************************/

    private void handleTriggerTimelineReload() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
    }

    private void handleTriggerLineShow(JSONObject data) {
        try {
            final Activity tmp = floozApp.getCurrentActivity();
            if (tmp instanceof HomeActivity) {
                if (data.has("_id")) {
                    this.showLoadView();
                    this.transactionWithId(data.optString("_id"), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                            ((HomeActivity) tmp).showTransactionCard(transac);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                        }
                    });
                }
            }
        } catch (ClassCastException e) {

        }
    }

    private void handleTriggerAvatarShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingLeftMenu());
            ((HomeActivity)floozApp.getCurrentActivity()).leftMenu.userView.performClick();
        }
    }

    private void handleTriggerProfileReload() {
        this.updateCurrentUser(null);
    }

    private void handleTriggerCardShow() {
        if (!(floozApp.getCurrentActivity() instanceof CreditCardSettingsActivity)) {
            FloozRestClient.getInstance().updateCurrentUser(null);
            Intent intent = new Intent(floozApp.getCurrentActivity(), CreditCardSettingsActivity.class);
            intent.putExtra("modal", true);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerFriendReload() {
        this.updateCurrentUser(null);
    }

    private void handleTriggerFriendShow() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingRightMenu());
    }

    private void handleTriggerProfileShow() {
        if (!(floozApp.getCurrentActivity() instanceof ProfileSettingsActivity)) {
            FloozRestClient.getInstance().updateCurrentUser(null);
            Intent intent = new Intent(floozApp.getCurrentActivity(), ProfileSettingsActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerTransactionReload() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
    }

    private void handleTriggerLoginShow(JSONObject data) {
        handleTriggerLogout();
    }

    private void handleTriggerSignupShow(JSONObject data) {
        if (floozApp.getCurrentActivity() instanceof StartActivity) {
            StartActivity activity = (StartActivity) floozApp.getCurrentActivity();

            try {
                Map<String, Object> fbData = JSONHelper.toMap(data);
                Map<String, Object> userData = new HashMap<>();

                fbData.remove("type");

                userData.put("fb", fbData);

                if (fbData.containsKey("email"))
                    userData.put("email", fbData.get("email"));

                if (fbData.containsKey("lastName"))
                    userData.put("lastName", fbData.get("lastName"));

                if (fbData.containsKey("firstName"))
                    userData.put("firstName", fbData.get("firstName"));

                if (fbData.containsKey("id"))
                    userData.put("avatarURL", "https://graph.facebook.com/" + fbData.get("id") + "/picture?width=360&height=360");

                activity.updateUserData(userData);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void handleTriggerSignupCodeShow(final JSONObject data) {

    }

    private void handleTriggerLogout() {
        this.logout();
    }

    private void handleTriggerAppUpdate(final JSONObject data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(floozApp.getCurrentActivity());
        builder.setTitle(R.string.GLOBAL_UPDATE);
        builder.setMessage(R.string.MSG_UPDATE);
        builder.setPositiveButton(R.string.BTN_UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Activity tmp = floozApp.getCurrentActivity();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(data.optString("uri")));
                tmp.startActivity(i);
                tmp.finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void handleTriggerContactInfoShow() {
        if (!(floozApp.getCurrentActivity() instanceof CoordsSettingsActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), CoordsSettingsActivity.class);
            intent.putExtra("modal", true);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerUserIdentityShow() {
        if (!(floozApp.getCurrentActivity() instanceof IdentitySettingsActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), IdentitySettingsActivity.class);
            intent.putExtra("modal", true);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTrigger3DSecureShow(JSONObject data) {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
//            HomeActivity tmp = (HomeActivity)floozApp.getCurrentActivity();
//            ((CreditCardSettingsFragment)tmp.contentFragments.get("settings_credit_card")).next3DSecure = true;
//            ((CreditCardSettingsFragment)tmp.contentFragments.get("settings_credit_card")).secureData = data.optString("html");
        }
    }

    private void handleTrigger3DSecureComplete() {
        Handler handler = new Handler(floozApp.getMainLooper());
        handler.post(() -> updateCurrentUser(null));

        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
//            ((Secure3DFragment)((HomeActivity)floozApp.getCurrentActivity()).contentFragments.get("settings_3ds")).dismiss();
        }
    }

    private void handleTrigger3DSecureFail() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
//            ((Secure3DFragment)((HomeActivity)floozApp.getCurrentActivity()).contentFragments.get("settings_3ds")).dismiss();
        }
    }

    private void handleTriggerResetPassword(JSONObject data) {

    }

    private void handleTriggerClearSecureCode() {
        this.clearSecureCode();
    }

    private void handleTriggerCheckSecureCode() {
        this.checkSecureCodeForUser(getSecureCode(), null);
    }

    private void handleTriggerPresetLine(JSONObject data) {
        Intent intent = new Intent(floozApp.getApplicationContext(), NewTransactionActivity.class);
        intent.putExtra("preset", data.toString());
        Activity tmpActivity = floozApp.getCurrentActivity();
        tmpActivity.startActivity(intent);
        tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
    }

    private void handleTriggerReadFeed(JSONObject data) {
        this.readNotification(data.optString("_id"), null);
    }

    private void handleTriggerInvitationShow() {
        if (!(floozApp.getCurrentActivity() instanceof ShareAppActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), ShareAppActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerFeedReload() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
    }

    private void handleTriggerHttpCall(JSONObject data) {
        HttpRequestType method;

        switch (data.optString("method")) {
            case "GET":
                method = HttpRequestType.GET;
                break;
            case "POST":
                method = HttpRequestType.POST;
                break;
            case "PUT":
                method = HttpRequestType.PUT;
                break;
            case "DELETE":
                method = HttpRequestType.DELETE;
                break;
            default:
                method = HttpRequestType.GET;
                break;
        }

        Map param = null;

        if (data.has("body")) {
            try {
                param = JSONHelper.toMap(data.optJSONObject("body"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String url = data.optString("url");

        if (url.charAt(0) != '/')
            url = "/" + url;

        this.request(url, method, param, null);
    }

    private void handleTriggerPopupShow(JSONObject data) {
        Handler handler = new Handler(floozApp.getMainLooper());
        handler.post(() -> {
            final Dialog dialog = new Dialog(floozApp.getCurrentActivity());

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog_balance);

            TextView title = (TextView) dialog.findViewById(R.id.dialog_wallet_title);
            title.setTypeface(CustomFonts.customContentRegular(floozApp.getCurrentActivity()), Typeface.BOLD);
            title.setText(data.optString("title"));

            TextView text = (TextView) dialog.findViewById(R.id.dialog_wallet_msg);
            text.setTypeface(CustomFonts.customContentRegular(floozApp.getCurrentActivity()));
            text.setText(data.optString("content"));

            Button close = (Button) dialog.findViewById(R.id.dialog_wallet_btn);
            if (data.has("button") && !data.optString("button").isEmpty())
                close.setText(data.optString("button"));

            close.setOnClickListener(v -> {
                dialog.dismiss();
                handleRequestTriggers(data);
            });

            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });
    }

    private void handleTriggerHomeShow() {
        Handler handler = new Handler(floozApp.getMainLooper());
        handler.post(floozApp::displayMainView);
    }

    private void handleTriggerIbanShow() {
        if (!(floozApp.getCurrentActivity() instanceof BankSettingsActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), BankSettingsActivity.class);
            intent.putExtra("modal", true);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    public void handleTrigger(final FLTrigger trigger) {
        if (trigger == null)
            return;

        switch (trigger.type) {
            case TriggerNone:
                break;
            case TriggerReloadTimeline:
                handleTriggerTimelineReload();
                break;
            case TriggerShowLine:
                handleTriggerLineShow(trigger.data);
                break;
            case TriggerShowAvatar:
                handleTriggerAvatarShow();
                break;
            case TriggerReloadProfile:
                handleTriggerProfileReload();
                break;
            case TriggerShowCard:
                handleTriggerCardShow();
                break;
            case TriggerReloadFriend:
                handleTriggerFriendReload();
                break;
            case TriggerShowProfile:
                handleTriggerProfileShow();
                break;
            case TriggerShowFriend:
                handleTriggerFriendShow();
                break;
            case TriggerReloadLine:
                handleTriggerTransactionReload();
                break;
            case TriggerShowLogin:
                handleTriggerLoginShow(trigger.data);
                break;
            case TriggerShowSignup:
                handleTriggerSignupShow(trigger.data);
                break;
            case TriggerShowSignupCode:
                handleTriggerSignupCodeShow(trigger.data);
                break;
            case TriggerLogout:
                handleTriggerLogout();
                break;
            case TriggerAppUpdate:
                handleTriggerAppUpdate(trigger.data);
                break;
            case TriggerShowContactInfo:
                handleTriggerContactInfoShow();
                break;
            case TriggerShowUserIdentity:
                handleTriggerUserIdentityShow();
                break;
            case TriggerShow3DSecure:
                handleTrigger3DSecureShow(trigger.data);
                break;
            case TriggerComplete3DSecure:
                handleTrigger3DSecureComplete();
                break;
            case TriggerResetPassword:
                handleTriggerResetPassword(trigger.data);
                break;
            case TriggerFail3DSecure:
                handleTrigger3DSecureFail();
                break;
            case TriggerSecureCodeClear:
                handleTriggerClearSecureCode();
                break;
            case TriggerSecureCodeCheck:
                handleTriggerCheckSecureCode();
                break;
            case TriggerPresetLine:
                handleTriggerPresetLine(trigger.data);
                break;
            case TriggerFeedRead:
                handleTriggerReadFeed(trigger.data);
                break;
            case TriggerShowInvitation:
                handleTriggerInvitationShow();
                break;
            case TriggerFeedReload:
                handleTriggerFeedReload();
                break;
            case TriggerShowPopup:
                handleTriggerPopupShow(trigger.data);
                break;
            case TriggerHttpCall:
                handleTriggerHttpCall(trigger.data);
                break;
            case TriggerShowIban:
                handleTriggerIbanShow();
                break;
            case TriggerShowHome:
                handleTriggerHomeShow();
                break;
            default:
                break;
        }
    }

    public void handleRequestTriggers(JSONObject responseObject) {
        if (FloozApplication.appInForeground) {
            if (responseObject != null && responseObject.has("triggers")) {
                JSONArray t = responseObject.optJSONArray("triggers");

                Boolean canExecute = true;
                Class handlerClass = null;

                for (int i = 0; i < t.length(); i++) {
                    final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                    if (trigger.handlerClass != null && !this.floozApp.getCurrentActivity().getClass().isAssignableFrom(trigger.handlerClass)) {
                        canExecute = false;
                        handlerClass = trigger.handlerClass;
                        break;
                    }
                }

                if (canExecute) {
                    for (int i = 0; i < t.length(); i++) {
                        final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                        if (trigger.delay.intValue() > 0) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(() -> handleTrigger(trigger), (int) (trigger.delay.doubleValue() * 1000));
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> handleTrigger(trigger));
                        }
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this.floozApp, handlerClass);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("inApp", true);
                    intent.putExtra("triggers", t.toString());
                    Activity tmpActivity = floozApp.getCurrentActivity();
                    tmpActivity.startActivity(intent);
                    tmpActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        }
    }

    /***************************/
    /******  SOCKET IO  ********/
    /***************************/

    public void initializeSockets() {
        if (this.currentUser != null) {
            try {
                if (this.socket == null) {
                    IO.Options options = new IO.Options();
                    options.transports = new String[]{WebSocket.NAME};
                    options.reconnection = false;
                    options.reconnectionAttempts = 2;
                    options.timeout = 3000;
                    options.port = 80;
                    options.secure = false;

                    String url = BASE_URL;

                    if (!BuildConfig.DEBUG_API)
                        url = "http://api.flooz.me";

                    this.socket = IO.socket(url, options);
                    this.socket.on(Socket.EVENT_CONNECT, args -> {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("nick", currentUser.username);
                            obj.put("token", accessToken);
                            socket.emit("session start", obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }).on("event", args -> {
                        JSONObject data = (JSONObject) args[0];
                        if (data.has("popup")) {
                            FLError errorContent = new FLError(data.optJSONObject("popup"));
                            CustomToast.show(FloozApplication.getAppContext(), errorContent);
                        }
                        handleRequestTriggers(data);
                    }).on("session end", args -> socket.disconnect()).on("feed", args -> {
                        JSONObject data = (JSONObject) args[0];

                        Integer badgeNb = data.optInt("total");
                        FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications = badgeNb;
                        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
                    }).on(Socket.EVENT_DISCONNECT, args -> Log.d("SocketIO", "Socket Disconnected")).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.d("SocketIO", "error: " + args[0].toString());
                        }
                    }).on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
                        Log.d("SocketIO", "Socket Timeout");
                        socket.off();
                        socket = null;
                    });
                }
                if (!this.socket.connected())
                    this.socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void socketSendSessionEnd() {
        if (this.socket != null && this.accessToken != null && this.socket.connected()) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("nick", currentUser.username);
                obj.put("token", accessToken);
                this.socket.emit("session end", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeSockets() {
        if (this.socket != null && this.socket.connected() && this.currentUser != null) {
            this.socketSendSessionEnd();
        }
    }
}
