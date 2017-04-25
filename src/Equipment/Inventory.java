package Equipment;

import Engine.ActorRequest;
import Util.StdDraw;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static final int ROW_SIZE = 10;

    private Item[] items;
    private Item[] equipped;
    private double currentWeight;
    private int slotCount, selected;

    public Inventory(int slotCount) {
        this.currentWeight = 0;
        this.slotCount = slotCount;
        this.items = new Item[slotCount];
        this.equipped = new Item[10];
    }

    // returns true if added completely, false if at least some of the
    // item could not be added.
    public boolean addItem(Item item) {
        int leftToAdd = item.getCount();
        // see if it's in the equipped slots
        for (Item i : equipped) {
            if (item.getId() == i.getId() && leftToAdd > 0) {
                leftToAdd = i.add(leftToAdd);
            }
        }
        // see if it's in general inventory
        for (Item i : items) {
            if (item.getId() == i.getId() && leftToAdd > 0) {
                leftToAdd = i.add(leftToAdd);
            }
        }
        // adjust for adds
        item.setCount(leftToAdd);

        // if there's room left in equipped
        for (int i = 0; i < ROW_SIZE; i++) {
            if (equipped[i] == null) {
                equipped[i] = item;
                setWeight();
                return true;
            }
        }
        if (item.getCount() == 0)
            return true;
        for (int i = 0; i < slotCount; i++) {
            if (items[i] == null) {
                items[i] = item;
                setWeight();
                return true;
            }
        }
        // no room left in items
        return false;
    }

    public int getCount() {
        int count = 0;
        for (Item i : equipped) {
            if (i != null)
                count++;
        }
        for (Item i : items) {
            if (i != null)
                count++;
        }
        return count;
    }

    public int getSelected() { return this.selected; }

    public void moveSelected(int offset) {
        System.out.println("called " + offset);
        selected = Math.max(selected + offset, 0);
        selected = Math.min(selected, ROW_SIZE - 1);
    }

    public Iterable<Item> getItems() {return Arrays.asList(items);}

    public int getSlotCount() {
        return this.slotCount;
    }

    private void setWeight() {
        currentWeight = 0;
        for (Item i : equipped)
            currentWeight += i.getWeight();
        for (Item i : items)
            currentWeight += i.getWeight();
    }

    public double getWeight() {
        return currentWeight;
    }

//    public Iterable<Package> useSelectedItem(double mouseX, double mouseY) {
//        return equipped[selected];
//    }

//    public Iterable<Object> releaseCharged(double mouseX, double mouseY) {
//
//    }
}
