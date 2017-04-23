package Mailroom;

import java.io.Serializable;

/**
 * Created by Gig on 3/21/2017.
 * Packages are the format in which data is sent from
 * client to server and back.  This class contains all
 * you need to form these packages
 */

public class Package implements Serializable {
    // Initial handshake to engine
    // Client:: Payload: Player object, Extra: Player Name
    // Server:: Payload: Integer id, Extra: n/a
    public static final char WELCOME = 0;

    // Client request to fire a hitscan, server then does hitscan logic
    // Client:: Payload: HitScan object, Extra: Source ID
    // Server:: n/a
    public static final char HITSCAN = 1;

    // Client requests a projectile be put on the field
    // Client:: Payload: Projectile object, Extra: ID of spawning actor
    // Server:: ""
   // public static final char PROJECT = 2;

    // Either client or Server demands a position update,
    // mostly used to notify of new player positions
    // Client:: Payload: Integer id, Extra: formatted coordinate string
    // Server:: ""
    public static final char NEW_POS = 3;

    // Either client or server asks to add an animation to the game
    // Client: Payload: Animation object, Extra: ID of source player
    // Server: Echoes client packet
    public static final char ANIMATE = 4;

    // Somebody requests an Actor be added to the game
    // sends an ActorStorage Object, not the full actor
    // Client:: ?????
    // Server:: Payload: ActorStorage, Extra: n/a
    public static final char ACTOR   = 5;

    // Server notifies the field of a hit to an actor
    // Client:: n/a
    // Server:: Payload: Integer id of actor, Extra: integer damage
    public static final char HIT     = 6;

    // Contains a GameMapStorage object the client ought to use
    // Client:: n/a
    // Server:: Payload: GameMapStorage, Extra: n/a
    public static final char GAME_MAP = 7; // signals the end of engine initialization.

    // remove an actor because it is no longer relevant
    // Client:: Payload: Integer id of actor, Extra: n/a
    // Server:: ""
    public static final char REMOVE = 8;


    // player tries to interact with an interactable, sends to server to
    // address conflicts with self and others
    // Client:: Payload: Integer id of actor, Extra: Integer id of interactable
    // Server:: Payload: Integer id of interactable, Extra: n/a
    public static final char INTERACT = 9;


    // informs server that a client disconnected and that player should get removed
    // Client:: n/a
    // Server:: Payload: Integer port number, Extra: n/a
    public static final char DISCONNECT = 10;

    // ping packet for connection checking and speed testing
    // Client: Payload: Long milliseconds
    // Server: Echoes client packet
    public static final char PING = 11;

    // orientation packet, sets logical max and min for client
    // Client:: n/a
    // Server:: Payload: formatted coordinate string, Extra: n/a
    public static final char SCR_SIZE = 12;

    private Object payload;
    private char type;
    private String extra;
    private int port;

    // basic package, payload and type
    public Package(Object payload, char type) {
        this.payload = payload;
        this.type = type;
        this.port = -1;
    }

    // for those that need the extra string
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

    void setPort(int port) { this.port = port; }
    public int getPort() { return this.port; }
    public int getType() { return this.type; }
    public Object getPayload() { return this.payload;}
    public String getExtra() { return this.extra; }
}
