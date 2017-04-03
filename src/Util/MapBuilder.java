package Util;

import Gui.UserBox;

import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 4/2/2017.
 */
public class MapBuilder {
    private MapGrid mapGrid;
    private UserBox userBox;

    public MapBuilder(int maxX, int maxY, int boxSize) {
        this.mapGrid = new MapGrid(maxX, maxY, boxSize);
        mapGrid.setShowGrid(true);
        mapGrid.setShowBoxes(true);
        this.userBox = new UserBox(new ConcurrentHashMap<>(), new ConcurrentLinkedQueue<>());
        StdDraw.attachUserBox(userBox);
        userBox.setMapGrid(mapGrid);
        userBox.setBounds(maxX, maxY);
        userBox.setMouseHandler((x, y)-> mark(userBox.getMouseX(), userBox.getMouseY()));
        userBox.setKeyboardHandler(this::handleKeyboard);

        userBox.begin();
    }

    public void mark(double x, double y) {
        System.out.println(x + " : " + y + " *****");
        mapGrid.block(x, y);
        System.out.println(mapGrid.toString());
    }

    public void handleKeyboard(KeyEvent e) {

    }
}
