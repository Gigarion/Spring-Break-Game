import Engine.ClientEngine;
import Engine.ServerEngine;
import Gui.ClientMenu;
import Gui.ServerMenu;
import com.sun.deploy.util.SessionState;

/**
 * Created by Gig on 4/6/2017.
 */
public class RunScript {
    public static void main(String[] args) {
        int clientCount = Integer.parseInt(args[0]);
        new ServerMenu(clientCount, 3333);
        for (int i = 0; i < clientCount; i++) {
            new Thread() {
                public void run() {
                    new ClientMenu();
                }
            }.run();
        }
    }
}
