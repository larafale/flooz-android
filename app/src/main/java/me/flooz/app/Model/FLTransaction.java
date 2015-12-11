package me.flooz.app.Model;

import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.MomentDate;

/**
 * Created by Flooz on 9/9/14.
 */
public class FLTransaction {

    public enum TransactionType {
        TransactionTypePayment,
        TransactionTypeCharge,
        TransactionTypeCollect,
        TransactionTypeNone
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
        TransactionScopePrivate,
        TransactionScopeAll
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

    public TransactionScope scope;
    public String title;
    public String content;
    public String attachmentURL;
    public String attachmentThumbURL;
    public String when;
    public String location;

    public List text3d;

    public Boolean isPrivate;
    public Boolean isCancelable;
    public Boolean isAcceptable;

    public MomentDate date;

    public FLUser from;
    public FLUser to;
    public FLUser starter;

    public FLSocial social;

    public List comments;

    public Boolean haveAction;

    public JSONObject json;

    public FLTransaction(JSONObject json) {
        super();

        this.status = TransactionStatus.TransactionStatusNone;

        this.setJson(json);
    }

    public FLTransaction() {
        super();

        this.status = TransactionStatus.TransactionStatusNone;
    }

    public void setJson(JSONObject json) {
        this.json = json;

        try {
            this.transactionId = json.getString("_id");

            if (json.has("method"))
                this.type = transactionsTypeParamToEnum(json.getString("method"));

            if (json.has("state"))
                this.status = transactionStatusParamToEnum(json.getString("state"));

            if (json.has("amount")) {
                this.amount = json.getDouble("amount");
                if (!this.amount.equals(0) && json.getBoolean("payer"))
                    this.amount = this.amount.floatValue() * -1.0;
            }

            this.amountText = json.optString("amountText");

            this.avatarURL = json.optString("avatar");

            if (this.avatarURL.equals("/img/nopic.png"))
                this.avatarURL = null;

            this.title = json.getString("text");
            this.content = json.getString("why");

            this.attachmentURL = json.optString("pic");
            this.attachmentThumbURL = json.optString("picMini");

            this.social = new FLSocial(json);

            this.isPrivate = json.getString("scopeString").contentEquals("private");

            this.scope = transactionScopeParamToEnum(json.getString("scopeString"));

            this.isCancelable = false;
            this.isAcceptable = false;

            this.haveAction = this.isPrivate && this.status == TransactionStatus.TransactionStatusPending;

            if (this.status == TransactionStatus.TransactionStatusPending)
            {
                if (json.has("actions")) {
                    if (json.getJSONObject("actions").length() == 1)
                        this.isCancelable = true;
                    else if (json.getJSONObject("actions").length() == 2)
                        this.isAcceptable = true;
                }
            }

            this.from = new FLUser(json.getJSONObject("from"));
            this.to = new FLUser(json.getJSONObject("to"));

            String starterId = json.optJSONObject("starter").optString("_id");

            if (starterId.contentEquals(this.from.userId))
                this.starter = this.from;
            else
                this.starter = this.to;

            this.date = new MomentDate(FloozApplication.getAppContext(), json.getString("cAt"));

            if (FloozRestClient.getInstance().isConnected() && json.has("when"))
                this.when = json.optString("when");
            else
                this.when = this.date.fromNow();

            if (json.has("location"))
                this.location = json.optString("location");
            else
                this.location = null;

            this.comments = new ArrayList();
            for (int i = 0; i < json.getJSONArray("comments").length(); i++) {
                FLComment comment = new FLComment(json.getJSONArray("comments").getJSONObject(i));
                this.comments.add(comment);
            }

            if (json.has("text3d")) {
                this.text3d = new ArrayList(3);
                JSONArray array = json.getJSONArray("text3d");
                for (int i = 0; i < array.length(); i++)
                    this.text3d.add(array.get(i));
            }

        } catch (JSONException | ParseException e) {
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
        int stringId;

        switch (this.type) {
            case TransactionTypePayment:
                stringId = R.string.TRANSACTION_TYPE_PAYMENT;
                break;
            default:
                stringId = R.string.TRANSACTION_TYPE_COLLECTION;
                break;
        }

        return (FloozApplication.getAppContext().getString(stringId));
    }

    public static TransactionType transactionsTypeParamToEnum(String param)
    {
        switch (param) {
            case "pay":
                return TransactionType.TransactionTypePayment;
            case "collect":
                return TransactionType.TransactionTypeCollect;
            default:
                return TransactionType.TransactionTypeCharge;
        }
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

    public static TransactionScope transactionScopeParamToEnum(String param)
    {
        if (param.contentEquals("private"))
            return TransactionScope.TransactionScopePrivate;
        else if (param.contentEquals("friend"))
            return TransactionScope.TransactionScopeFriend;

        return TransactionScope.TransactionScopePublic;
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

        return (FloozApplication.getAppContext().getString(stringId));
    }

    public static Drawable transactionScopeToImage(TransactionScope scope)
    {
        int imgId = 0;

        switch (scope) {
            case TransactionScopePublic:
                imgId = R.drawable.scope_public;
                break;
            case TransactionScopeFriend:
                imgId = R.drawable.scope_friend;
                break;
            case TransactionScopePrivate:
                imgId = R.drawable.scope_private;
                break;
            default:
                break;
        }

        return (FloozApplication.getAppContext().getResources().getDrawable(imgId));
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
        if (scope != null) {
            switch (scope) {
                case TransactionScopePublic:
                    return "public";
                case TransactionScopeFriend:
                    return "friend";
                case TransactionScopePrivate:
                    return "private";
                case TransactionScopeAll:
                    return "all";
                default:
                    break;
            }
        }

        return "public";
    }

    public static TransactionScope transactionParamsToScope(String scope)
    {
        if (scope != null) {
            switch (scope) {
                case"public":
                    return TransactionScope.TransactionScopePublic;
                case "friend":
                    return TransactionScope.TransactionScopeFriend;
                case "private":
                    return TransactionScope.TransactionScopePrivate;
                case "all":
                    return TransactionScope.TransactionScopeAll;
                default:
                    break;
            }
        }

        return TransactionScope.TransactionScopeAll;
    }

    public static int transactionScopeToFloozParams(TransactionScope scope)
    {
        switch (scope) {
            case TransactionScopePublic:
                return 0;
            case TransactionScopeFriend:
                return 1;
            case TransactionScopePrivate:
                return 2;
            default:
                break;
        }

        return 0;
    }

    public static String transactionTypeToParams(TransactionType type)
    {
        if (type != null)
            switch (type) {
                case TransactionTypePayment:
                    return "pay";
                case TransactionTypeCharge:
                    return "charge";
                default:
                    return "pay";
            }
        else
            return "pay";
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
