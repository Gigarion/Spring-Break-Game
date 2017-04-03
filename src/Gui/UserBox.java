package Gui;

import Actors.Actor;
import Actors.Player;
import Animations.Animation;
import Util.MapGrid;
import Util.MapLoader;
import Util.StdDraw;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 3/27/2017.
 * This is going to be the graphics and user event handling engine, decoupling
 * ClientEngine from StdDraw to improve modularity
 *
 * Mostly cause imma hack StdDraw to pieces eventually.
 *
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

    private MapGrid mapGrid;

    public UserBox(ConcurrentHashMap<Integer, Actor> actorMap, ConcurrentLinkedQueue<Animation> animationQueue) {
        this.actorMap = actorMap;
        this.animationQueue = animationQueue;
        StdDraw.attachUserBox(this);
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

        StdDraw.text(600, 450, "Loading");
        Timer drawTimer = new Timer("Draw Timer", true);
        drawTimer.schedule(new TimerTask() {
            public void run() {
                draw();
            }
        }, 0, DRAW_INTERVAL);
    }

    public void setBounds(int maxLogX, int maxLogY) {
        this.maxLogX = maxLogX;
        this.maxLogY = maxLogY;
    }
    public void setMapGrid(MapGrid mapGrid) {this.mapGrid = mapGrid;}
    public boolean inVisibleRange(double x, double y) {
        return (x > getVisibleXMin() && x < getVisibleXMax() + HUD_WIDTH
                && y > getVisibleYMin() && y < getVisibleYMax());
    }

    // request to move the screen in the given direction, only does so
    // if player is in an appropriate place
    public void moveScreen(int direction, double movementSize) {
        switch(direction) {
            case UP: {
                if (getVisibleYMax() < maxLogY && getVisibleYMax() + movementSize < maxLogY) {
                    StdDraw.setYscale(getVisibleYMin() + movementSize, getVisibleYMax() + movementSize);
                }
            } break;
            case DOWN: {
                if (getVisibleYMin() > 0 && getVisibleYMin() - movementSize > 0) {
                    StdDraw.setYscale(getVisibleYMin() - movementSize, getVisibleYMax() - movementSize);
                }
            } break;
            case LEFT: {
                if (getVisibleXMin() > 0 && getVisibleXMin() - movementSize > 0) {
                    StdDraw.setXscale(getVisibleXMin() - movementSize, getVisibleXMax() - movementSize);
                }
            }
            break;
            case RIGHT: {
                if (getVisibleXMax() < maxLogX && getVisibleXMax() + movementSize < maxLogX) {
                    StdDraw.setXscale(getVisibleXMin() + movementSize, getVisibleXMax() + movementSize);
                }
            }
            break;
            default: System.out.println("WTF mate");
        }
    }

    // called to display the current state of the board
    public void draw() {
        StdDraw.clear();
        StdDraw.rectangle(450, 450, 300, 300);
        drawHUD();

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
        StdDraw.show();

        if (isKeyPressed(KeyEvent.VK_ESCAPE)) {
            StdDraw.close();
        }
        drawFrame = (drawFrame + 1) % 10000000;
    }

    private void drawHUD() {
        double hudHeight = VIS_Y_RADIUS * 0.66;
        double hudCenterX = getVisibleXMax() - (HUD_WIDTH / 2);
        double hudNameY = getVisibleYMin() + (hudHeight * 2 / 3);
        double hudHealthY = getVisibleYMin() + (hudHeight * 1 / 3);
        double hudIDY = getVisibleYMin() + (hudHeight * 1 / 6);
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
    }
    public void setExitHandler(ExitHandler exitHandler) {this.exitHandler = exitHandler; }
    public void setKeyboardHandler(KeyboardHandler keyboardHandler) {this.keyboardHandler = keyboardHandler;}
    public void setMouseHandler(MouseHandler mouseHandler) {this.mouseHandler = mouseHandler; }
    public void updatePing(long ping) {
        this.ping = ping;
    }
    public void addAnimation(Animation a) {
        animationQueue.add(a);
    }
    public void setClickedButton(int button) {this.clickedButton = button;}

    // getters
    public int getClickedButton() {return this.clickedButton;}
    public double getMouseX() {return StdDraw.mouseX();}
    public double getMouseY() {return StdDraw.mouseY();}
    public boolean isMousePressed() {return StdDraw.mousePressed();}
    public boolean isKeyPressed(int keyCode) {return StdDraw.isKeyPressed(keyCode);}

    // bounding functions
    private double getVisibleYMin() {
        if (player.getY() - VIS_Y_RADIUS <= 0)
            return 0;
        if (player.getY() + VIS_Y_RADIUS > maxLogY)
            return maxLogY - 2 * VIS_Y_RADIUS;
        return player.getY() - VIS_Y_RADIUS;
    }
    private double getVisibleYMax() {
        if (player.getY() + VIS_Y_RADIUS >= maxLogY)
            return maxLogY;
        if (player.getY() - VIS_Y_RADIUS <= 0)
            return 2 * VIS_Y_RADIUS;
        return player.getY() + VIS_Y_RADIUS;
    }
    private double getVisibleXMin() {
        if (player.getX() - vis_x_radius <= 0)
            return 0;
        if (player.getX() + vis_x_radius >= maxLogX)
            return maxLogX - 2 * vis_x_radius;
        return player.getX() - vis_x_radius;
    }
    private double getVisibleXMax() {
        if (player.getX() + vis_x_radius > maxLogX)
            return maxLogX;
        if (player.getX() - vis_x_radius < 0)
            return (2 * vis_x_radius);
        return player.getX() + vis_x_radius;
    }

    // handler triggers
    public void keyTyped(KeyEvent e) {
        if (keyboardHandler != null) {
            keyboardHandler.handleKeyboard(e);
        }
    }
    public void click(MouseEvent e) {
        setClickedButton(e.getButton());
        mouseHandler.handleMouse(getMouseX(), getMouseY());
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
}
