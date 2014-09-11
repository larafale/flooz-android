package flooz.android.com.flooz.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.*;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/1/14.
 */
public class FloozRestClient
{
    public String PREFS_NAME = "FloozPrefs";

    private String accessToken = null;
    private String BASE_URL = "http://dev.flooz.me";

    private RequestQueue requestQueue = Volley.newRequestQueue(FloozApplication.getAppContext());
    private static FloozRestClient instance = new FloozRestClient();

    public FLUser currentUser = null;

    public FloozRestClient() {
        super();

        SharedPreferences settings = FloozApplication.getAppContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.accessToken = settings.getString("access_token", "");
    }

    public static FloozRestClient getInstance()
    {
        return instance;
    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }

    private void setNewAccessToken(String token) {
        this.accessToken = token;

        SharedPreferences settings = FloozApplication.getAppContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        settings.edit().putString("access_token", token).apply();
    }

    public void loginQuick()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("login", "+33607751208");
        params.put("password", "0414");

        this.request("/login/basic", Request.Method.POST, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject responseObject = (JSONObject)response;

                setNewAccessToken(responseObject.optJSONArray("items").optJSONObject(0).optString("token"));

                if (currentUser == null)
                    currentUser = new FLUser(responseObject.optJSONArray("items").optJSONObject(1));
                else
                    currentUser.setJson(responseObject.optJSONArray("items").optJSONObject(1));
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadCurrentUser());
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
            }

            @Override
            public void failure(int statusCode, FLError error) {
                Log.d(this.getClass().getSimpleName(), error.text);
            }
        });

    }

    public void timeline(FLTransaction.TransactionScope scope, final FloozHttpResponseHandler responseHandler) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("scope", FLTransaction.transactionScopeToParams(scope));

        this.request("/flooz", Request.Method.GET, params, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                JSONObject jsonResponse = (JSONObject)response;

                List<FLTransaction> transactions = createTransactionArrayFromResult(jsonResponse);

                currentUser.updateStatsPending(jsonResponse);

                if (responseHandler != null) {
                    Map<String, Object> ret = new HashMap<String, Object>();
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

    private List<FLTransaction> createTransactionArrayFromResult(JSONObject jsonObject) {
        List<FLTransaction> ret = new ArrayList<FLTransaction>();
        JSONArray transactions = jsonObject.optJSONArray("items");

        if (transactions != null && transactions.length() > 0) {
            for (int i = 0; i < transactions.length(); i++)
                ret.add(new FLTransaction(transactions.optJSONObject(i)));
        }

        return ret;
    }

    private void request(String path, int method, Map<String, Object> params, final FloozHttpResponseHandler responseHandler) {

        if (!this.accessToken.isEmpty())
        {
            if (path.indexOf('?') == -1) {
                path = path + "?token=" + this.accessToken;
            }
            else if (path.indexOf("token=") == -1) {
                path = path + "&token=" + this.accessToken;
            }
        }

        if (path.indexOf('?') == -1) {
            path = path + "?via=android";
        }
        else {
            path = path + "&via=android";
        }

        final Response.Listener respondListener = new Response.Listener() {

            @Override
            public void onResponse(Object response) {

                if (responseHandler != null)
                    responseHandler.success(response);
            }
        };

        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    JSONObject errorData = new JSONObject(new String(error.networkResponse.data));

                    if (responseHandler != null)
                        responseHandler.failure(error.networkResponse.statusCode, new FLError(errorData.optJSONObject("item")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        if (method == Request.Method.GET && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                path += "&" + entry.getKey() + "=" + entry.getValue().toString();
            }
        }

        JsonObjectRequest requestJson = new JsonObjectRequest(method, getAbsoluteUrl(path), params.isEmpty() ? null : new JSONObject(params), respondListener, responseErrorListener);

        this.requestQueue.add(requestJson);
    }
}
