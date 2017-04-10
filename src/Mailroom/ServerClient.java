package Mailroom;


import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * Created by Gig on 3/22/2017.
 * helper class for clients of the server
 */
class ServerClient {
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final Object isDeadLock;
    private int id;
    private boolean live;

    ServerClient(Socket socket, int id) {
        System.out.println("HEY IM NEW" + id);
        isDeadLock = new Object();
        this.id = id;
        this.live = true;
        try {
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMessage(Package pack) {
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
    Package getMessage() {
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

    int getPort() {
        return this.id;
    }

    boolean isAlive() {
        return this.live;
    }
}
