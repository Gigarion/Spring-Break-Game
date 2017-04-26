package Equipment;

import Actors.Actor;
import Projectiles.HitScan;
import Projectiles.Projectile;
import Util.Constants;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by Gig on 4/24/2017.
 * This will be a player's inventory,
 * managing the things a player is currently carrying.
 * Only important in-game, not useful outside
 * <p>
 * WILL NEED TO STORE FOR DISCONNECT FIXING SOMEHOW THOUGH...
 */
public class Inventory {
    public static final int ROW_SIZE = 10;

    private Item[] items;
    private Item[] quickItems;
    private double currentWeight;
    private int slotCount, selected;

    private Item currAmmo;
    private Item[] ammoSet;

    public Inventory(int slotCount) {
        this.currentWeight = 0;
        this.slotCount = slotCount;
        this.items = new Item[slotCount];
        this.quickItems = new Item[10];
        this.ammoSet = new Item[5];
        this.currAmmo = null;
    }

    // scan the inventory in the following order to
    // determine which item is currently in use as ammo
    // first hit is the ammo to use
    // 1. The quickItems item (throwables)
    // 2. Items in the ammo list
    // 3. Items in the quickItems set, from 0
    // 4. Items in regular inventory
    private void setAmmo() {
        currAmmo = null;
        Weapon w = equippedToWeapon();
        if (w == null)
            return;
        String needAmmo = w.getUsesAmmo();
        System.out.println(needAmmo);
        // quickItems item first
        if (needAmmo.equals(w.ammoType)) {
            currAmmo = w;
            return;
        }
        // then privileged ammo set
        for (Item i : ammoSet) {
            if (i == null) continue;
            if (needAmmo.equals(i.ammoType)) {
                currAmmo = i;
                return;
            }
        }
        // then quickItems items
        for (Item i : quickItems) {
            if (i == null) continue;
            if (needAmmo.equals(i.ammoType)) {
                currAmmo = i;
                return;
            }
        }
        // then regular items
        for (Item i : items) {
            if (i == null) continue;
            if (needAmmo.equals(i.ammoType)) {
                currAmmo = i;
                return;
            }
        }
    }

    // returns true if added completely, false if at least some of the
    // item could not be added.
    public boolean addItem(Item item) {
        int leftToAdd = item.getCount();
        // see if it's in the quickItems slots
        for (Item i : quickItems) {
            if (i == null)
                continue;
            if (item.getName().equals(i.getName()) && leftToAdd > 0) {
                leftToAdd = i.add(leftToAdd);
            }
        }
        // see if it's in general inventory
        for (Item i : items) {
            if (i == null)
                continue;
            if (item.getName().equals(i.getName()) && leftToAdd > 0) {
                leftToAdd = i.add(leftToAdd);
            }
        }
        // adjust for adds
        item.setCount(leftToAdd);
        setAmmo();
        if (item.count == 0)
            return true;

        // if there's room left in quickItems
        for (int i = 0; i < ROW_SIZE; i++) {
            if (quickItems[i] == null) {
                quickItems[i] = item;
                setWeight();
                setAmmo();
                return true;
            }
        }
        for (int i = 0; i < slotCount; i++) {
            if (items[i] == null) {
                items[i] = item;
                setWeight();
                setAmmo();
                return true;
            }
        }
        // no room left in items
        return false;
    }

    public int getCount() {
        int count = 0;
        for (Item i : quickItems) {
            if (i != null)
                count++;
        }
        for (Item i : items) {
            if (i != null)
                count++;
        }
        return count;
    }

    public int getSelected() {
        return this.selected;
    }

    public void moveSelected(int offset) {
        selected = Math.max(selected + offset, 0);
        selected = Math.min(selected, ROW_SIZE - 1);
        setAmmo();
    }

    public void select(int index) {
        if (index >= 0 && index < ROW_SIZE)
            selected = index;
        setAmmo();
    }

    public Item[] getEquippedItems() {
        return quickItems;
    }

    public Iterable<Item> getItems() {
        return Arrays.asList(items);
    }

    public int getSlotCount() {
        return this.slotCount;
    }

