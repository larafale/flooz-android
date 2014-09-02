package flooz.android.com.flooz.UI.Fragment;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.View.FloozFilterTabItem;

public class FloozFragment extends Fragment implements FloozFilterTabItem.Delegate
{
    private FloozFilterTabItem publicTabElementView;
    private FloozFilterTabItem privateTabElementView;
    private FloozFilterTabItem friendsTabElementView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.flooz_fragment, null);

        this.publicTabElementView = (FloozFilterTabItem) view.findViewById(R.id.public_tab);
        this.privateTabElementView = (FloozFilterTabItem) view.findViewById(R.id.private_tab);
        this.friendsTabElementView = (FloozFilterTabItem) view.findViewById(R.id.friends_tab);

        this.publicTabElementView.setDelegate(this);
        this.privateTabElementView.setDelegate(this);
        this.friendsTabElementView.setDelegate(this);

        this.publicTabElementView.select();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void tabElementViewClick(FloozFilterTabItem.TabType tabType)
    {
        switch (tabType)
        {
            case PUBLIC:
                this.privateTabElementView.unselect();
                this.friendsTabElementView.unselect();
                break;
            case PRIVATE:
                this.publicTabElementView.unselect();
                this.friendsTabElementView.unselect();
                break;
            case FRIENDS:
                this.privateTabElementView.unselect();
                this.publicTabElementView.unselect();
                break;
        }
    }
}