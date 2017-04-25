package Engine;

import Actors.*;
import Animations.Animation;
import Animations.HitScanLine;
import Animations.SwingAnimation;
import Gui.UserBox;
import Mailroom.ClientMailroom;
import Mailroom.Package;
import Maps.GameMap;
import Maps.GameMapStorage;
import Projectiles.HitScan;
import Projectiles.Projectile;
import Maps.MapGrid;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
    private static final double MOVEMENT_SIZE = 1; // TODO: player speeds/stats
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
    private MapGrid mapGrid;        // impassability grid

    private int selectedID;         // currently  selected actor
    private boolean selectedLock;   // a boolean to lock selections the user clicks on


    // constructor, takes logical maxes and visual radius for userbox
    public ClientEngine(String ip, int port) {
        animationQueue = new ConcurrentLinkedQueue<>();
        actorMap = new ConcurrentHashMap<>();
        this.clientMailroom = new ClientMailroom(ip, port);

        // set up user interface
        userBox = new UserBox(actorMap, animationQueue);
        userBox.setBounds(maxLogX, maxLogY);
        userBox.setExitHandler(this::exit);
        userBox.setKeyboardHandler(this::keyPressed);
        userBox.setMouseHandler(this::handleMouse);
        userBox.setMouseWheelHandler(this::handleMouseWheel);

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
            handleMouse(null, userBox.getMouseX(), userBox.getMouseY());
        }
        if ((logicFrame % 100) == 0) {
            clientMailroom.sendMessage(new Package(player.getID(), Package.ROTATE, Double.toString(player.getRads())));
        }
        logicFrame = ((logicFrame + 1) % 100000);
        for (Actor a : actorMap.values()) {
            if (!(a instanceof Mob)) a.update();
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

    private void handleMouse(MouseEvent e, double x, double y) {
        if (!init)
            return;
        if (e != null && (e.getID() ^ MouseEvent.MOUSE_RELEASED) == 0) {
            handleMouseRelease(e, x, y);
        }
        if (e == null || (e.getID() ^ MouseEvent.MOUSE_PRESSED) == 0) {
            handleMousePressed(e, x, y);
        }
        if (e != null && (e.getID() ^ MouseEvent.MOUSE_DRAGGED) == 0) {
            handleMouseDragged(e, x, y);
        }
    }   // individual handleMouse

    private void handleMouseRelease(MouseEvent e, double x, double y) {
        switch (e.getButton()) {
            case MouseEvent.BUTTON3:
                handleUseEquipped(player.release(x, y));
            default:
                break;
        }
    }

    private void handleMousePressed(MouseEvent e, double x, double y) {
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
                handleUseEquipped(player.useEquipped(userBox.getMouseX(), userBox.getMouseY()));
            }
            break;
            default:
                System.out.println(userBox.getClickedButton() + " : " + MouseEvent.BUTTON3);
                break;
        }
    }

    private void handleMouseDragged(MouseEvent e, double x, double y) {
        switch (e.getButton()) {
            default:
                break;
        }
    }

    private void handleMouseWheel(MouseWheelEvent e) {
        player.swapWeapon(e.getWheelRotation());
    }

    private void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'e':
                player.swapWeapon(1);
                break;
            case 'q':
                player.swapWeapon(-1);
                break;
            case 'y':
                userBox.toggleCameraLock();
                break;
            case 'r':
                player.reload();
                break;
            case ' ': {
                Actor a = actorMap.get(selectedID);
                if (a != null && a instanceof Interactable) {
                    clientMailroom.sendMessage(new Package(player.getID(), Package.INTERACT, a.getID() + ""));
                }
            }
            break;
            default:
                try {
                    int index = Integer.parseInt(e.getKeyChar() + "") - 1;
                    if (index < 0)
                        index = 9;
                    player.selectItem(index);
                } catch (Exception ex) {
                    // ignore it
            }
            break;
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

        double movement_size = MOVEMENT_SIZE;
        if (userBox.isKeyPressed(KeyEvent.VK_SHIFT)) {
            movement_size *= 2.5;
        }

        if (userBox.isKeyPressed(UP)) {
            if (mapGrid.validMove(player.getX(), player.getY() + movement_size, player)) {
                player.moveY(movement_size);
            }
        }
        if (userBox.isKeyPressed(DOWN)) {
            if (mapGrid.validMove(player.getX(), player.getY() - movement_size, player)) {
                player.moveY(-movement_size);
            }
        }
        if (userBox.isKeyPressed(LEFT)) {
            if (mapGrid.validMove(player.getX() - movement_size, player.getY(), player)) {
                player.moveX(-movement_size);
            }
        }
        if (userBox.isKeyPressed(RIGHT)) {
            if (mapGrid.validMove(player.getX() + movement_size, player.getY(), player)) {
                player.moveX(movement_size);
            }
        }
        if (player.getX() != oldX || player.getY() != oldY) {
            userBox.movePlayer();
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
        switch (p.getType()) {
            case Package.WELCOME:
                handleWelcome(p);
                break;
            case Package.HITSCAN:
                handleHitscan(p);
                break;
            case Package.NEW_POS:
                handleNewPosition(p);
                break;
            case Package.ANIMATE:
                handleAnimation(p);
                break;
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
            case Package.SCR_SIZE:
                handleScreenSize(p);
                break;
            case Package.GAME_MAP:
                handleGameMap(p);
                break;
            case Package.INTERACT:
                handleInteract(p);
                break;
            case Package.ROTATE:
                handleRotate(p);
                break;
            default:
                System.out.println("Unused package type: " + p.getType());
        }
    }

    private void handleNewPosition(Package p) {
        int id = (Integer) (p.getPayload());
        Actor a = actorMap.get(id);
        if (a != null && id != player.getID()) {
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
        ActorStorage as = (ActorStorage) p.getPayload();
        Actor a = ActorStorage.getActor(as);
        int id = a.getID();
        if (id != player.getID())
            actorMap.put(id, a);
        if (player.getID() == -1 && a instanceof Player && (((Player) a).getName().equals(player.getName()))) {
            player.setID(id);
            actorMap.put(id, player);
            userBox.setPlayer(id);
        }
    }

    private void handleHit(Package p) {
        int id = (Integer) p.getPayload();
        int damage = Integer.parseInt(p.getExtra());
        Actor a = actorMap.get(id);
        if (a != null) {
            a.hit(damage);
        }
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
            if (id == player.getID()) {
                handlePlayerDeath();
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

    private void handleGameMap(Package p) {
        GameMapStorage gms = (GameMapStorage) p.getPayload();
        GameMap gameMap = gms.extractGameMap();
        this.mapGrid = gameMap.getMapGrid();
        userBox.setGameMap(gameMap);
        mapGrid.setShowGrid(false);
        mapGrid.setShowPlayerBoxes(false);
        mapGrid.setShowBoxes(true);
    }

    private void handleScreenSize(Package p) {
        String sizeString = (String) p.getPayload();
        double[] sizes = Package.extractCoords(sizeString);
        maxLogX = (int) sizes[0];
        maxLogY = (int) sizes[1];
        userBox.setBounds(maxLogX, maxLogY);
    }

    private void handleInteract(Package p) {
        Interactable a = (Interactable) actorMap.get(p.getPayload());
        Iterable<Object> results = a.interact(player);
        if (results != null) {
            for (Object result : results) {
                System.out.println("result");
            }
        }
    }

    private void handleRotate(Package p) {
        Actor a = actorMap.get(p.getPayload());
        if (a == player || a == null) {
            return;
        }
        a.setRads(Double.parseDouble(p.getExtra()));
    }

    /******************************************************
     *  Miscellaneous
     ******************************************************/

    private void handleUseEquipped(Iterable<Object> effects) {
        if (effects == null) {
            return;
        }
        for (Object effect : effects) {
            if (effect instanceof HitScan) {
                // fire a hitscan to the server
                // adds to animation queue
                Animation a = new SwingAnimation(player, 10, "sord.png", userBox.getMouseX(), userBox.getMouseY());
                clientMailroom.sendMessage(new Package(a, Package.ANIMATE, Integer.toString(player.getID())));
                clientMailroom.sendMessage(new Package(effect, Package.HITSCAN, Integer.toString(player.getID())));
            }
            if (effect instanceof Projectile) {
                // fire a projectile to the server
                // adds to animation queue cause that happens already which is probs bad
                clientMailroom.sendActor((Projectile) effect);
            }
            if (effect instanceof Animation) {
                clientMailroom.sendMessage(new Package(effect, Package.ANIMATE, "" + player.getID()));
            }
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
        while (!clientMailroom.isAlive()) {
            Thread.yield();
        }
        ActorStorage as = ActorStorage.getActorStore(player);
        clientMailroom.sendMessage(new Package(as, Package.WELCOME, player.getName()));
        player.giveWeapons();
    }

    private void handlePlayerDeath() {
        System.out.println("removed me");
        Player replacement = new Player(player.getName());
        replacement.giveWeapons();
        ActorStorage as = ActorStorage.getActorStore(replacement);
        clientMailroom.sendMessage(new Package(as, Package.ACTOR));
        this.player = replacement;
    }

}
