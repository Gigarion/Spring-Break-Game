package Engine;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Gig on 4/5/2017.
 * This guy is gonna be the control class that can create server engines and such.
 * It'll need to listen to any number of clients and set up lobbies and such
 * I wonder how much my poor computer can take lol
 * Oh and this one should tell the clients port numbers probably??
 */
public class ServerBase {
    private int port;
    public ServerBase() {
        this.port = 3333;
    }

    public void startLobby(int playerCap) {
        while (!available(port)) {
            port++;
        }
        new ServerEngine(playerCap, port);
    }

    public boolean available(int port) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            return true;
        } catch(IOException e) {
            System.out.println(port + " in use");
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    System.out.println("Ver bad, shouldn't be here");
                }
            }
        }
        return false;
    }
}
