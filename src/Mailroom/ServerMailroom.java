package Mailroom;

import java.net.*;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Gig on 3/22/2017.
 * Handles server-side mail
 */
public class ServerMailroom {
    private ServerSocket acceptSocket;
    private LinkedList<ServerClient> clients;
    private ConcurrentLinkedQueue<Package> mailForServer;
    private ConcurrentLinkedQueue<Package> mailForClients;

    public ServerMailroom(int maxClients) {
        clients = new LinkedList<>();
        mailForServer = new ConcurrentLinkedQueue<>();
        mailForClients = new ConcurrentLinkedQueue<>();

        try {
            acceptSocket = new ServerSocket(3333);

            // yes, will loop until all clients hook in
            // fight me
            while (clients.size() < maxClients) {
                Socket clientSocket = acceptSocket.accept();
                clients.add(new ServerClient(clientSocket));
                Thread.yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTimers();
    }

    public void init() {

    }

    private void setTimers() {
        // Create a thread for each client, block on IO for each one until that socket is closed
        for (ServerClient client : clients) {
            new Timer(client.toString(), true).schedule(new TimerTask() {
                @Override
                public void run() {
                    while (!client.isClosed()) {
                        Package p = client.getMessage();
                        synchronized (ServerMailroom.class) {
                            mailForServer.add(p);
                            System.out.println("testing consumption");
                        }
                    }
                }
            }, 0);
        }
    }

    public void sendPackage(Package p) {
        for (ServerClient client : clients) {
            client.sendMessage(p);
        }
    }

    public Iterable<Package> getMessages() {
        synchronized (ServerMailroom.class) {
            if (mailForServer.size() > 0)
                System.out.println("server bound : " + mailForServer.size());
            ConcurrentLinkedQueue<Package> toReturn = new ConcurrentLinkedQueue<>(mailForServer);
            mailForServer = new ConcurrentLinkedQueue<>();
            if (toReturn.size() > 0)
                System.out.println("Size : " + toReturn.size());
            return toReturn;
        }
    }
}
