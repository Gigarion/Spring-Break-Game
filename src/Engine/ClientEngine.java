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
import java.util.concurrent.ConcurrentHashMap;
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
    private static final double MOVEMENT_SIZE = 1.2;
    private static final int DRAW_INTERVAL = 16;
    private static final int LOGIC_INTERVAL = 1;
    private static final int HUD_WIDTH = 300;
    // active animations
    //private ConcurrentLinkedQueue<Projectile> projectileQueue;
    private ConcurrentLinkedQueue<Animation> animationQueue;
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ConcurrentLinkedQueue<Mob> mobQueue;
    private ClientMailroom clientMailroom;      // communications to server
    private int maxLogX;            //logical boundaries for game
    private int maxLogY;
    private int visibleRadius;      // sightline size
    private int clickedButton;      // which button is now clicked?
    private Player player;          // player associated with this client
    private int frame;              // which frame are we on
    private Timer mailTimer;        // timer for checking mail
    private boolean init; // is the engine ready to start?
    private long ping; // the current round trip ping in milliseconds


    // constructor
    public ClientEngine(int lXMax, int lYMax, int vRadius) {
        animationQueue = new ConcurrentLinkedQueue<>();
        mobQueue = new ConcurrentLinkedQueue<>();
        actorMap = new ConcurrentHashMap<>();
        this.visibleRadius = vRadius;
        this.clientMailroom = new ClientMailroom();
        this.maxLogX = lXMax;
        this.maxLogY = lYMax;
        frame = 0;
        mailTimer = new Timer("Client Engine Mail Timer", true);
        init = false;
        mailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    handleMail(clientMailroom.getMessage());
                }
            }
        }, 0);
    }

    // starts the engine loops
    private void begin() {
        init = true;
        setTimers();
        clientMailroom.sendMessage(new Package(System.currentTimeMillis(), Package.PING));
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
                while (true) {
                    logicTick();
                    try {
                        Thread.sleep(LOGIC_INTERVAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 200);
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
        for (Actor actor : actorMap.values()) {
            actor.draw();
        }
        StdDraw.circle(StdDraw.mouseX(), StdDraw.mouseY(), 10);
        StdDraw.show();

    }

    // helper function to draw the HUD on the screen
    private void drawHUD() {
        double hudHeight = visibleRadius * 0.66;
        double hudCenterX = getVisibleXMax() + HUD_WIDTH / 2;
        double hudNameY = getVisibleYMin() + (hudHeight * 2 / 3);
        double hudHealthY = getVisibleYMin() + (hudHeight * 1 / 3);
        double hudIDY = getVisibleYMin() + (hudHeight * 1 / 6);
        StdDraw.line(getVisibleXMax(), getVisibleYMin(), getVisibleXMax(), getVisibleYMin() + hudHeight);
        StdDraw.line(getVisibleXMax(), getVisibleYMin() + hudHeight, getVisibleXMax() + HUD_WIDTH, getVisibleYMin() + hudHeight);
        StdDraw.text(hudCenterX, hudNameY, player.getName());
        StdDraw.text(hudCenterX, hudHealthY, Integer.toString(player.getHP()) + "/" + Integer.toString(player.getMaxHP()));
        StdDraw.text(hudCenterX, hudIDY, ping + "");
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
        for (Actor a : actorMap.values()) {
            a.update();
        }
    }

    /*******************************************************
     *  UI interactions
     *******************************************************/

    private void mouseClick(int x, int y) {
        if (!init)
            return;
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
            default:
                break;
        }
    }

    public void setClickedButton(int clickedButton) {

        this.clickedButton = clickedButton;
    }

    private void handleKeyboard() {
        if (!init)
            return;
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

    public void setPlayer(Player player) {
        this.player = player;
        clientMailroom.sendMessage(new Package(player, Package.WELCOME));
        player.giveWeapons();
        actorMap.put(-1, player);
    }

    /*******************************************************
     *  Mail handling, main switch and helper functions
     *******************************************************/

    // given a set of mail, handle it appropriately, utilizes helper functions
    private synchronized void handleMail(Package p) {
        if (p == null) return;
        System.out.println(p.getType());
        switch (p.getType()) {
            case Package.WELCOME:
                handleWelcome(p);
                break;
            case Package.HITSCAN:
                handleHitscan(p);
                break;
            case Package.PROJECT:
                handleProjectile(p);
                break;
            case Package.NEW_POS:
                handleNewPosition(p);
                break;
            case Package.ANIMATE:
                handleAnimation(p);
                break; // server gives new animation
            case Package.ACTOR:
                handleNewActor(p);
                break;
            case Package.HIT:
                handleHit(p);
                break;
            case Package.REMOVE:
                handleRemove(p);
                break;
            case Package.PING:
                handlePing(p);
                break;
            default:
                System.out.println("Unused package type: " + p.getType());
        }
    }

    private void handleNewPosition(Package p) {
        int id = (Integer) (p.getPayload());
        Actor a = actorMap.get(id);
        if (a != null) {
            double[] coords = Package.extractCoords(p.getExtra());
            a.moveTo(coords[0], coords[1]);
        }
    }

    private void handleWelcome(Package p) {
        int id = (Integer) p.getPayload();
        player.setID(id);
        actorMap.put(id, player);
        begin();
    }

    private void handleHitscan(Package p) {
        HitScan hs = (HitScan) p.getPayload();
        if (hs.getShowLine())
            animationQueue.add(new HitScanLine(hs));
    }

    private void handleProjectile(Package p) {
        Projectile proj = (Projectile) p.getPayload();
        actorMap.put(proj.getID(), proj);
    }

    private void handleAnimation(Package p) {
        Animation a = (Animation) (p.getPayload());
        if (a instanceof SwingAnimation) {
            int id = Integer.parseInt(p.getExtra());
            if (id >= 0) {
                Actor act = actorMap.get(id);
                if (act != null) {
                    ((SwingAnimation) a).setSrc(act);
                }
            }
        }
        animationQueue.add(a);
    }

    private void handleNewActor(Package p) {
        Actor a = (Actor) p.getPayload();
        int id = a.getID();
        actorMap.put(a.getID(), a);
        if (a instanceof Mob)
            mobQueue.add((Mob) a);
    }

    private void handleHit(Package p) {
        int damage = (Integer) p.getPayload();
        int id = Integer.parseInt(p.getExtra());
        Actor a = actorMap.get(id);
        if (a != null)
            a.hit(damage);
    }

    private void handleRemove(Package p) {
        int id = (Integer) p.getPayload();
        Actor a = actorMap.get(id);
        if (a != null)
            actorMap.remove(id);

        for (Mob mob : mobQueue) {
            if (mob.getID() == id) {
                mobQueue.remove(mob);
            }
        }
    }

    private void handlePing(Package p) {
        long currentTime = System.currentTimeMillis();
        long oldTime = (Long) p.getPayload();
        ping = currentTime - oldTime;
        clientMailroom.sendMessage(new Package(System.currentTimeMillis(), Package.PING));
    }

    /******************************************************
     *  Various utility functions
     ******************************************************/

    private double getVisibleYMin() {
        if (player.getY() - visibleRadius <= 0)
            return 0;
        if (player.getY() + visibleRadius > maxLogY)
            return maxLogY - 2 * visibleRadius;
        return player.getY() - visibleRadius;
    }

    private double getVisibleYMax() {
        if (player.getY() + visibleRadius >= maxLogY)
            return maxLogY;
        if (player.getY() - visibleRadius <= 0)
            return 2 * visibleRadius;
        return player.getY() + visibleRadius;
    }

    private double getVisibleXMin() {
        if (player.getX() - visibleRadius <= 0)
            return 0;
        if (player.getX() + visibleRadius >= maxLogX)
            return maxLogX - 2 * visibleRadius;
        return player.getX() - visibleRadius;
    }

    private double getVisibleXMax() {
        if (player.getX() + visibleRadius > maxLogX)
            return maxLogX;
        if (player.getX() - visibleRadius < 0)
            return 2 * visibleRadius;
        return player.getX() + visibleRadius;
    }

    public void exit() {
        clientMailroom.exit();
    }
}
