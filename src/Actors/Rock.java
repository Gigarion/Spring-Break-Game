package Actors;

import Util.StdDraw;
import Weapons.WeaponRock;

/**
 * Created by Gig on 3/28/2017.
 */
public class Rock extends Actor implements Interactable {
    public Rock(int id, double x, double y) {
        super(id, x, y, 5);
        setInteractable(true);
    }
    @Override
    public void update() {}
    @Override
    public void draw(boolean selected) {
        StdDraw.setPenColor(StdDraw.DARK_GRAY);
        StdDraw.filledCircle(x, y, 5);
        if (selected) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.filledSquare(x, y, 5);
        }
        StdDraw.setPenColor();
    }
    @Override
    public void hit(int damage) {}

    @Override
    public void interact(Player p) {
        if (p.getAmmoCount("rock") == 0)
            p.giveWeapon(new WeaponRock());
        p.giveAmmo("rock", 1);
    }
}
