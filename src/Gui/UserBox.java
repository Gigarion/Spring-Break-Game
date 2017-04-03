package Gui;

import Actors.Actor;
import Actors.Player;
import Animations.Animation;
import Util.MapGrid;
import Util.MapLoader;
import Util.StdDraw;
import com.sun.org.apache.bcel.internal.generic.SWITCH;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 3/27/2017.
 * This is going to be the graphics and user event handling engine, decoupling
 * ClientEngine from StdDraw to improve modularity
 * <p>
 * Mostly cause imma hack StdDraw to pieces eventually.
 * <p>
 * I'm rather happy with how this module went, actually
 */

public class UserBox {
    // a handler for the engine (or superclass) to gracefully accept exits
    public interface ExitHandler {
        void exit();
    }

    // a handler for mouse events from the user
    public interface MouseHandler {
        void handleMouse(double x, double y);
    }
    // a handler for mouse events from the user

    public interface KeyboardHandler {
        void handleKeyboard(KeyEvent e);
    }

    private static final int UP = KeyEvent.VK_W;
    private static final int DOWN = KeyEvent.VK_S;
    private static final int LEFT = KeyEvent.VK_A;
    private static final int RIGHT = KeyEvent.VK_D;

    private static final int HUD_WIDTH = 300;
    private static final int DRAW_INTERVAL = 5;
    private static final int Y_SCALE = 900;
    private static final int VIS_Y_RADIUS = Y_SCALE / 2;
    private static final int EDGE_RADIUS = 50;

    private double vis_x_radius;

    private int drawFrame;                      // which frame are we on
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ConcurrentLinkedQueue<Animation> animationQueue;
    private ExitHandler exitHandler;            // called for engine to handle exits
    private MouseHandler mouseHandler;          // called for engine to handle mouse events
    private KeyboardHandler keyboardHandler;    // called for engine to handle keyboard events
    private int selectedActor;                  // id of the selected actor

    private Player player;
    private int maxLogX, maxLogY;
    private long ping;
    private int clickedButton;
    private Point2D.Double center;

    private boolean showEdges;
    private boolean lockCamera;
    private long lockTimer;

    private MapGrid mapGrid;

    public UserBox(ConcurrentHashMap<Integer, Actor> actorMap, ConcurrentLinkedQueue<Animation> animationQueue) {
        this.actorMap = actorMap;
        this.animationQueue = animationQueue;
        StdDraw.attachUserBox(this);
        this.lockCamera = false;
        this.showEdges = false;
    }

    public void begin() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        double aspect = d.getWidth() / d.getHeight();
        //StdDraw.setCanvasSize((int) d.getWidth(), (int) d.getHeight());
        StdDraw.setCanvasSize((int) (Y_SCALE * aspect), Y_SCALE);
        StdDraw.enableDoubleBuffering();
        StdDraw.setYscale(0, Y_SCALE);
        this.vis_x_radius = Y_SCALE * aspect / 2;
        StdDraw.setXscale(0, vis_x_radius * 2);

        this.center = new Point2D.Double(Y_SCALE / 2, vis_x_radius / 2);

