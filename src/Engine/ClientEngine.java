package Engine;

import Actors.Actor;
import Actors.Mob;
import Actors.Player;
import Animations.Animation;
import Animations.HitScanLine;
import Animations.SwingAnimation;
import Mailroom.ClientMailroom;
import Mailroom.Package;
import Projectiles.HitScan;
import Projectiles.Projectile;
import Util.StdDraw;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 3/21/2017.
 * Client size game engine for the game
 */


public class ClientEngine {
    // constants
    private static final int UP = KeyEvent.VK_W;
    private static final int DOWN = KeyEvent.VK_S;
    private static final int LEFT = KeyEvent.VK_A;
    private static final int RIGHT = KeyEvent.VK_D;
    private static final double MOVEMENT_SIZE = 0.8;
    private static final int DRAW_INTERVAL = 16;
    private static final int LOGIC_INTERVAL = 1;
    private static final int HUD_WIDTH = 300;
    // active animations
    //private ConcurrentLinkedQueue<Projectile> projectileQueue;
    private ConcurrentLinkedQueue<Animation> animationQueue;
    private ConcurrentLinkedQueue<Actor> actorQueue;
    private ConcurrentLinkedQueue<Mob> mobQueue;
    private ClientMailroom clientMailroom;      // communications to server
    private int maxLogX;            //logical boundaries for game
    private int maxLogY;
    private int visibleRadius;      // sightline size
    private int clickedButton;      // which button is now clicked?
    private Player player;          // player associated with this client
    private int frame;              // which frame are we on
    private Timer mailTimer;        // timer for checking mail


    // constructor
    public ClientEngine(int lXMax, int lYMax, int vRadius) {
        animationQueue = new ConcurrentLinkedQueue<>();
        mobQueue = new ConcurrentLinkedQueue<>();
        actorQueue = new ConcurrentLinkedQueue<>();
        this.visibleRadius = vRadius;
        this.clientMailroom = new ClientMailroom();
        this.maxLogX = lXMax;
        this.maxLogY = lYMax;
        frame = 0;
    }

    // starts the engine loops
    public void begin() {
        setTimers();
    }

    /******************************************************
     * Logic and drawing
     ******************************************************/

    // sets the draw and logic timer threads
    private void setTimers() {
        Timer drawTimer = new Timer("Draw Timer", true);
        drawTimer.schedule(new TimerTask() {
            public void run() {
                drawTick();
            }
        }, 0, DRAW_INTERVAL);
        Timer logicTimer = new Timer("Logic Timer", true);
        logicTimer.schedule(new TimerTask() {
            public void run() {
                logicTick();
            }
        }, 200, LOGIC_INTERVAL);

        mailTimer = new Timer("Client Engine Mail Timer", true);
        mailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    handleMail(clientMailroom.getMessages());
                    try {
                        Thread.yield();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0);
    }

    // draw loop, called every DRAW_INTERVAL milliseconds
    private void drawTick() {
        StdDraw.clear();
        StdDraw.rectangle(450, 450, 300, 300);
        drawHUD();

        for (Animation hsl : animationQueue) {
            if (hsl.getTTL() <= 0)
                animationQueue.remove(hsl);
            hsl.draw(frame);
        }
        for (Actor actor : actorQueue) {
            actor.draw();
        }
        StdDraw.circle(StdDraw.mouseX(), StdDraw.mouseY(), 10);
        StdDraw.show();

    }

    // helper function to draw the HUD on the screen
    private void drawHUD() {
        double hudHeight = visibleRadius * 0.66;
        double hudCenterX = getVisibleXMax() + HUD_WIDTH/2;
        double hudNameY = getVisibleYMin() + (hudHeight * 2/3);
        double hudHealthY = getVisibleYMin() + (hudHeight * 1/3);
        double hudIDY = getVisibleYMin() + (hudHeight * 1/6);
        StdDraw.line(getVisibleXMax(), getVisibleYMin(), getVisibleXMax(),  getVisibleYMin() + hudHeight);
        StdDraw.line(getVisibleXMax(), getVisibleYMin() + hudHeight, getVisibleXMax() + HUD_WIDTH, getVisibleYMin() + hudHeight);
        StdDraw.text(hudCenterX, hudNameY, player.getName());
        StdDraw.text(hudCenterX, hudHealthY, Integer.toString(player.getHP()) + "/" + Integer.toString(player.getMaxHP()));
        StdDraw.text(hudCenterX, hudIDY, player.getID() + "");
    }

