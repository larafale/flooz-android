package flooz.android.com.flooz.Model;

import org.json.JSONObject;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLError {

    public enum ErrorType {
        ErrorTypeSuccess,
        ErrorTypeWarning,
        ErrorTypeError
    }

    public Number time;
    public String text;
    public String title;
    public Number code;
    public Boolean isVisible = true;
    public ErrorType type;

    public FLError(JSONObject json) {
        super();
        if (json != null)
            this.setJson(json);
    }

    private void setJson(JSONObject json) {
        this.time = json.optInt("time");
        this.text = json.optString("message");
        this.title = json.optString("title");
        this.code = json.optInt("code");
        this.isVisible = json.optBoolean("visible");
        this.type = errorTypeParamToEnum(json.optString("type"));
    }

    public static ErrorType errorTypeParamToEnum(String param) {
        if (param.equals("red"))
            return ErrorType.ErrorTypeError;
        else if (param.equals("blue"))
            return ErrorType.ErrorTypeWarning;
        else if (param.equals("green"))
            return ErrorType.ErrorTypeSuccess;

        return null;
    }
}
