package Mailroom;

import Engine.ServerEngine;

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
    private ServerEngine.MessageHandler handler;

    public ServerMailroom(int maxClients, ServerEngine.MessageHandler handler) {
        clients = new LinkedList<>();
        mailForServer = new ConcurrentLinkedQueue<>();
        mailForClients = new ConcurrentLinkedQueue<>();
        this.handler = handler;
        try {
            acceptSocket = new ServerSocket(3333);
            // yes, will loop until all clients hook in
            // fight me
            int i = 0;
            while (clients.size() < maxClients) {
                Socket clientSocket = acceptSocket.accept();
                clients.add(new ServerClient(clientSocket, i++));
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
                        // blocks
                        Package p = client.getMessage();
                        handler.handleMail(p);
                        synchronized (ServerMailroom.class) {
                            mailForServer.add(p);
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

    public void sendPackage(Package p, int port) {
        for (ServerClient client : clients) {
            if (client.getPort() == port) {
                client.sendMessage(p);
            }
        }
    }

    public ConcurrentLinkedQueue<Package> getMessages() {
        synchronized (ServerMailroom.class) {
            ConcurrentLinkedQueue<Package> toReturn = new ConcurrentLinkedQueue<>(mailForServer);
            mailForServer = new ConcurrentLinkedQueue<>();
            return toReturn;
        }
    }
}
