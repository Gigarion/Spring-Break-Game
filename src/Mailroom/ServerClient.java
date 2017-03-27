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

    public ServerClient(Socket socket, int id) {
        System.out.println("HEY IM NEW" + id);
        isDeadLock = new Object();
        this.socket = socket;
        this.id = id;
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
                if (id == -1)
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
                if (id == -1)
                    return null;
            }
            p = (Package) inputStream.readObject();
            p.setPort(id);
            return p;
        } catch (EOFException e) {
            System.out.println("WTF EOF Exception");
            this.id = -1;
            return null;
        } catch (Exception e) {
            System.out.println("*****************************");
            this.id = -1;
            e.printStackTrace();
            return null;
        }
    }

    public int getPort() {
        return this.id;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
