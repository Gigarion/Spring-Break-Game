package Weapons;

import Actors.Actor;
import Projectiles.Projectile;

public abstract class Weapon {
    private int damage; // damage per shot
    private int reload; // relaod time ms
    private int type;      // type of weapon...
    protected int clip;      // clip size before reload
    private int fireRate;  // minimum time millis between shots
    private long lastShot; // time millis when last shot was fired
    protected boolean isThrowable;

    public Weapon(int damage, int reload, int clip, int fireRate) {
        this.damage = damage;
        this.reload = reload;
        this.type = type;
        this.clip = clip;
        this.fireRate = fireRate;
        this.lastShot = 0;
        this.isThrowable = false;
    }

    // returns either a projectile or hitscan, I didn't want to make them inherit a superclass honestly
    // use typing to determine how to cast this.
    public abstract Object fire(Actor src, double destX, double destY);

    public abstract void addAmmo(int count);

    protected boolean fire() {
        if (System.currentTimeMillis() > lastShot + fireRate) {
            lastShot = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean isThrowable() {return isThrowable;}
}