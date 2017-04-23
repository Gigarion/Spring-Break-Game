package Mailroom;

import Actors.*;
import Engine.ServerEngine;
import Projectiles.Projectile;
import com.sun.security.ntlm.Server;

import java.net.ServerSocket;
import java.net.Socket;
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
    private ConcurrentLinkedQueue<ServerClient> clients;
    private ServerEngine.MessageHandler handler;
    private ServerEngine.PlayerJoinHandler playerJoinHandler;
    private AtomicInteger nextId;
    private int maxClients;

    public ServerMailroom(int maxClients, ServerEngine.MessageHandler handler, ServerEngine.PlayerJoinHandler playerJoinHandler) {
        clients = new ConcurrentLinkedQueue<>();
        this.handler = handler;
        this.playerJoinHandler = playerJoinHandler;
        this.maxClients = maxClients;
        this.nextId = new AtomicInteger();
    }

    public void begin() {
        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    acceptSocket = new ServerSocket(3333);
                    // yes, will loop until all clients hook in
                    // fight me, but it blocks yeh? so why the fight?
                    while (clients.size() < maxClients) {
                        Socket clientSocket = acceptSocket.accept();
                        ServerClient client = new ServerClient(clientSocket, getNextId());
                        clients.add(client);
                        setTimer(client);
                        playerJoinHandler.playerJoined();
                        Thread.yield();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.setDaemon(true);
        t.run();
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

    public void sendActor(Actor a) {
        for (ServerClient client : clients) {
            sendActor(a, client.getPort());
        }
    }

    public void sendActor(Actor a, int port) {
        ActorStorage as = null;
        if (a instanceof Projectile)
            as = ActorStorage.getProjectile((Projectile) a);
        else if (a instanceof Player)
            as = ActorStorage.getPlayerStore((Player) a);
        else if (a instanceof Mob)
            as = ActorStorage.getMob((Mob) a);
        else if (a instanceof WeaponDrop)
            as = ActorStorage.getWeaponDropStore((WeaponDrop) a);
        else return;
        sendPackage(new Package(as, Package.ACTOR), port);
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
    private void handleLostClient(ServerClient client) {
        if (!clients.contains(client)) {
            return;
        }
        System.out.println("one died...");
        clients.remove(client);
        handler.handleMessage(new Package(client.getPort(), Package.DISCONNECT));

        Runnable task2 = () -> {
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
        };
        task2.run();
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
