package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.TransactionCardController;
import me.flooz.app.UI.Controllers.TransactionController;

/**
 * Created by Flooz on 9/25/14.
 */
public class TransactionCardFragment extends TabBarFragment {
    public TransactionController controller;
    public Boolean insertComment = false;
    public FLTransaction transaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transaction_card_fragment, null);

        if (this.controller == null) {
            this.controller = new TransactionController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER, this.triggerData);
            this.controller.insertComment = this.insertComment;
            this.controller.setTransaction(this.transaction);
        }

        return this.controller.currentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.controller.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        this.controller.onStop();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.controller.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        this.controller.onBackPressed();
    }
}
