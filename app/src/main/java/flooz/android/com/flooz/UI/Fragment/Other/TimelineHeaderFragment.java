package flooz.android.com.flooz.UI.Fragment.Other;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/8/14.
 */
public class TimelineHeaderFragment extends Fragment {

    public String text;
    private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timeline_header_fragment, null);

        textView = (TextView) view.findViewById(R.id.timeline_header_text);
        textView.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        textView.setText(this.text);

        return view;
    }
}
