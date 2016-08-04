package me.flooz.app.UI.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.Model.FLPreset;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 02/08/16.
 */
public class FLTransactionActionBar extends LinearLayout {

    public enum ActionBarType {
        ActionBarTypeBase,
        ActionBarTypeCollect,
        ActionBarTypeParticipation
    }

    public interface FLTransactionActionBarDelegate {
        void presentCamera();
        void presentLocation();
        void presentImagePicker();
        void presentGIFPicker();
        void focusDescription();

        void validatePay();
        void validateCharge();
        void validateParticipate();
        void validateCreate();
    }

    private Context context;

    public FLTransactionActionBarDelegate delegate;
    public ActionBarType type;

    private LinearLayout locationContainer;
    private ImageView locationPic;
    private TextView locationText;

    public ImageView cameraButton;
    public ImageView imageButton;
    public ImageView gifButton;
    public ImageView textButton;
    public ImageView locationButton;

    public TextView payButton;
    public TextView chargeButton;
    public TextView participateButton;
    public TextView createButton;

    public View actionSeparator;

    private FLPreset currentPreset;
    private JSONObject currentGeo;
    private JSONObject userBlockObject;

    public FLTransactionActionBar(Context context) {
        super(context);
        init(context);
    }

    public FLTransactionActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {

        this.context = context;

        View view = View.inflate(context, R.layout.new_transaction_action_bar, this);

        this.locationContainer = (LinearLayout) view.findViewById(R.id.action_bar_geo);
        this.locationPic = (ImageView) view.findViewById(R.id.action_bar_geo_pic);
        this.locationText = (TextView) view.findViewById(R.id.action_bar_geo_text);

        this.cameraButton = (ImageView) view.findViewById(R.id.action_bar_media_camera);
        this.imageButton = (ImageView) view.findViewById(R.id.action_bar_media_album);
        this.gifButton = (ImageView) view.findViewById(R.id.action_bar_media_gif);
        this.textButton = (ImageView) view.findViewById(R.id.action_bar_media_text);
        this.locationButton = (ImageView) view.findViewById(R.id.action_bar_media_location);

        this.payButton = (TextView) view.findViewById(R.id.action_bar_pay);
        this.chargeButton = (TextView) view.findViewById(R.id.action_bar_charge);
        this.participateButton = (TextView) view.findViewById(R.id.action_bar_participate);
        this.createButton = (TextView) view.findViewById(R.id.action_bar_create);

        this.actionSeparator = view.findViewById(R.id.action_bar_payment_separator);

        int whiteColor = this.context.getResources().getColor(android.R.color.white);

        this.locationPic.setColorFilter(whiteColor);

        this.cameraButton.setColorFilter(whiteColor);
        this.imageButton.setColorFilter(whiteColor);
        this.gifButton.setColorFilter(whiteColor);
        this.textButton.setColorFilter(whiteColor);
        this.locationButton.setColorFilter(whiteColor);

        this.chargeButton.setTypeface(CustomFonts.customTitleLight(this.context));
        this.payButton.setTypeface(CustomFonts.customTitleLight(this.context));
        this.participateButton.setTypeface(CustomFonts.customTitleLight(this.context));
        this.createButton.setTypeface(CustomFonts.customTitleLight(this.context));

        this.locationText.setTypeface(CustomFonts.customContentRegular(this.context));

        this.cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.presentCamera();
            }
        });

        this.imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.presentImagePicker();
            }
        });

        this.gifButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.presentGIFPicker();
            }
        });

        this.textButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.focusDescription();
            }
        });

        this.locationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.presentLocation();
            }
        });

        this.payButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.validatePay();
            }
        });

        this.chargeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.validateCharge();
            }
        });

        this.createButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.validateCreate();
            }
        });

        this.participateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.validateParticipate();
            }
        });

        this.updateGeo();
    }

    public void setType(ActionBarType type) {
        this.type = type;

        switch (this.type) {
            case ActionBarTypeBase:
                this.upateBaseTypeButtons();
                this.createButton.setVisibility(GONE);
                this.participateButton.setVisibility(GONE);
                break;
            case ActionBarTypeCollect:
                this.participateButton.setVisibility(GONE);
                this.payButton.setVisibility(GONE);
                this.chargeButton.setVisibility(GONE);
                break;
            case ActionBarTypeParticipation:
                this.createButton.setVisibility(GONE);
                this.payButton.setVisibility(GONE);
                this.chargeButton.setVisibility(GONE);
                break;
        }
    }

    void upateBaseTypeButtons() {
        if (this.userBlockObject != null) {
            if (this.userBlockObject.has("charge") && this.userBlockObject.optBoolean("charge")) {
                this.hideChargeButton(true);
                this.hidePayButton(false);
            } else if (this.userBlockObject.has("pay") && this.userBlockObject.optBoolean("pay")) {
                this.hideChargeButton(false);
                this.hidePayButton(true);
            } else
                this.resetPaymentButton();
        } else
            this.resetPaymentButton();
    }

    void hideChargeButton(Boolean hidden) {
        this.chargeButton.setVisibility((hidden ? View.GONE : View.VISIBLE));
    }

    void hidePayButton(Boolean hidden) {
        this.payButton.setVisibility((hidden ? View.GONE : View.VISIBLE));
    }

    void hideParticipateButton(Boolean hidden) {
        this.participateButton.setVisibility((hidden ? View.GONE : View.VISIBLE));
    }

    void resetPaymentButton() {
        if (this.currentPreset != null) {
            if (this.currentPreset.type != null && this.currentPreset.type == FLTransaction.TransactionType.TransactionTypePayment) {
                this.hideChargeButton(true);
                this.hidePayButton(false);
            } else if (this.currentPreset.type != null && this.currentPreset.type == FLTransaction.TransactionType.TransactionTypeCharge) {
                this.hideChargeButton(false);
                this.hidePayButton(true);
            } else {
                this.hideChargeButton(false);
                this.hidePayButton(false);
            }
        } else {
            this.hideChargeButton(false);
            this.hidePayButton(false);
        }
    }


    public void setCurrentPreset(FLPreset preset) {
        this.currentPreset = preset;

        if (this.type != null && this.type == ActionBarType.ActionBarTypeBase)
            this.upateBaseTypeButtons();
    }


    public void setCurrentGeo(JSONObject currentGeo) {
        this.currentGeo = currentGeo;
        this.updateGeo();
    }

    public void updateGeo() {
        if (this.currentGeo != null) {
            this.locationButton.setColorFilter(this.getResources().getColor(R.color.blue));
            this.locationContainer.setVisibility(View.VISIBLE);
            this.locationText.setText(this.currentGeo.optString("name"));
            this.locationButton.setColorFilter(this.context.getResources().getColor(R.color.blue));
        } else {
            this.locationButton.setColorFilter(this.getResources().getColor(android.R.color.white));
            this.locationContainer.setVisibility(View.GONE);
            this.locationButton.setColorFilter(this.context.getResources().getColor(android.R.color.white));
        }
    }

    public void highlightButton(int buttonPos, Boolean highlight) {
        int color;

        if (highlight)
            color = this.context.getResources().getColor(R.color.blue);
        else
            color = this.context.getResources().getColor(android.R.color.white);

        switch (buttonPos) {
            case 0:
                textButton.setColorFilter(color);
                break;
            case 1:
                cameraButton.setColorFilter(color);
                break;
            case 2:
                imageButton.setColorFilter(color);
                break;
            case 3:
                gifButton.setColorFilter(color);
                break;
            case 4:
                locationButton.setColorFilter(color);
                break;
        }
    }
}
