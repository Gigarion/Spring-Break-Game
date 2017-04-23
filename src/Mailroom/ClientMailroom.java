package Mailroom;

import Actors.*;
import Projectiles.Projectile;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by Gig on 3/21/2017.
 * M
 */
public class ClientMailroom {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean alive;

    public ClientMailroom(String ip, int port) {
        beginAndConnect(ip, port);
    }

    private void beginAndConnect(String ip, int port) {
        try {
            //String addr = "52.3.232.116";
            String addr = "localhost";
            socket = new Socket(addr, port);
            System.out.println(socket);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            alive = true;
        } catch (ConnectException e) {
            beginAndConnect(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendActor(Actor a) {
        ActorStorage as = ActorStorage.getActorStore(a);
        sendMessage(new Package(as, Package.ACTOR));
    }


    // send a single message
    public synchronized void sendMessage(Package pack) {
        try {
            outputStream.writeObject(pack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // return a message, blocks. gotta stop that infinite loop business
    public Package getMessage() {
        try {
            Package p = null;
            if (!socket.isClosed())
                p = (Package) inputStream.readObject();
            return p;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAlive() {
        return alive;
    }

    public void exit() {
        synchronized (ClientMailroom.class) {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
