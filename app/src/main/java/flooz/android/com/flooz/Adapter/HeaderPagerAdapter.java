package flooz.android.com.flooz.Adapter;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

public class HeaderPagerAdapter extends FragmentStatePagerAdapter
{
    public HeaderPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
