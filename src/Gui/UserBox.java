package Gui;

import Actors.Actor;
import Actors.Player;
import Animations.Animation;
import Maps.GameMap;
import Maps.MapGrid;
import Util.StdDraw;

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
        void handleMouse(MouseEvent e, double x, double y);
    }
    // a handler for mouse events from the user

    public interface KeyboardHandler {
        void handleKeyboard(KeyEvent e);
    }


    private static final int UP = KeyEvent.VK_W;
    private static final int DOWN = KeyEvent.VK_S;
    private static final int LEFT = KeyEvent.VK_A;
    private static final int RIGHT = KeyEvent.VK_D;

    private static final int Y_SCALE = 1000;
    private static final int DRAW_INTERVAL = 10;
    private static final int HUD_WIDTH = (int) (0.27 * Y_SCALE);
    private static final int VIS_Y_RADIUS = Y_SCALE / 2;
    private static final int EDGE_RADIUS = (int )(Y_SCALE * 0.11);

    private double vis_x_radius;

    private int drawFrame;                      // which frame are we on
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ConcurrentLinkedQueue<Animation> animationQueue;

    private ExitHandler exitHandler;            // called for engine to handle exits
    private MouseHandler mouseHandler;          // called for engine to handle mouse events
    private KeyboardHandler keyboardHandler;    // called for engine to handle keyboard events


    private int clickedButton;
    private int selectedActor;                  // id of the selected actor
    private Player player;

    private int maxLogX, maxLogY;
    private Point2D.Double center;
    private long ping;

    private boolean showEdges;
    private boolean lockCamera;
    private long lockTimer;

    private MapGrid mapGrid;
    private GameMap gameMap;

    private double mouseX, mouseY;

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

        StdDraw.clearCursor();
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

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
        this.mapGrid = gameMap.getMapGrid();
        mapGrid.setShowBoxes(true);
    }

    public boolean inVisibleRange(double x, double y) {
        return (x > getVisibleXMin() && x < getVisibleXMax() + HUD_WIDTH
                && y > getVisibleYMin() && y < getVisibleYMax());
    }

    public void movePlayer() {
        if (!lockCamera)
            return;
        centerCamera();
    }

    // request to move the screen in the given direction, only does so
    // if player is in an appropriate place
    private void moveScreen(int direction, double movementSize) {
        switch (direction) {
            case UP: {
                if (getVisibleYMax() < maxLogY && getVisibleYMax() + movementSize < maxLogY) {
                    if (player != null && player.getY() - movementSize < getVisibleYMin() + EDGE_RADIUS)
                        return;
                    StdDraw.setYscale(getVisibleYMin() + movementSize, getVisibleYMax() + movementSize);
                    center = new Point2D.Double(center.x, center.y + movementSize);
                    mouseY += movementSize;
                }
            }
            break;
            case DOWN: {
                if (getVisibleYMin() > 0 && getVisibleYMin() - movementSize > 0) {
                    if (player != null &&player.getY() + movementSize > getVisibleYMax() - EDGE_RADIUS)
                        return;
                    StdDraw.setYscale(getVisibleYMin() - movementSize, getVisibleYMax() - movementSize);
                    center = new Point2D.Double(center.x, center.y - movementSize);
                    mouseY -= movementSize;
                }
            }
            break;
            case LEFT: {
                if (getVisibleXMin() > 0 && getVisibleXMin() - movementSize > 0) {
                    if (player != null && player.getX() > getVisibleXMax() - movementSize - EDGE_RADIUS)
                        return;
                    StdDraw.setXscale(getVisibleXMin() - movementSize, getVisibleXMax() - movementSize);
                    center = new Point2D.Double(center.x - movementSize, center.y);
                    mouseX -= movementSize;
                }
            }
            break;
            case RIGHT: {
                if (getVisibleXMax() < maxLogX && getVisibleXMax() + movementSize < maxLogX) {
                    if (player != null &&player.getX() < getVisibleXMin() + movementSize + EDGE_RADIUS)
                        return;
                    StdDraw.setXscale(getVisibleXMin() + movementSize, getVisibleXMax() + movementSize);
                    center = new Point2D.Double(center.x + movementSize, center.y);
                    mouseX += movementSize;
                }
            }
            break;
            default:
                System.out.println("WTF mate, improper move attempt");
        }
    }

    // called to display the current state of the board
    public void draw() {
        if (gameMap != null) {
            gameMap.draw();
        }
        else {
            try {
                StdDraw.picture(maxLogX / 2, maxLogY / 2, "src/img/Maps/map.png", maxLogX, maxLogY);
            } catch (Exception e) {
                StdDraw.picture(maxLogX / 2, maxLogY / 2, "img/Maps/map.png", maxLogX, maxLogY);
            }
        }

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

        drawHUD();
        drawCrosshair();

        handleMouseLocation();
        drawFrame = (drawFrame + 1) % 10000000;
        StdDraw.show();
    }

    private void drawHUD() {
        if (player == null) {
            return;
        }
        double hudHeight = VIS_Y_RADIUS * 0.5;
        double hudCenterX = getVisibleXMax() - (HUD_WIDTH / 2) - 10;
        double hudThirdX = getVisibleXMax() - (HUD_WIDTH * 2 / 3) - 10;
        double hudHalfY = getVisibleYMin() + (hudHeight / 2);
        double hudThirdY = getVisibleYMin() + (hudHeight * 1 / 3);
        double hudSixthY = getVisibleYMin() + (hudHeight * 1 / 6);
        double[] xBox = {getVisibleXMax() - HUD_WIDTH, getVisibleXMax() - HUD_WIDTH, getVisibleXMax(), getVisibleXMax()};
        for (int i = 0; i < xBox.length; i++) {
            xBox[i] -= 10;
        }
        double[] yBox = {getVisibleYMin(), getVisibleYMin() + hudHeight, getVisibleYMin() + hudHeight, getVisibleYMin()};
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.filledPolygon(xBox, yBox);
        StdDraw.setPenColor();
        StdDraw.polygon(xBox, yBox);

        StdDraw.text(hudCenterX,  hudSixthY + (hudHeight * 2/3), player.getName());
        StdDraw.text(hudCenterX, hudHalfY, "HP: " + player.getHP() + "/" + player.getMaxHP());
        StdDraw.text(hudCenterX, hudThirdY, "Ping: " + ping);
        StdDraw.text(hudCenterX, hudSixthY, "Weapon: " + player.getWeaponName() + " : " + player.getCurrentClip() + "/" + player.getAmmoCount());
    }

    private void drawCrosshair() {
        double ratio = player.getChargeRatio();
        StdDraw.setPenRadius(0.004);
        if (ratio == 0)
            StdDraw.setPenColor(StdDraw.RED);
        else if (ratio < 1)
            StdDraw.setPenColor(StdDraw.YELLOW);
        else StdDraw.setPenColor(StdDraw.GREEN);
        StdDraw.circle(mouseX, mouseY, 10);
        StdDraw.setPenRadius();
        StdDraw.setPenColor();
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
    public void setClickedButton(int button) {
        this.clickedButton = button;
    }

    public void updatePing(long ping) {
        this.ping = ping;
    }

    // getters
    public int getClickedButton() {
        return this.clickedButton;
    }
    public double getMouseX() {
        return mouseX;
    }
    public double getMouseY() {
        return mouseY;
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
    public void handleMouse(MouseEvent e) {
        //setClickedButton(e.getButton());
        this.mouseX = StdDraw.mouseX();
        this.mouseY = StdDraw.mouseY();
        if (mouseHandler != null) {
            mouseHandler.handleMouse(e, getMouseX(), getMouseY());
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
        // update mouse
        double oldX =  center.x;
        double oldY =  center.y;

        // set to player location
        center = new Point2D.Double(player.getX(), player.getY());

        // adjust for edges
        center.x = (getVisibleXMin() + getVisibleXMax()) / 2;
        center.y = (getVisibleYMin() + getVisibleYMax()) / 2;

        mouseX += center.x - oldX;
        mouseY += center.y - oldY;

        orientCamera();
    }
    private void orientCamera() {
        StdDraw.setXscale(getVisibleXMin(), getVisibleXMax());
        StdDraw.setYscale(getVisibleYMin(), getVisibleYMax());
    }

    public void setShowEdges(boolean showEdges) {
        this.showEdges = showEdges;
    }

    public void setMouseXY(double x, double y) {
        mouseX = x;
        mouseY = y;
    }
}
