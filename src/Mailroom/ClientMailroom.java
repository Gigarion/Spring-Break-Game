package Mailroom;

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

    public ClientMailroom() {
        beginAndConnect();
    }

    private void beginAndConnect() {
        try {
            socket = new Socket("localhost", 3333);
            System.out.println(socket);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            alive = true;
        } catch (ConnectException e) {
            beginAndConnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
