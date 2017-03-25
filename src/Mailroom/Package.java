package Mailroom;

import java.io.Serializable;

/**
 * Created by Gig on 3/21/2017.
 */

public class Package implements Serializable {
    public static final char WELCOME = 0;
    public static final char HITSCAN = 1;
    public static final char PROJECT = 2;
    public static final char NEW_POS = 3;
    public static final char ANIMATE = 4;
    public static final char ACTOR   = 5;
    public static final char HIT     = 6;
    private Object payload;
    private char type;
    private String extra;
    private int port;

    // the welcome package to the server to ID the user
    public Package(String userName) {
        payload = userName;
        type = WELCOME;
        this.port = -1;
    }

    public Package(Object payload, char type) {
        this.payload = payload;
        this.type = type;
        this.port = -1;
    }

    public Package(Object payload, char type, String extra) {
        this.payload = payload;
        this.type = type;
        this.extra = extra;
        this.port = -1;
    }

    public static String formCoords(double x, double y) {
        return Double.toString(x) + "/" + Double.toString(y);
    }

    public static double[] extractCoords(String payload) {
        String[] strVersion = payload.split("/");
        double[] toReturn = new double[2];
        toReturn[0] = Double.parseDouble(strVersion[0]);
        toReturn[1] = Double.parseDouble(strVersion[1]);
        return toReturn;
    }

    public void setPort(int port) { this.port = port; }
    public String getPort(String port) { return port; }
    public int getType() { return this.type; }
    public Object getPayload() { return this.payload;}
    public String getExtra() { return this.extra; }
}
