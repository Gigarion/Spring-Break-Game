package Engine;

import Actors.Actor;
import Actors.Mob;
import Actors.Player;
import Animations.HitScanLine;
import Mailroom.Package;
import Mailroom.ServerMailroom;
import Projectiles.HitScan;
import Projectiles.Projectile;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gig on 3/22/2017.
 * Big daddy server
 * handles some logic and spreading the news
 */
public class ServerEngine {
    private ServerMailroom mailroom;
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ConcurrentLinkedQueue<Projectile> projectileQueue;
    private AtomicInteger nextFreeId;

    public ServerEngine() {
        mailroom = new ServerMailroom(2);
        this.actorMap = new ConcurrentHashMap<>();
        this.projectileQueue = new ConcurrentLinkedQueue<>();
        setTimers();
        nextFreeId = new AtomicInteger(0);
    }

    private void setTimers() {
        // TODO: optimize by removing this timer, make it a direct call
        // TODO: from the mail thread
        Timer checkMailTimer = new Timer("Server Engine Mail Timer", true);
        checkMailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    handleMessages();
                }
            }
        }, 0);
        Timer updateTimer = new Timer("Server update timer", true);
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, 1);
    }

    private void update() {
        for (Actor a : actorMap.values()) {
            a.update();
        }
    }

    // get the current slew of messages and do stuff with them.
    // I should probs modularize this function
    private void handleMessages() {
        for (Package p : mailroom.getMessages()) {
            System.out.println("heyyy");
            switch (p.getType()) {
                case Package.WELCOME: { // requires port #'s
                    // TODO: think about handshakey game init protocols
                    // TODO: think about engine orientation
                    // upon getting a welcome, send a return packet containing
                    // the id the player should be assigned.
                    int id = getNextId();
                    Player newPlayer = (Player) p.getPayload();
                    newPlayer.setID(id);
                    actorMap.put(id, newPlayer);
                    mailroom.sendPackage(new Package(id, Package.WELCOME), p.getPort());
                    Mob m = new Mob(getNextId(), 800, 800, 12, 10);
                    actorMap.put(m.getID(), m);
                    mailroom.sendPackage(new Package(m, Package.ACTOR));
                }
                break;

                case Package.HITSCAN: { // chill, move engine logic to here
                    // TODO: add fireHitscan method to this class
                    // runs the hitscan, then asks whether
                    HitScan hs = (HitScan) p.getPayload();
                    int srcId = Integer.parseInt(p.getExtra());
                    for (Actor hit : fireHitScan(hs, srcId)) {
                        mailroom.sendPackage(new Package(hs.getDamage(), Package.HIT, Integer.toString(hit.getID())));
                    }
                    if (hs.getShowLine())
                        mailroom.sendPackage(new Package(new HitScanLine(hs), Package.ANIMATE));
                }
                break;

                case Package.PROJECT: { // chill, ""
                    Projectile proj = (Projectile) p.getPayload();
                    projectileQueue.add(proj);
                    mailroom.sendPackage(p);
                }
                break;

                case Package.NEW_POS: {
                    int id = (Integer) p.getPayload();
                    double[] coords = Package.extractCoords(p.getExtra());
                    Actor a = actorMap.get(id);
                    if (a != null)
                        a.moveTo(coords[0], coords[1]);
                    mailroom.sendPackage(p);
                }
                break; // requires actor/player IDS

                case Package.ANIMATE: {
                    mailroom.sendPackage(p);
                }
                break;

                case Package.ACTOR: { // actually this one is less chill
                    // add actor to my queue
                    Actor a = (Actor) p.getPayload();
                    if (a.getID() == -1)
                        a.setID(getNextId());
                    actorMap.put(a.getID(), a);
                    // broadcast packet
                    mailroom.sendPackage(p);
                }
                break;

                case Package.HIT: {
                    System.out.println("ERROR: hit package @ server");
                }
                break; // doesn't occur
                case Package.REMOVE: {
                    System.out.println("heyyyy");
                    Actor actor = actorMap.get((Integer) p.getPayload());
                    if (actor != null) {
                        actorMap.remove(actor);
                        if (actor instanceof Mob) {
                            int x = 10 + (int) (Math.random() * 580);
                            Mob mob = new Mob(-1, x, 800, 12, 10);
                            mailroom.sendPackage(new Package(mob, Package.ACTOR));
                        }
                        System.out.println("gone");
                    }
                    mailroom.sendPackage(p);
                    break;
                }
                default:
                    System.out.println("unhandled package type: " + p.getType());
                    break;
            }
        }
    }

    // fire a hitscan, currently ignores hitting the initiating player
    public Iterable<Actor> fireHitScan(HitScan hs, int id) {
        ConcurrentLinkedQueue<Actor> areHit = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Actor> notHit = new ConcurrentLinkedQueue<>(actorMap.values());
        notHit.remove(actorMap.get(id));
        double angle = getRads(hs.getSrcX(), hs.getSrcY(), hs.getDestX(), hs.getDestY());
        double currX = hs.getSrcX();
        double currY = hs.getSrcY();
        double dist = 0;

        while (inBounds(currX, currY) && dist < hs.getRange()) {
            double xDiff = currX - hs.getSrcX();
            double yDiff = currY - hs.getSrcY();
            dist = Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
            currX += (2 * Math.cos(angle));
            currY += (2 * Math.sin(angle));
            Mob a = new Mob(15, currX, currY, 5, 0);
            int hits = 0;
            System.out.println("notHitSize " + notHit.size());
            System.out.println("actormap size " + actorMap.size());
            for (Actor mob : notHit) {
                if (mob.collides(a) && !areHit.contains(mob)) {
                    System.out.println("hit");
                    mob.hit(hs.getDamage());
                    checkActor(mob);
                    areHit.add(mob);
                    notHit.remove(mob);
                    hits++;
                }
                if (hits > hs.getPierceCount())
                    break;
            }
            if (hits > hs.getPierceCount())
                break;
        }
        return areHit;
    }

    private void checkActor(Actor a) {
        if (a instanceof Mob) {
            if (((Mob) a).getHP() <= 0) {
                System.out.println("checked");
                actorMap.remove(a.getID());
                mailroom.sendPackage(new Package(a.getID(), Package.REMOVE));
            }
        }
    }

    private boolean inBounds(double x, double y) {
        // TODO: return these 10000's to proper constants, make clients get deets from server
        return (x < (10000 * 1.002) && x > -1 && y < (10000 * 1.002) && y > -1);
    }

    private double getRads(double srcX, double srcY, double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - srcY, destX - srcX));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    private int getNextId() {
        return nextFreeId.getAndIncrement();
    }
}
