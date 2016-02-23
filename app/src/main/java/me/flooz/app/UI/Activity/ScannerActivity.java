package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 4/24/15.
 */
public class ScannerActivity extends BaseActivity {

    private ScannerActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private ZXingScannerView scannerView;

    private Vibrator vibrator;

    private Boolean handleError = true;
    private Handler timer;
    private int countdown = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        instance = this;
        floozApp = (FloozApplication) this.getApplicationContext();
        vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        this.setContentView(R.layout.scanner_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.scanner_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.scanner_header_title);
        this.scannerView = (ZXingScannerView) this.findViewById(R.id.scanner_zxing);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.scannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
            @Override
            public void handleResult(Result result) {
                String codeValue = result.getText();

                if (codeValue.contains("flooz://")) {
                    String code = codeValue.replace("flooz://", "");
                    if (NumberUtils.isDigits(code)) {
                        FloozRestClient.getInstance().showLoadView();
                        vibrator.vibrate(300);
                    } else {
                        handleScanError();
                    }
                } else {
                    handleScanError();
                }
            }
        });

        this.scannerView.setAutoFocus(true);

        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);

        this.scannerView.setFormats(formats);
    }

    private void handleScanError() {
        if (handleError) {
            CustomToast.show(this, new FLError("QR Code invalide", "Veuillez scanner un QR Code Flooz.", 3, FLError.ErrorType.ErrorTypeError));
            vibrator.vibrate(300);
            handleError = false;

            timer = new Handler();
            timer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ++countdown;
                    if (countdown >= 4) {
                        countdown = 0;
                        timer = null;
                        handleError = true;
                    } else if (timer != null)
                        timer.postDelayed(this, 1000);
                }
            }, 1000);
        }
        scannerView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
        this.scannerView.startCamera();

        this.countdown = 0;
        this.timer = null;
        this.handleError = true;
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
        this.scannerView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }


    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
