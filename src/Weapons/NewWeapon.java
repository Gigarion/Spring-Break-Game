package Weapons;

import Actors.Actor;
import Projectiles.Projectile;
import Projectiles.ProjectileFactory;

import java.io.Serializable;

/**
 * Created by Gig on 3/31/2017.
 * weaponString format:
 * 0    1     2        3   4   5         6          7
 * name ammo  maxclip  FR  RR  throwable chargeable chargeTime
 */
public class NewWeapon implements Serializable {
    private ProjectileFactory pFactory;
    private String name;
    private int clip;
    private int maxClip;
    private int fireRate;
    private int reloadRate;
    private long lastShot;
    private long reloadStart;
    private boolean isThrowable;
    private int chargeTime;
    private boolean isChargeable;
    private boolean hasFired;
    private String ammoType;

    public NewWeapon(String weaponString, String factoryString) {
        this.clip = 0;
        this.reloadStart = 0;
        this.lastShot = 0;
        this.loadFromString(weaponString);
        this.pFactory = new ProjectileFactory(factoryString);
    }

    private void loadFromString(String weaponString) {
        String[] info = weaponString.split("/");
        this.name = info[0];
        this.ammoType = info[1];
        this.maxClip = Integer.parseInt(info[2]);
        this.fireRate = Integer.parseInt(info[3]);
        this.reloadRate = Integer.parseInt(info[4]);
        this.isThrowable = Boolean.parseBoolean(info[5]);
        this.isChargeable = Boolean.parseBoolean(info[6]);
        if (isChargeable)
            this.chargeTime = Integer.parseInt(info[7]);
    }

    // attempt to fire this weapon, only valid if not reloading,
    // constrained by fireRate, and current clip.
    // returns the stack of objects associated with firing this weapon
    public Iterable<Object> fire(Actor src, double destX, double destY) {
        if (isChargeable && !hasFired) {
            lastShot = System.currentTimeMillis();
        }
        if (canFire()) {
            clip--;
            hasFired = false;
            Iterable<Object> stuff = pFactory.fire(src, destX, destY);
            for (Object o : stuff) {
                if (o instanceof Projectile && isChargeable) {
                    double percent = ((System.currentTimeMillis() - lastShot) / chargeTime);
                    if (chargeTime > 1)
                        percent = 1;
                    if (chargeTime < 0.25)
                        percent = 0.25;
                    ((Projectile) o).modifyRange(percent);
                }

            }
            lastShot = System.currentTimeMillis();
            return pFactory.fire(src, destX, destY);
        }
        System.out.println("cant fire");
        return null;
    }

    // attempt to fill clip using @param ammoAvailable
    // return the new amount left in ammoAvailable
    public int reload(int ammoAvailable) {
        reloadStart = System.currentTimeMillis();
        for (int i = clip; (i < maxClip) && (ammoAvailable > 0); i++) {
            clip++;
            ammoAvailable--;
        }
        return ammoAvailable;
    }

    public int getClip() {return this.clip;}
    public int getMaxClip() {return this.maxClip;}
    public String getAmmoType() {return this.ammoType;}
    public String getName() {return this.name;}

    protected boolean canFire() {
        System.out.println("Clip: " + clip);
        System.out.println("Reload: " + isReloading());
        System.out.println("time: " + ((System.currentTimeMillis() - lastShot) > fireRate));

        return (clip > 0 && !isReloading()
                && (System.currentTimeMillis() - lastShot > fireRate));
    }

    public boolean isReloading() {
        return (((System.currentTimeMillis() - reloadStart) < reloadRate));
    }

    public boolean isThrowable() {
        return isThrowable;
    }
}
