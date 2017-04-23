package Actors;

import Projectiles.Projectile;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Gig on 4/10/2017.
 * Serializable storage class for saving actor info in files
 * Particularly useful for the Mapbuilder's actor additions.
 * <p>
 * Does not save player weapons, ammo, or inventory
 * <p>
 * May refactor Mailroom to send these instead
 */
public class ActorStorage implements Serializable {
    // type constants
    public static final int ACTOR_TYPE = -1;
    public static final int PLAYER_TYPE = 0;
    // use 1 next
    public static final int WEAPON_DROP_TYPE = 2;
    public static final int MOB_TYPE = 3;
    public static final int PROJ_TYPE = 4;


    // player constants (and mob for maxhp)
    static final String NAME = "NAME";
    static final String MAXHP = "MAX_HP";
    static final String INTERACT_RANGE = "INTERACT";

    // weapondrop constants
    static final String WEAPON_STR = "WPN_STR";
    static final String PROJ_STR = "PROJ_STR";
    static final String AMMO_COUNT = "AMMO_COUNT";

    // projectile constants
    public static final String SRC_ID = "srcid";
    public static final String START_X = "sx";
    public static final String START_Y = "sy";
    public static final String RANGE = "range";
    public static final String VEL = "vel";
    public static final String RAD = "rad";
    public static final String DAMAGE = "DAM";
    public static final String PIERCE_COUNT = "pc";

    private int type;
    public final double x, y, r;
    public final int id;
    final boolean canHit;
    final char passesHeight;
    public final String image;
    final HashMap<String, Object> extras;

    private ActorStorage(Actor a) {
        this.type = ACTOR_TYPE;
        this.x = a.getX();
        this.y = a.getY();
        this.r = a.getR();
        this.id = a.getID();
        this.canHit = a.canHit();
        this.passesHeight = a.getPassesHeight();
        this.extras = new HashMap<>();
        this.image = a.image;
    }

    private void put(String s, Object o) {
        extras.put(s, o);
    }

    public Object get(String s) {
        return extras.get(s);
    }

    public static ActorStorage getActorStore(Actor a) {
        if (a instanceof Player) {
            return getPlayerStore((Player) a);
        } else if (a instanceof WeaponDrop) {
            return getWeaponDropStore((WeaponDrop) a);
        } else if (a instanceof Mob) {
            return getMob((Mob) a);
        } else if (a instanceof Projectile) {
            return getProjectile((Projectile) a);
        } else {
            return null;
        }
    }

    private static ActorStorage getPlayerStore(Player p) {
        ActorStorage toReturn = new ActorStorage(p);
        toReturn.type = PLAYER_TYPE;
        toReturn.put(NAME, p.getName());
        toReturn.put(MAXHP, p.getMaxHP());
        toReturn.put(INTERACT_RANGE, p.getInteractRange());
        return toReturn;
    }

    private static ActorStorage getWeaponDropStore(WeaponDrop wd) {
        ActorStorage toReturn = new ActorStorage(wd);
        toReturn.type = WEAPON_DROP_TYPE;
        toReturn.put(WEAPON_STR, wd.getWeaponString());
        toReturn.put(PROJ_STR, wd.getProjectileString());
        toReturn.put(AMMO_COUNT, wd.getAmmoCount());
        return toReturn;
    }

    private static ActorStorage getMob(Mob m) {
        ActorStorage toReturn = new ActorStorage(m);
        toReturn.type = MOB_TYPE;
        toReturn.put(MAXHP, m.getHP());
        return toReturn;
    }

    private static ActorStorage getProjectile(Projectile p) {
        ActorStorage toReturn = new ActorStorage(p);
        toReturn.type = PROJ_TYPE;
        toReturn.put(SRC_ID, p.getSrcID());
        toReturn.put(START_X, p.getStartX());
        toReturn.put(START_Y, p.getStartY());
        toReturn.put(RANGE, p.getRange());
        toReturn.put(VEL, p.getVel());
        toReturn.put(RAD, p.getRad());
        toReturn.put(DAMAGE, p.getDamage());
        toReturn.put(PIERCE_COUNT, p.getPierceCount());
        return toReturn;
    }

    public static Actor getActor(ActorStorage as) {
        switch (as.getType()) {
            case MOB_TYPE:
                return new Mob(as);
            case PLAYER_TYPE:
                return new Player(as);
            case WEAPON_DROP_TYPE:
                return new WeaponDrop(as);
            case PROJ_TYPE:
                return new Projectile(as);
            default:
                return null;
        }
    }

    public int getType() {
        return type;
    }
}
