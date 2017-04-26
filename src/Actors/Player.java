package Actors;

import Engine.ActorRequest;
import Util.StdDraw;
import Equipment.*;

public class Player extends Actor {
    private static final int INTERACT_RANGE = 30;
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
        this.interactRange = INTERACT_RANGE;
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

        //this.weapons = new LinkedList<>();
        this.interactRange = (Double) as.extras.get(ActorStorage.INTERACT_RANGE);
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

    boolean giveItem(Item item) {
        boolean result = inventory.addItem(item);
        reload();
        return result;
    }

    public void giveWeapons() {
        ItemStorage is = ItemStorage.loadItemStore("Bow");
        Weapon w = ItemStorage.getWeapon(is);
        giveItem(w);

        is = ItemStorage.loadItemStore("Sword");
        w = ItemStorage.getWeapon(is);
        giveItem(w);
    }

    public void swapWeapon(int howFar) {
        if (inventory.isCharging()) return;
        if (System.currentTimeMillis() - lastSwap < 50)
            return;
        lastSwap = System.currentTimeMillis();
        inventory.moveSelected(howFar);
    }

    public void selectItem(int index) {
        inventory.select(index);
    }

    public int getAmmoCount() {
        return inventory.getAmmoCount();
    }

    private int getAmmoCount(String type) {
        return inventory.getAmmoCount();
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

    public Iterable<Object> useEquipped(double destX, double destY) {
        return inventory.useSelectedItem(this, destX, destY);
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
    public void draw(boolean selected, double xOff, double yOff) {
        try {
            StdDraw.picture(x + xOff, y + yOff, "src/img/Actors/Player2.png", xScale, yScale, Math.toDegrees(rads) - 90);
        } catch (Exception e) {
            StdDraw.picture(x + xOff, y + yOff, "img/Actors/Player.png", xScale, yScale, Math.toDegrees(rads) - 90);
        }
    }

    @Override
    public void hit(int damage) {
        this.hp -= damage;
    }

    public void reload() {
        inventory.reload();
    }
    public String getName() { return this.name; }
    public int getHP() {return hp;}
    public int getMaxHP() {return maxHP;}
    public double getInteractRange() { return interactRange; }

    public String getEquippedName() {
        return inventory.getEquippedName();
    }
    public int getCurrentCount() {
        return inventory.getCurrentCount();
    }

    public Iterable<Object> release(double destX, double destY) {
        return inventory.release(this, destX, destY);
    }

    public double getChargeRatio() {
        return inventory.getChargeRatio();
    }
}