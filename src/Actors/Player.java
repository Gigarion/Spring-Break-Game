package Actors;

import Engine.ActorRequest;
import Util.DefaultMap;
import Util.StdDraw;
import Equipment.*;

import java.util.LinkedList;

public class Player extends Actor {
    private static final int INTERACT_RANGE = 30;
    private LinkedList<Weapon> weapons;
    private DefaultMap<String, Integer> ammoMap; // count of each ammo type
    private Weapon equipped;
    private String name;
    public Inventory inventory;

    private int maxHP;
    private int hp;

    private double interactRange;
    private long lastSwap;

    public Player(String userName) {
        super(-1, 300, 300, 10);
        this.name = userName;
        this.maxHP = 100;
        this.hp = maxHP;
        this.weapons = new LinkedList<>();
        this.ammoMap = new DefaultMap<>(0);
        this.interactRange = INTERACT_RANGE;
        this.equipped = null;
        this.lastSwap = 0;
        this.canHit = true;
        this.inventory = new Inventory(20);
        setxScale(40);
        setyScale(40);
    }

    // load player from ActorStorage
    public Player(ActorStorage as) {
        super(as.id, as.x, as.y, as.r);
        this.name = (String) as.extras.get(ActorStorage.NAME);
        this.maxHP = (Integer) as.extras.get(ActorStorage.MAXHP);
        this.hp = maxHP;

        this.weapons = new LinkedList<>();
        this.ammoMap = new DefaultMap<>(0);
        this.interactRange = (Double) as.extras.get(ActorStorage.INTERACT_RANGE);

        this.equipped = null;
        this.lastSwap = 0;
        this.canHit = as.canHit;
        this.passesHeight = as.passesHeight;

        setxScale(as.xScale);
        setyScale(as.yScale);
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    void giveWeapon(Weapon weapon) {
        if (weapon.isThrowable()) {
            String ammoType = weapon.getAmmoType();
            int ammoCount = ammoMap.get(ammoType);
            if (ammoCount > 0 || equipped.getAmmoType().equals(ammoType)) {
                ammoMap.put(ammoType, ammoCount + 1);
                return;
            } else if ( ammoCount == 0) {
                ammoMap.put(ammoType, ammoCount + 1);
            }
        }
        if (equipped != null)
            weapons.add(equipped);
        equipped = weapon;
        reload();
    }

    public void giveWeapons() {
        ItemStorage is = ItemStorage.loadItemStore("Bow");
        Weapon w = ItemStorage.getWeapon(is);
        weapons.add(w);
        weapons.add(new Weapon("Bow/Arrow/1/700/100/false/true/300/3/1", "P/400/200/1/1/5/2/-/"));
        giveAmmo("Arrow", 5);

        weapons.add(new Weapon("Sword/Melee/0/300/50/false/false/0/3/1", "H/40/20/1/1/false"));

        giveAmmo("Melee", Integer.MAX_VALUE);
        equipped = weapons.removeFirst();
        reload();
    }

    public void swapWeapon(int howFar) {
        if (equipped.isCharging()) return;
        if (System.currentTimeMillis() - lastSwap < 50)
            return;
        lastSwap = System.currentTimeMillis();
        weapons.add(equipped);
        equipped = weapons.removeFirst();
        inventory.moveSelected(howFar);
    }

    public int getAmmoCount() {
        if (equipped == null)
            return -1;
        return getAmmoCount(equipped.getAmmoType());
    }

    private int getAmmoCount(String type) {
        Integer count = ammoMap.get(type);
        if (count == null)
            return 0;
        return count;
    }

    void giveAmmo(String type, int count) {
        Integer current = ammoMap.get(type);
        if (current == null)
            current = 0;
        current += count;
        ammoMap.put(type, current);

        if (equipped != null && equipped.getAmmoType().equals(type) && equipped.getClip() == 0)
            reload();
    }

    // fully heal player
    public void fillHP() {
        this.hp = maxHP;
    }

    // attempt to heal player by healPoints
    public void heal(int healPoints) {
        this.hp += healPoints;
        if (this.hp > maxHP)
            this.hp = maxHP;
    }

    // returns either a hitscan or a projectile to register as an attack
    public Iterable<Object> fireWeapon(double destX, double destY) {
        if (equipped == null) return null;

        if (equipped.isChargeable()) {
            equipped.charge();
            return null;
        }
        Iterable<Object> toReturn = equipped.fire(this, destX, destY);

        if (toReturn != null && equipped.isThrowable()) {
            ammoMap.put(equipped.getAmmoType(), ammoMap.get(equipped.getAmmoType()));
        }
        if (equipped.getClip() == 0) {
            reload();
        }
        if (equipped.isThrowable()) {
            if (equipped.getClip() == 0) {
                equipped = weapons.removeFirst();
                equipped.equip();
            }
        }
        return toReturn;
    }

    // shift the player's location by dist in the x direction
    // trusts game engine to call this and protect border cases
    public void moveX(double dist) {
        this.x += dist;
    }

    // shift the player's location by dist in the y direction
    // trusts game engine to call this and protect border cases
    public void moveY(double dist) {
        this.y += dist;
    }

    @Override
    public Iterable<ActorRequest> update() {
        return null;
    }

    @Override
    public void draw(boolean selected) {
        try {
            StdDraw.picture(x, y, "src/img/Actors/Player2.png", xScale, yScale, Math.toDegrees(rads) - 90);
        } catch (Exception e) {
            StdDraw.picture(x, y, "img/Actors/Player.png", xScale, yScale, Math.toDegrees(rads) - 90);
        }
    }

    @Override
    public void hit(int damage) {
        this.hp -= damage;
    }

    public void reload() {
        int current = ammoMap.get(equipped.getAmmoType());
        current = equipped.reload(current);
        ammoMap.put(equipped.getAmmoType(), current);
    }
    public String getName() { return this.name; }
    public int getHP() {return hp;}
    public int getMaxHP() {return maxHP;}
    public double getInteractRange() { return interactRange; }
    public String getWeaponName() {
        if (equipped == null)
            return "none";
        return this.equipped.getName();
    }
    public int getCurrentClip() {
        if (equipped == null)
            return -1;
        return this.equipped.getClip();
    }

    public Iterable<Object> release(double destX, double destY) {
        Iterable<Object> toReturn = equipped.release(this, destX, destY);
        if (equipped.getClip() == 0) {
            reload();
        }
        return toReturn;
    }

    public double getChargeRatio() {
        if (equipped == null)
            return 0;
        return equipped.getChargeRatio();
    }
}