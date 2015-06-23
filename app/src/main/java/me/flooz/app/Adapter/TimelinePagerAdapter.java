package me.flooz.app.Adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import me.flooz.app.Model.FLTransaction;
import me.flooz.app.UI.Fragment.Home.TimelineFragment;

/**
 * Created by Flooz on 9/23/14.
 */
public class TimelinePagerAdapter extends FragmentStatePagerAdapter {

    public TimelineFragment homeTimeline;
    public TimelineFragment publicTimeline;
    public TimelineFragment privateTimeline;

    public TimelinePagerAdapter(FragmentManager fm) {
        super(fm);

        this.homeTimeline = new TimelineFragment(FLTransaction.TransactionScope.TransactionScopeFriend);
        this.publicTimeline = new TimelineFragment(FLTransaction.TransactionScope.TransactionScopePublic);
        this.privateTimeline = new TimelineFragment(FLTransaction.TransactionScope.TransactionScopePrivate);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return this.homeTimeline;
        else if (position == 1)
            return this.publicTimeline;
        else
            return this.privateTimeline;
    }

    @Override
    public int getCount() {
        return 3;
    }
}