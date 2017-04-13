package me.flooz.app.UI.Fragment.Start;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import me.flooz.app.Model.FLSliderPage;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 4/7/15.
 */
public class SlideFragment extends Fragment {

    public FLSliderPage data;

    public SlideFragment() {
        this.data = null;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide_fragment, null);

        TextView text = (TextView) view.findViewById(R.id.slide_text);

        text.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        if (this.data != null) {
            text.setText(this.data.text);
        }

        return view;
    }
}