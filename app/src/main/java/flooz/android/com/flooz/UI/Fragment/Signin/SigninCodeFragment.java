package flooz.android.com.flooz.UI.Fragment.Signin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 11/27/14.
 */
public class SigninCodeFragment extends SigninBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_code_fragment, null);


        return view;
    }
}
