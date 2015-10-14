package me.flooz.app.UI.Fragment.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.SearchController;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;

/**
 * Created by Wapazz on 12/10/15.
 */
public class SearchFragment extends TabBarFragment  {
    public SearchController controller;
    public FLUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, null);

        this.controller = new SearchController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.controller.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.controller.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.controller.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.controller.onDestroy();
    }

    @Override
    public void onBackPressed() {
        this.controller.onBackPressed();
    }
}

