package Mailroom;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import Projectiles.Projectile;

/**
 * Created by Gig on 3/22/2017.
 * helper class for clients of the server
 */
public class ServerClient {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    public ServerClient(Socket socket) {
        this.socket = socket;
        try {
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Package pack) {
        try {
            outputStream.writeObject(pack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // polls the top package from the inputstream
    // hopefully it's fast enough..  lel
    public Package getMessage() {
        try {
            Package p = (Package) inputStream.readObject();
            p.setPort(socket.getLocalPort());
            return  p;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
