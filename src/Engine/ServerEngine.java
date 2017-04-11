package Engine;

import Actors.*;
import Animations.HitScanLine;
import Mailroom.Package;
import Mailroom.ServerMailroom;
import Maps.GameMap;
import Maps.MapGrid;
import Projectiles.HitScan;
import Projectiles.Projectile;

import java.awt.print.PrinterJob;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gig on 3/22/2017.
 * Big daddy server
 * handles almost all logic and spreading the news
 * actively pushing worry about network congestion and
 * memory issues out of mind
 */


public class ServerEngine {
    private ServerMailroom mailroom;
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ConcurrentHashMap<Integer, Integer> portToPlayerMap;
    private AtomicInteger nextFreeId;
    private int maxLogX, maxLogY;
    private EventLog eventLog;
    private MapGrid mapGrid;

    public interface MessageHandler {
        void handleMessage(Package p);
    }

    public ServerEngine(int playerCap, int port) {
        maxLogX = 2000;
        maxLogY = 2000;
        System.out.println("server");

        //TODO: make this not static and shitty
        this.mapGrid = new GameMap("ServerTest.gm").getMapGrid();

        this.eventLog = new EventLog(port);

        this.nextFreeId = new AtomicInteger(0);
        this.actorMap = new ConcurrentHashMap<>();
        this.portToPlayerMap = new ConcurrentHashMap<>();
        for (int i = 0; i < 50; i++)
            makeRocks();
        this.mailroom = new ServerMailroom(playerCap, this::handleMessage);
        setTimers();
    }

