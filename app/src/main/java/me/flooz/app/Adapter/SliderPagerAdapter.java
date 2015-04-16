package me.flooz.app.Adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;

import me.flooz.app.UI.Fragment.Sliders.SlideFragment;

/**
 * Created by Flooz on 4/7/15.
 */
public class SliderPagerAdapter extends FragmentStatePagerAdapter {

    private List<SlideFragment> slides;

    public SliderPagerAdapter(FragmentManager fm, List<SlideFragment> pages) {
        super(fm);

        this.slides = pages;
    }

    @Override
    public Fragment getItem(int position) {
        return this.slides.get(position);
    }

    @Override
    public int getCount() {
        return this.slides.size();
    }

}
