package com.hitchh1k3rsguide.utils.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.hitchh1k3rsguide.ld31.entities.AbstractEntity;
import com.hitchh1k3rsguide.ld31.entities.EntityPlayer;

public class Server
{

    public static final String HOST = "127.0.0.1"; // TODO make sure to change this!!!
    public final static int PORT = 8875;

    public static final int OKAY = 0x00;
    public final static int CLOSE = 0x01;

    public static GameServerThread gameThread;

    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;

    public static final int MAX_CLIENT_COUNT = 100;
    public static volatile ClientThread[] threads = new ClientThread[MAX_CLIENT_COUNT];
    public static int clientsConnected = 0;
    public static int monsterCount = 0;

    public static void main(String args[])
    {
        Packet.isServer = true;
        gameThread = new GameServerThread();

        try
        {
            serverSocket = new ServerSocket(PORT);
        }
        catch (IOException e)
        {
            System.out.println(e);
            System.exit(-1);
        }

        System.out.println("Starting server...");
        while (true)
        {
            try
            {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < MAX_CLIENT_COUNT; i++)
                {
                    if (threads[i] == null)
                    {
                        System.out.println("Client connected ("
                                + clientSocket.getRemoteSocketAddress() + ")");
                        (threads[i] = new ClientThread(i, clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == MAX_CLIENT_COUNT)
                {
                    final OutputStream os = clientSocket.getOutputStream();
                    System.out.println("sending close packet...");
                    new Packet(Packet.CLOSE, "There are too many people on the server.")
                            .send(new IPacketQueTarget()
                            {

                                @Override
                                public OutputStream getStream()
                                {
                                    return os;
                                }

                                @Override
                                public ClientThread getClientThread()
                                {
                                    return null;
                                }

                            });
                    os.close();
                    clientSocket.close();
                }
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        }
    }

    public static void broadcast(Packet packet)
    {
        for (int i = 0; i < MAX_CLIENT_COUNT; i++)
        {
            if (threads[i] != null)
            {
                packet.send(threads[i]);
            }
        }
    }

    public static class ClientThread extends Thread implements IPacketQueTarget
    {

        private InputStream is = null;
        public OutputStream os = null;
        private Socket clientSocket = null;
        private volatile ClientThread[] threads;
        private int maxClientsCount;
        private UUID uuid;
        public EntityPlayer player;
        public int index;
        public SendThread sendBuffer;
        private Thread sendThread;

        public ClientThread(int index, Socket clientSocket, ClientThread[] threads)
        {
            this.index = index;
            this.clientSocket = clientSocket;
            this.threads = threads;
            maxClientsCount = threads.length;
        }

        @Override
        public void run()
        {
            int maxClientsCount = this.maxClientsCount;

            try
            {
                is = clientSocket.getInputStream();
                os = clientSocket.getOutputStream();
                sendBuffer = new SendThread(os);
                sendThread = new Thread(sendBuffer);
                sendThread.start();
                ++clientsConnected;
                while (true)
                {
                    Packet packet = null;
                    try
                    {
                        packet = new Packet(is);

                        if (packet.process(this) == Server.CLOSE)
                        {
                            break;
                        }
                    }
                    catch (Exception ex)
                    {
                        if (!(ex instanceof IOException))
                        {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                --clientsConnected;
                sendBuffer.running = false;

                for (int i = 0; i < maxClientsCount; i++)
                {
                    if (threads[i] == this)
                    {
                        threads[i] = null;
                    }
                }

                Server.gameThread.leaveWorld(this);

                System.out.println("Client left (" + clientSocket.getRemoteSocketAddress() + ")");
                if (uuid != null)
                {
                    this.player.stashPlayer("player-" + uuid.toString());
                }

                is.close();
                os.close();
                clientSocket.close();
            }
            catch (IOException e)
            {
            }
        }

        public void setUUID(UUID uuid)
        {

            for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
            {
                if (Server.threads[i] != null && Server.threads[i].uuid != null
                        && Server.threads[i].uuid.equals(uuid))
                {
                    new Packet(Packet.CLOSE, "Someone with your UUID is already connected.")
                            .send(this);
                    return;
                }
            }

            System.out.println("Setting client's UUID to: " + uuid.toString());
            this.uuid = uuid;
            this.player = new EntityPlayer();
            this.player.getPlayer("player-" + uuid.toString());
            if (this.player.team == 0)
            {
                new Packet(Packet.REQUEST_TEAM, null).send(this);
            }
            else
            {
                Server.gameThread.joinWorld(this);
            }
        }

        public static class SendThread implements Runnable
        {

            private OutputStream stream;
            public boolean running = true;
            private ArrayList<Packet> packetBuffer = new ArrayList<Packet>();
            // private boolean lock = false;
            private Lock lock = new ReentrantLock();

            public SendThread(OutputStream os)
            {
                stream = os;
            }

            public synchronized void addPacket(Packet packet)
            {
                lock.lock();
                packetBuffer.add(packet);
                lock.unlock();
            }

            @Override
            public void run()
            {
                while (running)
                {
                    if (lock.tryLock())
                    {
                        Iterator<Packet> packetIT = packetBuffer.iterator();
                        while (packetIT.hasNext())
                        {
                            Packet packet = packetIT.next();
                            try
                            {
                                stream.write(packet.serialData);
                                stream.flush(); // this does nothing?
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            packetIT.remove();
                        }
                        lock.unlock();
                    }
                    try
                    {
                        Thread.sleep(25);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        public OutputStream getStream()
        {
            return null;
        }

        @Override
        public ClientThread getClientThread()
        {
            return this;
        }

    }

    public static void addEntity(AbstractEntity entity)
    {
        try
        {
            if (entity != null)
            {
                int key = GameServerThread.getFreeKey();
                GameServerThread.entities.put(key, entity);
                broadcast(new Packet(Packet.ENTITY_ADD, new Object[] { entity.getType(), key,
                        entity.serialize() }));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static EntityPlayer getRandomPlayer()
    {
        int startIndex = (int) (Math.random() * Math.min(MAX_CLIENT_COUNT, clientsConnected));
        for (int i = startIndex; i < MAX_CLIENT_COUNT; i++)
        {
            if (threads[i] != null && threads[i].player != null && threads[i].player.canBeHit())
            {
                return threads[i].player;
            }
        }
        for (int i = 0; i < startIndex; i++)
        {
            if (threads[i] != null && threads[i].player != null && threads[i].player.canBeHit())
            {
                return threads[i].player;
            }
        }
        return null;
    }

}