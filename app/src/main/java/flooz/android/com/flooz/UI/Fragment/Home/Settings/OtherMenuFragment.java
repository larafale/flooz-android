package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;

/**
 * Created by Flooz on 12/8/14.
 */
public class OtherMenuFragment extends HomeBaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.flooz_fragment, null);



        return view;
    }
}
