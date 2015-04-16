package me.flooz.app.UI.Fragment.Sliders;

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

import com.nostra13.universalimageloader.core.ImageLoader;

import me.flooz.app.Model.FLSliderPage;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.SliderActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 4/7/15.
 */
public class SlideFragment extends Fragment {

    public FLSliderPage data;

    private ImageView image;
    private TextView text;
    private Button button;

    public SlideFragment() {
        this.data = null;
    }

    @SuppressLint("ValidFragment")
    public SlideFragment(FLSliderPage page) {
        this.data = page;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide_fragment, null);

        this.image = (ImageView) view.findViewById(R.id.slide_image);
        this.text = (TextView) view.findViewById(R.id.slide_text);
        this.button = (Button) view.findViewById(R.id.slide_btn);

        this.text.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.button.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        if (this.data != null) {
            ImageLoader.getInstance().displayImage(this.data.imgURL, this.image);
            this.text.setText(this.data.text);

            if (this.data.skipText != null && !this.data.skipText.isEmpty()) {
                this.button.setText(this.data.skipText);
                this.button.setVisibility(View.VISIBLE);
            }
        }

        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SliderActivity)getActivity()).dismiss();
            }
        });

        return view;
    }
}