package flooz.android.com.flooz.Network;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.*;
import org.apache.http.*;
import com.loopj.android.http.*;

import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.Locale;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/1/14.
 */
public class FloozRestClient
{
    private String BASE_URL = "http://dev.flooz.me";

    private AsyncHttpClient client = new AsyncHttpClient();
    private static FloozRestClient instance = new FloozRestClient();

    public FLUser currentUser = null;

    public FloozRestClient()
    {
        super();
    }

    public static FloozRestClient getInstance()
    {
        return instance;
    }

    public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }

    public void loginQuick()
    {
        JSONObject jsonParams = new JSONObject();
        StringEntity entity = null;

        try {
            jsonParams.put("login", "+33607751208");
            jsonParams.put("password", "0415");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            entity = new StringEntity(jsonParams.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        String url = this.getAbsoluteUrl("/login/basic");

        client.post(null, url, entity, "application/json",new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (currentUser == null)
                    currentUser = new FLUser(response.optJSONArray("items").optJSONObject(1));
                else
                    currentUser.setJson(response.optJSONArray("items").optJSONObject(1));
                FloozApplication.performLocalNotification(CustomNotificationIntents.reloadCurrentUser());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("FloozRestClient", "Object Failure");
            }
        });
    }

    public void loadTimeline(String scope, String state, FloozHttpResponseHandler responseHandler) {

    }
}
