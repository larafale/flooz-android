package me.flooz.app.Utils.TriggerTasks;

import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.JSONHelper;

public class CallTask extends ActionTask {

    public CallTask() { super(); }

    @Override public void run() {
        if (this.trigger.category.contentEquals("http")) {
            if (this.trigger.data != null && this.trigger.data.has("url") && this.trigger.data.has("method")) {
                if (this.trigger.data.has("external") && this.trigger.data.optBoolean("external")) {
                    AsyncHttpClient httpClient;

                    if (Looper.myLooper() == null) {
                        httpClient = new SyncHttpClient();
                    } else {
                        httpClient = new AsyncHttpClient();
                    }

                    httpClient.addHeader("Accept", "*/*");

                    ByteArrayEntity entity = null;

                    try {
                        entity = new ByteArrayEntity(this.trigger.data.optJSONObject("body").toString().getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String contentType = "application/json";

                    if (this.trigger.data.has("type") && this.trigger.data.optString("type").contentEquals("urlencoded")) {
                        httpClient.setURLEncodingEnabled(true);
                        contentType = "application/x-www-form-urlencoded";
                    } else {
                        httpClient.setURLEncodingEnabled(false);

                        if (entity != null)
                            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    }

                    AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                                FloozRestClient.getInstance().hideLoadView();

                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);

                            if (trigger.data.has("success")) {
                                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("success")));
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                                FloozRestClient.getInstance().hideLoadView();

                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);

                            if (trigger.data.has("failure")) {
                                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("failure")));
                            }
                        }
                    };

                    if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                        FloozRestClient.getInstance().showLoadView();

                    switch (this.trigger.data.optString("method").toUpperCase()) {
                        case "GET":
                            httpClient.get(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                            break;
                        case "POST":
                            httpClient.post(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                            break;
                        case "PUT":
                            httpClient.put(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                            break;
                        case "DELETE":
                            httpClient.delete(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                            break;
                        default:
                            break;
                    }
                } else {
                    FloozRestClient.HttpRequestType method;

                    switch (this.trigger.data.optString("method").toUpperCase()) {
                        case "GET":
                            method = FloozRestClient.HttpRequestType.GET;
                            break;
                        case "POST":
                            method = FloozRestClient.HttpRequestType.POST;
                            break;
                        case "PUT":
                            method = FloozRestClient.HttpRequestType.PUT;
                            break;
                        case "DELETE":
                            method = FloozRestClient.HttpRequestType.DELETE;
                            break;
                        default:
                            method = FloozRestClient.HttpRequestType.GET;
                            break;
                    }

                    Map param = null;

                    if (this.trigger.data.has("body") && this.trigger.data.opt("body") instanceof JSONObject) {
                        try {
                            param = JSONHelper.toMap(this.trigger.data.optJSONObject("body"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (this.trigger.data.has("body") && this.trigger.data.opt("body") instanceof String) {
                        try {
                            param = JSONHelper.toMap(new JSONObject(this.trigger.data.optString("body")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    String url = this.trigger.data.optString("url");

                    if (url.charAt(0) != '/')
                        url = "/" + url;

                    if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                        FloozRestClient.getInstance().showLoadView();

                    FloozRestClient.getInstance().request(url, method, param, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);

                            if (trigger.data.has("success")) {
                                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("success")));
                            }
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);

                            if (trigger.data.has("failure")) {
                                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("failure")));
                            }
                        }
                    });
                }
            }
        }
    }
}
