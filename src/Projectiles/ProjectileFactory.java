package Projectiles;

import Actors.Actor;
import Animations.HitScanLine;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Gig on 3/31/2017.
 *  Factory object for easy creation of weapons from data files
 *  factoryString format
 *
 *  Projectile
 *  0    1     2      3  4     5   6     7
 *  type range damage pc count rad speed image
 *
 *  HitScan
 *  0    1     2      3  4     5
 *  type range damage pc count showLine
 */
public class ProjectileFactory implements Serializable {
    private final char PROJECTILE = 'P';
    private final char HITSCAN = 'H';
    private char type;
    private int damage;
    private int radius;
    private double speed;
    private int pierceCount;
    private int count;
    private double range;
    private String image;
    private double accuracy;
    private boolean hitScanLine;

    public ProjectileFactory(String factoryString) {
        String[] info = factoryString.split("/");
        this.type = info[0].charAt(0);
        this.range = Integer.parseInt(info[1]);
        this.damage = Integer.parseInt(info[2]);
        this.pierceCount = Integer.parseInt(info[3]);
        this.count = Integer.parseInt(info[4]);
        if (type == HITSCAN) {
            this.hitScanLine = Boolean.parseBoolean(info[5]);
        }
        if(type == PROJECTILE) {
            System.out.println(info);
            System.out.println(info[6] + " 6 ");
            this.radius = Integer.parseInt(info[5]);
            this.speed = Double.parseDouble(info[6]);
            this.image = "img/" + info[7];
        }
    }

    public Iterable<Object> fire(Actor src, double destX, double destY) {
        LinkedList<Object> toReturn = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            if (type == HITSCAN) {
                HitScan hs = getHitScan(src, destX, destY);
                toReturn.add(hs);
                if (hitScanLine)
                    toReturn.add(new HitScanLine(hs));
            }
            if (type == PROJECTILE) {
                toReturn.add(getProjectile(src, destX, destY));
            }
        }
        return toReturn;
    }

    private HitScan getHitScan(Actor src, double destX, double destY) {
        return new HitScan(src, destX, destY, damage, pierceCount, range);
    }
    private Projectile getProjectile(Actor src, double destX, double destY) {
        return new Projectile(src, destX, destY, radius, range, speed, damage, image);
    }
}
