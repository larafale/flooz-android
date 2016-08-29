package me.flooz.app.Model;

import org.json.JSONObject;

import java.util.List;

import me.flooz.app.Utils.FLTriggerManager;

/**
 * Created by Flooz on 26/08/16.
 */
public class FLShopItem {

    public enum ShopItemType {
        ShopItemTypeCategory,
        ShopItemTypeCard
    }

    public ShopItemType type;
    public String itemId;
    public String name;
    public String pic;
    public String description;
    public String value;
    public String tosString;
    public String shareUrl;

    public List<FLTrigger> openTriggers;
    public List<FLTrigger> purchaseTriggers;

    public JSONObject jsonData;

    public FLShopItem(JSONObject json) {
        super();

        this.setJson(json);
    }

    public void setJson(JSONObject json) {
        this.jsonData  = json;

        if (json.optString("type").contentEquals("category"))
            this.type = ShopItemType.ShopItemTypeCategory;
        else
            this.type = ShopItemType.ShopItemTypeCard;

        this.itemId = json.optString("_id");
        this.name = json.optString("name");
        this.pic = json.optString("pic");
        this.description = json.optString("description");
        this.value = json.optString("amountText");
        this.tosString = json.optString("tosUrl");
        this.shareUrl = json.optString("shareUrl");

        if (json.has("action")) {
            if (json.optJSONObject("action").has("open"))
                this.openTriggers = FLTriggerManager.convertTriggersJSONArrayToList(json.optJSONObject("action").optJSONArray("open"));

            if (json.optJSONObject("action").has("purchase"))
                this.purchaseTriggers = FLTriggerManager.convertTriggersJSONArrayToList(json.optJSONObject("action").optJSONArray("purchase"));
        } else {
            this.openTriggers = null;
            this.purchaseTriggers = null;
        }
    }
}
