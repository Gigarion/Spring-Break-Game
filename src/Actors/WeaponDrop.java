package Actors;

import Engine.ActorRequest;
import Util.StdDraw;
import Weapons.Weapon;

/**
 * Created by Gig on 4/1/2017.
 * This is an actor class that gives users weapons
 * and ammo when they interact with it.
 */
public class WeaponDrop extends Actor implements Interactable {
    private String image;
    private String weaponString;
    private String projectileString;
    private int ammoCount;

    public WeaponDrop(int id, double x, double y, String weaponString, String projectileString, int ammoCount) {
        super(id, x, y, 5);
        image = null;
        this.weaponString = weaponString;
        this.projectileString = projectileString;
        this.ammoCount = ammoCount;
        this.canHit = false;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // in case the player doesn't pick up the weapon or something else
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
        if (image == null)
            image = "crate.jpg";
        try {
            StdDraw.picture(x, y, "src/img/" + image);
        } catch (Exception e) {
            StdDraw.picture(x, y, "img/" + image);
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
