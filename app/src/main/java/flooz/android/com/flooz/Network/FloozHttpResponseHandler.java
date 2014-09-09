package flooz.android.com.flooz.Network;

import org.json.JSONObject;

/**
 * Created by Flooz on 9/9/14.
 */
public abstract class FloozHttpResponseHandler {

    public abstract void success(Object response);
    public abstract void failure(JSONObject errorResponse);
}