    // logic tick, calculates ttl's and stuff here.. not exactly sure what else tbh...
    // a fairly obvious bug about which frame is here, no wonder draw was so confused.
    // though it's so ingrained at this point... feature.
    private void logicTick() {
        if ((frame % 5 == 0))
            handleKeyboard();
        if ((frame % 20 == 0) && StdDraw.mousePressed()) {
            mouseClick((int) StdDraw.mouseX(), (int) StdDraw.mouseY());
        }
        frame = ((frame + 1) % 100000);
        for (Actor a : actorQueue) {
            if (checkActor(a))
                a.update();
        }
    }

    /*******************************************************
     *  UI interactions
     *******************************************************/

    private void mouseClick(int x, int y) {
        switch (clickedButton) {
            case MouseEvent.BUTTON1: {
                fireWeapon(0);
                // swing animation packet
            }
            break;
            case MouseEvent.BUTTON3: {
                fireWeapon(1);
            }
            break;
            default:  break;
        }
    }

    public void setClickedButton(int clickedButton) {

        this.clickedButton = clickedButton;
    }

    private void handleKeyboard() {
        // CURRENT: Move and declare, don't ask for permission.  potential position diffs between self and other players,
        // but we'll see... this way makes the client a less flaccid vessel
        // server might have to send forcible corrections, oh well, we'll see
        // movement is going to need some more sophisticated packages if we develop a speed stat
        double oldX, oldY;
        oldX = player.getX();
        oldY = player.getY();
        if (StdDraw.isKeyPressed(UP)) {
            if (player.getY() + MOVEMENT_SIZE < maxLogY) {
                player.moveY(MOVEMENT_SIZE);
                if (getVisibleYMax() < maxLogY && getVisibleYMax() + MOVEMENT_SIZE < maxLogY) {
                    StdDraw.setYscale(getVisibleYMin() + MOVEMENT_SIZE, getVisibleYMax() + MOVEMENT_SIZE);
                }
            }
        }
        if (StdDraw.isKeyPressed(DOWN)) {
            if (player.getY() - MOVEMENT_SIZE > 0) {
                player.moveY(-MOVEMENT_SIZE);
                if (getVisibleYMin() > 0 && getVisibleYMin() - MOVEMENT_SIZE > 0) {
                    StdDraw.setYscale(getVisibleYMin() - MOVEMENT_SIZE, getVisibleYMax() - MOVEMENT_SIZE);
                }
            }
        }
        if (StdDraw.isKeyPressed(LEFT)) {
            if (player.getX() - MOVEMENT_SIZE > 0) {
                player.moveX(-MOVEMENT_SIZE);
                if (getVisibleXMin() > 0 && getVisibleXMin() - MOVEMENT_SIZE > 0) {
                    StdDraw.setXscale(getVisibleXMin() - MOVEMENT_SIZE, getVisibleXMax() - MOVEMENT_SIZE + 300);
                }
            }
        }
        if (StdDraw.isKeyPressed(RIGHT)) {
            if (player.getX() + MOVEMENT_SIZE < maxLogX) {
                player.moveX(MOVEMENT_SIZE);
                if (getVisibleXMax() < maxLogX && getVisibleXMax() + MOVEMENT_SIZE < maxLogX) {
                    StdDraw.setXscale(getVisibleXMin() + MOVEMENT_SIZE, getVisibleXMax() + MOVEMENT_SIZE + 300);
                }
            }
        }
        if (player.getX() != oldX || player.getY() != oldY) {
            Package newPos = new Package(player.getID(), Package.NEW_POS, Package.formCoords(player.getX(), player.getY()));
            clientMailroom.sendMessage(newPos);
        }
    }

    private void fireWeapon(int which) {
        Object attack = player.fireWeapon(which);
        if (attack == null) {
            return;
        }
        if (attack instanceof HitScan) {
            // fire a hitscan to the server
            // adds to animation queue
            Animation a = new SwingAnimation(player, 5, "sord.png", StdDraw.mouseX(), StdDraw.mouseY());
            clientMailroom.sendMessage(new Package(a, Package.ANIMATE, Integer.toString(player.getID())));
            clientMailroom.sendMessage(new Package(attack, Package.HITSCAN, Integer.toString(player.getID())));
        }
        if (attack instanceof Projectile) {
            // fire a projectile to the server
            // adds to animation queue cause that happens already which is probs bad
            clientMailroom.sendMessage(new Package(attack, Package.PROJECT));
        }
    }

    public void setPlayer(Player a) {
        this.player = a;
        clientMailroom.sendMessage(new Package(a, Package.WELCOME));
        a.giveWeapons();
        actorQueue.add(a);
    }

