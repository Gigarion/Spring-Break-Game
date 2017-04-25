package Equipment;

import javafx.scene.effect.Effect;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static Equipment.Item.WEAPON_TYPE;

/**
 * Created by Gig on 4/24/2017.
 * Serializable storage class for items, should make life simpler
 */
public class ItemStorage {
    private static String NAME = "NAME";
    private static String TYPE = "TYPE";
    private static String XSCL = "XSCL";
    private static String YSCL = "YSCL";
    private static String INVIMG = "INVIMG";
    private static String WEIGHT = "WEIGHT";
    private static String COUNT = "COUNT";
    private static String MAX_COUNT = "MAX_COUNT";
    private static String EXTRAS = "EXTRAS";

    private HashMap<String, Object> dataMap;

    private ItemStorage(){}
    private ItemStorage(Item item) {
        this.dataMap = new HashMap<>();
        dataMap.put(NAME, item.getName());
        System.out.println("putting: " + item.getName());
        dataMap.put(TYPE, item.getType());
        dataMap.put(XSCL, item.xScale);
        dataMap.put(YSCL, item.yScale);
        dataMap.put(WEIGHT, item.getWeight());
        dataMap.put(INVIMG, item.invImage);
        dataMap.put(COUNT, item.count);
        dataMap.put(MAX_COUNT, item.maxCount);
        dataMap.put(EXTRAS, new HashMap<>());
    }

    public ItemStorage(ItemStorage is) {
        dataMap = new HashMap<>();
        for (String key : is.dataMap.keySet()) {
            String newKey = new String(key);
            Object newObj = is.dataMap.get(key);
            if (newKey.equals(EXTRAS)) {
                dataMap.put(newKey, new HashMap<>((Map) newObj));
            }
            else {
                dataMap.put(newKey, newObj);
            }
        }
    }

    public String getName() {return (String) get(NAME);}

    public static ItemStorage loadItemStore(String fileName) {
        if (!fileName.contains(".is"))
            fileName += ".is";
        try {
            FileInputStream fis = new FileInputStream("data/Items/" + fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            ItemStorage is = new ItemStorage();
            is.dataMap = (HashMap<String, Object>) ois.readObject();
            return new ItemStorage(is);

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeItemStore(ItemStorage is) {
        try {
            FileOutputStream fos = new FileOutputStream("data/Items/" + is.getName() + ".is");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(is.dataMap);
            oos.close();
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ItemStorage getWeaponStore(Weapon weapon) {
        ItemStorage toReturn = new ItemStorage(weapon);
        System.out.println(toReturn.getName() + " and  here");
        //toReturn.type = WEAPON_TYPE;
        toReturn.putExtra("WPNSTR", weapon.weaponString);
        toReturn.putExtra("PFACSTR", weapon.pFactoryString);
        return toReturn;
    }

    public static Item getItem(ItemStorage is) {
        switch((Integer) is.get(TYPE)) {
            case WEAPON_TYPE: return getWeapon(is);
            default: return null;
        }
    }

    // TODO: make weapons actual items rather than this
    public static Weapon getWeapon(ItemStorage is) {
        Weapon toReturn = new Weapon((String) is.getExtra("WPNSTR"), (String) is.getExtra("PFACSTR"));
        return toReturn;
    }

    private void put(String key, Object val) {
        dataMap.put(key, val);
    }
    private void putExtra(String key, Object val) {
        ((HashMap<String, Object>) dataMap.get(EXTRAS)).put(key, val);
    }
    private Object get(String key) {
        return dataMap.get(key);
    }
    private Object getExtra(String key) {
        return ((HashMap<String, Object>) dataMap.get(EXTRAS)).get(key);
    }

    public static void main(String[] args) {
        ItemStorage isOne = new ItemStorage(new Weapon("Sword/Melee/0/300/50/false/false/", "H/40/20/1/1/false"));
        ItemStorage isTwo = new ItemStorage(isOne);

        System.out.println(isTwo.get("Hey"));
        System.out.println(isOne.get(NAME));
        isOne.put("Hey", "there");
        isOne.put(NAME, "othername");
        System.out.println(isOne);
        System.out.println(isTwo);
        System.out.println(isTwo.get("Hey"));
        System.out.println(isTwo.get(NAME));
    }
}
