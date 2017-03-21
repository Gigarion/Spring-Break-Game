package Engine;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import Actors.*;
import Projectiles.*;
import Util.StdDraw;
import Animations.*;

import java.awt.event.KeyEvent;

public class Engine {
    private static final double MOVEMENT_SIZE = 0.5;
    private Timer logicTimer = new Timer("Logic Timer", true);
    private Timer drawTimer = new Timer("Draw Timer", true);
    private ConcurrentLinkedQueue<Mob> mobQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Actor> actorQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Actor> projectileQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Animation> animationQueue = new ConcurrentLinkedQueue<>();
    // min always zero, visible is a square
    private int maxLogX, maxLogY;
    // radius around the player normally seen
    private int visibleRadius;
    private Player player;
    private int frame;
    private int stopCount;
    private int clickedButton;

    public Engine(int lXMax, int lYMax, int vRadius) {
        this.maxLogX = lXMax;
        this.maxLogY = lYMax;
        this.visibleRadius = vRadius;
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
        if ((frame % 20 == 0) && StdDraw.mousePressed()) {
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
        StdDraw.rectangle(450, 450, 300, 300);
        StdDraw.line(getVisibleXMax(), 0, getVisibleXMax(),  maxLogY);

        for (Animation hsl : animationQueue) {
            if (hsl.getTTL() <= 0)
                animationQueue.remove(hsl);
            hsl.draw(frame);
        }
        for (Actor actor : actorQueue) {
            actor.draw();
        }
        StdDraw.show();
    }

    private double getVisibleYMin() {
        if (player.getY() - visibleRadius <= 0)
            return 0;
        if (player.getY() + visibleRadius > maxLogY)
            return maxLogY - 2*visibleRadius;
        return player.getY() - visibleRadius;
    }

    private double getVisibleYMax() {
        if (player.getY() + visibleRadius >= maxLogY)
            return maxLogY;
        if (player.getY() - visibleRadius <= 0)
            return 2*visibleRadius;
        return player.getY() + visibleRadius;
    }

    private double getVisibleXMin() {
        if (player.getX() - visibleRadius <= 0)
            return 0;
        if (player.getX() + visibleRadius >= maxLogX)
            return maxLogX - 2*visibleRadius;
        return player.getX() - visibleRadius;
    }

    private double getVisibleXMax() {
        if (player.getX() + visibleRadius > maxLogX)
            return maxLogX;
        if (player.getX() - visibleRadius < 0)
            return 2*visibleRadius;
        return player.getX() + visibleRadius;
    }

    private void handleKeyboard() {
        if (StdDraw.isKeyPressed(KeyEvent.VK_W)) {
            player.moveY(MOVEMENT_SIZE);
            if (getVisibleYMax() < maxLogY && getVisibleYMax() + MOVEMENT_SIZE < maxLogY) {
                //System.out.println(getVisibleYMax() + " " + getVisibleYMin());
                StdDraw.setYscale(getVisibleYMin() + MOVEMENT_SIZE, getVisibleYMax() + MOVEMENT_SIZE);
            }
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_S)) {
            player.moveY(-MOVEMENT_SIZE);
            if (getVisibleYMin() > 0 && getVisibleYMin() - MOVEMENT_SIZE > 0)
                StdDraw.setYscale(getVisibleYMin() - MOVEMENT_SIZE, getVisibleYMax() - MOVEMENT_SIZE);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_A)) {
            if (getVisibleXMin() > 0 && getVisibleXMin() - MOVEMENT_SIZE > 0)
                StdDraw.setXscale(getVisibleXMin() - MOVEMENT_SIZE, getVisibleXMax() - MOVEMENT_SIZE + 300);
            player.moveX(-MOVEMENT_SIZE);
        }
        if (StdDraw.isKeyPressed(KeyEvent.VK_D)) {
            if (getVisibleXMax() < maxLogX && getVisibleXMax() + MOVEMENT_SIZE < maxLogX) {
                StdDraw.setXscale(getVisibleXMin() + MOVEMENT_SIZE, getVisibleXMax() + MOVEMENT_SIZE + 300);
            }
            player.moveX(MOVEMENT_SIZE);
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

    public void fireProjectile(Projectile projectile) {
        actorQueue.add(projectile);
        projectileQueue.add(projectile);
    }

    private void mouseClick(int x, int y) {
        switch (clickedButton) {
            case MouseEvent.BUTTON1: {
                animationQueue.add(new SwingAnimation(player, 8, "sord.png", StdDraw.mouseX(), StdDraw.mouseY()));
                HitScan hs = new HitScan(player, StdDraw.mouseX(), StdDraw.mouseY(), 200, 1, 80);
                hs.setShowLine(false);
                fireHitScan(hs);
            } break;
            case MouseEvent.BUTTON3: {
                Projectile proj = new Projectile(player, StdDraw.mouseX(), StdDraw.mouseY(), 5, 200, 2);
                fireProjectile(proj);
            } break;
            default: break;
        }
        //HitScan hs = new HitScan(player, StdDraw.mouseX(), StdDraw.mouseY(), 200, 0, 10000);
        //fireHitScan(hs);
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
        return (x < (maxLogX * 1.002) && x > 0 && y < (maxLogY * 1.002) && y > 0);
    }

    // fire a hitscan, currently ignores hitting (all?) players
    public Iterable<Actor> fireHitScan(HitScan hs) {

        ConcurrentLinkedQueue<Actor> mobsHit = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Mob> notHit = new ConcurrentLinkedQueue<>(mobQueue);
        double angle = getRads(hs.getDestX(), hs.getDestY());
        double currX = hs.getSrcX();
        double currY = hs.getSrcY();
        double dist = 0;

        while (inBounds(currX, currY) && dist < hs.getRange()) {
            double xDiff = currX - hs.getSrcX();
            double yDiff = currY - hs.getSrcY();
            dist = Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
            currX += (2 * Math.cos(angle));
            currY += (2 * Math.sin(angle));
            Actor a = new Actor(currX, currY, 1);
            int hits = 0;
            for (Mob mob : notHit) {
                if (mob.collides(a) && !mobsHit.contains(mob)) {
                    mob.hit(hs.getDamage());
                    mobsHit.add(mob);
                    notHit.remove(mob);
                    hits++;
                }
                if (hits > hs.getPierceCount())
                    break;
            }
            if (hits > hs.getPierceCount())
                break;
        }
        if (hs.getShowLine())
            animationQueue.add(new HitScanLine(hs.getSrcX(), hs.getSrcY(), currX, currY));
        return mobsHit;
    }

    private double getRads(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - player.getY(), destX - player.getX()));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    public void setClickedButton(int clickedButton) {
        this.clickedButton = clickedButton;
        System.out.println(clickedButton);
    }
}
