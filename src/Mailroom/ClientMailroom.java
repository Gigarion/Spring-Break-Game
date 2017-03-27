package Mailroom;

import java.net.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 3/21/2017.
 * M
 */
public class ClientMailroom {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ConcurrentLinkedQueue<Package> inboundMail;
    private ConcurrentLinkedQueue<Package> outboundMail;
    private Object inboundLock;
    private Object outboundLock;

    public ClientMailroom() {
        outboundMail = new ConcurrentLinkedQueue<>();
        outboundLock = new Object();
        inboundMail = new ConcurrentLinkedQueue<>();
        inboundLock = new Object();
        connectAndBegin();
    }

    public boolean isGood() {
        return socket != null;
    }

    public boolean connectAndBegin() {
        try {
            socket = new Socket("localhost", 3333);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            startTimers();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // two daemon threads to check for packages to send and receive messages
    private void startTimers() {
        new Timer("Receive Timer", true).schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Package p;
                        synchronized (ClientMailroom.class) {
                            if (socket.isClosed())
                                break;
                            p = (Package) inputStream.readObject();
                        }
                        synchronized (inboundLock) {
                            inboundMail.add(p);
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0);
    }

    public void sendMessage(Package pack) {
        synchronized (outboundLock) {
            try {
                outputStream.writeObject(pack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // get the current stack of mail from this mailroom
    public Iterable<Package> getMessages() {
        synchronized (inboundLock) {
            ConcurrentLinkedQueue<Package> currentMessages = new ConcurrentLinkedQueue<>(inboundMail);
            inboundMail = new ConcurrentLinkedQueue<>();
            return currentMessages;
        }
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
