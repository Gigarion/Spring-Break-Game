package Mailroom;

import Engine.ServerEngine;
import com.sun.security.ntlm.Server;

import java.net.*;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Gig on 3/22/2017.
 * Handles server-side mail
 */
public class ServerMailroom {
    private ServerSocket acceptSocket;
    private LinkedList<ServerClient> clients;
    private ServerEngine.MessageHandler handler;
    private AtomicInteger nextId;

    public ServerMailroom(int maxClients, ServerEngine.MessageHandler handler) {
        clients = new LinkedList<>();
        this.handler = handler;
        this.nextId = new AtomicInteger();
        //mailTimer = new Timer("Server Mailroom Timer", true);
        try {
            acceptSocket = new ServerSocket(3333);
            // yes, will loop until all clients hook in
            // fight me
            int i = 0;
            while (clients.size() < maxClients) {
                Socket clientSocket = acceptSocket.accept();
                clients.add(new ServerClient(clientSocket, getNextId()));
                Thread.yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (ServerClient client : clients)
            setTimer(client);
    }

    private void setTimer(ServerClient client) {
        System.out.println("new client" + client.getPort());
        // Create a thread for the given client, block on IO for each one until that socket is closed
        new Timer(client.toString(), true).schedule(new TimerTask() {
            @Override
            public void run() {
                while (true) {
                    // blocks
                    Package p = client.getMessage();
                    if (!client.isAlive()) {
                        handleLostClient(client);
                        break;
                    }
                    handler.handleMessage(p);
                }
            }
        }, 100);
    }

    public void sendPackage(Package p) {
        for (ServerClient client : clients) {
            if (!client.isAlive()) {
                handleLostClient(client);
                continue;
            }
            client.sendMessage(p);
        }
    }

    // attempt to allow a new client to join
    private synchronized void handleLostClient(ServerClient client) {
        if (!clients.contains(client)) {
            return;
        }
        System.out.println("one died...");
        clients.remove(client);
        handler.handleMessage(new Package(client.getPort(), Package.DISCONNECT));
        new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("trying to get new client");
                    Socket clientSocket = acceptSocket.accept();
                    System.out.println("new client");
                    ServerClient newClient = new ServerClient(clientSocket, getNextId());
                    clients.add(newClient);
                    setTimer(newClient);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.run();
    }

    public void sendPackage(Package p, int port) {
        for (ServerClient client : clients) {
            if (client.getPort() == port) {
                client.sendMessage(p);
            }
        }
    }

    private int getNextId() {
        return nextId.getAndIncrement();
    }
}
