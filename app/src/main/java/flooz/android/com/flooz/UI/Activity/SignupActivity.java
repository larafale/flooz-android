package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.os.Bundle;

import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 9/17/14.
 */
public class SignupActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_phone_activity);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

}