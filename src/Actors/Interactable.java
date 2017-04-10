package Actors;

/**
 * Created by Gig on 3/28/2017.
 * actors which implement this class can be interacted with
 * by players and must implement the interact() method
 */
public interface Interactable {
     Iterable<Object> interact(Player p);
}
