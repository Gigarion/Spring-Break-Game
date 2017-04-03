package Util;

import Actors.Actor;
import Actors.Mob;
import Actors.Player;

import java.awt.*;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by Gig on 4/2/2017.
 * used for blocking movement through areas
 * Rather than having to make a bunch of invisible impassable actors
 */
public class MapGrid {
    private int boxSize; // size in pixels of the impassability box
    private boolean[][] grid;
    private int maxBoxX, maxBoxY;
    private boolean showGrid;
    private boolean showBoxes;
    private boolean showPlayerBoxes;
    private Player player;

    public MapGrid(int maxLogX, int maxLogY, int boxSize) {
        this.maxBoxX = maxLogX / boxSize;
        this.maxBoxY = maxLogY / boxSize;
        this.grid = new boolean[maxBoxX][maxBoxY];
        this.boxSize = boxSize;
    }


    public void setPlayer(Player player) {this.player = player;}
    public void setShowGrid(boolean show) {
        this.showGrid = show;
    }
    public void setShowBoxes(boolean show) {
        this.showBoxes = show;
    }

    // returns whether or not the location is inside a blocked box
    public boolean isBlocked(double x, double y) {
        int gridX = convertIndex(x);
        int gridY = convertIndex(y);
        return grid[gridX][gridY];
    }

    public void block(Actor a) {
        for (Point p : getBoxes(a)) {
            block(p);
        }
    }

    public void block(double x, double y) {
        block(new Point(convertIndex(x), convertIndex(y)));
    }

    private void block(Point p) {
        grid[p.x][p.y] = true;
    }

    public void unblock(Actor a) {
        Iterable<Point> toUnblock = getBoxes(a);
        for (Point p : toUnblock) {
            unblock(p);
        }
    }

    private void unblock(Point p) {
        grid[p.x][p.y] = false;
    }

    public void draw() {
        // draw individual boxes
        if (showBoxes) {
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    double x = (boxSize * r) + boxSize / 2.0;
                    double y = (boxSize * r) + boxSize / 2.0;
                    if (grid[r][c])
                        StdDraw.filledRectangle(x, y, boxSize / 2, boxSize / 2);
                }
            }
        }
        if (player != null && showPlayerBoxes) {
            for (Point p : getBoxes(player)) {
                StdDraw.setPenColor(StdDraw.RED);
                StdDraw.filledRectangle((boxSize * p.getX()) + boxSize / 2, (boxSize * p.getY()) + boxSize / 2, boxSize / 2, boxSize / 2);
                StdDraw.setPenColor();
            }
        }
        // draw gridlines
        if (showGrid) {
            for (int i = 0; i < maxBoxX; i++) {
                StdDraw.line(i * boxSize, 0, i * boxSize, maxBoxY * boxSize);
            }
            for (int i = 0; i < maxBoxY; i++) {
                StdDraw.line(0, i * boxSize, maxBoxX * boxSize, i * boxSize);
            }
        }
    }

    private int convertIndex(double index) {
        return (int) (index / boxSize);
    }

    private Iterable<Point> getBoxes(Actor a) {
        LinkedList<Point> toReturn = new LinkedList<>();
        int startX = convertIndex(a.getX());
        int startY = convertIndex(a.getY());

        int minX = Math.max(0, convertIndex(a.getX() - a.getR()));
        int minY = Math.max(0, convertIndex(a.getY() - a.getR()));

        int maxX = Math.min(maxBoxX, convertIndex(a.getX() + a.getR()));
        int maxY = Math.min(maxBoxY, convertIndex(a.getY() + a.getR()));

        // add cross and fill to half diffs
        // pretty much a box i think
        int halfDiffX = (maxX - minX) / 2;
        int halfDiffY = (maxY - minY) / 2;
        for (int i = minX; i <= maxX; i++) {
            toReturn.add(new Point(i, startY));
            for (int j = startY; j < halfDiffY; j++) {;
                toReturn.add(new Point(i, startY + halfDiffY));
                toReturn.add(new Point(i, startY - halfDiffY));
            }
        }
        for (int i = minY; i <= maxY; i++) {
            toReturn.add(new Point(startX, i));
            for (int j = startX; j < halfDiffX; j++) {
                toReturn.add(new Point(i, startX + halfDiffX));
                toReturn.add(new Point(i, startX - halfDiffX));
            }
        }
        return toReturn;
    }

    public boolean validMove(double destX, double destY, Actor a) {
        Mob test = new Mob(-1, destX, destY, a.getR(), 100);
        if (destX < 0 || destY < 0 || destX > maxBoxX * boxSize|| destY > maxBoxY * boxSize)
            return false;
        Iterable<Point> newBoxes = getBoxes(test);
        for (Point p : newBoxes) {
            if (grid[p.x][p.y])
                return false;
        }
        return true;
    }

    public void setShowPlayerBoxes(boolean showPlayerBoxes) {
        this.showPlayerBoxes = showPlayerBoxes;
    }

    // string format = {maxX}/{maxY}/{boxSize}/x,y/x,y/
    public static MapGrid loadFromString(String initString) {
        String[] units = initString.split("/");
        int maxX = Integer.parseInt(units[0]);
        int maxY = Integer.parseInt(units[1]);
        int boxSize = Integer.parseInt(units[2]);
        MapGrid mapGrid = new MapGrid(maxX, maxY, boxSize);
        for (int i = 3; i < units.length; i++) {
            String[] coords = units[i].split(",");
            mapGrid.block(new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
        }
        return mapGrid;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(maxBoxX);
        sb.append("/");
        sb.append(maxBoxY);
        sb.append("/");
        sb.append(boxSize);
        for 
    }
}
