package me.flooz.app.Network;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import cz.msebera.android.httpclient.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpStatus;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.Console;
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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.BuildConfig;
import me.flooz.app.Model.FLComment;
import me.flooz.app.Model.FLCreditCard;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLNotification;
import me.flooz.app.Model.FLReport;
import me.flooz.app.Model.FLShareText;
import me.flooz.app.Model.FLTexts;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.FriendsActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.UI.Activity.NotificationActivity;
import me.flooz.app.UI.Activity.Secure3DActivity;
import me.flooz.app.UI.Activity.Settings.BankSettingsActivity;
import me.flooz.app.UI.Activity.Settings.IdentitySettingsActivity;
import me.flooz.app.UI.Activity.Settings.CreditCardSettingsActivity;
import me.flooz.app.UI.Activity.Settings.DocumentsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.SetSecureCodeActivity;
import me.flooz.app.UI.Activity.ShareAppActivity;
import me.flooz.app.UI.Activity.StartActivity;
import me.flooz.app.UI.Activity.ValidateSMSActivity;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.UI.View.CustomDialog;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.DeviceManager;
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
    public static String kShareData = "shareData";
    public static String kNotificationsData = "notifData";
    public static String kLocationData = "locationData";
    public static String kLlData = "llData";

    public enum FriendAction {
        Accept,
        Decline,
        Delete,
        Request,
        Follow,
        Unfollow
    }

    public FloozApplication floozApp;

    public CallbackManager fbLoginCallbackManager;

    public FLTexts currentTexts = null;
    public FLShareText currentShareText = null;
    public FLUser currentUser = null;
    public String secureCode = null;
    public String accessToken = null;
    public String fbAccessToken= null;

    public static String customIpAdress = "http://dev.flooz.me";

    public SharedPreferences appSettings;

    private String BASE_URL;

    public NotificationsManager notificationsManager;

    private static FloozRestClient instance;

    private Socket socket;

    private AsyncHttpClient aHttpClient = new AsyncHttpClient();
    private AsyncHttpClient sHttpClient = new SyncHttpClient();

    private DeviceManager deviceManager;

    private Handler socketHandler = new Handler(Looper.getMainLooper());

    private Runnable socketCloseRunnable = new Runnable() {
        @Override
        public void run() {
            if (socket != null && socket.connected() && currentUser != null) {
                socketSendSessionEnd();
            }
        }
    };

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
        this.deviceManager = new DeviceManager(FloozApplication.getAppContext());
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

        String path = "/users/login";

        path += "?os=" + Build.VERSION.RELEASE;

        path += "&mo=" + this.deviceManager.getDeviceName();

        path += "&uuid=" + this.deviceManager.getDeviceUuid();

        this.request(path, HttpRequestType.POST, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

                JSONObject responseObject = (JSONObject)response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

                currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                appSettings.edit().putString("userId", currentUser.userId).apply();

                saveUserData();

                checkDeviceToken();
                floozApp.didConnected();
                floozApp.displayMainView();

                updateCurrentUser(null);
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

        String path = "/users/login";

        path += "?os=" + Build.VERSION.RELEASE;

        path += "&mo=" + this.deviceManager.getDeviceName();

        path += "&uuid=" + this.deviceManager.getDeviceUuid();

        this.request(path, HttpRequestType.POST, params, responseHandler);
    }

    public void loginWithPseudoAndPassword(String pseudo, String password, final FloozHttpResponseHandler responseHandler) {
        final Map<String, Object> params = new HashMap<>();
        params.put("login", pseudo);
        params.put("password", password);

        String path = "/users/login";

        path += "?os=" + Build.VERSION.RELEASE;

        path += "&mo=" + this.deviceManager.getDeviceName();

        path += "&uuid=" + this.deviceManager.getDeviceUuid();

        this.request(path, HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

                currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                appSettings.edit().putString("userId", currentUser.userId).apply();
                initializeSockets();


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

        String path = "/users/facebook";

        path += "?os=" + Build.VERSION.RELEASE;

        path += "&mo=" + this.deviceManager.getDeviceName();

        path += "&uuid=" + this.deviceManager.getDeviceUuid();

        this.request(path, HttpRequestType.POST, param, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject) response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

                currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                appSettings.edit().putString("userId", currentUser.userId).apply();


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

    public void logout() {
        Map<String, Object> param = new HashMap<>();

        if (this.currentUser != null && this.currentUser.device != null) {
            param.put("device", this.currentUser.device);
            this.request("/users/logout", HttpRequestType.GET, param, null);
        }

        this.socketSendSessionEnd();
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
                if (userJson.has("fb")) {
                    this.updateFBToken(userJson.optJSONObject("fb").optString("token"));
                }

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

    public void loadShareData() {
        String textData = this.appSettings.getString(kShareData, null);
        if (textData != null) {
            try {
                JSONObject textJson = new JSONObject(textData);
                this.currentShareText = new FLShareText(textJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String loadLlData() {
        return this.appSettings.getString(kLlData, null);
    }

    public JSONArray loadLocationData() {
        String locationData = this.appSettings.getString(kLocationData, null);
        if (locationData != null) {
            try {
                JSONArray locationJson = new JSONArray(locationData);
                return locationJson;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    public void saveShareData() {
        this.appSettings.edit().putString(kShareData, currentShareText.json.toString()).apply();
    }
    public void saveNotificationData(JSONArray notifs) {
        if (notifs != null)
            this.appSettings.edit().putString(kNotificationsData, notifs.toString()).apply();
    }

    public void saveLocationData(JSONArray locations) {
        if (locations != null)
            this.appSettings.edit().putString(kLocationData, locations.toString()).apply();
    }

    public void saveLlData(String ll) {
        if (ll != null)
            this.appSettings.edit().putString(kLlData, ll).apply();
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
        tmpEditor.remove(kLocationData);
        tmpEditor.remove(kLlData);
        tmpEditor.apply();
    }

    public void clearLocationData() {
        SharedPreferences.Editor tmpEditor = this.appSettings.edit();

        tmpEditor.remove(kLocationData);
        tmpEditor.remove(kLlData);
        tmpEditor.apply();
    }

    /***************************/
    /********  SIGNUP  *********/
    /***************************/

    public void signupPassStep(String step, Map<String, Object> params, FloozHttpResponseHandler responseHandler) {
        String path = "/signup/" + step;

        path += "?os=" + Build.VERSION.RELEASE;

        path += "&mo=" + this.deviceManager.getDeviceName();

        path += "&uuid=" + this.deviceManager.getDeviceUuid();

        this.request(path, HttpRequestType.POST, params, responseHandler);
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

        if (this.secureCode == null || this.secureCode.length() < 4)
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

    public void checkSMSForUser(final String code, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("field", "phone");
        params.put("value", code);

        this.request("/utils/asserts", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
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

    public void checkSecureCodeForUser(final String code, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("field", "secureCode");
        params.put("value", code);

        this.request("/utils/asserts", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
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

    public void sendDiscountCode(String code, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);

        this.request("/users/promo", HttpRequestType.POST, params, responseHandler);
    }


    public void sendInvitationMetric(String channel) {
        Map<String, Object> params = new HashMap<>();
        params.put("canal", channel);

        this.request("/invitations/callback", HttpRequestType.GET, params, null);
    }

    public void invitationFacebook(String message, final FloozHttpResponseHandler responseHandler) {

        Map<String, Object> params = new HashMap<>();
        params.put("message", message);

        this.request("/invitations/facebook", HttpRequestType.POST, params, responseHandler);
    }

    public void getInvitationText(final FloozHttpResponseHandler responseHandler) {
        this.request("/invitations/text", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject res = (JSONObject)response;
                currentShareText = new FLShareText(res.optJSONObject("item"));
                saveShareData();

                if (responseHandler != null)
                    responseHandler.success(currentTexts);

                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadInvitation());
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (statusCode != 442) {
                    if (responseHandler != null)
                        responseHandler.failure(statusCode, error);
                } else {
                    loadShareData();
                    if (currentShareText != null) {
                        if (responseHandler != null)
                            responseHandler.success(currentShareText);
                    } else {
                        if (responseHandler != null)
                            responseHandler.failure(statusCode, error);
                    }
                }
            }
        });
    }

    public void textObjectFromApi(final FloozHttpResponseHandler responseHandler) {
        loadTextData();
        if (currentTexts != null && responseHandler != null)
            responseHandler.success(currentTexts);

        this.request("/utils/texts", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject res = (JSONObject)response;
                currentTexts = new FLTexts(res.optJSONObject("item"));
                saveTextsData();

                if (responseHandler != null)
                    responseHandler.success(currentTexts);

                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadText());
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

        if (responseObject.optJSONArray("items").optJSONObject(1).has("fb")) {
            updateFBToken(responseObject.optJSONArray("items").optJSONObject(1).optJSONObject("fb").optString("token"));
        }

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

                currentUser = new FLUser(responseObject.optJSONObject("item"));
                appSettings.edit().putString("userId", currentUser.userId).apply();
                initializeSockets();

                if (responseObject.optJSONObject("item").has("fb")
                        && responseObject.optJSONObject("item").optJSONObject("fb") != null
                        && responseObject.optJSONObject("item").optJSONObject("fb").optString("token") != null)
                    updateFBToken(responseObject.optJSONObject("item").optJSONObject("fb").optString("token"));
                else
                    fbAccessToken = null;

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

                currentUser.setJson(responseObject.optJSONObject("item"));

                if (responseObject.optJSONObject("item").has("fb")
                        && responseObject.optJSONObject("item").optJSONObject("fb") != null
                        && responseObject.optJSONObject("item").optJSONObject("fb").optString("token") != null)
                    updateFBToken(responseObject.optJSONObject("item").optJSONObject("fb").optString("token"));
                else
                    fbAccessToken = null;

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

                currentUser.setJson(responseObject.optJSONObject("item"));

                if (responseObject.optJSONObject("item").has("fb")
                        && responseObject.optJSONObject("item").optJSONObject("fb") != null
                        && responseObject.optJSONObject("item").optJSONObject("fb").optString("token") != null)
                    updateFBToken(responseObject.optJSONObject("item").optJSONObject("fb").optString("token"));

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

        if (ActivityCompat.checkSelfPermission(floozApp.getApplicationContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            new UploadContactTask().execute();
        }
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
        this.request("/psp/3ds/abort", HttpRequestType.GET, null, null);
    }

    /***************************/
    /********  CASHOUT  ********/
    /***************************/

    public void cashoutValidate(Number amount, FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>();

        param.put("amount", amount);
        param.put("validate", true);

        if (this.getSecureCode() != null)
            param.put("secureCode", this.getSecureCode());

        this.request("/cashouts", HttpRequestType.POST, param, responseHandler);
    }

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

        if (this.getSecureCode() != null)
            param.put("secureCode", this.getSecureCode());

        this.request("/cashouts", HttpRequestType.POST, param, responseHandler);
    }

    /***************************/
    /*********  GEOLOC  ********/
    /***************************/

    public void placesFrom(String ll, final FloozHttpResponseHandler responseHandler) {
        JSONArray cachedPlaces = this.loadLocationData();

        if (cachedPlaces != null) {
            if (responseHandler != null)
                responseHandler.success(cachedPlaces);
        } else {
            HashMap<String, Object> params = new HashMap<>();

            params.put("ll", ll);

            this.request("/geo/search", HttpRequestType.GET, params, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    JSONArray items = ((JSONObject)response).optJSONArray("items");

                    saveLocationData(items);

                    if (responseHandler != null)
                        responseHandler.success(items);
                }

                @Override
                public void failure(int statusCode, FLError error) {
                    if (responseHandler != null)
                        responseHandler.failure(statusCode, error);
                }
            });
        }
    }

    public void placesSearch(String search, String ll, final FloozHttpResponseHandler responseHandler) {
        HashMap<String, Object> params = new HashMap<>();

        params.put("ll", ll);
        params.put("q", search);

        this.request("/geo/suggest", HttpRequestType.GET, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONArray items = ((JSONObject)response).optJSONArray("items");

                if (responseHandler != null)
                    responseHandler.success(items);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    /***************************/
    /*****  TRANSACTIONS  ******/
    /***************************/

    public void getUserTransactions(String userId, final FloozHttpResponseHandler responseHandler) {
        this.request("/users/" + userId + "/flooz", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject jsonResponse = (JSONObject) response;

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

            }
        });
    }

    public void transactionWithId(String transacId, FloozHttpResponseHandler responseHandler) {
        this.request("/flooz/" + transacId, HttpRequestType.GET, null, responseHandler);
    }

    public void timeline(final FLTransaction.TransactionScope scope, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> params = new HashMap<>();
        params.put("scope", FLTransaction.transactionScopeToParams(scope));

        if (responseHandler != null) {
            List<FLTransaction> transactions = loadTimelineData(scope);
            if (transactions != null) {
                Map<String, Object> ret = new HashMap<>();
                ret.put("transactions", transactions);
                ret.put("nextUrl", null);
                ret.put("scope", FLTransaction.transactionScopeToParams(scope));
                responseHandler.success(ret);
            }
        }

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
                    ret.put("scope", jsonResponse.optString("scope"));
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
                            ret.put("scope", FLTransaction.transactionScopeToParams(scope));
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

        if (getSecureCode() != null) {
            params.put("secureCode", getSecureCode());
        }

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

    /***************************/
    /*******  SOCIAL  **********/
    /***************************/

    public void getFullUser(String userId, final FloozHttpResponseHandler responseHandler)
    {
        this.request("/social/profile/" + userId, HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(new FLUser(((JSONObject) response).optJSONObject("item")));
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
            }
        });
    }

    public void likeTransaction(String idTransaction, final FloozHttpResponseHandler responseHandler)
    {
        this.request("/social/likes/" + idTransaction, HttpRequestType.POST, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (responseHandler != null) {
                    responseHandler.success(((JSONObject) response).optJSONObject("item"));
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
                    responseHandler.success(((JSONObject) response).optJSONObject("item"));
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

        this.request("/social/" + userID + "/" + this.friendActionToParams(FriendAction.Request), HttpRequestType.POST, param, new FloozHttpResponseHandler() {
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
        this.request("/social/" + friendID + "/" + this.friendActionToParams(action), HttpRequestType.POST, null, responseHandler);
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
                notificationsManager.setNotifications(createNotificationArrayFromResult((JSONObject) response));
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
            case Follow:
                ret = "follow";
                break;
            case Unfollow:
                ret = "unfollow";
                break;
        }
        return ret;
    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }

    public void setNewAccessToken(String token) {
        this.accessToken = token;

        this.appSettings.edit().putString("access_token", token).apply();
    }

    public void showLoadView() {
        final int[] showTentative = {0};
        final Handler showHandler = new Handler(Looper.getMainLooper());

        final Runnable showRunnable = new Runnable() {
            @Override
            public void run() {
                ++showTentative[0];
                Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

                if (currentActivity != null && currentActivity.getWindow().isActive()) {
                    showTentative[0] = 0;
                    if (loadDialog == null || !loadDialog.isShowing()) {
                        loadDialog = new ProgressDialog(floozApp.getCurrentActivity());
                        loadDialog.setCancelable(false);
                        loadDialog.setCanceledOnTouchOutside(false);
                        loadDialog.setMessage(floozApp.getResources().getString(R.string.GLOBAL_WAIT));
                        loadDialog.show();
                    }
                } else if (showTentative[0] < 5) {
                    showHandler.removeCallbacks(this);
                    showHandler.postDelayed(this, 100);
                } else {
                    showTentative[0] = 0;
                }
            }
        };

        showHandler.post(showRunnable);
    }

    public void hideLoadView() {
        if (this.loadDialog != null && this.loadDialog.isShowing())
            this.loadDialog.dismiss();
    }

    /***************************/
    /*******  REQUEST  *********/
    /***************************/

    public boolean isConnected() {
        ConnectivityManager conMgr = (ConnectivityManager) floozApp.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnected());
    }

    private void request(String path, HttpRequestType type, Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

        ConnectivityManager conMgr = (ConnectivityManager) floozApp.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {

            if (BuildConfig.DEBUG_API) {
                Log.d(type.toString() + "Request", path + " - " + (params != null ? params.toString() : "(null)"));
            }

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

            if (!path.contains("&api="))
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

                            if (response.has("popup"))
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

//            HttpEntity entity = null;
//            try {
//                entity = new StringEntity("");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//
//            if (params != null) {
//                if (!fileUpload) {
//                    JSONObject jsonParams;
//                    try {
//                        jsonParams = (JSONObject) JSONHelper.toJSON(params);
//                        StringEntity sEntity = new StringEntity(jsonParams.toString(), "UTF-8");
//                        sEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8"));
//                        entity = sEntity;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    try {
//                        entity = requestParams.getEntity(null);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

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
                        httpClient.post(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                    }
                    break;
                case DELETE:
                    httpClient.delete(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), jsonHttpResponseHandler);
                    break;
                case PUT:
                    if (fileUpload)
                        httpClient.put(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                    else {
                        httpClient.put(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
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

    private void updateFBToken(String token) {
        if (token != null) {
            if (this.fbAccessToken == null || !token.contentEquals(this.fbAccessToken)) {
                this.fbAccessToken = token;

                AccessToken.setCurrentAccessToken(new AccessToken(token, FloozApplication.getAppContext().getResources().getString(R.string.facebook_app_id), currentUser.json.optJSONObject("fb").optString("id"), null, null, null, null, null));
                Profile.fetchProfileForCurrentAccessToken();
            }
        }
    }

    public Boolean isConnectedToFacebook() {
        return this.fbAccessToken != null && !this.fbAccessToken.isEmpty();
    }

    public void connectFacebook() {
        fbLoginCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(fbLoginCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        didConnectFacebook();
                        hideLoadView();
                    }

                    @Override
                    public void onCancel() {
                        hideLoadView();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        hideLoadView();
                    }
                });

        LoginManager.getInstance().logInWithReadPermissions(floozApp.getCurrentActivity(), Arrays.asList("public_profile", "email", "user_friends"));
    }

    public void disconnectFacebook() {
        LoginManager.getInstance().logOut();
        this.fbAccessToken = null;

        Map<String, Object> data = new HashMap<>(1);
        data.put("fb", false);

        this.updateUser(data, null);
    }

    public void didConnectFacebook() {
        this.fbAccessToken =  AccessToken.getCurrentAccessToken().getToken();

        if (this.currentUser != null) {

            Map<String, Object> data = new HashMap<>();

            data.put("token", (fbAccessToken != null ? fbAccessToken : ""));

            Map<String, Object> tmp = new HashMap<>();
            tmp.put("fb", data);

            showLoadView();
            updateUser(tmp, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozApplication.performLocalNotification(CustomNotificationIntents.connectFacebook());
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
        else {
            loginWithFacebook(fbAccessToken);
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
            if (data.has("_id")) {
                this.showLoadView();
                this.transactionWithId(data.optString("_id"), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                        HomeActivity.showTransactionCard(transac);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                    }
                });
            }
        } catch (ClassCastException e) {

        }
    }

    private void handleTriggerAvatarShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
//            FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingLeftMenu());
//            ((HomeActivity)floozApp.getCurrentActivity()).leftMenu.userView.performClick();
        }
    }

    private void handleTriggerProfileReload() {
        this.updateCurrentUser(null);
    }

    private void handleTriggerCardShow(JSONObject data) {
        if (!(floozApp.getCurrentActivity() instanceof CreditCardSettingsActivity)) {
            FloozRestClient.getInstance().updateCurrentUser(null);
            Intent intent = new Intent(floozApp.getCurrentActivity(), CreditCardSettingsActivity.class);
            intent.putExtra("modal", true);

            if (data != null && data.has("label"))
                intent.putExtra("label", data.optString("label"));

            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerFriendReload() {
        this.updateCurrentUser(null);
    }

    private void handleTriggerFriendShow() {
        if (!(floozApp.getCurrentActivity() instanceof FriendsActivity)) {
            FloozRestClient.getInstance().updateCurrentUser(null);
            Intent intent = new Intent(floozApp.getCurrentActivity(), FriendsActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerProfileShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            ((HomeActivity)floozApp.getCurrentActivity()).changeCurrentTab(HomeActivity.TabID.ACCOUNT_TAB);
        }
    }

    private void handleTriggerTransactionReload() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
    }

    private void handleTriggerSignupShow(JSONObject data) {
        if (floozApp.getCurrentActivity() instanceof StartActivity) {
            StartActivity activity = (StartActivity) floozApp.getCurrentActivity();

            try {
                Map<String, Object> userData = JSONHelper.toMap(data);

                if (userData.containsKey("fb") && ((Map)userData.get("fb")).containsKey("id"))
                    userData.put("avatarURL", "https://graph.facebook.com/" + ((Map)userData.get("fb")).get("id") + "/picture");

                activity.updateUserData(userData);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        if (!(floozApp.getCurrentActivity() instanceof IdentitySettingsActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), IdentitySettingsActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerUserDocumentsShow() {
        if (!(floozApp.getCurrentActivity() instanceof DocumentsSettingsActivity)) {
            Activity tmpActivity = floozApp.getCurrentActivity();
            Intent intent = new Intent(tmpActivity, DocumentsSettingsActivity.class);
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTrigger3DSecureShow(JSONObject data) {
        if (!(floozApp.getCurrentActivity() instanceof Secure3DActivity)) {
            Activity tmpActivity = floozApp.getCurrentActivity();
            Intent intent = new Intent(tmpActivity, Secure3DActivity.class);
            intent.putExtra("html", data.optString("html"));

            if (tmpActivity instanceof CreditCardSettingsActivity)
                ((CreditCardSettingsActivity)tmpActivity).controller.next3DSecure = true;

            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTrigger3DSecureComplete() {
        Handler handler = new Handler(floozApp.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateCurrentUser(null);
            }
        });

        if (floozApp.getCurrentActivity() instanceof Secure3DActivity) {
            floozApp.getCurrentActivity().finish();
            floozApp.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        }
    }

    private void handleTrigger3DSecureFail() {
        Handler handler = new Handler(floozApp.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateCurrentUser(null);
            }
        });

        if (floozApp.getCurrentActivity() instanceof Secure3DActivity) {
            floozApp.getCurrentActivity().finish();
            floozApp.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        }
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

    private void handleTriggerHttpCall(JSONObject data) {
        if (data.has("src") && data.optString("src").contentEquals("ext")) {
            floozApp.getCurrentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data.optString("url"))));
        } else if (!data.has("src") || data.optString("src").contentEquals("int")) {
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
    }

    private void handleTriggerPopupShow(final JSONObject data) {
        CustomDialog.show(floozApp.getCurrentActivity(), data, null);
    }

    private void handleTriggerHomeShow() {
        Handler handler = new Handler(floozApp.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                floozApp.displayMainView();
            }
        });
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

    private void handleTriggerViewClose() {
        Activity tmpActivity = floozApp.getCurrentActivity();
        if (tmpActivity != null)
            tmpActivity.onBackPressed();
    }

    private void handleTriggerSendContact() {
        this.sendUserContacts();
    }

    private void handleTriggerUserShow(JSONObject data) {
        if (data.has("nick")) {
            FLUser user = new FLUser(data);
            floozApp.showUserProfile(user);
        } else if (data.has("_id")) {
            this.showLoadView();
            this.getFullUser(data.optString("_id"), new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLUser user = (FLUser) response;
                    floozApp.showUserProfile(user);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
    }

    private void handleTriggerInvitationSMSShow() {
        handleTriggerInvitationShow();
    }

    private void handleTriggerSMSValidate() {
        if (!(floozApp.getCurrentActivity() instanceof ValidateSMSActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), ValidateSMSActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerSecureCodeValidate() {
        if (!(floozApp.getCurrentActivity() instanceof SetSecureCodeActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), SetSecureCodeActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerEditProfile() {
        if (!(floozApp.getCurrentActivity() instanceof EditProfileActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), EditProfileActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerFbConnect() {
        this.connectFacebook();
    }

    private void handleTriggerTextReload() {
        this.textObjectFromApi(null);
    }

    private void handleTriggerInvitationReload() {
        this.getInvitationText(null);
    }

    private void handleTriggerPayClick() {
        if ((floozApp.getCurrentActivity() instanceof NewTransactionActivity)) {
            NewTransactionActivity activity = (NewTransactionActivity)floozApp.getCurrentActivity();

            activity.performTransaction();
        }
    }

    private void handleTriggerNotificationShow() {
        if (!(floozApp.getCurrentActivity() instanceof EditProfileActivity)) {
            Intent intent = new Intent(floozApp.getCurrentActivity(), NotificationActivity.class);
            Activity tmpActivity = floozApp.getCurrentActivity();
            tmpActivity.startActivity(intent);
            tmpActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        }
    }

    private void handleTriggerNotificationReload() {
        FloozRestClient.getInstance().updateNotificationFeed(null);
        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
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
                handleTriggerCardShow(trigger.data);
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
            case TriggerShowSignup:
                handleTriggerSignupShow(trigger.data);
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
            case TriggerShowUserDocuments:
                handleTriggerUserDocumentsShow();
                break;
            case TriggerShow3DSecure:
                handleTrigger3DSecureShow(trigger.data);
                break;
            case TriggerComplete3DSecure:
                handleTrigger3DSecureComplete();
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
            case TriggerResetTuto:
                break;
            case TriggerCloseView:
                handleTriggerViewClose();
                break;
            case TriggerSendContacts:
                handleTriggerSendContact();
                break;
            case TriggerUserShow:
                handleTriggerUserShow(trigger.data);
                break;
            case TriggerInvitationSMSShow:
                handleTriggerInvitationSMSShow();
                break;
            case TriggerSMSValidate:
                handleTriggerSMSValidate();
                break;
            case TriggerSecureCodeValidate:
                handleTriggerSecureCodeValidate();
                break;
            case TriggerEditProfile:
                handleTriggerEditProfile();
                break;
            case TriggerFbConnect:
                handleTriggerFbConnect();
                break;
            case TriggerReloadText:
                handleTriggerTextReload();
                break;
            case TriggerReloadInvitation:
                handleTriggerInvitationReload();
                break;
            case TriggerPayClick:
                handleTriggerPayClick();
                break;
            case TriggerShowNotification:
                handleTriggerNotificationShow();
                break;
            case TriggerReloadNotification:
                handleTriggerNotificationReload();
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
                        if (trigger.delay.doubleValue() > 0) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    handleTrigger(trigger);
                                }
                            }, (int) (trigger.delay.doubleValue() * 1000));
                        } else {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    handleTrigger(trigger);
                                }
                            });
                        }
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this.floozApp, handlerClass);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Activity tmpActivity = floozApp.getCurrentActivity();
                    tmpActivity.startActivity(intent);
                    tmpActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        }
    }

    public void handleTriggerArray(JSONArray t) {
        if (FloozApplication.appInForeground) {

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
                    if (trigger.delay.doubleValue() > 0) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handleTrigger(trigger);
                            }
                        }, (int)(trigger.delay.doubleValue()*1000));
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                handleTrigger(trigger);
                            }
                        });
                    }
                }
            } else {
                FloozApplication.getInstance().pendingTriggers = t;
                Intent intent = new Intent();
                intent.setClass(this.floozApp, handlerClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("triggers", t.toString());
                Activity tmpActivity = floozApp.getCurrentActivity();
                tmpActivity.startActivity(intent);
                tmpActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
    }

    /***************************/
    /******  SOCKET IO  ********/
    /***************************/

    public void initializeSockets() {
        this.socketHandler.removeCallbacks(this.socketCloseRunnable);
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
                    this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("nick", currentUser.username);
                                obj.put("token", accessToken);
                                socket.emit("session start", obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).on("event", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                            JSONObject data = (JSONObject) args[0];
                            if (data.has("popup")) {
                                FLError errorContent = new FLError(data.optJSONObject("popup"));
                                CustomToast.show(FloozApplication.getAppContext(), errorContent);
                            }
                            handleRequestTriggers(data);
                        }
                    }).on("session end", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                            socket.disconnect();
                        }
                    }).on("feed", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                            JSONObject data = (JSONObject) args[0];

                            FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications = data.optInt("total");
                            FloozRestClient.getInstance().updateNotificationFeed(null);
                            FloozApplication.performLocalNotification(CustomNotificationIntents.reloadNotifications());
                        }
                    }).on(Socket.EVENT_DISCONNECT,  new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.d("SocketIO", "Socket Disconnected");
                        }
                    }).on(Socket.EVENT_CONNECT_ERROR,  new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.d("SocketIO", "error: " + args[0].toString());
                        }
                    }).on(Socket.EVENT_CONNECT_TIMEOUT,  new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.d("SocketIO", "Socket Timeout");
                            socket.off();
                            socket = null;
                        }
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
        this.socketHandler.postDelayed(this.socketCloseRunnable, 200);
    }
}