        StdDraw.text(600, 450, "Loading");
        Timer drawTimer = new Timer("Draw Timer", true);
        drawTimer.schedule(new TimerTask() {
            public void run() {
                draw();
            }
        }, 0, DRAW_INTERVAL);
    }

    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            StdDraw.setFullScreen();
        } else {
            StdDraw.setNormal();
        }
    }

    public void toggleCameraLock() {
        if (System.currentTimeMillis() - lockTimer < 50) {
            return;
        }
        lockTimer = System.currentTimeMillis();
        this.lockCamera = !this.lockCamera;
        if (lockCamera) {
            centerCamera();
            orientCamera();
        }
    }

    public void setBounds(int maxLogX, int maxLogY) {
        this.maxLogX = maxLogX;
        this.maxLogY = maxLogY;
    }

    public void setMapGrid(MapGrid mapGrid) {
        this.mapGrid = mapGrid;
    }

    public boolean inVisibleRange(double x, double y) {
        return (x > getVisibleXMin() && x < getVisibleXMax() + HUD_WIDTH
                && y > getVisibleYMin() && y < getVisibleYMax());
    }

    public void movePlayer() {
        if (!lockCamera)
            return;
        centerCamera();
        orientCamera();
    }

    // request to move the screen in the given direction, only does so
    // if player is in an appropriate place
    public void moveScreen(int direction, double movementSize) {
        switch (direction) {
            case UP: {
                if (getVisibleYMax() < maxLogY && getVisibleYMax() + movementSize < maxLogY) {
                    if (player != null && player.getY() - movementSize < getVisibleYMin() + EDGE_RADIUS)
                        return;
                    StdDraw.setYscale(getVisibleYMin() + movementSize, getVisibleYMax() + movementSize);
                    center = new Point2D.Double(center.x, center.y + movementSize);
                }
            }
            break;
            case DOWN: {
                if (getVisibleYMin() > 0 && getVisibleYMin() - movementSize > 0) {
                    if (player != null &&player.getY() + movementSize > getVisibleYMax() - EDGE_RADIUS)
                        return;
                    StdDraw.setYscale(getVisibleYMin() - movementSize, getVisibleYMax() - movementSize);
                    center = new Point2D.Double(center.x, center.y - movementSize);
                }
            }
            break;
            case LEFT: {
                if (getVisibleXMin() > 0 && getVisibleXMin() - movementSize > 0) {
                    if (player != null && player.getX() > getVisibleXMax() - movementSize - EDGE_RADIUS)
                        return;
                    StdDraw.setXscale(getVisibleXMin() - movementSize, getVisibleXMax() - movementSize);
                    center = new Point2D.Double(center.x - movementSize, center.y);
                }
            }
            break;
            case RIGHT: {
                if (getVisibleXMax() < maxLogX && getVisibleXMax() + movementSize < maxLogX) {
                    if (player != null &&player.getX() < getVisibleXMin() + movementSize + EDGE_RADIUS)
                        return;
                    StdDraw.setXscale(getVisibleXMin() + movementSize, getVisibleXMax() + movementSize);
                    center = new Point2D.Double(center.x + movementSize, center.y);
                }
            }
            break;
            default:
                System.out.println("WTF mate");
        }
    }

    // called to display the current state of the board
    public void draw() {
        StdDraw.clear();
        StdDraw.rectangle(450, 450, 300, 300);


        for (Animation hsl : animationQueue) {
            if (hsl.getTTL() <= 0)
                animationQueue.remove(hsl);
            hsl.draw(drawFrame);
        }
        for (Actor actor : actorMap.values()) {
            if (actor.getID() == player.getID()) {
                ((Player) actor).draw(false, getAngle(getMouseX(), getMouseY()));
                continue;
            }
            if (actor.getID() == selectedActor)
                actor.draw(true);
            else
                actor.draw(false);
        }
        StdDraw.circle(StdDraw.mouseX(), StdDraw.mouseY(), 10);
        if (mapGrid != null)
            mapGrid.draw();

        if (isKeyPressed(KeyEvent.VK_ESCAPE)) {
            StdDraw.close();
        }

        if (showEdges) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(getVisibleXMin(), getVisibleYMin() + EDGE_RADIUS, getVisibleXMax(), getVisibleYMin() + EDGE_RADIUS);
            StdDraw.line(getVisibleXMin(), getVisibleYMax() - EDGE_RADIUS, getVisibleXMax(), getVisibleYMax() - EDGE_RADIUS);
            StdDraw.line(getVisibleXMin() + EDGE_RADIUS, getVisibleYMin(), getVisibleXMin() + EDGE_RADIUS, getVisibleYMax());
            StdDraw.line(getVisibleXMax() - EDGE_RADIUS, getVisibleYMin(), getVisibleXMax() - EDGE_RADIUS, getVisibleYMax());
            StdDraw.setPenColor();
        }
        StdDraw.setPenColor(StdDraw.GREEN);
        StdDraw.filledCircle(center.x, center.y, 10);
        StdDraw.setPenColor();

        drawHUD();

        handleMouseLocation();
        drawFrame = (drawFrame + 1) % 10000000;
        StdDraw.show();
    }

    private void handleMouseLocation() {
        if (lockCamera)
            return;
        double x = getMouseX();
        double y = getMouseY();
        if (getVisibleXMax() - x < EDGE_RADIUS)
            moveScreen(RIGHT, 10);
        if (x - getVisibleXMin() < EDGE_RADIUS)
            moveScreen(LEFT, 10);
        if (getVisibleYMax() - y < EDGE_RADIUS)
            moveScreen(UP, 10);
        if (y - getVisibleYMin() < EDGE_RADIUS)
            moveScreen(DOWN, 10);
    }

    private void drawHUD() {
        if (player == null) {
            return;
        }
        double hudHeight = VIS_Y_RADIUS * 0.66;
        double hudCenterX = getVisibleXMax() - (HUD_WIDTH / 2);
        double hudNameY = getVisibleYMin() + (hudHeight * 2 / 3);
        double hudHealthY = getVisibleYMin() + (hudHeight * 1 / 3);
        double hudIDY = getVisibleYMin() + (hudHeight * 1 / 6);
        double[] xBox = {getVisibleXMax() - HUD_WIDTH, getVisibleXMax() - HUD_WIDTH, getVisibleXMax(), getVisibleXMax()};
        double[] yBox = {getVisibleYMin(), getVisibleYMin() + HUD_WIDTH, getVisibleYMin() + HUD_WIDTH, getVisibleYMin()};
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.filledPolygon(xBox, yBox);
        StdDraw.setPenColor();
        StdDraw.polygon(xBox, yBox);
        StdDraw.line(getVisibleXMax() - HUD_WIDTH, getVisibleYMin(), getVisibleXMax() - HUD_WIDTH, getVisibleYMin() + hudHeight);
        StdDraw.line(getVisibleXMax() - HUD_WIDTH, getVisibleYMin() + hudHeight, getVisibleXMax(), getVisibleYMin() + hudHeight);
        StdDraw.text(hudCenterX, hudNameY, player.getName());
        StdDraw.text(hudCenterX, hudHealthY, Integer.toString(player.getHP()) + "/" + Integer.toString(player.getMaxHP()));
        StdDraw.text(hudCenterX - 100, hudIDY, ping + "");
        StdDraw.text(hudCenterX + 30, hudIDY, "Weapon: " + player.getWeaponName() + " : " + player.getCurrentClip() + "/" + player.getAmmoCount());
    }

    // the multitude of setters
    public void setSelectedActor(int selectedActor) {
        this.selectedActor = selectedActor;
    }

    public void setPlayer(int playerID) {
        this.player = (Player) actorMap.get(playerID);
        this.center = new Point2D.Double(player.getX(), player.getY());
    }

    public void setExitHandler(ExitHandler exitHandler) {
        this.exitHandler = exitHandler;
    }

    public void setKeyboardHandler(KeyboardHandler keyboardHandler) {
        this.keyboardHandler = keyboardHandler;
    }

    public void setMouseHandler(MouseHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
    }

    public void updatePing(long ping) {
        this.ping = ping;
    }

    public void addAnimation(Animation a) {
        animationQueue.add(a);
    }

    public void setClickedButton(int button) {
        this.clickedButton = button;
    }

    // getters
    public int getClickedButton() {
        return this.clickedButton;
    }

    public double getMouseX() {
        return StdDraw.mouseX();
    }

    public double getMouseY() {
        return StdDraw.mouseY();
    }

    public boolean isMousePressed() {
        return StdDraw.mousePressed();
    }

    public boolean isKeyPressed(int keyCode) {
        return StdDraw.isKeyPressed(keyCode);
    }

    // bounding functions
    private double getVisibleYMin() {
        if (center.y - VIS_Y_RADIUS <= 0)
            return 0;
        if (center.y + VIS_Y_RADIUS > maxLogY)
            return maxLogY - 2 * VIS_Y_RADIUS;
        return center.y - VIS_Y_RADIUS;
    }

    private double getVisibleYMax() {
        if (center.y + VIS_Y_RADIUS >= maxLogY)
            return maxLogY;
        if (center.y - VIS_Y_RADIUS <= 0)
            return 2 * VIS_Y_RADIUS;
        return center.y + VIS_Y_RADIUS;
    }

    private double getVisibleXMin() {
        if (center.x - vis_x_radius <= 0)
            return 0;
        if (center.x + vis_x_radius >= maxLogX)
            return maxLogX - 2 * vis_x_radius;
        return center.x - vis_x_radius;
    }

    private double getVisibleXMax() {
        if (center.x + vis_x_radius > maxLogX)
            return maxLogX;
        if (center.x - vis_x_radius < 0)
            return (2 * vis_x_radius);
        return center.x + vis_x_radius;
    }

    // handler triggers
    public void keyTyped(KeyEvent e) {
        if (keyboardHandler != null) {
            keyboardHandler.handleKeyboard(e);
        }
    }

    public void click(MouseEvent e) {
        setClickedButton(e.getButton());
        if (mouseHandler != null) {
            mouseHandler.handleMouse(getMouseX(), getMouseY());
        }
    }

    public void exit() {
        if (exitHandler != null)
            exitHandler.exit();
    }

    private double getAngle(double destX, double destY) {
        double angle = Math.toDegrees(Math.atan2(destY - player.getY(), destX - player.getX()));

        if (angle < 0) {
            angle += 360;
        }

        return Math.toRadians(angle);
    }

    private void centerCamera() {
        // set to player location
        center = new Point2D.Double(player.getX(), player.getY());

        // adjust for edges
        double centerX = (getVisibleXMin() + getVisibleXMax()) / 2;
        center.x = centerX;
        double centerY = (getVisibleYMin() + getVisibleYMax()) / 2;
        center.y = centerY;

        orientCamera();
    }

    private void orientCamera() {
        StdDraw.setXscale(getVisibleXMin(), getVisibleXMax());
        StdDraw.setYscale(getVisibleYMin(), getVisibleYMax());
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
    }
}
