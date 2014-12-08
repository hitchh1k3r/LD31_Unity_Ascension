package com.hitchh1k3rsguide.utils.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import com.hitchh1k3rsguide.ld31.Game;
import com.hitchh1k3rsguide.ld31.data.TeamScore;
import com.hitchh1k3rsguide.ld31.data.WorldEvents;
import com.hitchh1k3rsguide.ld31.entities.AbstractEntity;
import com.hitchh1k3rsguide.ld31.entities.EntityBullet;
import com.hitchh1k3rsguide.ld31.entities.EntityPlayer;
import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.math.Serialization;

public class Packet
{

    public static boolean isServer = false;

    public static final int CLOSE = 0x01; // tells the other side to close the connection
    public static final int JOIN = 0x02; // join the game (and send a self generated UUID)
    public static final int REQUEST_TEAM = 0x03; // ask client to provide a team
    public static final int SET_TEAM = 0x04; // tell the other side what team you're on
    public static final int ENTITY_ADD = 0x05; // tell client to add a new entity (and provide type and ID)
    public static final int ENTITY_REMOVE = 0x06; // tell client to remove an entity
    public static final int ENTITY_UPDATE = 0x07; // tell server where your player is, tell client where an entity is
    public static final int SCORE_UPDATE = 0x08; // tells client what the current score board stats are
    public static final int WORLD_EVENT = 0x09; // tells clients to play sound or do graphical event at location 

    public int type;
    public Object data;
    public byte[] serialData;

    public Packet(int type, Object data)
    {
        this.type = type;
        this.data = data;
    }

    public Packet(InputStream is) throws Exception
    {
        int typeByte = is.read();
        if (typeByte == -1)
        {
            throw new IOException();
        }
        deserialize(typeByte, is);
    }