    private void setTimers() {
        Timer updateTimer = new Timer("Server update timer", true);
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    update();
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0);
    }

    private void update() {
        for (Actor a : actorMap.values()) {
            Iterable<ActorRequest> requests = a.update();
            if (requests != null) {
                for (ActorRequest ar : requests) {
                    handleActorRequest(a, ar);
                }
            }
            checkActor(a);
        }
    }

    private void handleActorRequest(Actor a, ActorRequest ar) {
        switch(ar.getType()) {
            case ActorRequest.MOVE: {
                double[] coords = Package.extractCoords(ar.getExtra());
                if (mapGrid.validMove(coords[0], coords[1], a))
                    a.update(ar);
                else if (a instanceof Projectile) {
                    removeActor(a);
                }
                mailroom.sendPackage(new Package(a.getID(), Package.NEW_POS, ar.getExtra()));
            }
                break;
            default: System.out.println("not a move");
        }
    }

    /*******************************************************
     *  Mail handling, main switch and helper functions
     *******************************************************/

    private synchronized void handleMessage(Package p) {
        switch (p.getType()) {
            case Package.WELCOME: handleWelcome(p); break;
            case Package.HITSCAN: handleHitScan(p); break;
            case Package.PROJECT: handleProjectile(p); break;
            case Package.NEW_POS: handleNewPosition(p); break;
            case Package.ANIMATE: handleAnimation(p); break;
            case Package.ACTOR: handleActor(p); break;
            case Package.HIT: System.out.println("ERROR: hit package @ server");break; // doesn't occur
            case Package.REMOVE: handleRemove(p); break;
            case Package.PING: handlePing(p); break;
            case Package.DISCONNECT: handleDisconnect(p); break;
            default:   System.out.println("unhandled package type: " + p.getType()); break;
        }
    }

    private void handleWelcome(Package p) {
        System.out.println("welcomed");
        // upon getting a welcome, send a return packet containing
        // the id the player should be assigned.
        int id = getNextId();
        Player newPlayer = (Player) p.getPayload();
        newPlayer.setID(id);
        actorMap.put(id, newPlayer);
        portToPlayerMap.put(p.getPort(), id);
        mailroom.sendPackage(new Package(id, Package.WELCOME), p.getPort());

        // handle onboarding
        setupNewUser(newPlayer, p.getPort());

        // for now, make a new mob whenever a player gets added
        int x = 10 + (int) (Math.random() * 580);
        Mob m = new Mob(getNextId(), x, 800, 12, 10);
        actorMap.put(m.getID(), m);
        mailroom.sendActor(m);
    }

    private void handleHitScan(Package p) {
        // runs the hitscan, then asks whether
        HitScan hs = (HitScan) p.getPayload();
        int srcId = Integer.parseInt(p.getExtra());
        for (Actor hit : fireHitScan(hs, srcId)) {
            mailroom.sendPackage(new Package(hit.getID(), Package.HIT, Integer.toString(hs.getDamage())));
        }
        if (hs.getShowLine())
            mailroom.sendPackage(new Package(new HitScanLine(hs), Package.ANIMATE));
    }

    private void handleProjectile(Package p) {
        Projectile proj = (Projectile) p.getPayload();
        int id = getNextId();
        proj.setID(id);
        actorMap.put(id, proj);
        mailroom.sendPackage(p);
    }

    private void handleNewPosition(Package p) {
        int id = (Integer) p.getPayload();
        double[] coords = Package.extractCoords(p.getExtra());
        Actor a = actorMap.get(id);
        if (a != null)
            a.moveTo(coords[0], coords[1]);
        mailroom.sendPackage(p);
    }

    private void handleAnimation(Package p) {
        mailroom.sendPackage(p);

    }

    private void handleActor(Package p) {
        ActorStorage as = (ActorStorage) p.getPayload();
        Actor a = null;
        switch (as.getType()) {
            case ActorStorage.PLAYER_TYPE:
                a = new Player(as);
                break;
            case ActorStorage.MOB_TYPE:
                a = new Mob(as);
                break;
            case ActorStorage.WEAPON_DROP_TYPE:
                a = new WeaponDrop(as);
                break;
            default: System.out.println("bad actorstorage type"); return;
        }
        if (a.getID() == -1)
            a.setID(getNextId());
        actorMap.put(a.getID(), a);
        // broadcast packet
        mailroom.sendActor(a);
    }

    private void handleRemove(Package p) {
        Actor actor = actorMap.get(p.getPayload());
        if (actor != null) {
            actorMap.remove(actor.getID());
            if (actor instanceof Mob) {
                killMob(actor);
            }
        }
        mailroom.sendPackage(p);
    }

    private void handleDisconnect(Package p) {
        int actorId = portToPlayerMap.get(p.getPayload());
        actorMap.remove(actorId);
        mailroom.sendPackage(new Package(actorId, Package.REMOVE));
    }

    private void handlePing(Package p) {
        mailroom.sendPackage(p, p.getPort());
    }

    // not technically mail but relevant, called from handleWelcome
    private void setupNewUser(Actor newPlayer, int port) {
        String sizeString = Package.formCoords(2000, 2000);

        mailroom.sendPackage(new Package(sizeString, Package.SCR_SIZE));

        GameMap gameMap = new GameMap("ServerTest.gm");
        mailroom.sendPackage(new Package(gameMap.getStorage(), Package.GAME_MAP));

        for (Actor actor : actorMap.values()) {
            mailroom.sendActor(actor, port);
        }
        mailroom.sendActor(newPlayer);
    }

    // fire a hitscan, currently ignores hitting the initiating player
    private Iterable<Actor> fireHitScan(HitScan hs, int id) {
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
            for (Actor mob : notHit) {
                if (mob.collides(a) && !areHit.contains(mob)) {
                    mob.hit(hs.getDamage());
                    checkActor(mob);
                    areHit.add(mob);
                    notHit.remove(mob);
                    hits++;
                }
                if (hits >= hs.getPierceCount())
                    break;
            }
            if (hits >= hs.getPierceCount())
                break;
        }
        System.out.println(areHit.size());
        return areHit;
    }

    /******************************************************
     *  Various utility functions
     ******************************************************/

    // check actor for liveness/validity
    private synchronized void checkActor(Actor a) {
        int id = a.getID();
        if (a instanceof Projectile) {
            Projectile p = (Projectile) a;
            if (p.outOfRange()) {
                removeActor(p);
            }
            for (Actor target : actorMap.values()) {
                if (target == p || !target.canHit() || target.getID() == p.getSrcID())
                    continue;
                if (target.collides(p)) {
                    target.hit(p.getDamage());
                    mailroom.sendPackage(new Package(target.getID(), Package.HIT, p.getDamage() + ""));
                    int piercesLeft = p.decrementPierceCount();
                    if (piercesLeft == 0) {
                        removeActor(p);
                    }
                }
            }
        }

        if (a instanceof Mob) {
            if (((Mob) a).getHP() <= 0) {
                actorMap.remove(id);
                killMob(a);
            }
        }

        if (a instanceof Player) {
            if (((Player) a).getHP() <=0) {
                removeActor(a);
                System.out.println("removing player");
                return;
            }
        }

        if (!inBounds(a.getX(), a.getY())) {
            removeActor(a);
            if (a instanceof Mob)
                killMob(a);
        }
    }

    // is x, y in bounds
    private boolean inBounds(double x, double y) {
        return (x < (maxLogX * 1.002) && x > -1 && y < (maxLogY * 1.002) && y > -1);
    }

    // angle calculation, thx stackoverflow
    private double getRads(double srcX, double srcY, double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - srcY, destX - srcX));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    // gets next free id for assignment and increments
    private int getNextId() {
        return nextFreeId.getAndIncrement();
    }

    // @name
    private void killMob(Actor a) {
        mailroom.sendPackage(new Package(a.getID(), Package.REMOVE));
        int x = 10 + (int) (Math.random() * 580);
        int id = getNextId();
        Mob mob = new Mob(id, x, 800, 12, 80);
        actorMap.put(id, mob);
        mailroom.sendActor(mob);
    }

    private void makeRocks() {
        int x = 10 + (int) (Math.random() * 580);
        int id = getNextId();
        actorMap.put(id, new WeaponDrop(id, x, 500, "Rock/Rock/1/150/100/true/false/", "P/200/20/1/1/5/.8/Rock.png/", 1));
    }

    private void removeActor(Actor a) {
        actorMap.remove(a.getID());
        mailroom.sendPackage(new Package(a.getID(), Package.REMOVE));
    }
}
