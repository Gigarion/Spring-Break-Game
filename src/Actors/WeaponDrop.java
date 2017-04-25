package Actors;

import Engine.ActorRequest;
import Util.StdDraw;
import Equipment.Weapon;

/**
 * Created by Gig on 4/1/2017.
 * This is an actor class that gives users weapons
 * and ammo when they interact with it.
 */
public class WeaponDrop extends Actor implements Interactable {
    private String weaponString;
    private String projectileString;
    private int ammoCount;

    public WeaponDrop(int id, double x, double y, String weaponString, String projectileString, int ammoCount) {
        super(id, x, y, 5);
        this.image = null;
        this.weaponString = weaponString;
        this.projectileString = projectileString;
        this.ammoCount = ammoCount;
        this.canHit = false;
    }

    public WeaponDrop(ActorStorage as) {
        super(as.id, as.x, as.y, as.r);
        this.image = as.image;
        this.weaponString = (String) as.extras.get(ActorStorage.WEAPON_STR);
        this.projectileString = (String) as.extras.get(ActorStorage.PROJ_STR);
        this.ammoCount = (Integer) as.extras.get(ActorStorage.AMMO_COUNT);
        this.canHit = false;
        setxScale(as.xScale);
        setyScale(as.yScale);
    }

    public void setImage(String image) {
        this.image = image;
    }

    // methods for ActorStorage
    String getImage() {return this.image;}
    String getWeaponString() { return this.weaponString;}
    String getProjectileString() { return this.projectileString;}
    int getAmmoCount() {return this.ammoCount;}
    // in case the player doesn't pick up the weapon or something else


    // weapon drops don't return anything
    @Override
    public Iterable<Object> interact(Player p) {
        Weapon toGive = new Weapon(weaponString, projectileString);
        String ammoType = toGive.getAmmoType();
        p.giveWeapon(toGive);
        p.giveAmmo(ammoType, ammoCount);
        return null;
    }

    // no need to update
    @Override
    public Iterable<ActorRequest> update() {
        return null;
    }

    @Override
    public void draw(boolean selected) {
        if (image == null) {
            image = "crate.jpg";
            setxScale(20);
            setyScale(20);
        }
        try {
            StdDraw.picture(x, y, "src/img/" + image, xScale, yScale);
        } catch (Exception e) {
            StdDraw.picture(x, y, "img/" + image, xScale, yScale);
        }
        if (selected) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.circle(x, y, 5);
            StdDraw.setPenColor();
        }
    }

    @Override
    public void hit(int damage) {

    }
}
