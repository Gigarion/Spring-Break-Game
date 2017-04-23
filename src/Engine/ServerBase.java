package Engine;

import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    public ServerBase() {
        this.port = 3333;
        this.lobbies = new HashMap<>();
    }

    public void begin() {
        openSocket();
    }

    private void openSocket() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(1901);
                    while (true) {
                        acceptClient();
                        System.out.println("accepted");
                    }
                } catch (Exception e) {
                    System.out.println("bad serversocket");
                }
            }
        }, 0);
    }

    private void acceptClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            String username = (String) inputStream.readObject();
            String pass = (String) inputStream.readObject();
            System.out.println(username + " : " + pass);

            outputStream.writeBoolean(true);

            outputStream.writeObject("localhost");
            outputStream.writeInt(3333);
            System.out.println("wrote port");
            outputStream.flush();
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("error in acceptclient");
        }
    }

    public void startLobby(String name, int playerCap) {
        while (!available(port)) {
            port++;
        }
        System.out.println("Starting lobby on port " + port);
        lobbies.put(name, new ServerEngine(playerCap, port, "ServerTest.gm"));
        //TODO: show lobby player counts, whether they're accepting people
        System.out.println("done");
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
