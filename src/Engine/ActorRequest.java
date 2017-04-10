package Engine;

import Actors.Actor;
import Mailroom.Package;

/**
 * Created by Gig on 4/9/2017.
 * (hopefully) lightweight class of small objects that
 * Actors return from their Update methods.  Server uses
 * these to update actor behavior and check for update
 * validity
 */
public class ActorRequest {
    public final static char MOVE = 0;

    private int type;
    private String extra;

    // if you really know what you want
    public ActorRequest(int type, String extra) {
        this.type = type;
        this.extra = extra;
    }

    public static ActorRequest moveTo(double x, double y) {
        return new ActorRequest(MOVE, Package.formCoords(x, y));
    }

    public int getType() {return this.type;}
    public String getExtra() {return this.extra;}
}
