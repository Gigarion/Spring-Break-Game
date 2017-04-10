package Tools;

import Actors.Actor;
import Actors.Player;
import Gui.UserBox;
import Maps.GameMap;
import Maps.MapGrid;
import Util.StdDraw;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 4/2/2017.
 * This beautiful class is MSPaint for map building
 * Currently allows single-type invisible wall building in the background
 * TODO: implement the 0-3 scale of walls, click the numbers to switch
 * TODO: implement actor adding, make a palette form or something
 */
public class MapBuilder {
    private static final int UP = KeyEvent.VK_W;
    private static final int DOWN = KeyEvent.VK_S;
    private static final int LEFT = KeyEvent.VK_A;
    private static final int RIGHT = KeyEvent.VK_D;
    private static final int MOVEMENT_SIZE = 1;
    private MapGrid mapGrid;
    private UserBox userBox;
    private Player player;
    private GameMap gameMap;
    private char currHeight;

    public MapBuilder(int maxX, int maxY, int boxSize, String image) {
        this.mapGrid = new MapGrid(maxX, maxY, boxSize);
        mapGrid.setShowGrid(true);
        mapGrid.setShowBoxes(true);
        currHeight = 3;

        this.gameMap = new GameMap(maxX, maxY, image);
        gameMap.setMapGrid(mapGrid);

        setupUserBox(maxX, maxY);

        new Timer("event loop mapbuilder", true).schedule(new TimerTask() {
            @Override
            public void run() {
                handleKeyboard();
                handleMouse();
            }
        }, 0, 1);
    }

    public MapBuilder(String selectedMap) {
        this.gameMap = new GameMap(selectedMap);
        this.mapGrid = gameMap.getMapGrid();
        mapGrid.setShowGrid(true);
        mapGrid.setShowBoxes(true);
        setupUserBox(gameMap.getMaxX(), gameMap.getMaxY());

        new Timer("event loop mapbuilder", true).schedule(new TimerTask() {
            @Override
            public void run() {
                handleKeyboard();
                handleMouse();
            }
        }, 0, 1);
    }

    private void setupUserBox(int maxX, int maxY) {
        Player sample = new Player("Test");
        ConcurrentHashMap<Integer, Actor> actorMap = new ConcurrentHashMap<>();
        actorMap.put(0, sample);
        this.userBox = new UserBox(actorMap, new ConcurrentLinkedQueue<>());

        userBox.setPlayer(0);
        this.player = sample;

        StdDraw.attachUserBox(userBox);
        userBox.setGameMap(gameMap);
        userBox.setBounds(maxX, maxY);
        userBox.setShowEdges(true);

        userBox.setKeyboardHandler(this::keyboard);
        userBox.begin();
    }

    private void mark(double x, double y) {
        mapGrid.block(x, y, currHeight);
    }

    private void unmark(double x, double y) {
        mapGrid.unblock(x, y);
    }

    private void handleMouse() {
        if (userBox.isMousePressed()) {
            if (userBox.getClickedButton() == MouseEvent.BUTTON1)
                mark(userBox.getMouseX(), userBox.getMouseY());
            else
                unmark(userBox.getMouseX(), userBox.getMouseY());
        }
    }

    private void handleKeyboard() {
        double oldX = player.getX();
        double oldY = player.getY();
        if (userBox.isKeyPressed(UP)) {
            if (mapGrid.validMove(player.getX(), player.getY() + MOVEMENT_SIZE, player)) {
                player.moveY(MOVEMENT_SIZE);
            }
        }
        if (userBox.isKeyPressed(DOWN)) {
            if (mapGrid.validMove(player.getX(), player.getY() - MOVEMENT_SIZE, player)) {
                player.moveY(-MOVEMENT_SIZE);
            }
        }
        if (userBox.isKeyPressed(LEFT)) {
            if (mapGrid.validMove(player.getX() - MOVEMENT_SIZE, player.getY(), player)) {
                player.moveX(-MOVEMENT_SIZE);
            }
        }
        if (userBox.isKeyPressed(RIGHT)) {
            if (mapGrid.validMove(player.getX() + MOVEMENT_SIZE, player.getY(), player)) {
                player.moveX(MOVEMENT_SIZE);
            }
        }
        if (player.getX() != oldX || player.getY() != oldY) {
            userBox.movePlayer();
        }
    }

    private void keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            SaveMapDialog smd = new SaveMapDialog(gameMap);
            smd.pack();
            smd.setVisible(true);
        }
        switch(e.getKeyCode()) {
            case KeyEvent.VK_0: currHeight = 0; break;
            case KeyEvent.VK_1: currHeight = 1; break;
            case KeyEvent.VK_2: currHeight = 2; break;
            case KeyEvent.VK_3: currHeight = 3; break;
            default: break;
        }
    }
}
