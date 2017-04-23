package Engine;

import com.sun.security.ntlm.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

/**
 * Created by Gig on 4/5/2017.
 * This guy is gonna be the control class that can create server engines and such.
 * It'll need to listen to any number of clients and set up lobbies and such
 * I wonder how much my poor computer can take lol
 * Oh and this one should tell the clients port numbers probably??
 */
public class ServerBase {
    private int port;
    private HashMap<String, ServerEngine> lobbies;
    private ServerSocket serverSocket;
    public ServerBase() {
        this.port = 3333;
        this.lobbies = new HashMap<>();
        openSocket();
    }

    private void openSocket() {
        try {
            this.serverSocket = new ServerSocket(1901);
        } catch (Exception e) {
            
        }
    }

    public void startLobby(String name, int playerCap) {
        while (!available(port)) {
            port++;
        }
        lobbies.put(name, new ServerEngine(playerCap, port, "ServerTest.gm"));
        //TODO: show lobby player counts, whether they're accepting people
    }

    private Iterable<String> getLobbies() {
        return lobbies.keySet();
    }

    private Iterable<String> getPlayersInLobby(String lobby) {
        return lobbies.get(lobby).getPlayerStates().values();
    }

    private boolean available(int port) {
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
