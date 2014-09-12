package flooz.android.com.flooz.Model;

import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLTransaction {

    public enum TransactionType {
        TransactionTypePayment,
        TransactionTypeCharge,
        TransactionTypeCollect
    }

    public enum TransactionStatus {
        TransactionStatusAccepted,
        TransactionStatusRefused,
        TransactionStatusPending,
        TransactionStatusCanceled,
        TransactionStatusExpired,
        TransactionStatusNone
    }

    public enum TransactionScope {
        TransactionScopePublic,
        TransactionScopeFriend,
        TransactionScopePrivate
    }

    public enum TransactionPaymentMethod {
        TransactionPaymentMethodWallet,
        TransactionPaymentMethodCreditCard
    }

    public TransactionType type;
    public TransactionStatus status;

    public String transactionId;
    public Number amount;
    public String amountText;

    public String avatarURL;

    public String title;
    public String content;
    public String attachmentURL;
    public String attachmentThumbURL;
    public String when;
    public List text3d;

    public Boolean isPrivate;
    public Boolean isCancelable; // Si peut annuler la demande
    public Boolean isAcceptable; // Si peut accepter ou refuser de payer

    public Date date;

    public FLUser from;
    public FLUser to;

    public FLSocial social;

    public List comments;

    public Boolean isCollect;
    public Boolean collectCanParticipate;
    public List collectUsers;
    public String collectTitle;

    public Boolean haveAction;

    public FLTransaction(JSONObject json) {
        super();

        this.status = TransactionStatus.TransactionStatusNone;

        this.setJson(json);
    }

    private void setJson(JSONObject json) {

        try {
            this.transactionId = json.getString("_id");

            if (json.has("method"))
                this.type = transactionsTypeParamToEnum(json.getString("method"));

            if (json.has("state"))
                this.status = transactionStatusParamToEnum(json.getString("state"));

            if (json.has("amount")) {
                this.amount = json.getDouble("amount");
                if (!this.amount.equals(0) && json.getBoolean("payer") == true)
                    this.amount = this.amount.floatValue() * -1.0;
            }

            this.amountText = json.optString("amountText");

            this.avatarURL = json.optString("avatar");

            this.title = json.getString("text");
            this.content = json.getString("why");

            this.attachmentURL = json.optString("pic");
            this.attachmentThumbURL = json.optString("picMini");

            this.social = new FLSocial(json);

            this.isPrivate = false;
            if (json.getString("currentScope").equals("private"))
                this.isPrivate = true;

            this.isCancelable = false;
            this.isAcceptable = false;

            if (this.status == TransactionStatus.TransactionStatusPending)
            {
                if (json.getJSONArray("actions").length() == 1)
                    this.isCancelable = true;
                else if (json.getJSONArray("actions").length() == 2)
                    this.isAcceptable = true;
            }

            this.from = new FLUser(json.getJSONObject("from"));
            this.to = new FLUser(json.getJSONObject("to"));

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

            this.date = dateFormatter.parse(json.getString("cAt"));

            this.when = "";

            this.comments = new ArrayList();
            for (int i = 0; i < json.getJSONArray("comments").length(); i++) {
                FLComment comment = new FLComment(json.getJSONArray("comments").getJSONObject(i));
                this.comments.add(comment);
            }
//
//    _when = [FLHelper formatedDateFromNow:_date];
//

            if (json.has("text3d")) {
                this.text3d = new ArrayList(3);
                JSONArray array = json.getJSONArray("text3d");
                for (int i = 0; i < array.length(); i++)
                    this.text3d.add(array.get(i));
            }

            this.haveAction = false;
            if (this.isPrivate && this.status == TransactionStatus.TransactionStatusPending)
                this.haveAction = true;

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String statusText()
    {
        int stringId = 0;

        switch (this.status) {
            case TransactionStatusAccepted:
                stringId = R.string.TRANSACTION_STATUS_ACCEPTED;
                break;
            case TransactionStatusRefused:
                stringId = R.string.TRANSACTION_STATUS_REFUSED;
                break;
            case TransactionStatusPending:
                stringId = R.string.TRANSACTION_STATUS_PENDING;
                break;
            case TransactionStatusCanceled:
                stringId = R.string.TRANSACTION_STATUS_CANCELED;
                break;
            case TransactionStatusExpired:
                stringId = R.string.TRANSACTION_STATUS_EXPIRED;
                break;
            default:
                break;
        }

        if (stringId != 0)
            return (FloozApplication.getAppContext().getString(stringId));
        else
            return "";
    }

    public String typeText()
    {
        int stringId = 0;

        switch (this.type) {
            case TransactionTypePayment:
                stringId = R.string.TRANSACTION_TYPE_PAYMENT;
                break;
            default:
                stringId = R.string.TRANSACTION_TYPE_COLLECTION;
                break;
        }

        if (stringId != 0)
            return (FloozApplication.getAppContext().getString(stringId));
        else
            return "";
    }

    public static TransactionType transactionsTypeParamToEnum(String param)
    {
        if (param.equals("pay"))
            return TransactionType.TransactionTypePayment;
        else if (param.equals("collect"))
            return TransactionType.TransactionTypeCollect;
        else
            return TransactionType.TransactionTypeCharge;
    }

    public static TransactionStatus transactionStatusParamToEnum(String param)
    {
        if (param.isEmpty())
            return TransactionStatus.TransactionStatusNone;
        else if (param.equals("0"))
            return TransactionStatus.TransactionStatusPending;
        else if (param.equals("1"))
            return TransactionStatus.TransactionStatusAccepted;
        else if (param.equals("2"))
            return TransactionStatus.TransactionStatusRefused;
        else if (param.equals("3"))
            return TransactionStatus.TransactionStatusCanceled;
        else if (param.equals("4"))
            return TransactionStatus.TransactionStatusExpired;

        return TransactionStatus.TransactionStatusNone;
    }

    public static String transactionScopeToText(TransactionScope scope)
    {
        int stringId = 0;

        switch (scope) {
            case TransactionScopePublic:
                stringId = R.string.TRANSACTION_SCOPE_PUBLIC;
                break;
            case TransactionScopeFriend:
                stringId = R.string.TRANSACTION_SCOPE_FRIEND;
                break;
            case TransactionScopePrivate:
                stringId = R.string.TRANSACTION_SCOPE_PRIVATE;
                break;
            default:
                break;
        }

        if (stringId != 0)
            return (FloozApplication.getAppContext().getString(stringId));
        else
            return "";
    }

    public static Drawable transactionScopeToImage(TransactionScope scope)
    {
        int imgId = 0;

        switch (scope) {
            case TransactionScopePublic:
                imgId = R.drawable.public_filter_scope;
                break;
            case TransactionScopeFriend:
                imgId = R.drawable.friend_filter_scope;
                break;
            case TransactionScopePrivate:
                imgId = R.drawable.private_filter_scope;
                break;
            default:
                break;
        }

        if (imgId != 0)
            return (FloozApplication.getAppContext().getResources().getDrawable(imgId));
        else
            return null;
    }

    public static String transactionStatusToParams(TransactionStatus status)
    {
        switch (status) {
            case TransactionStatusAccepted:
                return "accept";
            case TransactionStatusRefused:
                return "decline";
            case TransactionStatusCanceled:
                return "cancel";
            default:
                break;
        }
        return "";
    }

    public static String transactionScopeToParams(TransactionScope scope)
    {
        switch (scope) {
            case TransactionScopePublic:
                return "public";
            case TransactionScopeFriend:
                return "friend";
            case TransactionScopePrivate:
                return "private";
            default:
                break;
        }

        return "";
    }

    public static String transactionTypeToParams(TransactionType type)
    {
        switch (type) {
            case TransactionTypePayment:
                return "pay";
            case TransactionTypeCharge:
                return "charge";
            default:
                return "event";
        }
    }

    public static String transactionPaymentMethodToParams(TransactionPaymentMethod paymentMethod)
    {
        switch (paymentMethod) {
            case TransactionPaymentMethodWallet:
                return "balance";
            default:
                return "card";
        }
    }
}
