package Weapons;

import Actors.Actor;
import Animations.FlyText;
import Projectiles.Projectile;
import Projectiles.ProjectileFactory;
import Util.StdDraw;

import java.util.LinkedList;

/**
 * Created by Gig on 3/31/2017.
 * weaponString format:
 * 0    1     2        3   4   5         6          7
 * name ammo  max clip FR  RR  throwable chargeable maxChargeTime
 */
public class Weapon {
    private ProjectileFactory pFactory;
    private String name;
    private int clip;
    private int maxClip;
    private int fireRate;
    private int reloadRate;
    private long lastShot;
    private long reloadStart;
    private boolean isThrowable;
    private boolean isChargeable;
    private int maxChargeTime;

    private long startCharge; // when did it start charging?
    private boolean charging; // is this weapon charging?

    private String ammoType;

    public Weapon(String weaponString, String factoryString) {
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
            this.maxChargeTime = Integer.parseInt(info[7]);
    }

    public void equip() {
        this.lastShot = System.currentTimeMillis();
    }

    // attempt to fire this weapon, only valid if not reloading,
    // constrained by fireRate, and current clip.
    // returns the stack of objects associated with firing this weapon
    public Iterable<Object> fire(Actor src, double destX, double destY) {
        if (canFire()) {
            clip--;
            Iterable<Object> stuff = pFactory.fire(src, destX, destY);
            for (Object o : stuff) {
                if (o instanceof Projectile && isChargeable) {
                    double percent = ((System.currentTimeMillis() - lastShot) / maxChargeTime);
                    if (maxChargeTime > 1)
                        percent = 1;
                    if (maxChargeTime < 0.25)
                        percent = 0.25;
                    ((Projectile) o).modifyRange(percent);
                }

            }
            lastShot = System.currentTimeMillis();
            LinkedList toReturn = (LinkedList) pFactory.fire(src, destX, destY);
            return toReturn;
        }
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

    private boolean canFire() {
        return ((clip > 0 || ammoType.equals("Melee")) && !isReloading()
                && (System.currentTimeMillis() - lastShot > fireRate));
    }

    private boolean isReloading() {
        return (((System.currentTimeMillis() - reloadStart) < reloadRate));
    }

    public boolean isThrowable() {
        return isThrowable;
    }

    public synchronized void charge() {
        if (!isChargeable || charging || !canFire()) {
            return;
        }
        charging = true;
        startCharge = System.currentTimeMillis();
    }

    public synchronized Iterable<Object> release(Actor src, double destX, double destY) {
        if (!isChargeable || !charging) return null;

        long timeCharged = System.currentTimeMillis() - startCharge;
        double chargeRatio = timeCharged / maxChargeTime;
        if (chargeRatio < 0.25)
            chargeRatio = 0.25;
        if (chargeRatio > 1)
            chargeRatio = 1;
        charging = false;
        Iterable<Object> toReturn = fire(src, destX, destY);
        for (Object o : toReturn) {
            if (o instanceof Projectile) {
                ((Projectile) o).modifyRange(chargeRatio);
                ((Projectile) o).modifyDamage(chargeRatio);
            }
        }
        return toReturn;
    }

    public boolean isChargeable() {
        return isChargeable;
    }

    public boolean isCharging() {
        return isChargeable && charging;
    }

    public double getChargeRatio() {
        if (!charging)
            return 0;

        return Math.min(1, (System.currentTimeMillis() - startCharge) / (double) maxChargeTime);
    }
}
