package Equipment;

import Util.StdDraw;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Gig on 4/24/2017.
 * This will be a player's inventory,
 * managing the things a player is currently carrying.
 * Only important in-game, not useful outside
 *
 * WILL NEED TO STORE FOR DISCONNECT FIXING SOMEHOW THOUGH...
 */
public class Inventory {
    public static final char ADDED = 0;
    public static final char OVER_WEIGHT = 1;
    public static final char NO_ROOM = 2;

    private LinkedList<Item> items;
    private ArrayList<Item> equipped;
    private double currentWeight, maxWeight;
    private int slotCount, selected;

    public Inventory(int slotCount, double maxWeight) {
        this.currentWeight = 0;
        this.maxWeight = maxWeight;
        this.slotCount = slotCount;
        this.items = new LinkedList<>();
        this.equipped = new ArrayList<>(10);
    }

    // returns ADDED if added fine, OVER_WEIGHT if added but weight is exceeded,
    // and NO_ROOM if there are no inventory slots left
    public char addItem(Item item) {
        int leftToAdd = item.getCount();
        // see if it's in the equipped slots
        for (Item i : equipped) {
            if (item.getId() == i.getId() && leftToAdd > 0) {
                currentWeight -= i.getWeight();
                leftToAdd = i.add(leftToAdd);
                currentWeight += i.getWeight();
            }
        }
        // see if it's in general inventory
        for (Item i : items) {
            if (item.getId() == i.getId() && leftToAdd > 0) {
                currentWeight -= i.getWeight();
                leftToAdd = i.add(leftToAdd);
                currentWeight += i.getWeight();
            }
        }
        // adjust for adds
        item.setCount(leftToAdd);

        // if there's room left
        if (items.size() >= slotCount && item.getCount() > 0)
            return NO_ROOM;

        // add, update weight
        items.add(item);
        currentWeight += item.getWeight();

        if (currentWeight > maxWeight)
            return OVER_WEIGHT;
        return ADDED;
    }

    public int getCount() {
        return items.size();
    }

    public int getSelected() { return this.selected; }

    public Iterable<Item> getItems() {return items;}

    public int getSlotCount() {
        return this.slotCount;
    }
}
