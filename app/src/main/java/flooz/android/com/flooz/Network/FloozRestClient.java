package flooz.android.com.flooz.Network;

import org.json.*;
import com.loopj.android.http.*;

/**
 * Created by Flooz on 9/1/14.
 */
public class FloozRestClient
{
    private static final String BASE_URL = "http://dev.flooz.me/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler)
    {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }
}
