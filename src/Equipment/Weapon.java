package Equipment;

import Actors.Actor;
import Projectiles.Projectile;
import Projectiles.ProjectileFactory;
import Util.Constants;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

import java.util.LinkedList;

/**
 * Created by Gig on 3/31/2017.
 * weaponString format:
 * 0    1     2        3   4   5         6          7             8      9
 * name ammo  max clip FR  RR  throwable chargeable maxChargeTime weight maxCount
 */
public class Weapon extends Item {
    String weaponString, pFactoryString;
    private ProjectileFactory pFactory;
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

    private String usesAmmo;

    public Weapon(String weaponString, String pFactoryString) {
        super("not_set", WEAPON_TYPE, 1, 1);
        this.weaponString = weaponString;
        this.pFactoryString = pFactoryString;
        this.loadFromString(weaponString);
        this.clip = 0;
        this.reloadStart = 0;
        this.lastShot = 0;
        this.pFactory = new ProjectileFactory(pFactoryString);
        this.count = 1;
    }

    private void loadFromString(String weaponString) {
        System.out.println(weaponString);
        String[] info = weaponString.split("/");
        this.name = info[0];
        this.usesAmmo = info[1];
        this.maxClip = Integer.parseInt(info[2]);
        this.fireRate = Integer.parseInt(info[3]);
        this.reloadRate = Integer.parseInt(info[4]);
        this.isThrowable = Boolean.parseBoolean(info[5]);
        if (isThrowable) this.ammoType = usesAmmo;
        this.isChargeable = Boolean.parseBoolean(info[6]);
        if (isChargeable)
            this.maxChargeTime = Integer.parseInt(info[7]);
        this.weight = Double.parseDouble(info[8]);
        this.maxCount = Integer.parseInt(info[9]);
        this.type = WEAPON_TYPE;
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
            return stuff;
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
    public String getUsesAmmo() {return this.usesAmmo;}

    private boolean canFire() {
        return ((clip > 0 || ammoType.equals(Constants.AMMO_MELEE) || ammoType.equals(Constants.NOT_AMMO)) && !isReloading()
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
