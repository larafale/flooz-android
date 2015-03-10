package me.flooz.app.Network;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLError;

/**
 * Created by Flooz on 9/9/14.
 */
public abstract class FloozHttpResponseHandler {

    public abstract void success(Object response);
    public abstract void failure(int statusCode, FLError error);
}
