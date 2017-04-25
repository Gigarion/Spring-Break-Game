package Equipment;

/**
 * Created by Gig on 4/24/2017.
 * A
 */
public class EffectItem extends Item {
    private boolean consumable;
    EffectItem(String name, int type, double weight, int maxCount) {
        super(name, type, weight, maxCount);
    }

    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
    }

    public Iterable<String> use() {
        if (consumable) count--;
        return null;
    }
}
