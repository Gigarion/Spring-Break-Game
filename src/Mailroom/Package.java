package Mailroom;

import java.io.Serializable;

/**
 * Created by Gig on 3/21/2017.
 */
public class Package implements Serializable {
    public static final char WELCOME = 0;
    public static final char CLICK   = 1;
    public static final char HITSCAN = 2;
    public static final char PROJECT = 3;
    public static final char NEW_POS = 4;
    public static final char ANIMATE = 5;
    public static final char ACTOR   = 6;
    public static final char HIT     = 7;
    private Object payload;
    private char type;

    // the welcome package to the server to ID the user
    public Package(String userName) {
        payload = userName;
        type = WELCOME;
    }

    public Package(Object payload, char type) {
        this.payload = payload;
        this.type = type;
    }

    public int getType() { return this.type; }
}
