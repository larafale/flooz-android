package me.flooz.app.Network;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.json.*;
import cz.msebera.android.httpclient.Header;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
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
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.DeviceManager;
import me.flooz.app.Utils.FLTriggerManager;
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
            if (socket != null && socket.connected()) {
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

    public void loginWithToken(String token, final FloozHttpResponseHandler responseHandler) {

        if (token == null && token.isEmpty()) {
            if (responseHandler != null) {
                responseHandler.failure(400, null);
            }

            return;
        }

        this.setNewAccessToken(token);

        String path = "/users/login";

        path += "?os=" + Build.VERSION.RELEASE;

        path += "&mo=" + this.deviceManager.getDeviceName();

        path += "&uuid=" + this.deviceManager.getDeviceUuid();

        this.request(path, HttpRequestType.POST, null, new FloozHttpResponseHandler() {
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

                updateCurrentUser(null);

                if (responseHandler != null) {
                    responseHandler.success(response);
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (statusCode != 442 && statusCode != 426) {
                    logout();
                    if (responseHandler != null) {
                        responseHandler.failure(statusCode, error);
                    }
                } else if (statusCode != 426) {
                    loadUserData();
                    if (currentUser != null) {
                        FloozApplication.getInstance().didConnected();
                        FloozApplication.getInstance().displayMainView();
                        if (responseHandler != null) {
                            responseHandler.success(null);
                        }
                    } else {
                        logout();
                        if (responseHandler != null) {
                            responseHandler.failure(statusCode, error);
                        }
                    }
                }
            }
        });
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

    public void createCreditCard(Map<String, Object> params, Boolean signup, final FloozHttpResponseHandler responseHandler) {

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

    public void createCollect(Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {
        if (getSecureCode() != null) {
            params.put("secureCode", getSecureCode());
        }

        this.request("/pots", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
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
        this.request("/social/suggests", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
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

        String url = "/social/search";

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

    public void request(String path, HttpRequestType type, Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

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
                } else {
                    path = path + "&version=" + FloozApplication.getAppVersionName(floozApp.getApplicationContext());
                }
            }

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

                try {
                    AccessToken accessToken = new AccessToken(this.fbAccessToken, FloozApplication.getAppContext().getResources().getString(R.string.facebook_app_id), currentUser.json.optJSONObject("fb").optString("id"), null, null, null, null, null);
                    AccessToken.setCurrentAccessToken(accessToken);
                    Profile.fetchProfileForCurrentAccessToken();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    this.fbAccessToken = null;
                }
            }
        }
    }

    public Boolean isConnectedToFacebook() {
        return this.fbAccessToken != null && !this.fbAccessToken.isEmpty();
    }

    public void connectFacebook() {
        fbLoginCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logOut();

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

        Activity currentActivity = floozApp.getCurrentActivity();

        if (currentActivity != null)
            LoginManager.getInstance().logInWithReadPermissions(currentActivity, Arrays.asList("public_profile", "email", "user_friends"));
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

    public void handleRequestTriggers(JSONObject responseObject) {
        if (responseObject.has("triggers")) {
            if (responseObject.opt("triggers") instanceof JSONArray) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(responseObject.optJSONArray("triggers")));
            } else if (responseObject.opt("triggers") instanceof JSONObject) {
                FLTrigger tmp = new FLTrigger(responseObject.optJSONObject("triggers"));

                if (tmp.valid)
                    FLTriggerManager.getInstance().executeTrigger(tmp);
            }
        }
    }

    public void handleTriggerArray(JSONArray t) {
        FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(t));
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
                            if (currentUser != null) {
                                try {
                                    JSONObject obj = new JSONObject();
                                    obj.put("nick", currentUser.username);
                                    obj.put("token", accessToken);
                                    socket.emit("session start", obj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            closeSockets();
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
                if (currentUser != null)
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
