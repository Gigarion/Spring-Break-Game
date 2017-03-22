package Engine;

import Mailroom.ServerMailroom;

/**
 * Created by Gig on 3/22/2017.
 * Big daddy server
 * handles logic and spreading the news
 */
public class ServerEngine {
    private ServerMailroom mailroom;
    public ServerEngine() {
        mailroom = new ServerMailroom(1);
    }
}