    private void setWeight() {
        currentWeight = 0;
        for (Item i : quickItems) {
            if (i != null)
                currentWeight += i.getWeight();
        }
        for (Item i : items) {
            if (i != null)
                currentWeight += i.getWeight();
        }
        for (Item i : ammoSet) {
            if (i != null)
                currentWeight += i.getWeight();
        }
    }

    public double getWeight() {
        return currentWeight;
    }

    public Item getQuickItems() {
        return quickItems[selected];
    }

    public Iterable<Object> useSelectedItem(double mouseX, double mouseY) {
        Item current = quickItems[selected];
        if (current == null) return null;
        if (current instanceof Weapon)
            return fireWeapon(mouseX, mouseY);
        return null;
    }

    // Precondition: equipped item is non-null and is a weapon
    private Iterable<Object> fireWeapon(double destX, double destY) {
        Weapon current = (Weapon) quickItems[selected];
        String usesAmmo = current.getUsesAmmo();
        int clip = current.getClip();
        if (!(usesAmmo.equals(Constants.NOT_AMMO) || usesAmmo.equals(Constants.AMMO_MELEE)) && clip <= 0)
            return null;
        if (current.isChargeable()) {
            current.charge();
            return null;
        }

        Iterable<Object> attempt = current.fire(destX, destY);
        if (attempt == null) return null;
        LinkedList<Object> toReturn = new LinkedList<>();

        if (current.getUsesAmmo().equals(Constants.NOT_AMMO)
                || current.getUsesAmmo().equals(Constants.AMMO_MELEE)
                || current.getUsesAmmo().equals("Melee"))
            return attempt;

        for (Object o : attempt) {
            if (o instanceof Projectile || o instanceof HitScan) {
                if (useAmmo()) {
                    toReturn.add(o);
                }
            }
        }

        if (current.getClip() == 0) {
            reload();
        }
        cleanItems();
        return toReturn;
    }

    public Iterable<Object> release(double destX, double destY) {
        Weapon equipped = equippedToWeapon();
        if (equipped == null) return null;
        Iterable<Object> toReturn = equipped.release(destX, destY);
        if (equipped.getClip() == 0) {
            reload();
        }
        return toReturn;
    }

    private boolean useAmmo() {
        if (currAmmo == null) {
            setAmmo();
            if (currAmmo == null)
                return false;
        }
        if (currAmmo.getCount() <= 0) {
            cleanItems();
            return false;
        }
        currAmmo.decrement();
        return true;
    }

    // check the entire inventory for bad items
    private void cleanItems() {
        cleanSet(quickItems);
        cleanSet(items);
        cleanSet(ammoSet);
        setWeight();
    }

    private void cleanSet(Item[] set) {
        for (int i = 0; i < set.length; i++) {
            if (set[i] != null && set[i].getCount() <= 0) {
                set[i] = null;
            }
        }
    }

    public boolean isCharging() {
        Weapon equipped = equippedToWeapon();
        return equipped != null && equipped.isCharging();
    }

    public double getChargeRatio() {
        Weapon equipped = equippedToWeapon();
        if (equipped == null)
            return 0;
        return equipped.getChargeRatio();
    }

    public int getAmmoCount() {
        Weapon equipped = equippedToWeapon();
        return (equipped == null) ? 0 : getAmmoCount(equipped.getUsesAmmo());
    }

    public int getAmmoCount(String ammoType) {
        if (currAmmo == null) return 0;
        return currAmmo.getCount();
    }

    public String getEquippedName() {
        if (quickItems[selected] == null ) return "none";
        return quickItems[selected].getName();
    }

    public int getCurrentCount() {
        Item equipped = quickItems[selected];
        if (equipped == null) return 0;
        else if (equipped instanceof Weapon)
            return ((Weapon) equipped).getClip();
        else return equipped.getCount();
    }

    public void reload() {
        Weapon equipped = equippedToWeapon();
        if (equipped == null || currAmmo == null) return;
        int current = currAmmo.getCount();
        current = equipped.reload(current);
        //currAmmo.setCount(current);
        cleanItems();
    }

    private Weapon equippedToWeapon() {
        Item equipped = quickItems[selected];
        if (equipped == null || !(equipped instanceof Weapon))
            return null;
        return (Weapon) equipped;
    }
}
