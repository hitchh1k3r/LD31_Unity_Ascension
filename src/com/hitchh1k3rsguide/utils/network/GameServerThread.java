package com.hitchh1k3rsguide.utils.network;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hitchh1k3rsguide.ld31.data.TeamScore;
import com.hitchh1k3rsguide.ld31.data.WorldEvents;
import com.hitchh1k3rsguide.ld31.entities.AbstractEntity;
import com.hitchh1k3rsguide.ld31.entities.EntityMonster;
import com.hitchh1k3rsguide.ld31.entities.EntityPlayer;

public class GameServerThread implements Runnable
{

    public static ConcurrentHashMap<Integer, AbstractEntity> entities = new ConcurrentHashMap<Integer, AbstractEntity>();
    public static Set<Integer> entityKeys = entities.keySet();
    public TeamScore[] teamScores = new TeamScore[7];

    public GameServerThread()
    {
        for (int i = 0; i < 7; ++i)
        {
            teamScores[i] = new TeamScore(i);
        }

        new Thread(this).start();
    }

    public static int getFreeKey()
    {
        int i = 100;
        while (entities.containsKey(i))
        {
            ++i;
        }
        return i;
    }

    private final double TIME_STEP = 1.0 / 20.0;

    @Override
    public void run()
    {
        double lastTime = getTime();
        Server.ClientThread[] threads = Server.threads;
        int counter = 10;
        int playerCounter = 0;
        double passedTime = 0;
        double secondTimer = 0;
        double monsterTimer = 0;
        int scoreSaveTimer = 10000; // 33ish minutes
        while (true)
        {
            double time = (getTime() - lastTime);
            passedTime += time;
            secondTimer += time;
            lastTime = getTime();
            if (Server.monsterCount < 7 && Server.monsterCount < (Server.clientsConnected + 2))
            {
                if (monsterTimer <= 0)
                {
                    monsterTimer = (Math.random() * 3) + 2;
                    ++Server.monsterCount;
                    Server.addEntity(new EntityMonster((int) (Math.random() * (240 - 52))));
                }
                monsterTimer -= time;
            }
            if (secondTimer > 1)
            {
                secondTimer = 0;
                for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
                {
                    if (threads[i] != null && threads[i].player != null)
                    {
                        if (threads[i].player.deathCooldown > 0)
                        {
                            threads[i].player.deathCooldown--;
                            if (threads[i].player.deathCooldown == 0)
                            {
                                Server.broadcast(WorldEvents.makePacket(
                                        (int) threads[i].player.position.x
                                                + EntityPlayer.ITEM_OFFSET_X,
                                        (int) threads[i].player.position.y
                                                + EntityPlayer.ITEM_OFFSET_Y,
                                        WorldEvents.PLAYER_SPAWN, threads[i].player.team));
                            }
                        }
                        else if (threads[i].player.team > 0 && threads[i].player.ascension == 0)
                        {
                            ++threads[i].player.scores.scores[4];
                            ++teamScores[threads[i].player.team - 1].scores[4];
                        }
                    }
                }
                for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
                {
                    if (threads[i] != null && threads[i].player != null)
                    {
                        new Packet(Packet.SCORE_UPDATE, new Object[] { threads[i].player.scores,
                                teamScores[0], teamScores[1], teamScores[2], teamScores[3],
                                teamScores[4], teamScores[5] }).send(threads[i]);
                    }
                }
            }
            while (passedTime > TIME_STEP)
            {
                passedTime -= TIME_STEP;
                for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
                {
                    if (threads[i] != null && threads[i].player != null)
                    {
                        threads[i].player.serverUpdate();
                    }
                }
                for (Iterator<Map.Entry<Integer, AbstractEntity>> it = entities.entrySet()
                        .iterator(); it.hasNext();)
                {
                    Map.Entry<Integer, AbstractEntity> entry = it.next();
                    if (entry.getValue().fixedUpdate())
                    {
                        int key = entry.getKey();
                        Server.broadcast(new Packet(Packet.ENTITY_REMOVE, key));
                        it.remove();
                    }
                }
            }
            --counter;
            if (counter < 0)
            {
                counter = 10;
                for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
                {
                    if (threads[i] != null)
                    {
                        updateWorld(threads[i]);
                    }
                }
            }
            --scoreSaveTimer;
            if (scoreSaveTimer < 0)
            {
                System.out.println("Saving scores..."); // runs about every 30 minutes
                scoreSaveTimer = 10000;
                for (int i = 0; i < 7; ++i)
                {
                    teamScores[i].stashScores("team_" + i);
                }
            }
            --playerCounter;
            if (playerCounter < 0)
            {
                playerCounter = 0; // update interval
                for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
                {
                    if (threads[i] != null && threads[i].player != null)
                    {
                        try
                        {
                            Packet packet = new Packet(Packet.ENTITY_UPDATE, new Object[] { i,
                                    threads[i].player.serialize() });
                            Packet packet2 = new Packet(Packet.ENTITY_UPDATE, new Object[] { -1,
                                    threads[i].player.serialize() });
                            for (int p = 0; p < Server.MAX_CLIENT_COUNT; ++p)
                            {
                                if (threads[p] != null && i != p)
                                {
                                    packet.send(threads[p]);
                                }
                            }
                            packet2.send(threads[i]);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            cpuNice(passedTime);
        }
    }

    private double getTime()
    {
        return System.nanoTime() / 1000000000.0;
    }

    private void updateWorld(Server.ClientThread client)
    {
        for (int key : entityKeys)
        {
            try
            {
                new Packet(Packet.ENTITY_UPDATE,
                        new Object[] { key, entities.get(key).serialize() }).send(client);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void leaveWorld(Server.ClientThread client)
    {
        Server.ClientThread[] threads = Server.threads;
        Packet packet = new Packet(Packet.ENTITY_REMOVE, client.index);
        Packet leavePacket = WorldEvents.makePacket((int) client.player.position.x
                + EntityPlayer.ITEM_OFFSET_X, (int) client.player.position.y
                + EntityPlayer.ITEM_OFFSET_Y, WorldEvents.PLAYER_LEAVE, 0);

        for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
        {
            if (threads[i] != null && threads[i] != client)
            {
                packet.send(threads[i]);
                leavePacket.send(threads[i]);
            }
        }
    }

    public void joinWorld(Server.ClientThread client)
    {
        Server.ClientThread[] threads = Server.threads;
        // new Packet(Packet.SET_TEAM, client.player.team).send(client.os);
        try
        {
            new Packet(Packet.ENTITY_UPDATE, new Object[] { -1, client.player.serialize() })
                    .send(client);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        for (int key : entityKeys)
        {
            try
            {
                new Packet(Packet.ENTITY_ADD, new Object[] { entities.get(key).getType(), key,
                        entities.get(key).serialize() }).send(client);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
        {
            if (threads[i] != null && threads[i] != client)
            {
                try
                {
                    new Packet(Packet.ENTITY_ADD, new Object[] { AbstractEntity.ENTITY_PLAYER, i,
                            threads[i].player.serialize() }).send(client);
                    new Packet(Packet.ENTITY_ADD, new Object[] { AbstractEntity.ENTITY_PLAYER,
                            client.index, client.player.serialize() }).send(threads[i]);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void cpuNice(double passedTime)
    {
        int wait = (int) (200 - passedTime);
        if (wait < 10)
        {
            wait = 10;
        }
        try
        {
            Thread.sleep(wait);
        }
        catch (InterruptedException e)
        {
        }
    }

}
