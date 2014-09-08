package flooz.android.com.flooz.Network;

import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.*;
import org.apache.http.*;
import com.loopj.android.http.*;

import java.io.UnsupportedEncodingException;

import flooz.android.com.flooz.Model.FLUser;

/**
 * Created by Flooz on 9/1/14.
 */
public class FloozRestClient
{
    private String BASE_URL = "http://dev.flooz.me";

    private AsyncHttpClient client = new AsyncHttpClient();
    private static FloozRestClient instance = new FloozRestClient();

    private FLUser currentUser = null;

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
            jsonParams.put("password", "0414");
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

        Log.d("FloozRestClient", "Quick Login at : " + url);

        client.post(null, url, entity, "application/json",new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (currentUser == null) {
                    try {
                        currentUser = new FLUser(response.getJSONArray("items").getJSONObject(1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        currentUser.setJson(response.getJSONArray("items").getJSONObject(1));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("FloozAPI", "User : " + currentUser.username);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                Log.d("FloozRestClient", "Array Response");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("FloozRestClient", "Object Failure");
            }
        });
    }
}
