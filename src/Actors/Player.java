package Actors;

import Util.StdDraw;
import Weapons.Bow;
import Weapons.StabSword;
import Weapons.Weapon;

import java.util.ArrayList;
import java.util.LinkedList;

public class Player extends Actor {
    private static final int INTERACT_RANGE = 30;
    private LinkedList<Weapon> weapons;
    private Weapon equipped;
    private String name;
    private int maxHP;
    private int hp;
    private double interactRange;
    private int level;
    private int exp;

    public Player(String userName) {
        super(-1, 300, 300, 10);
        this.name = userName;
        this.maxHP = 100;
        this.hp = maxHP;
        this.weapons = new LinkedList<>();
        this.interactRange = INTERACT_RANGE;
        equipped = null;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public void giveWeapons() {
        weapons.add(new StabSword());
        weapons.add(new Bow());
        equipped = weapons.removeFirst();
    }

    public void swapWeapon() {
        weapons.add(equipped);
        equipped = weapons.removeFirst();
    }

    // fully heal player, return result hp
    public void fillHP() {
        this.hp = maxHP;
    }

    // attempt to heal player by healPoints, return result hp
    public void heal(int healPoints) {
        this.hp += healPoints;
        if (this.hp > maxHP)
            this.hp = maxHP;
    }

    // attempt to damage player by damagePoints, return result hp
    public void damage(int damagePoints) {
        this.hp -= damagePoints;
        if (this.hp < 0)
            this.hp = 0;
    }

    // returns either a hitscan or a projectile to register as an attack
    // (or other later???)
    public Object fireWeapon() {
        if (equipped == null) return null;
        return equipped.fire(this, StdDraw.mouseX(), StdDraw.mouseY());
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
    public void update() {}

    @Override
    public void draw(boolean selected) {
        StdDraw.filledCircle(x, y, 10);
        StdDraw.circle(x, y, interactRange);
    }

    @Override
    public void hit(int damage) {}

    public String getName() { return this.name; }
    public int getHP() {return hp;}
    public int getMaxHP() {return maxHP;}
    public double getInteractRange() { return interactRange; }
}