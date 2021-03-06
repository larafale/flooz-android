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

    public enum TransactionAttachmentType {
        TransactionAttachmentImage,
        TransactionAttachmentVideo,
        TransactionAttachmentNone
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

    public FLScope scope;
    public String title;
    public String name;
    public String content;

    public TransactionAttachmentType attachmentType = TransactionAttachmentType.TransactionAttachmentNone;

    public String attachmentURL;
    public String attachmentThumbURL;
    public String when;
    public String location;

    public List text3d;

    public Boolean isCancelable;
    public Boolean isAcceptable;
    public Boolean isAnswerable;

    public Boolean isAvailable;
    public Boolean isClosable;
    public Boolean isPublishable;
    public Boolean isShareable;

    public Boolean isCollect;
    public Boolean isParticipation;

    public MomentDate date;

    public FLUser from;
    public FLUser to;
    public FLUser starter;
    public FLUser creator;

    public List<FLUser> participants;
    public List<String> invitations;

    public FLSocial social;

    public List comments;

    public Boolean haveAction;

    public JSONObject actions;
    public JSONArray settings;

    public JSONObject json;
    public JSONArray triggerImage;

    public FLTransactionOptions options;

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
        if (json != null) {
            this.json = json;

            this.options = FLTransactionOptions.defaultWithJSON(json.optJSONObject("options"));

            try {
                this.transactionId = json.getString("_id");

                if (json.has("method"))
                    this.type = transactionsTypeParamToEnum(json.getString("method"));

                if (json.has("state"))
                    this.status = transactionStatusParamToEnum(json.getString("state"));

                this.isCollect = json.optBoolean("isPot");
                this.isParticipation = json.optBoolean("isParticipation");

                if (json.has("amount")) {
                    this.amount = json.getDouble("amount");
                    if (!this.amount.equals(0) && !this.isCollect)
                        this.amount = this.amount.floatValue() * -1.0;
                }

                this.isCollect = json.optBoolean("isPot");
                this.isParticipation = json.optBoolean("isParticipation");

                this.amountText = json.optString("amountText");

                this.avatarURL = json.optString("avatar");

                if (this.avatarURL.equals("/img/nopic.png"))
                    this.avatarURL = null;

                this.name = json.optString("name");
                this.title = json.optString("text");
                this.content = json.optString("why");

                if (json.has("pic")) {
                    this.attachmentURL = json.optString("pic");
                    this.attachmentThumbURL = json.optString("picThumb");
                    this.attachmentType = TransactionAttachmentType.TransactionAttachmentImage;
                } else if (json.has("video")) {
                    this.attachmentURL = json.optString("video");
                    this.attachmentThumbURL = json.optString("videoThumb");
                    this.attachmentType = TransactionAttachmentType.TransactionAttachmentVideo;
                } else {
                    this.attachmentType = TransactionAttachmentType.TransactionAttachmentNone;
                    this.attachmentURL = null;
                    this.attachmentThumbURL = null;
                }

                this.social = new FLSocial(json);

                if (json.has("scope"))
                    this.scope = FLScope.scopeFromObject(json.opt("scope"));
                else
                    this.scope = null;

                this.isCancelable = false;
                this.isAcceptable = false;
                this.isPublishable = false;
                this.isShareable = false;
                this.isAnswerable = false;

                if (json.has("isShareable")) {
                    this.isShareable = json.optBoolean("isShareable");
                }

                this.isAvailable = false;
                this.isClosable = false;

                this.haveAction = this.status == TransactionStatus.TransactionStatusPending;

                if (json.has("actions") && json.get("actions") instanceof JSONObject)
                    this.actions = json.optJSONObject("actions");

                if (this.isCollect && this.actions != null) {
                    if (this.actions.has("participate"))
                        isAvailable = true;

                    if (this.actions.has("close"))
                        isClosable = true;

                    if (this.actions.has("publish"))
                        isPublishable = true;
                }

                if (!this.isCollect && this.actions != null) {
                    if (this.actions.has("accept"))
                        isAcceptable = true;

                    if (this.actions.has("decline"))
                        isCancelable = true;

                    if (this.actions.has("answer"))
                        isAnswerable = true;
                 }

                if (json.has("from"))
                    this.from = new FLUser(json.optJSONObject("from"));

                if (json.has("to"))
                    this.to = new FLUser(json.optJSONObject("to"));

                if (json.has("creator") && json.get("creator") instanceof JSONObject) {
                    this.creator = new FLUser(json.optJSONObject("creator"));
                }

                if (json.has("participants")) {
                    this.participants = new ArrayList<>();

                    JSONArray arrayParticipants = json.getJSONArray("participants");

                    for (int i = 0; i < arrayParticipants.length(); i++) {
                        this.participants.add(new FLUser(arrayParticipants.getJSONObject(i)));
                    }
                }

                if (json.has("invitations")) {
                    this.invitations = new ArrayList<>();

                    JSONArray arrayInvitations = json.getJSONArray("invitations");
                    for (int i = 0; i < arrayInvitations.length(); i++) {
                        String tmp = arrayInvitations.optString(i);

                        if (!tmp.contains("+"))
                            this.invitations.add(tmp);
                    }
                }

                if (!this.isCollect) {
                    String starterId = json.optJSONObject("starter").optString("_id");

                    if (starterId.contentEquals(this.from.userId))
                        this.starter = this.from;
                    else
                        this.starter = this.to;
                }

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

                this.settings = json.optJSONArray("settings");

                this.triggerImage = null;

                if (this.settings != null && this.settings.length() > 0) {
                    JSONArray listItems = this.settings.optJSONObject(0).optJSONObject("data").optJSONArray("items");

                    for (int i = 0; i < listItems.length(); i++) {
                        JSONObject item = listItems.optJSONObject(i);

                        if (item.has("id") && item.optString("id").contentEquals("image")) {
                            this.triggerImage = item.optJSONArray("triggers");
                            break;
                        }
                    }
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
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
