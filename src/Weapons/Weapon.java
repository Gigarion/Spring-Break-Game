package Weapons;

import Projectiles.Projectile;

public abstract class Weapon {
    public final int TYPE_HITSCAN = 0;
    public final int TYPE_PROJECTILE = 1;
    private int damage; // damage per shot
    private int reload; // relaod time ms
    private int type;
    private int clip;
    private int fireRate;
    private long nextShot; // time millis when next shot is allowed

    public Weapon(int damage, int reload, int type, int clip, int fireRate) {
        this.damage = damage;
        this.reload = reload;
        this.type = type;
        this.clip = clip;
        this.fireRate = fireRate;
        this.nextShot = System.currentTimeMillis();
    }

    // returns either a projectile or hitscan, I didn't want to make them inherit a superclass honestly
    // use typing to determine how to cast this.
    public abstract Object fire(double destX, double destY);
}