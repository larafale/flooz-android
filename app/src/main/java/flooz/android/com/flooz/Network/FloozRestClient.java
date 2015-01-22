package flooz.android.com.flooz.Network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.*;

import com.facebook.HttpMethod;
import com.facebook.Session;
import com.facebook.SessionState;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.BuildConfig;
import flooz.android.com.flooz.Model.FLComment;
import flooz.android.com.flooz.Model.FLCreditCard;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLNotification;
import flooz.android.com.flooz.Model.FLPreset;
import flooz.android.com.flooz.Model.FLReport;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Model.FLTrigger;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Activity.InvitationCodeActivity;
import flooz.android.com.flooz.UI.Activity.SigninActivity;
import flooz.android.com.flooz.UI.Activity.SignupActivity;
import flooz.android.com.flooz.UI.Fragment.Home.NewFloozFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Settings.CreditCardSettingsFragment;
import flooz.android.com.flooz.UI.Fragment.Home.Settings.Secure3DFragment;
import flooz.android.com.flooz.UI.Tools.CustomToast;
import flooz.android.com.flooz.Utils.ContactsManager;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import flooz.android.com.flooz.Utils.NotificationsManager;

/**
 * Created by Flooz on 9/1/14.
 */
public class FloozRestClient
{
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

    public FLUser currentUser = null;
    private String secureCode = null;
    private String accessToken = null;
    private String fbAccessToken= null;

    public SharedPreferences appSettings;

    private String BASE_URL;

    public NotificationsManager notificationsManager;

    private static FloozRestClient instance = new FloozRestClient();

    private Socket socket;
    public Boolean isSocketConnected = false;

    private AsyncHttpClient httpClient = new AsyncHttpClient();

    public static FloozRestClient getInstance()
    {
        return instance;
    }

    private ProgressDialog loadDialog;

    public FloozRestClient() {
        super();

        this.appSettings = FloozApplication.getAppContext().getSharedPreferences("FloozPrefs", Context.MODE_PRIVATE);

        this.secureCode =  this.appSettings.getString("secure_code", null);
        this.accessToken =  this.appSettings.getString("access_token", null);

        this.floozApp = (FloozApplication)FloozApplication.getAppContext().getApplicationContext();

        if (BuildConfig.DEBUG_API)
            this.BASE_URL = "http://dev.flooz.me";
        else
            this.BASE_URL = "https://api.flooz.me";

        this.notificationsManager = new NotificationsManager();

        this.httpClient.addHeader("Accept", "*/*");
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
                logout();
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

                if (currentUser == null)
                    currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                else
                    currentUser.setJson(responseObject.optJSONArray("items").optJSONObject(1));
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

    public void loginWithPhone(String phone) {
        String formattedPhone = phone.replace(" ", "").replace(".", "").replace("-", "");

        if (formattedPhone.startsWith("+33"))
            formattedPhone = formattedPhone.replace("+33", "0");

        final Map<String, Object> params = new HashMap<>();
        params.put("q", formattedPhone);
        params.put("distinctId", floozApp.mixpanelAPI.getDistinctId());

        this.showLoadView();
        this.request("/login", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                hideLoadView();
            }

            @Override
            public void failure(int statusCode, FLError error) {
                hideLoadView();
            }
        });
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

