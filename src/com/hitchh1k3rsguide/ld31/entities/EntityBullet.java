package com.hitchh1k3rsguide.ld31.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.hitchh1k3rsguide.ld31.Main;
import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.math.CollisionChecks;
import com.hitchh1k3rsguide.utils.math.Serialization;
import com.hitchh1k3rsguide.utils.math.VectorMath;
import com.hitchh1k3rsguide.utils.network.GameServerThread;
import com.hitchh1k3rsguide.utils.network.Packet;
import com.hitchh1k3rsguide.utils.network.Server;

public class EntityBullet extends AbstractEntity
{

    public int owner = -1;
    public int type = 0;
    private int TTL = 30;

    public EntityBullet()
    {
    }

    public EntityBullet(int x, int y, double angle, int type)
    {
        position.x = x;
        position.y = y;
        float speed = (type == 0) ? 10.0f : 2.5f;
        if (type == 2)
        { // if a monster bullet
            type = 0;
            speed = 5.0f;
            TTL = 60;
        }
        float velX = (float) (-1 * Math.sin(angle));
        float velY = (float) (-1 * Math.cos(angle));
        velocity.x = velX * speed;
        velocity.y = velY * speed;
        position.add(velocity);
        lastPosition.x = position.x;
        lastPosition.y = position.y;
        this.type = type;
    }

    @Override
    public boolean fixedUpdate()
    {
        if (Packet.isServer)
        {
            VectorMath.Vec2 start = new VectorMath.Vec2(position.x, position.y);
            super.fixedUpdate();

            if (owner != -1)
            {
                for (Integer key : GameServerThread.entityKeys)
                {
                    AbstractEntity entity = GameServerThread.entities.get(key);
                    if (type == 0 && entity instanceof EntityMonster)
                    {
                        if (CollisionChecks.SegvAABB(start, position, entity.position.x,
                                entity.position.y, 32, 32))
                        {
                            ((EntityMonster) entity).hit = owner;
                            TTL = 0;
                        }
                    }
                }
            }
            for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
            {
                if (Server.threads[i] != null)
                {
                    if (owner != Server.threads[i].index)
                    {
                        EntityPlayer shooter = null;
                        if (owner >= 0 && Server.threads[owner] != null)
                        {
                            shooter = Server.threads[owner].player;
                        }
                        EntityPlayer player = Server.threads[i].player;
                        if (player != null
                                && player.canBeHit()
                                && (shooter == null || type == 1 || shooter.team != player.team)
                                && CollisionChecks.SegvAABB(start, position, player.position.x,
                                        player.position.y, EntityPlayer.PLAYER_WIDTH,
                                        EntityPlayer.PLAYER_HEIGHT))
                        {
                            player.hit = type;
                            player.attacker = owner;
                            TTL = 0;
                        }
                    }
                }
            }

        }
        else
        {
            super.fixedUpdate();
        }

        return --TTL < 0;
    }

    @Override
    public void draw(double interpolation)
    {
        float x = GenericUtils.lerp(lastPosition.x, position.x, interpolation);
        float y = GenericUtils.lerp(lastPosition.y, position.y, interpolation);
        Main.plainShader.use();
        Main.plainShader.setUniform("spriteSize", 3, 3);
        Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_PROJECTILES);
        Main.plainShader.setUniform("color", (type == 0) ? 0 : 9);
        Main.plainShader.setUniform("spritePos", x, y);
        Sprite.drawBox();
    }

    @Override
    public int getType()
    {
        return AbstractEntity.ENTITY_BULLET;
    }

    @Override
    public synchronized byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(Serialization.serializeFloat(position.x));
        bytes.write(Serialization.serializeFloat(position.y));
        bytes.write(Serialization.serializeFloat(velocity.x));
        bytes.write(Serialization.serializeFloat(velocity.y));
        bytes.write(Serialization.serializeInt(type));
        return bytes.toByteArray();
    }

    @Override
    public synchronized void deserialize(byte[] data)
    {
        int offset = 0;
        float x = Serialization.deserializeFloat(Arrays.copyOfRange(data, offset, offset
                + 4));
        offset += 4;
        float y = Serialization.deserializeFloat(Arrays.copyOfRange(data, offset, offset
                + 4));
        offset += 4;
        position.x = x;
        position.y = y;

        x = Serialization.deserializeFloat(Arrays
                .copyOfRange(data, offset, offset + 4));
        offset += 4;
        y = Serialization.deserializeFloat(Arrays
                .copyOfRange(data, offset, offset + 4));
        offset += 4;
        velocity.x = x;
        velocity.y = y;

        int getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset
                + 4));
        offset += 4;
        type = getInt;

    }

}
