package Engine;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import Actors.*;
import Projectiles.*;
import Util.StdDraw;
import Animations.*;

import java.awt.event.KeyEvent;

public class Engine {
    private Timer logicTimer = new Timer("Logic Timer", true);
    private Timer drawTimer = new Timer("Draw Timer", true);
    private ConcurrentLinkedQueue<Mob> mobQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Actor> actorQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Actor> projectileQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Animation> hitScanQueue = new ConcurrentLinkedQueue<>();
    private int xMin, xMax, yMin, yMax;
    private Player player;
    private int frame;
    private int stopCount;

    public Engine(int xMin, int xMax, int yMin, int yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.frame = 0;
        this.stopCount = 0;

        drawTimer.schedule(new TimerTask() {
            public void run() {
                drawTick();
            }
        }, 0, 16);
        logicTimer.schedule(new TimerTask() {
            public void run() {
                logicTick();
            }
        }, 200, 1);

    }

    // logic loop cycle
    private void logicTick() {
        for (Actor actor : actorQueue) {
            actor.update();
            checkActor(actor);
        }
        if ((frame % 100 == 0) && StdDraw.mousePressed()) {
            mouseClick((int) StdDraw.mouseX(), (int) StdDraw.mouseY());
        }
        handleKeyboard();
        frame = ((frame + 1) % 10000);
        if (stopCount > 0)
            stopCount--;
    }

    // draw loop cycle
    private void drawTick() {
        StdDraw.clear();
        StdDraw.line(900, 0, 900, 900);

        for (Animation hsl : hitScanQueue) {
            if (hsl.getTTL() <= 0)
                hitScanQueue.remove(hsl);
            hsl.draw(frame);
        }
        for (Actor actor : actorQueue) {
            if (checkActor(actor))
                actor.draw();
        }
        StdDraw.show();
    }

    private void handleKeyboard() {
        if (StdDraw.isKeyPressed(KeyEvent.VK_W)) {
            player.moveY(.5);
            StdDraw.setYscale(50, 950);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_S)) {
            player.moveY(-0.5);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_A)) {
            player.moveX(-0.5);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_D)) {
            player.moveX(0.5);
        }
    }

    public void setPlayer(Player a) {
        addActor(a);
        this.player = a;
    }

    public boolean addMob(Mob m) {
        mobQueue.add(m);
        return actorQueue.add(m);
    }

    public boolean addActor(Actor a) {
        return actorQueue.add(a);
    }

    public void fireProjectile(Actor src, double x, double y) {
        Projectile p = new Projectile(src.getX(), src.getY(), 5, 200);
        p.setDest(x, y);
        p.setSpeed(1);
        actorQueue.add(p);
        projectileQueue.add(p);
    }

    private void mouseClick(int x, int y) {
        if (stopCount == 0) {
            hitScanQueue.add(new SwingAnimation(player, 8, "sord.png", StdDraw.mouseX(), StdDraw.mouseY()));
            stopCount += 3;
        }
        fireProjectile(player, Util.StdDraw.mouseX(), Util.StdDraw.mouseY());
        fireHitScan(player, Util.StdDraw.mouseX(), Util.StdDraw.mouseY());
        for (Actor a : actorQueue) {
            if (a.contains(x, y)) {
                if (a instanceof Mob) {
                    Mob m = (Mob) a;
                    //m.hit(20);
                }
            }
        }
    }

    private boolean checkActor(Actor a) {
        if (a instanceof Projectile) {
            Projectile p = (Projectile) a;
            if (p.outOfRange()) {
                actorQueue.remove(a);
                projectileQueue.remove(a);
            }
            for (Mob mob : mobQueue) {
                if (mob.collides(a)) {
                    System.out.println("ouch");
                    mob.hit(20);
                }
            }
        }

        if (a instanceof Mob) {
            Mob m = (Mob) a;
            if (m.getHP() <= 0) {
                actorQueue.remove(a);
                mobQueue.remove(a);
                int x = 10 + (int) (Math.random() * 580);
                Mob mob = new Mob(x, 900, 10, 100);
                addMob(mob);
            }
        }

        if (!inBounds(a.getX(), a.getY())) {
            actorQueue.remove(a);

            if (a instanceof Mob) {
                mobQueue.remove(a);
                int x = 10 + (int) (Math.random() * 580);
                Mob mob = new Mob(x, 900, 10, 100);
                addMob(mob);
            }
            if (a instanceof Projectile)
                projectileQueue.remove(a);

            if (a instanceof Player) {
                setPlayer(new Player());
            }
            return false;
        }
        return true;
    }

    private boolean inBounds(double x, double y) {
        return (x < (xMax * 1.02) && x > (xMin * 98) && y < (yMax * 1.02) && y > (yMin * 0.98));
    }

    // fire a hitscan, currently ignores hitting (all?) players
    public Iterable<Actor> fireHitScan(Actor src, double destX, double destY) {
        ConcurrentLinkedQueue<Actor> mobsHit = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Mob> notHit = new ConcurrentLinkedQueue<>(mobQueue);
        double angle = getRads(destX, destY);
        double currX = src.getX();
        double currY = src.getY();

        while (currX > xMin && currX < xMax && currY > yMin && currY < yMax) {
            currX += (2 * Math.cos(angle));
            currY += (2 * Math.sin(angle));
            Actor a = new Actor(currX, currY, 1);
            for (Mob mob : notHit) {
                if (mob.collides(a) && !mobsHit.contains(mob)) {
                    mob.hit(200);
                    mobsHit.add(mob);
                    notHit.remove(mob);
                }
            }
        }
        hitScanQueue.add(new HitScanLine(src.getX(), src.getY(), currX, currY));
        return mobsHit;
    }

    private double getRads(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - player.getY(), destX - player.getX()));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }
}
