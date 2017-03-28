package Mailroom;


import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * Created by Gig on 3/22/2017.
 * helper class for clients of the server
 */
public class ServerClient {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Object isDeadLock;
    private int id;
    private boolean live;

    public ServerClient(Socket socket, int id) {
        System.out.println("HEY IM NEW" + id);
        isDeadLock = new Object();
        this.socket = socket;
        this.id = id;
        this.live = true;
        try {
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Package pack) {
        try {
            synchronized (isDeadLock) {
                if (!live)
                    return;
                outputStream.writeObject(pack);
            }
        } catch (Exception e) {
            System.out.println("failed write to socket");
            //e.printStackTrace();
        }
    }

    // polls the top package from the inputstream
    // hopefully it's fast enough..  lel
    public Package getMessage() {
        Package p;
        try {
            synchronized (isDeadLock) {
                if (!live)
                    return null;
            }
            p = (Package) inputStream.readObject();
            p.setPort(id);
            return p;
        } catch (EOFException e) {
            System.out.println("Client closed");
            this.live = false;
            return null;
        } catch (Exception e) {
            System.out.println("*****************************");
            this.live = false;
            e.printStackTrace();
            return null;
        }
    }

    public int getPort() {
        return this.id;
    }

    public boolean isAlive() {
        return this.live;
    }
}