    public void send(IPacketQueTarget target)
    {
        try
        {
            if (serialData == null)
            {
                reserialize();
            }
            Server.ClientThread clientThread = target.getClientThread();
            if (clientThread == null)
            {
                target.getStream().write(serialData);
            }
            else
            {
                clientThread.sendBuffer.addPacket(this);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void deserialize(int typeByte, InputStream is) throws IOException
    {
        this.type = typeByte;
        if (type == Packet.JOIN)
        {
            byte[] bytes = new byte[Long.SIZE / 8];
            is.read(bytes);
            long least = Serialization.deserializeLong(bytes);
            is.read(bytes);
            long most = Serialization.deserializeLong(bytes);
            data = new UUID(most, least);
        }
        else if (type == Packet.CLOSE)
        {
            System.out.println("deserializing close packet...");
            byte[] bytes = new byte[4];
            is.read(bytes);
            int stringLength = Serialization.deserializeInt(bytes);
            bytes = new byte[stringLength];
            is.read(bytes);
            data = new String(bytes, Charset.forName("UTF-8"));
        }
        else if (type == Packet.SET_TEAM)
        {
            byte[] bytes = new byte[4];
            is.read(bytes);
            data = Serialization.deserializeInt(bytes);
        }
        else if (type == Packet.WORLD_EVENT)
        {
            byte[] bytes = new byte[4];
            is.read(bytes);
            int x = Serialization.deserializeInt(bytes);
            is.read(bytes);
            int y = Serialization.deserializeInt(bytes);
            is.read(bytes);
            int type = Serialization.deserializeInt(bytes);
            is.read(bytes);
            int variant = Serialization.deserializeInt(bytes);
            data = new int[] { x, y, type, variant };
        }
        else if (type == Packet.SCORE_UPDATE)
        {
            // data = Object[] {TeamScore, TeamScore, TeamScore, TeamScore, TeamScore, TeamScore, TeamScore}
            TeamScore[] scores = new TeamScore[7];
            byte[] bytes = new byte[4];
            for (int i = 0; i < 7; ++i)
            {
                scores[i] = new TeamScore();
                for (int s = 0; s < 7; ++s)
                {
                    is.read(bytes);
                    scores[i].scores[s] = Serialization.deserializeInt(bytes);
                }
            }
            data = new Object[] { scores[0], scores[1], scores[2], scores[3], scores[4], scores[5],
                    scores[6] };
        }
        else if (type == Packet.ENTITY_ADD)
        {
            byte[] bytes = new byte[4];
            is.read(bytes);
            int entityType = Serialization.deserializeInt(bytes);
            is.read(bytes);
            int entityID = Serialization.deserializeInt(bytes);
            is.read(bytes);
            int entityDataLength = Serialization.deserializeInt(bytes);
            byte[] entityData = new byte[entityDataLength];
            is.read(entityData);
            data = new Object[] { entityType, entityID, entityData };
        }
        else if (type == Packet.ENTITY_REMOVE)
        {
            byte[] bytes = new byte[4];
            is.read(bytes);
            data = Serialization.deserializeInt(bytes);
        }
        else if (type == Packet.ENTITY_UPDATE)
        {
            byte[] bytes = new byte[4];
            is.read(bytes);
            int entityID = Serialization.deserializeInt(bytes);
            is.read(bytes);
            int entityDataLength = Serialization.deserializeInt(bytes);
            byte[] entityData = new byte[entityDataLength];
            is.read(entityData);
            data = new Object[] { entityID, entityData };
        }
    }

    private byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(type);
        if (type == Packet.JOIN)
        {
            UUID uuid = (UUID) data;

            bytes.write(Serialization.serializeLong(uuid.getLeastSignificantBits()));
            bytes.write(Serialization.serializeLong(uuid.getMostSignificantBits()));
        }
        else if (type == Packet.CLOSE)
        {
            System.out.println("serializing close packet... ");
            byte[] stringBytes = ((String) data).getBytes(Charset.forName("UTF-8"));

            bytes.write(Serialization.serializeInt(stringBytes.length));
            bytes.write(stringBytes);
        }
        else if (type == Packet.SET_TEAM)
        {
            int team = (int) data;

            bytes.write(Serialization.serializeInt(team));
        }
        else if (type == Packet.WORLD_EVENT)
        {
            int[] eventData = (int[]) data;

            bytes.write(Serialization.serializeInt(eventData[0]));
            bytes.write(Serialization.serializeInt(eventData[1]));
            bytes.write(Serialization.serializeInt(eventData[2]));
            bytes.write(Serialization.serializeInt(eventData[3]));
        }
        else if (type == Packet.SCORE_UPDATE)
        {
            TeamScore score;
            for (int i = 0; i < 7; ++i)
            {
                score = (TeamScore) ((Object[]) data)[i];
                for (int s = 0; s < 7; ++s)
                {
                    bytes.write(Serialization.serializeInt(score.scores[s]));
                }
            }
        }
        else if (type == Packet.ENTITY_ADD)
        {
            int entityType = (int) ((Object[]) data)[0];
            int entityID = (int) ((Object[]) data)[1];
            byte[] entityData = (byte[]) ((Object[]) data)[2];

            bytes.write(Serialization.serializeInt(entityType));
            bytes.write(Serialization.serializeInt(entityID));
            bytes.write(Serialization.serializeInt(entityData.length));
            bytes.write(entityData);
        }
        else if (type == Packet.ENTITY_REMOVE)
        {
            int id = (int) data;

            bytes.write(Serialization.serializeInt(id));
        }
        else if (type == Packet.ENTITY_UPDATE)
        {
            int entityID = (int) ((Object[]) data)[0];
            byte[] entityData = (byte[]) ((Object[]) data)[1];

            bytes.write(Serialization.serializeInt(entityID));
            bytes.write(Serialization.serializeInt(entityData.length));
            bytes.write(entityData);
        }
        return bytes.toByteArray();
    }

    public int process(Object callingThread)
    {
        Server.ClientThread client = null;
        // GameClientThread server = null;
        if (isServer)
        {
            client = (Server.ClientThread) callingThread;
        }
        else
        {
            // this used to be used....
            // server = (GameClientThread) callingThread;
        }

        if (type == Packet.CLOSE)
        {
            System.out.println("Server Connection Closed: " + ((String) data));
            if (!Packet.isServer)
            {
                GenericUtils.crash("Connection to the server was closed because:\n"
                        + ((String) data));
            }
            return Server.OKAY;
        }
        else if (type == Packet.JOIN)
        {
            if (isServer)
            {
                client.setUUID((UUID) data);
            }
            return Server.OKAY;
        }
        else if (type == Packet.REQUEST_TEAM)
        {
            if (!isServer)
            {
                Game.instance.mainPlayer.needsTeam = true;
            }
            return Server.OKAY;
        }
        else if (type == Packet.SET_TEAM)
        {
            if (isServer && client.player.team == 0)
            {
                client.player.team = (int) data;
                Server.gameThread.joinWorld(client);
                Server.broadcast(WorldEvents.makePacket((int) client.player.position.x
                        + EntityPlayer.ITEM_OFFSET_X, (int) client.player.position.y
                        + EntityPlayer.ITEM_OFFSET_Y, WorldEvents.PLAYER_SPAWN, client.player.team));
            }
            else
            {
                // This should be set via an Entity_Update now...
                // System.out.println("team is set");
                // Game.instance.mainPlayer.team = (int) data;
            }
            return Server.OKAY;
        }
        else if (type == Packet.WORLD_EVENT)
        {
            if (!isServer)
            {
                int[] eventData = (int[]) data;
                WorldEvents.doEvent(eventData[0], eventData[1], eventData[2], eventData[3]);
            }
            return Server.OKAY;
        }
        else if (type == Packet.SCORE_UPDATE)
        {
            if (!isServer)
            {
                Game.instance.mainPlayer.scores = (TeamScore) ((Object[]) data)[0];
                for (int i = 1; i < 7; ++i)
                {
                    Game.instance.teamScores[i - 1] = (TeamScore) ((Object[]) data)[i];
                }
            }
            return Server.OKAY;
        }
        else if (type == Packet.ENTITY_ADD)
        {
            if (isServer)
            {
                int type = (int) ((Object[]) data)[0];
                if (type == AbstractEntity.ENTITY_BULLET && (int) ((Object[]) data)[1] == -1)
                {
                    AbstractEntity entity = AbstractEntity
                            .entityFromType((int) ((Object[]) data)[0]);
                    int key = GameServerThread.getFreeKey();
                    if (entity != null && client.player.health > 1)
                    {
                        GameServerThread.entities.put(key, entity);
                        entity.deserialize((byte[]) ((Object[]) data)[2]);
                        entity.lastPosition = entity.position;
                        ((EntityBullet) entity).owner = client.index;
                        try
                        {
                            if (((EntityBullet) entity).type == 0)
                            {
                                Server.broadcast(WorldEvents.makePacket((int) entity.position.x,
                                        (int) entity.position.y, WorldEvents.SHOOT_GUN, 0));
                            }
                            else
                            {
                                Server.broadcast(WorldEvents.makePacket((int) entity.position.x,
                                        (int) entity.position.y, WorldEvents.SHOOT_HEAL, 0));
                            }
                            Server.broadcast(new Packet(Packet.ENTITY_ADD, new Object[] {
                                    entity.getType(), key, entity.serialize() }));
                            client.player.changeHealth(-1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else
            {
                AbstractEntity entity = AbstractEntity.entityFromType((int) ((Object[]) data)[0]);
                if (entity != null)
                {
                    Game.instance.entities.put((int) ((Object[]) data)[1], entity);
                    entity.deserialize((byte[]) ((Object[]) data)[2]);
                    entity.lastPosition = entity.position;
                }
            }
            return Server.OKAY;
        }
        else if (type == Packet.ENTITY_REMOVE)
        {
            if (!isServer)
            {
                Game.instance.entities.remove((int) data);
            }
            return Server.OKAY;
        }
        else if (type == Packet.ENTITY_UPDATE)
        {
            if (isServer)
            {
                int entityIndex = (int) ((Object[]) data)[0];
                if (entityIndex == -1)
                {
                    client.player.deserialize((byte[]) ((Object[]) data)[1]);
                }
            }
            else
            {
                int entityIndex = (int) ((Object[]) data)[0];
                if (entityIndex == -1)
                {
                    Game.instance.mainPlayer.deserialize((byte[]) ((Object[]) data)[1]);
                }
                else
                {
                    AbstractEntity entity = Game.instance.entities.get(entityIndex);
                    if (entity != null)
                    {
                        entity.deserialize((byte[]) ((Object[]) data)[1]);
                    }
                }
            }
            return Server.OKAY;
        }
        return Server.CLOSE;
    }

    public void reserialize()
    {
        try
        {
            serialData = serialize();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
