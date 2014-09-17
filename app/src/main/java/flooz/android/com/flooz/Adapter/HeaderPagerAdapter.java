package flooz.android.com.flooz.Adapter;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.app.FragmentManager;

import flooz.android.com.flooz.UI.Fragment.FloozFragment;
import flooz.android.com.flooz.UI.Fragment.FriendsFragment;
import flooz.android.com.flooz.UI.Fragment.ProfileFragment;

public class HeaderPagerAdapter
{
    public HeaderPagerAdapter(FragmentManager fm) {

    }

    public Fragment getItem(int position)
    {
        Fragment ret = null;

        switch (position)
        {
            case 0:
                ret = new ProfileFragment();
                break;
            case 1:
                ret = new FloozFragment();
                break;
            case 2:
                ret = new FriendsFragment();
                break;
        }
        return ret;
    }

    public int getCount() {
        return 3;
    }
}
