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

    public ClientMailroom() {
        outboundMail = new ConcurrentLinkedQueue<>();
        inboundMail = new ConcurrentLinkedQueue<>();
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
        new Timer("Send Timer", true).schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (outboundMail) {
                    while (outboundMail.size() > 0) {
                        try {
                            outputStream.writeObject(outboundMail.remove());
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 1);
        new Timer("Receive Timer", true).schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (inboundMail) {
                    for (int i = 0; i < 5; i++) {
                        try {
                            if (inputStream.available() > 0) {
                                inboundMail.add((Package) inputStream.readObject());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 1);
    }

    public void sendMessage(Package pack) {
        synchronized (outboundMail) {
            if (!isGood())
                throw new IllegalArgumentException("Connection not established, invalid send");
            outboundMail.add(pack);
        }
    }

    // get the current stack of mail from this mailroom
    public Iterable<Package> getMessages() {
        synchronized (inboundMail) {
            ConcurrentLinkedQueue<Package> currentMessages = new ConcurrentLinkedQueue<>(inboundMail);
            inboundMail = new ConcurrentLinkedQueue<>();
            return currentMessages;
        }
    }
}
