package com.hitchh1k3rsguide.ld31.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.hitchh1k3rsguide.ld31.Game;
import com.hitchh1k3rsguide.ld31.Main;
import com.hitchh1k3rsguide.ld31.data.WorldEvents;
import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.math.CollisionChecks;
import com.hitchh1k3rsguide.utils.math.Serialization;
import com.hitchh1k3rsguide.utils.network.Packet;
import com.hitchh1k3rsguide.utils.network.Server;

public class EntityPickup extends AbstractEntity
{

    public int owner = -1;
    private boolean atRest = false;
    public boolean remove = false;
    private int TTL = 0;

    public EntityPickup()
    {
    }

    public EntityPickup(int x, int y, int owner)
    {
        lastPosition.x = x;
        lastPosition.y = y;
        position.x = x;
        position.y = y;
        velocity.x = (float) ((Math.random() * 5) - 2.5);
        velocity.y = (float) ((Math.random() * 5) - 2.5);
        TTL = (int) (Math.random() * 200 + 550);
        this.owner = owner; // this will cause a small problem where the owner changes if they log out and someone else logs in
    }

    @Override
    public void draw(double interpolation)
    {
        float x = GenericUtils.lerp(lastPosition.x, position.x, interpolation);
        float y = GenericUtils.lerp(lastPosition.y, position.y, interpolation);
        Main.plainShader.use();
        Main.plainShader.setUniform("spriteSize", 4.0f, 4.0f);
        Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_DROPS);
        Main.plainShader.setUniform("color", (int) (Math.random() * 7));
        Main.plainShader.setUniform("spritePos", x, y);
        Sprite.drawBox();
    }

    @Override
    public boolean fixedUpdate()
    {
        if (Packet.isServer)
        {
            --TTL;
            if (TTL <= 0)
            {
                remove = true;
                return true;
            }
            for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
            {
                if (Server.threads[i] != null)
                {
                    EntityPlayer player = Server.threads[i].player;
                    if (player != null
                            && player.deathCooldown <= 0
                            && CollisionChecks.AABBvAABB(position.x - 8, position.y - 8, 4 + 16,
                                    4 + 16, player.position.x, player.position.y,
                                    EntityPlayer.PLAYER_WIDTH, EntityPlayer.PLAYER_HEIGHT))
                    {
                        if (owner >= 0 && Server.threads[owner] != null
                                && Server.threads[owner].player != null)
                        {
                            int score = 0;
                            EntityPlayer orbOwner = Server.threads[owner].player;
                            if (i != owner)
                            {
                                score = 1;
                            }
                            if (player.team != orbOwner.team)
                            {
                                score = 5;
                            }
                            score = score; // this might help prevent a compiler over-optimization... I don't have time to check...
                            orbOwner.scores.scores[6] += score;
                            Server.gameThread.teamScores[orbOwner.team - 1].scores[6] += score;
                        }
                        player.changeHealth(1);
                        Server.broadcast(WorldEvents.makePacket((int) position.x, (int) position.y,
                                WorldEvents.SHOOT_HEAL, 0));
                        remove = true;
                    }
                }
            }
        }
        if (atRest == true)
        {
            super.fixedUpdate();
            return remove;
        }
        if (velocity.y < 10.0f)
        {
            velocity.y += 0.25f;
        }
        super.fixedUpdate();
        if (position.x < 0)
        {
            position.x = 0;
        }
        else if (position.x > Game.GAME_WIDTH - 4)
        {
            position.x = Game.GAME_WIDTH - 4;
        }
        if (position.y < 0)
        {
            position.y = 0;
            velocity.y = 0;
        }
        else if (position.y >= Game.GAME_HEIGHT - 4 - 12)
        {
            position.y = Game.GAME_HEIGHT - 4 - 12;
            velocity.y = 0;
        }
        if (velocity.y > 0)
        {
            for (int i = 0; i < Platform.platforms.length; ++i)
            {
                Platform platform = Platform.platforms[i];
                if (platform.x < position.x + 4 && platform.x > position.x - platform.width)
                {
                    if (platform.y <= position.y + 4 + velocity.y + 0.1
                            && platform.y > position.y + 4 - velocity.y - 0.1)
                    {
                        position.y = platform.y - 4;
                        velocity.y = 0;
                    }
                }
            }
        }
        velocity.x *= 0.9f;
        if (velocity.x > -0.01f && velocity.x < 0.01f && velocity.y > -0.01f && velocity.y < 0.01f)
        {
            atRest = true;
            velocity.x = 0;
            velocity.y = 0;
        }
        return remove;
    }

    @Override
    public int getType()
    {
        return AbstractEntity.ENTITY_PICKUP;
    }

    @Override
    public synchronized byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(Serialization.serializeFloat(position.x));
        bytes.write(Serialization.serializeFloat(position.y));
        bytes.write(Serialization.serializeFloat(velocity.x));
        bytes.write(Serialization.serializeFloat(velocity.y));
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
    }

}
