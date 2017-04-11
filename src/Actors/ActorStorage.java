package Actors;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Gig on 4/10/2017.
 * Serializable storage class for saving actor info in files
 * Particularly useful for the Mapbuilder's actor additions.
 *
 * Does not save player weapons, ammo, or inventory
 *
 * May refactor Mailroom to send these instead
 */
public class ActorStorage implements Serializable {
    public static final int ACTOR_TYPE  = -1;
    public static final int PLAYER_TYPE = 0;
    public static final int TERRAIN_TYPE = 1;
    public static final int WEAPON_DROP_TYPE = 2;
    public static final int MOB_TYPE = 3;


    static final String NAME = "NAME";
    static final String MAXHP = "MAX_HP";
    static final String INTERACT_RANGE = "INTERACT";

    static final String WEAPON_STR = "WPN_STR";
    static final String PROJ_STR = "PROJ_STR";
    static final String AMMO_COUNT = "AMMO_COUNT";

    private int type;
    double x, y, r;
    int id;
    boolean canHit;
    char passesHeight;
    String image;
    HashMap<String, Object> extras;

    private ActorStorage(Actor a) {
        this.type = ACTOR_TYPE;
        this.x = a.getX();
        this.y = a.getY();
        this.r = a.getR();
        this.id = a.getID();
        this.canHit = a.canHit();
        this.passesHeight = a.getPassesHeight();
        this.extras = new HashMap<>();
    }

    public static ActorStorage getPlayerStore(Player p) {
        ActorStorage toReturn = new ActorStorage(p);
        toReturn.type = PLAYER_TYPE;
        toReturn.extras.put(NAME, p.getName());
        toReturn.extras.put(MAXHP, p.getMaxHP());
        toReturn.extras.put(INTERACT_RANGE, p.getInteractRange());
        return toReturn;
    }

    public static ActorStorage getWeaponDropStore(WeaponDrop wd) {
        ActorStorage toReturn = new ActorStorage(wd);
        toReturn.type = WEAPON_DROP_TYPE;
        toReturn.image = wd.getImage();
        toReturn.extras.put(WEAPON_STR, wd.getWeaponString());
        toReturn.extras.put(PROJ_STR, wd.getProjectileString());
        toReturn.extras.put(AMMO_COUNT, wd.getAmmoCount());
        return toReturn;
    }

    public static ActorStorage getMob(Mob m) {
        ActorStorage toReturn = new ActorStorage(m);
        toReturn.type = MOB_TYPE;
        toReturn.extras.put(MAXHP, m.getHP());
        return toReturn;
    }

    public int getType() {return type;}
}
