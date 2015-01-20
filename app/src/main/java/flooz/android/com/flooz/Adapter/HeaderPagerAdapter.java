package flooz.android.com.flooz.Adapter;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.FloozFragment;
import flooz.android.com.flooz.UI.Fragment.Home.FriendsFragment;
import flooz.android.com.flooz.UI.Fragment.Home.ProfileFragment;
import flooz.android.com.flooz.UI.Fragment.Other.TimelineHeaderFragment;

public class HeaderPagerAdapter  extends FragmentStatePagerAdapter
{
    TimelineHeaderFragment homeTitle;
    TimelineHeaderFragment publicTitle;
    TimelineHeaderFragment privateTitle;

    public HeaderPagerAdapter(FragmentManager fm) {
        super(fm);

        this.homeTitle = new TimelineHeaderFragment();
        this.publicTitle = new TimelineHeaderFragment();
        this.privateTitle = new TimelineHeaderFragment();

        this.homeTitle.text = FloozApplication.getInstance().getResources().getString(R.string.FILTER_SCOPE_FRIEND);
        this.publicTitle.text = FloozApplication.getInstance().getResources().getString(R.string.FILTER_SCOPE_PUBLIC);
        this.privateTitle.text = FloozApplication.getInstance().getResources().getString(R.string.FILTER_SCOPE_PRIVATE);
    }

    public Fragment getItem(int position)
    {
        Fragment ret = null;

        switch (position)
        {
            case 0:
                ret = this.homeTitle;
                break;
            case 1:
                ret = this.publicTitle;
                break;
            case 2:
                ret = this.privateTitle;
                break;
        }
        return ret;
    }

    public int getCount() {
        return 3;
    }
}