        this.appSettings.edit().remove("access_token").commit();
        this.clearSecureCode();
    }

    public void checkDeviceToken() {
        if (this.currentUser == null || floozApp.regid == null || floozApp.regid.isEmpty())
            return;
        if (this.currentUser.device != null && this.currentUser.device.contentEquals(floozApp.regid))
            return;

        Map<String, Object> params = new HashMap<>();
        params.put("device", floozApp.regid);

        this.updateUser(params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                currentUser.device = floozApp.regid;
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    /***************************/
    /********  SIGNUP  *********/
    /***************************/

    public void verifySignup(Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

        this.showLoadView();
        this.request("/users/signup", HttpRequestType.POST, params, responseHandler);
    }

    public void signupPassStep(String step, Map<String, Object> params, FloozHttpResponseHandler responseHandler) {
        this.request("/users/signup/step-" + step, HttpRequestType.POST, params, responseHandler);
    }

    public void signup(final Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

        this.showLoadView();
        this.request("/users/signup", HttpRequestType.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

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

    public void askInvitationCode(String fullname, String email, String phone, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>(3);

        param.put("phone", phone);
        param.put("email", email);
        param.put("name", fullname);

        this.request("/invitations/ask", HttpRequestType.POST, param, responseHandler);
    }

    public void verifyInvitationCode(String code, String phone, final FloozHttpResponseHandler responseHandler) {
        Map<String, Object> param = new HashMap<>(1);

        param.put("phone", phone); // Fix to redirect signup
        param.put("distinctId", floozApp.mixpanelAPI.getDistinctId());

        this.globalVerify(VerifyAction.InvitationCode, code, param, responseHandler);
    }

    public void verifyPseudo(String pseudo, final FloozHttpResponseHandler responseHandler) {
        this.showLoadView();
        this.globalVerify(VerifyAction.Username, pseudo, null, responseHandler);
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

    public void updateCurrentUserAfterSignup(JSONObject responseObject) {

        this.currentUser = new FLUser((responseObject.optJSONArray("items").optJSONObject(1)));

        if (responseObject.optJSONArray("items").optJSONObject(1).has("fb"))
            fbAccessToken = responseObject.optJSONArray("items").optJSONObject(1).optJSONObject("fb").optString("token");

        checkDeviceToken();

        floozApp.didConnected();
    }

    public void updateCurrentUser(final FloozHttpResponseHandler responseHandler) {
        this.request("/users/profile", HttpRequestType.GET, null, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                if (responseObject.optJSONObject("item").has("fb"))
                    fbAccessToken = responseObject.optJSONObject("item").optJSONObject("fb").optString("token");

                if (currentUser == null) {
                    currentUser = new FLUser(responseObject.optJSONObject("item"));
                    initializeSockets();
                }
                else
                    currentUser.setJson(responseObject.optJSONObject("item"));

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

                if (responseObject.optJSONObject("item").has("fb"))
                    fbAccessToken = responseObject.optJSONObject("item").optJSONObject("fb").optString("token");

                currentUser.setJson(responseObject.optJSONObject("item"));
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadCurrentUser());

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
        List<FLUser> list = ContactsManager.getContactsList();
        List<String> phones = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            phones.add(list.get(i).phone);
        }

        Map<String, Object> params = new HashMap<>(1);
        params.put("phones", phones);

        this.request("/users/contacts", HttpRequestType.POST, params, null);
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

    public void timeline(FLTransaction.TransactionScope scope, final FloozHttpResponseHandler responseHandler) {

        Map<String, Object> params = new HashMap<>();
        params.put("scope", FLTransaction.transactionScopeToParams(scope));

        this.request("/flooz", HttpRequestType.GET, params, new FloozHttpResponseHandler() {
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
                if (responseHandler != null)
                    responseHandler.failure(statusCode, error);
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

    public void sendFriendRequest(String userID, final FloozHttpResponseHandler responseHandler) {
        this.performActionOnFriend(userID, FriendAction.Request, responseHandler);
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
                notificationsManager.setNotifications(createNotificationArrayFromResult((JSONObject)response));
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
            for (int i = 0; i < transactions.length(); i++)
                ret.add(new FLTransaction(transactions.optJSONObject(i)));
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

        if (this.accessToken != null && !this.accessToken.isEmpty())
        {
            if (path.indexOf('?') == -1) {
                path = path + "?token=" + this.accessToken;
            }
            else if (!path.contains("token=")) {
                path = path + "&token=" + this.accessToken;
            }
        }

        if (path.indexOf('?') == -1) {
            path = path + "?via=android";
        }
        else {
            path = path + "&via=android";
        }

        path += "&api=v2";

        final JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                hideLoadView();

                if (response.has("popup")) {
                    FLError errorContent = new FLError(response.optJSONObject("popup"));
                    CustomToast.show(FloozApplication.getAppContext(), errorContent);
                }

                handleRequestTriggers(response);

                if (responseHandler != null)
                    responseHandler.success(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                hideLoadView();

                if (errorResponse != null) {

                    if (errorResponse.has("popup")) {
                        FLError errorContent = new FLError(errorResponse.optJSONObject("popup"));
                        CustomToast.show(FloozApplication.getAppContext(), errorContent);

                        if (responseHandler != null)
                            responseHandler.failure(statusCode, errorContent);
                    }
                    else if (responseHandler != null)
                        responseHandler.failure(statusCode, null);

                    handleRequestTriggers(errorResponse);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String errorMsg, Throwable throwable) {
                if (responseHandler != null)
                    responseHandler.failure(statusCode, null);
            }
        };

        RequestParams requestParams = new RequestParams();

        if (params != null && !params.isEmpty()) {
            if (type == HttpRequestType.GET || type == HttpRequestType.DELETE) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    path += "&" + entry.getKey() + "=" + entry.getValue().toString();
                }
            }
            else {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() instanceof File)
                        try {
                            requestParams.put(entry.getKey(), (File)entry.getValue(), "image/jpeg");
                        } catch(FileNotFoundException e) {
                            Log.d("Multipart Request", "File not found !!!");
                        }
                    else
                        requestParams.put(entry.getKey(), entry.getValue());
                }
            }
        }

        switch (type) {
            case GET:
                this.httpClient.get(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                break;
            case POST:
                this.httpClient.post(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                break;
            case DELETE:
                this.httpClient.delete(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), jsonHttpResponseHandler);
                break;
            case PUT:
                this.httpClient.put(floozApp.getCurrentActivity(), this.getAbsoluteUrl(path), requestParams, jsonHttpResponseHandler);
                break;
        }
    }

    /***************************/
    /*******  FACEBOOK  ********/
    /***************************/

    public Boolean isConnectedToFacebook() {
        if (this.fbAccessToken != null && !this.fbAccessToken.isEmpty())
            return true;
        return false;
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
        this.fbAccessToken = null;

        Map<String, Object> data = new HashMap<>();
        data.put("fb", "{}");

        this.updateUser(data, null);
    }

    public void didConnectFacebook() {
        this.fbAccessToken =  Session.getActiveSession().getAccessToken();

        if (this.currentUser != null) {
            Bundle params = new Bundle();
            params.putString("fields", "id,email,first_name,last_name,name,birthday");
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
            params.putString("fields", "id,email,first_name,last_name,name,birthday");
            com.facebook.Request request = new com.facebook.Request(Session.getActiveSession(), "me", params, HttpMethod.GET, new com.facebook.Request.Callback() {
                @Override
                public void onCompleted(com.facebook.Response response) {
                    hideLoadView();

                    if (response.getError() == null) {

                        FLUser newUser = ((SignupActivity) floozApp.getCurrentActivity()).userData;

                        newUser.email = (String) response.getGraphObject().getProperty("email");
                        newUser.avatarURL = "https://graph.facebook.com/" + response.getGraphObject().getProperty("id") + "/picture?width=360&height=360";
                        newUser.birthdate = FLUser.formattedBirthdateFromFacebook((String)response.getGraphObject().getProperty("birthday"));

                        try {
                            newUser.json = new JSONObject();

                            JSONObject data = new JSONObject();
                            data.put("email", response.getGraphObject().getProperty("email"));
                            data.put("id", response.getGraphObject().getProperty("id"));
                            data.put("firstName", response.getGraphObject().getProperty("first_name"));
                            data.put("lastName", response.getGraphObject().getProperty("last_name"));
                            data.put("name", response.getGraphObject().getProperty("name"));
                            data.put("token", (fbAccessToken != null ? fbAccessToken : ""));

                            newUser.json.put("fb", data);
                            newUser.json.put("email", response.getGraphObject().getProperty("email"));
                            newUser.json.put("avatarURL", "https://graph.facebook.com/" + response.getGraphObject().getProperty("id") + "/picture?width=360&height=360");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ((SignupActivity) floozApp.getCurrentActivity()).currentFragment.refreshView();
                    }
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
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            if (data.has("_id")) {
                this.showLoadView();
                this.transactionWithId(data.optString("_id"), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FLTransaction transac = new FLTransaction(((JSONObject)response).optJSONObject("item"));
                        ((HomeActivity)floozApp.getCurrentActivity()).showTransactionCard(transac);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {}
                });
            }
        }
    }

    private void handleTriggerAvatarShow() {
//    [[AvatarMenu new] showAvatarMenu:[appDelegate currentController]];
    }

    private void handleTriggerProfileReload() {
        this.updateCurrentUser(null);
    }

    private void handleTriggerCardShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) floozApp.getCurrentActivity();
            activity.pushMainFragment("settings_credit_card", R.animator.slide_up, android.R.animator.fade_out);
        }
    }

    private void handleTriggerFriendReload() {
        this.updateCurrentUser(null);
    }

    private void handleTriggerFriendShow() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingRightMenu());
    }

    private void handleTriggerProfileShow() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingLeftMenu());
    }

    private void handleTriggerTransactionReload() {
        FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
    }

    private void handleTriggerLoginShow(JSONObject data) {
        this.clearLogin();
        Intent intent = new Intent();
        intent.putExtra("phone", data.optString("phone"));
        intent.putExtra("secureCode", data.optBoolean("secureCode"));
        intent.setClass(this.floozApp, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.floozApp.getCurrentActivity().startActivity(intent);
        this.floozApp.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        this.floozApp.getCurrentActivity().finish();
    }

    private void handleTriggerSignupShow(JSONObject data) {
        if (floozApp.getCurrentActivity() instanceof SignupActivity) {
            SignupActivity activity = (SignupActivity) floozApp.getCurrentActivity();
            activity.userData.phone = data.optString("phone");
            activity.gotToNextPage();
        }
        else if (floozApp.getCurrentActivity() instanceof InvitationCodeActivity) {
            Intent intent = new Intent();
            intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupSMS.ordinal());
            intent.putExtra("phone", data.optString("phone"));
            intent.putExtra("coupon", data.optString("coupon"));
            intent.setClass(this.floozApp, SignupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.floozApp.getCurrentActivity().startActivity(intent);
            this.floozApp.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            this.floozApp.getCurrentActivity().finish();
        }
    }

    private void handleTriggerSignupCodeShow(final JSONObject data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(floozApp.getCurrentActivity());

        builder.setTitle(R.string.SIGNUP_PHONE_ALERT_TITLE);
        builder.setMessage(String.format(floozApp.getResources().getString(R.string.SIGNUP_PHONE_ALERT_CONTENT),  data.optString("phone")));
        builder.setNegativeButton(R.string.GLOBAL_EDIT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.GLOBAL_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra("phone", data.optString("phone"));
                intent.setClass(floozApp, InvitationCodeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                floozApp.getCurrentActivity().startActivity(intent);
                floozApp.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                floozApp.getCurrentActivity().finish();
            }
        });

        builder.show();
    }

    private void handleTriggerLogout() {
        this.clearLogin();
        this.logout();
    }

    private void handleTriggerAppUpdate(JSONObject data) {
//    [appDelegate lockForUpdate:data[@"uri"]];
    }

    private void handleTriggerContactInfoShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) floozApp.getCurrentActivity();
            activity.pushMainFragment("settings_coords", R.animator.slide_up, android.R.animator.fade_out);
        }
    }

    private void handleTriggerUserIdentityShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) floozApp.getCurrentActivity();
            activity.pushMainFragment("settings_identity", R.animator.slide_up, android.R.animator.fade_out);
        }
    }

    private void handleTrigger3DSecureShow(JSONObject data) {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            HomeActivity tmp = (HomeActivity)floozApp.getCurrentActivity();
            ((CreditCardSettingsFragment)tmp.contentFragments.get("settings_credit_card")).next3DSecure = true;
            ((CreditCardSettingsFragment)tmp.contentFragments.get("settings_credit_card")).secureData = data.optString("html");
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

        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            ((Secure3DFragment)((HomeActivity)floozApp.getCurrentActivity()).contentFragments.get("settings_3ds")).dismiss();
        }
    }

    private void handleTrigger3DSecureFail() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            ((Secure3DFragment)((HomeActivity)floozApp.getCurrentActivity()).contentFragments.get("settings_3ds")).dismiss();
        }
    }

    private void handleTriggerResetPassword(JSONObject data) {
        Intent intent = new Intent();
        intent.putExtra("phone", data.optString("phone"));
        intent.putExtra("secureCode", data.optBoolean("secureCode"));
        intent.putExtra("page", SigninActivity.SigninPageIdentifier.SigninResetPass.ordinal());
        intent.setClass(this.floozApp, SigninActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.floozApp.getCurrentActivity().startActivity(intent);
        this.floozApp.getCurrentActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        this.floozApp.getCurrentActivity().finish();
    }

    private void handleTriggerClearSecureCode() {
        this.clearSecureCode();
    }

    private void handleTriggerCheckSecureCode() {
        this.checkSecureCodeForUser(getSecureCode(), null);
    }

    private void handleTriggerPresetLine(JSONObject data) {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) floozApp.getCurrentActivity();
            ((NewFloozFragment) activity.contentFragments.get("create")).initWithPreset(new FLPreset(data));
            activity.pushMainFragment("create", R.animator.slide_up, android.R.animator.fade_out);
        }
    }

    private void handleTriggerReadFeed(JSONObject data) {
        this.readNotification(data.optString("_id"), null);
    }

    private void handleTriggerInvitationShow() {
        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
            HomeActivity activity = (HomeActivity) floozApp.getCurrentActivity();
            activity.pushMainFragment("invite", R.animator.slide_up, android.R.animator.fade_out);
        }
    }

    public void handleTrigger(final FLTrigger trigger) {

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
            default:
                break;
        }
    }

    public void handleRequestTriggers(JSONObject responseObject) {
        if (FloozApplication.appInForeground) {
            if (responseObject != null && responseObject.has("triggers")) {
                JSONArray t = responseObject.optJSONArray("triggers");
                for (int i = 0; i < t.length(); i++) {
                    final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                    if (trigger.delay.intValue() > 0) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handleTrigger(trigger);
                            }
                        }, (int) (trigger.delay.doubleValue() * 1000));
                    } else {
                        handleTrigger(trigger);
                    }
                }
            }
        }
    }

    /***************************/
    /******  SOCKET IO  ********/
    /***************************/

    public void initializeSockets() {
        if (!this.isSocketConnected && this.currentUser != null) {
            try {
                IO.Options options = new IO.Options();
                options.secure = !BuildConfig.DEBUG_API;
                options.transports = new String[]{"websocket"};

                this.socket = IO.socket(BASE_URL, options);
                this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("nick", currentUser.username);
                            obj.put("token", accessToken);
                            socket.emit("session start", obj);
                            isSocketConnected = true;
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
                }).on("feed", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.d("Socket", "Feed Socket");
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        isSocketConnected = false;
                    }
                });
                this.socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void socketSendSessionEnd() {
        if (this.socket != null && this.accessToken != null && this.isSocketConnected) {
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
        if (this.socket != null && this.isSocketConnected && this.currentUser != null) {
            this.socketSendSessionEnd();
            this.socket.close();
            this.socket = null;
        }
        this.isSocketConnected = false;
    }
}
