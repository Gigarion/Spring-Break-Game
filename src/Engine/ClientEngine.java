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
 */


public class ClientEngine {
    // constants
    private static final int UP = KeyEvent.VK_W;
    private static final int DOWN = KeyEvent.VK_S;
    private static final int LEFT = KeyEvent.VK_A;
    private static final int RIGHT = KeyEvent.VK_D;
    private static final double MOVEMENT_SIZE = 0.5;
    private static final int DRAW_INTERVAL = 16;
    private static final int LOGIC_INTERVAL = 1;
    private static final int HUD_WIDTH = 300;
    // active animations
    private ConcurrentLinkedQueue<Animation> animationQueue;
    private ConcurrentLinkedQueue<Actor> actorQueue;
    // my clientMailroom
    private ClientMailroom clientMailroom;
    //logical boundaries for game
    private int maxLogX;
    private int maxLogY;
    // sightline size
    private int visibleRadius;
    // which button is now clicked?
    private int clickedButton;
    // player associated with this client
    private Player player;
    // which frame are we on
    private int frame;
    // timer for checking mail, will block when it thinks it's finished and then get notified
    private Timer mailTimer;

    // constructor
    public ClientEngine(int lXMax, int lYMax, int vRadius, ClientMailroom clientMailroom) {
        this.visibleRadius = vRadius;
        this.clientMailroom = clientMailroom;
        this.maxLogX = lXMax;
        frame = 0;
        setTimers();
    }

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
                        wait(1);
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
        StdDraw.show();

    }

    // helper function to draw the HUD on the screen
    private void drawHUD() {
        double hudHeight = visibleRadius * 0.66;
        double hudCenterX = getVisibleXMax() + HUD_WIDTH/2;
        double hudNameY = getVisibleYMin() + (hudHeight * 2/3);
        double hudHealthY = getVisibleYMin() + (hudHeight * 1/3);
        StdDraw.line(getVisibleXMax(), getVisibleYMin(), getVisibleXMax(),  getVisibleYMin() + hudHeight);
        StdDraw.line(getVisibleXMax(), getVisibleYMin() + hudHeight, getVisibleXMax() + HUD_WIDTH, getVisibleYMin() + hudHeight);
        StdDraw.text(hudCenterX, hudNameY, player.getName());
        StdDraw.text(hudCenterX, hudHealthY, Integer.toString(player.getHP()) + "/" + Integer.toString(player.getMaxHP()));
    }

    // logic tick, calculates ttl's and stuff here.. not exactly sure what else tbh...
    // a fairly obvious bug about which frame is here, no wonder draw was so confused.
    // though it's so ingrained at this point... feature.
    private void logicTick() {
        handleKeyboard();
        if ((frame % 20 == 0) && StdDraw.mousePressed()) {
            mouseClick((int) StdDraw.mouseX(), (int) StdDraw.mouseY());
        }
        frame = ((frame + 1) % 100000);
    }

    // handles logic associated with the user clicking the mouse
    // CURRENT: Sends to server, adds animations to own queues
    // DOES NOT send projectiles or hitscans to own queue, waits for server to add those?? sure.
    // won't do logic for collisions here, let the server do that.
    // animations get started and are allowed to continue w/o interference from server
    public void mouseClick(int x, int y) {
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

    public void handleKeyboard() {
        // CURRENT: Move and declare, don't ask for permission.  potential position diffs between self and other players,
        // but we'll see... this way makes the client a less flaccid vessel
        // server might have to send forcible corrections, oh well, we'll see
        // movement is going to need some more sophisticated packages if we develop a speed stat
        if (StdDraw.isKeyPressed(UP)) {
            player.moveY(MOVEMENT_SIZE);
            if (getVisibleYMax() < maxLogY && getVisibleYMax() + MOVEMENT_SIZE < maxLogY) {
                StdDraw.setYscale(getVisibleYMin() + MOVEMENT_SIZE, getVisibleYMax() + MOVEMENT_SIZE);
                clientMailroom.sendMessage(new Package("UP", Package.NEW_POS));
            }
        }
        if (StdDraw.isKeyPressed(DOWN)) {
            player.moveY(-MOVEMENT_SIZE);
            if (getVisibleYMin() > 0 && getVisibleYMin() - MOVEMENT_SIZE > 0) {
                StdDraw.setYscale(getVisibleYMin() - MOVEMENT_SIZE, getVisibleYMax() - MOVEMENT_SIZE);
                clientMailroom.sendMessage(new Package("DOWN", Package.NEW_POS));
            }
        }
        if (StdDraw.isKeyPressed(LEFT)) {
            if (getVisibleXMin() > 0 && getVisibleXMin() - MOVEMENT_SIZE > 0) {
                StdDraw.setXscale(getVisibleXMin() - MOVEMENT_SIZE, getVisibleXMax() - MOVEMENT_SIZE + 300);
                clientMailroom.sendMessage(new Package("LEFT", Package.NEW_POS));
            }

            player.moveX(-MOVEMENT_SIZE);
        }
        if (StdDraw.isKeyPressed(RIGHT)) {
            if (getVisibleXMax() < maxLogX && getVisibleXMax() + MOVEMENT_SIZE < maxLogX) {
                StdDraw.setXscale(getVisibleXMin() + MOVEMENT_SIZE, getVisibleXMax() + MOVEMENT_SIZE + 300);
                clientMailroom.sendMessage(new Package("RIGHT", Package.NEW_POS));
            }
            player.moveX(MOVEMENT_SIZE);
        }
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

    public void setClickedButton(int clickedButton) {
        this.clickedButton = clickedButton;
    }

    public void fireWeapon(int which) {
        Object attack = player.fireWeapon(which);
        if (attack == null)
            return;
        if (attack instanceof HitScan) {
            // fire a hitscan to the server
            // adds to animation queue
            Animation a = new SwingAnimation(player, 8, "sord.png", StdDraw.mouseX(), StdDraw.mouseY());
            animationQueue.add(a);
            clientMailroom.sendMessage(new Package(a, Package.ANIMATE));
            clientMailroom.sendMessage(new Package(attack, Package.HITSCAN));
        }
        if (attack instanceof Projectile) {
            // fire a projectile to the server
            // adds to animation queue cause that happens already which is probs bad
            clientMailroom.sendMessage(new Package(attack, Package.PROJECT));
        }
    }

    private boolean inBounds(double x, double y) {
        return (x < (maxLogX * 1.002) && x > 0 && y < (maxLogY * 1.002) && y > 0);
    }

    private double getRads(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - player.getY(), destX - player.getX()));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    public void setPlayer(Player a) {
        this.player = a;
    }

    private void handleMail(Iterable<Package> packages) {
        for (Package p : packages) {
            switch(p.getType())
        }
    }

}
