package Mailroom;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Gig on 3/21/2017.
 * M
 */
public class ClientMailroom {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public ClientMailroom() {
        try {
            socket = new Socket("localhost", 3333);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
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
