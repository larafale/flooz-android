package me.flooz.app.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.UI.Fragment.Start.SlideFragment;

/**
 * Created by Flooz on 4/7/15.
 */
public class FLSlider {

    public List<FLSliderPage> slides;

    public FLSlider(JSONObject jsonObject) {
        JSONArray slidesData = jsonObject.optJSONArray("slides");

        this.slides = new ArrayList<>();

        for (int i = 0; i < slidesData.length(); i++) {
            this.slides.add(new FLSliderPage(slidesData.optJSONObject(i)));
        }
    }

    public List<SlideFragment> generateFragments() {
        List<SlideFragment> ret = new ArrayList<>();

        for (int i = 0; i < slides.size(); i++) {
            ret.add(new SlideFragment(slides.get(i)));
        }

        return ret;
    }
}
