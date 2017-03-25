package Engine;

import Actors.Actor;
import Mailroom.Package;
import Mailroom.ServerMailroom;
import Projectiles.Projectile;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 3/22/2017.
 * Big daddy server
 * handles logic and spreading the news
 */
public class ServerEngine {
    private ServerMailroom mailroom;
    private ConcurrentLinkedQueue<Actor> actorQueue;
    private ConcurrentLinkedQueue<Projectile> projectileQueue;
    public ServerEngine() {
        mailroom = new ServerMailroom(1);
        this.actorQueue = new ConcurrentLinkedQueue<>();
        this.projectileQueue = new ConcurrentLinkedQueue<>();
        setTimers();
    }
    private void setTimers() {
        Timer checkMailTimer = new Timer("Server Engine Mail Timer", true);
        checkMailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    handleMessages();
                }
            }
        }, 0);
    }

    // get the current slew of messages and do stuff with them.
    // I should probs modularize this function
    private void handleMessages() {
        for (Package p : mailroom.getMessages()) {
            System.out.println("handling packages");
            switch(p.getType()) {
                case Package.WELCOME: { // requires port #'s
                    // TODO: think about handshakey game init protocols
                } break;

                case Package.HITSCAN: { // chill, move engine logic to here
                    // TODO: add fireHitscan method to this class
                    // runs the hitscan, then asks whether
                } break;

                case Package.PROJECT: { // chill, ""
                    // TODO: add projectile logic
                    // TODO: verify projectiles before broadcast
                    System.out.println("projectilie");
                    Projectile proj = (Projectile) p.getPayload();
                    projectileQueue.add(proj);
                    mailroom.sendPackage(p);
                } break;

                case Package.NEW_POS: {
                    // TODO: add actor ID's so this is possible
                    // TODO: verify move is legal?? otherwise correct?? is this necessary??
                    mailroom.sendPackage(p);
                } break; // requires actor/player IDS

                case Package.ANIMATE: { mailroom.sendPackage(p); } break;

                case Package.ACTOR: { // actually this one is less chill
                    // TODO: verify actor before sending it out
                    // add actor to my queue
                    Actor a = (Actor) p.getPayload();
                    actorQueue.add(a);
                    // broadcast packet
                    mailroom.sendPackage(p);
                } break;

                case Package.HIT: { System.out.println("ERROR: hit package @ server");} break; // doesn't occur

                default: System.out.println("unhandled package type: " + p.getType()); break;
            }
        }
    }
}