    /*******************************************************
     *  Mail handling, main switch and helper functions
     *******************************************************/

    // given a set of mail, handle it appropriately, utilizes helper functions
    private void handleMail(Iterable<Package> packages) {
        for (Package p : packages) {
            switch(p.getType()) {
                case Package.WELCOME:   handleWelcome(p); break;
                case Package.HITSCAN:   handleHitscan(p); break;
                case Package.PROJECT:   handleProjectile(p); break;
                case Package.NEW_POS:   handleNewPosition(p); break;
                case Package.ANIMATE:   handleAnimation(p); break; // server gives new animation
                case Package.ACTOR:     handleNewActor(p); break;
                case Package.HIT:       handleHit(p); break;
                case Package.REMOVE:    handleRemove(p); break;
                default: System.out.println("Unused package type: " + p.getType());
            }
        }
    }

    private void handleNewPosition(Package p) {
        int id = (Integer) (p.getPayload());
        for (Actor a : actorQueue) {
            if (a.getID() == id && id != player.getID()) {
                double[] coords = Package.extractCoords(p.getExtra());
                a.moveTo(coords[0], coords[1]);
                // update position
                break;
            }
        }
    }

    private void handleWelcome(Package p) {
        int id = (Integer) p.getPayload();
        player.setID(id);
    }

    private void handleHitscan(Package p) {
        HitScan hs = (HitScan) p.getPayload();
        if (hs.getShowLine())
            animationQueue.add(new HitScanLine(hs));
    }

    private void handleProjectile(Package p) {
        Projectile proj = (Projectile) p.getPayload();
        actorQueue.add(proj);
    }

    private void handleAnimation(Package p) {
        Animation a = (Animation) p.getPayload();
        if (a instanceof SwingAnimation) {
            int id = Integer.parseInt(p.getExtra());
            if (id >= 0) {
                for (Actor act : actorQueue) {
                    if (act.getID() == id) {
                        ((SwingAnimation) a).setSrc(act);
                    }
                }
            }
        }
        animationQueue.add(a);
    }

    private void handleNewActor(Package p) {
        Actor a = (Actor) p.getPayload();
        actorQueue.add(a);
        if (a instanceof Mob)
            mobQueue.add((Mob) a);
    }

    private void handleHit(Package p) {
        int damage = (Integer) p.getPayload();
        int id = Integer.parseInt(p.getExtra());
        // TODO: figure out why hitscans sometimes hit twice......
        System.out.println(id + "hit " + Math.random());
        for (Actor a : actorQueue) {
            if (a.getID() == id) {
                a.hit(damage);
                checkActor(a);
            }
        }
    }

    private void handleRemove(Package p) {
        int id = (Integer) p.getPayload();
        for (Actor actor : actorQueue) {
            if (actor.getID() == id) {
                actorQueue.remove(actor);
            }
        }
        for (Mob mob : mobQueue) {
            if (mob.getID() == id) {
                mobQueue.remove(mob);
                int x = 10 + (int) (Math.random() * 580);
                Mob mob2 = new Mob(-1, x, 800, 12, 10);
                clientMailroom.sendMessage(new Package(mob2, Package.ACTOR));
            }
        }
    }

    /******************************************************
     *  Various utility functions
     ******************************************************/

    private boolean checkActor(Actor a) {
        if (a instanceof Projectile) {
            Projectile p = (Projectile) a;
            if (p.outOfRange()) {
                actorQueue.remove(a);
            }
            for (Mob mob : mobQueue) {
                if (mob.collides(a)) {
                    mob.hit(p.getDamage());
                }
            }
        }

        if (a instanceof Mob) {
            Mob m = (Mob) a;
            if (m.getHP() <= 0) {
                // dead mob
                actorQueue.remove(a);
                mobQueue.remove(a);
                clientMailroom.sendMessage(new Package(m.getID(), Package.REMOVE));
            }
        }

        if (!inBounds(a.getX(), a.getY())) {
            actorQueue.remove(a);

            if (a instanceof Mob) {
                mobQueue.remove(a);
                Mob m = new Mob(-1, 800, 800, 12, 10);
                clientMailroom.sendMessage(new Package(m, Package.ACTOR));
            }

            if (a instanceof Player) {
                setPlayer(new Player("ME"));
            }
            return false;
        }
        return true;
    }

    private boolean inBounds(double x, double y) {
        return (x < (maxLogX * 1.002) && x > 0 && y < (maxLogY * 1.002) && y > 0);
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

}
