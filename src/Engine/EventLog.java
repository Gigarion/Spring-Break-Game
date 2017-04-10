package Engine;

import Actors.Actor;
import Actors.Player;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gig on 4/9/2017.
 * this is designed to be a standing record of all important things that happen in a game
 * hopefully this doesn't take too much memory, I'll refactor to a no-memory handler
 * implementation if necessary
 */
public class EventLog {
    private int gameID;
    private LinkedList<String> eventLog;
    private HashMap<Integer, Player> playerMap;
    private long startTime;

    public EventLog(int gameID) {
        this.gameID = gameID;
        this.eventLog = new LinkedList<>();
        this.playerMap = new HashMap<>();
    }

    // start the event timer for accurate timekeeping
    public void begin() {
        startTime = System.currentTimeMillis();
    }

    public void givePlayer(int id, Player player) {
        playerMap.put(id, player);
    }

    public void kill(Actor killer, Actor victim) {
        String killerText, victimText;
        if (killer instanceof Player)
            killerText = ((Player) killer).getName();
        else
            killerText = "#" + killer.getID();
        if (victim instanceof Player) {
            victimText = ((Player) victim).getName();
            eventLog.add(getCurrTime() + victimText + " died");
        }
        else
            victimText = "#" + victim.getID();
        eventLog.add(getCurrTime() + killerText + " killed " + victimText);
    }

    private String getCurrTime() {
        long diff = System.currentTimeMillis() - startTime;
        return  TimeUnit.MILLISECONDS.toHours(diff) + " : " + TimeUnit.MILLISECONDS.toMinutes(diff) + " : "
                + TimeUnit.MICROSECONDS.toSeconds(diff) + "::";

    }

    public Iterable<String> getLogs() {
        return new LinkedList<>(eventLog);
    }
}
