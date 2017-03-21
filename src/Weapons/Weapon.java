package Weapons;

import Projectiles.Projectile;

public abstract class Weapon {
    public final int TYPE_HITSCAN = 0;
    public final int TYPE_PROJECTILE = 1;
    public final int TYPE_THROWN = 2;
    public final int TYPE_MELEE = 3;
    private int damage; // damage per shot
    private int reload; // relaod time ms
    private int type;
    private int clip;
    private int fireRate;
    private long nextShot; // time millis when next shot is allowed

    public Weapon(int damage, int reload, int type, int clip, int fireRate, int nextShot) {
        this.damage = damage;
        this.reload = reload;
        this.type = type;
        this.clip = clip;
        this.fireRate = fireRate;
        this.nextShot = nextShot;
    }

    public abstract Projectile fire(double destX, double destY);
}