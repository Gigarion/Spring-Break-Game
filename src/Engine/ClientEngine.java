package Engine;

import Actors.Actor;
import Actors.Interactable;
import Actors.Player;
import Actors.Rock;
import Animations.Animation;
import Animations.HitScanLine;
import Animations.SwingAnimation;
import Gui.UserBox;
import Mailroom.ClientMailroom;
import Mailroom.Package;
import Projectiles.HitScan;
import Projectiles.Projectile;

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
    private static final char WPN_SWAP = 'e';
    private static final double MOVEMENT_SIZE = 0.8; // TODO: player speeds/stats
    private static final int LOGIC_INTERVAL = 1;

    private ConcurrentLinkedQueue<Animation> animationQueue;     // active animations
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ClientMailroom clientMailroom;      // communications to server
    private int maxLogX;            //logical boundaries for game
    private int maxLogY;
    private Player player;          // player associated with this client
    private int logicFrame;         // which logicFrame are we on
    private boolean init;           // is the engine ready to start?
    private UserBox userBox;

    private int selectedID;         // currently  selected actor
    private boolean selectedLock;   // a boolean to lock selections the user clicks on

    // constructor, takes logical maxes and visual radius for userbox
    public ClientEngine() {
        animationQueue = new ConcurrentLinkedQueue<>();
        actorMap = new ConcurrentHashMap<>();
        this.clientMailroom = new ClientMailroom();

        // set up user interface
        userBox = new UserBox(actorMap, animationQueue);
        userBox.setVisibleBounds(maxLogX, maxLogY);
        userBox.setExitHandler(()-> exit());
        userBox.setKeyboardHandler((e) -> keyPressed(e));
        userBox.setMouseHandler((x, y)-> mouseClick(x, y));

        // gotta start mail thread here
        Timer mailTimer = new Timer("Client Engine Mail Timer", true);
        init = false;
        mailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    handleMail(clientMailroom.getMessage());
                }
            }
        }, 0);
        logicFrame = 0;
    }

    // starts the engine loops, userbox initialization
    private void begin() {
        init = true;
        userBox.begin();
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
        clientMailroom.sendMessage(new Package(System.currentTimeMillis(), Package.PING));
    }

    /******************************************************
     * Logic and logic helpers
     ******************************************************/

    private void logicTick() {
        if ((logicFrame % 5 == 0))
            handleKeyboard();
        if ((logicFrame % 20 == 0) && userBox.isMousePressed()) {
            mouseClick(userBox.getMouseX(), userBox.getMouseY());
        }
        logicFrame = ((logicFrame + 1) % 100000);
        for (Actor a : actorMap.values()) {
            a.update();
        }
        Actor selected = actorMap.get(selectedID);
        if (selected != null && !userBox.inVisibleRange(selected.getX(), selected.getY())) {
            selectedID = -1;
            selectedLock = false;
        }

        if (selectedLock) return;
        int nearestID = findNearestActor();
        selectedID = nearestID;
        userBox.setSelectedActor(nearestID);
    }

    private int findNearestActor() {
        double range = player.getInteractRange();
        double smallestDist = Double.POSITIVE_INFINITY;
        int closest = -1;
        for (Actor a : actorMap.values()) {
            if (!a.isInteractable() || a.getID() == player.getID())
                continue;
            double dist = player.distanceTo(a);
            if (dist < range && dist < smallestDist) {
                closest = a.getID();
                smallestDist = dist;
            }
        }
        return closest;
    }

    /***********************************************
     * UI interactions
     * Keyboard, mouse, exit handler functions,
     * and the loop based keyboard handler
     ***********************************************/

    private void mouseClick(double x, double y) {
        if (!init)
            return;
        switch (userBox.getClickedButton()) {
            case MouseEvent.BUTTON1: {
                boolean choose = false;
                for (Actor actor : actorMap.values()) {
                    if (actor instanceof Projectile)
                        continue;
                    if (actor.contains(x, y)) {
                        selectedLock = true;
                        selectedID = actor.getID();
                        choose = true;
                    }
                }
                if (!choose) {
                    selectedID = -1;
                    selectedLock = false;
                }
                userBox.setSelectedActor(selectedID);
            }
            break;
            case MouseEvent.BUTTON3: {
                fireWeapon();
            }
            break;
            default:
                break;
        }
    }   // individual click

    private void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == WPN_SWAP)
            player.swapWeapon();
        if (e.getKeyChar() == 'r') {
            Actor a = actorMap.get(selectedID);
            if (a != null && a instanceof Interactable)
                ((Interactable) a).interact(player);
            if (a instanceof Rock) {
                clientMailroom.sendMessage(new Package(selectedID, Package.REMOVE));
            }
        }
        if (e.getKeyChar() == 'q') {
            player.reload();
        }
    }           // individual key press
    private void exit() {
        clientMailroom.exit();
    }      // exit call

    // check keyboard on a loop to facilitate my current movement protocol
    // nice and snappy responsiveness this way
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

        if (userBox.isKeyPressed(UP)) {
            if (player.getY() + MOVEMENT_SIZE < maxLogY) {
                player.moveY(MOVEMENT_SIZE);
                userBox.moveScreen(UP, MOVEMENT_SIZE);
            }
        }
        if (userBox.isKeyPressed(DOWN)) {
            if (player.getY() - MOVEMENT_SIZE > 0) {
                player.moveY(-MOVEMENT_SIZE);
                userBox.moveScreen(DOWN, MOVEMENT_SIZE);
            }
        }
        if (userBox.isKeyPressed(LEFT)) {
            if (player.getX() - MOVEMENT_SIZE > 0) {
                player.moveX(-MOVEMENT_SIZE);
                userBox.moveScreen(LEFT, MOVEMENT_SIZE);
            }
        }
        if (userBox.isKeyPressed(RIGHT)) {
            if (player.getX() + MOVEMENT_SIZE < maxLogX) {
                player.moveX(MOVEMENT_SIZE);
                userBox.moveScreen(RIGHT, MOVEMENT_SIZE);
            }
        }
        if (player.getX() != oldX || player.getY() != oldY) {
            Package newPos = new Package(player.getID(), Package.NEW_POS, Package.formCoords(player.getX(), player.getY()));
            clientMailroom.sendMessage(newPos);
        }
    }

    /*******************************************************
     *  Mail handling, main switch and helper functions
     *******************************************************/

    // given a set of mail, handle it appropriately, utilizes helper functions
    private synchronized void handleMail(Package p) {
        if (p == null) return;
        switch (p.getType()){
            case Package.WELCOME:   handleWelcome(p);       break;
            case Package.HITSCAN:   handleHitscan(p);       break;
            case Package.PROJECT:   handleProjectile(p);    break;
            case Package.NEW_POS:   handleNewPosition(p);   break;
            case Package.ANIMATE:   handleAnimation(p);     break;
            case Package.ACTOR:     handleNewActor(p);      break;
            case Package.HIT:       handleHit(p);           break;
            case Package.REMOVE:    handleRemove(p);        break;
            case Package.PING:      handlePing(p);          break;
            case Package.SCR_SIZE:  handleScreenSize(p);    break;
            default: System.out.println("Unused package type: " + p.getType());
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
        userBox.setPlayer(id);
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
        userBox.addAnimation(a);
    }

    private void handleNewActor(Package p) {
        Actor a = (Actor) p.getPayload();
        int id = a.getID();
        actorMap.put(id, a);
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
        if (a != null) {
            actorMap.remove(id);
            if (id == selectedID) {
                selectedLock = false;
                selectedID = -1;
            }
        }
    }

    private void handlePing(Package p) {
        long currentTime = System.currentTimeMillis();
        long oldTime = (Long) p.getPayload();
        long ping = currentTime - oldTime;
        userBox.updatePing(ping);
        clientMailroom.sendMessage(new Package(System.currentTimeMillis(), Package.PING));
    }

    private void handleScreenSize(Package p) {
        String sizeString = (String) p.getPayload();
        double[] sizes = Package.extractCoords(sizeString);
        maxLogX = (int) sizes[0];
        maxLogY = (int) sizes[1];
        userBox.setVisibleBounds(maxLogX, maxLogY);
    }

    /******************************************************
     *  Miscellaneous
     ******************************************************/

    private void fireWeapon() {
        Iterable<Object> attacks = player.fireWeapon();
        if (attacks == null) {
            return;
        }
        for (Object attack : attacks) {
            if (attack instanceof HitScan) {
                // fire a hitscan to the server
                // adds to animation queue
                Animation a = new SwingAnimation(player, 10, "sord.png", userBox.getMouseX(), userBox.getMouseY());
                clientMailroom.sendMessage(new Package(a, Package.ANIMATE, Integer.toString(player.getID())));
                clientMailroom.sendMessage(new Package(attack, Package.HITSCAN, Integer.toString(player.getID())));
            }
            if (attack instanceof Projectile) {
                // fire a projectile to the server
                // adds to animation queue cause that happens already which is probs bad
                clientMailroom.sendMessage(new Package(attack, Package.PROJECT));
            }
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
        while (!clientMailroom.isAlive()) {Thread.yield();}
        clientMailroom.sendMessage(new Package(player, Package.WELCOME));
        player.giveWeapons();
    }
}
