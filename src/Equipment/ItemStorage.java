package Equipment;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Gig on 4/24/2017.
 * Serializable storage class for items, should make life simpler
 */
public class ItemStorage implements Serializable {
    private int id;
    private int type;
    private double xScale, yScale;
    private String invImage;
    private double weight;
    private int count, maxCount;
    private HashMap<String, Object> extras;

    public ItemStorage(Item item) {
        this.id = item.getId();
        this.type = item.getType();
        this.xScale = item.xScale;
        this.yScale = item.yScale;
        this.weight = item.getWeight();
        this.invImage = item.invImage;
        this.count = item.count;
        this.maxCount = item.maxCount;
    }

//    private ItemStorage getWeaponStore(Weapon weapon) {
//        ItemStorage toReturn = new ItemStorage(weapon);
//    }

    private void put(String key, Object val) {
        extras.put(key, val);
    }
}
