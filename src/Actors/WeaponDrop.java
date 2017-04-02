package Actors;

import Util.StdDraw;
import Weapons.Weapon;

/**
 * Created by Gig on 4/1/2017.
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
        //setInteractable(true);
        setCanHit(false);
    }

    // in case the player doesn't pick up the weapon or something else
    @Override
    public Iterable<Actor> interact(Player p) {
        Weapon toGive = new Weapon(weaponString, projectileString);
        String ammoType = toGive.getAmmoType();
        p.giveWeapon(toGive);
        //p.giveAmmo(ammoType, ammoCount);
        return null;
    }

    // no need to update
    @Override
    public void update() {
    }

    @Override
    public void draw(boolean selected) {
        if (image == null)
            image = "img/sord.png";
        try {
            StdDraw.picture(x, y, "src/img/crate.jpg", 20, 20);
        } catch (Exception e) {
            StdDraw.picture(x, y, "img/crate.jpg", 20, 20);
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
