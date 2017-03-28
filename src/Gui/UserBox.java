package Gui;

import Actors.Actor;
import Actors.Player;
import Animations.Animation;
import Util.StdDraw;

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
    private static final int DRAW_INTERVAL = 16;
    private static final int VIS_RADIUS = 450;


    private int drawFrame;                      // which frame are we on
    private ConcurrentHashMap<Integer, Actor> actorMap;
    private ConcurrentLinkedQueue<Animation> animationQueue;
    private ExitHandler exitHandler;            // called for engine to handle exits
    private MouseHandler mouseHandler;          // called for engine to handle mouse events
    private KeyboardHandler keyboardHandler;    // called for engine to handle keyboard events
    private int selectedActor;                  // id of the selected actor

    private Player player;
    private int visibleRadius;
    private int maxLogX, maxLogY;
    private long ping;
    private int clickedButton;

    public UserBox(ConcurrentHashMap<Integer, Actor> actorMap, ConcurrentLinkedQueue<Animation> animationQueue) {
        this.actorMap = actorMap;
        this.animationQueue = animationQueue;
        this.visibleRadius = VIS_RADIUS;
        StdDraw.attachUserBox(this);
    }

    public void begin() {
        Timer drawTimer = new Timer("Draw Timer", true);
        drawTimer.schedule(new TimerTask() {
            public void run() {
                draw();
            }
        }, 0, DRAW_INTERVAL);
    }

    public void setVisibleBounds(int maxLogX, int maxLogY) {
        this.maxLogX = maxLogX;
        this.maxLogY = maxLogY;
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
                    StdDraw.setXscale(getVisibleXMin() - movementSize, getVisibleXMax() - movementSize + HUD_WIDTH);
                }
            }
            break;
            case RIGHT: {
                if (getVisibleXMax() < maxLogX && getVisibleXMax() + movementSize < maxLogX) {
                    StdDraw.setXscale(getVisibleXMin() + movementSize, getVisibleXMax() + movementSize + HUD_WIDTH);
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
            if (actor.getID() == selectedActor)
                actor.draw(true);
            else
                actor.draw(false);
        }
        StdDraw.circle(StdDraw.mouseX(), StdDraw.mouseY(), 10);
        StdDraw.show();
        drawFrame = (drawFrame + 1) % 10000000;
    }

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
        StdDraw.text(hudCenterX - 30, hudIDY, ping + "");
        StdDraw.text(hudCenterX + 30, hudIDY, "selected: " + selectedActor);
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

    // handler triggers
    public void keyPressed(KeyEvent e) {
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
}
