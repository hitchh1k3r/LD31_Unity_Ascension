package com.hitchh1k3rsguide.ld31.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.hitchh1k3rsguide.ld31.Game;
import com.hitchh1k3rsguide.ld31.data.WorldEvents;
import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.graphics.SpriteSheet;
import com.hitchh1k3rsguide.utils.math.CollisionChecks;
import com.hitchh1k3rsguide.utils.math.Serialization;
import com.hitchh1k3rsguide.utils.network.Packet;
import com.hitchh1k3rsguide.utils.network.Server;

public class EntityMonster extends AbstractEntity
{

    private static Sprite[] frames = new Sprite[3];
    private static Sprite shadow;

    private int frame = 0;
    private float animationTime = 0;
    private float frameLength = 1.0f / 20.0f;
    private boolean facingRight = false;
    private int health = 3;
    public int hit = -1;
    private int shootCooldown = 50;

    public EntityMonster(int y)
    {
        facingRight = (Math.random() > 0.5);
        int x = (facingRight) ? -64 : (int) Game.GAME_WIDTH;
        lastPosition.x = x;
        lastPosition.y = y;
        position.x = x;
        position.y = y;
        velocity.x = (facingRight ? 1 : -1) * (1.0f + (float) Math.random());
    }

    public static void initSprites(SpriteSheet spriteSheet)
    {
        frames[0] = new Sprite(spriteSheet, 64, 0, 32, 32);
        frames[1] = new Sprite(spriteSheet, 96, 0, 32, 32);
        frames[2] = new Sprite(spriteSheet, 128, 0, 32, 32);
        shadow = new Sprite(spriteSheet, 96, 32, 32 + 5, 32);
    }

    @Override
    public void update(double time)
    {
        super.update(time);
        animationTime += time;
        if (animationTime > frameLength)
        {
            animationTime = 0;
            if (++frame >= 3)
            {
                frame = 0;
            }
        }
    }

    @Override
    public boolean fixedUpdate()
    {
        super.fixedUpdate();
        if (Packet.isServer)
        {
            if (hit != -1)
            {
                EntityPlayer attacker = null;
                if (Server.threads[hit] != null)
                {
                    attacker = Server.threads[hit].player;
                }
                --health;
                if (health <= 0)
                {
                    --Server.monsterCount;
                    Server.broadcast(WorldEvents.makePacket((int) position.x + 16,
                            (int) position.y + 16, WorldEvents.MONSTER_DEATH, 0));
                    int drops = (int) ((Math.random() * 2) + 3);
                    for (int i = 0; i < drops; ++i)
                    {
                        Server.addEntity(new EntityPickup((int) position.x + 16,
                                (int) position.y + 16, hit));
                    }
                    if (attacker != null)
                    {
                        attacker.scores.scores[3] += 10;
                        Server.gameThread.teamScores[attacker.team - 1].scores[3] += 10;
                    }
                }
                else
                {
                    Server.broadcast(WorldEvents.makePacket((int) position.x + 16,
                            (int) position.y + 16, WorldEvents.MONSTER_HIT, 0));
                    if (attacker != null)
                    {
                        attacker.scores.scores[3] += 1;
                        Server.gameThread.teamScores[attacker.team - 1].scores[3] += 1;
                    }
                }
                hit = -1;
            }
            for (int i = 0; i < Server.MAX_CLIENT_COUNT; ++i)
            {
                if (Server.threads[i] != null)
                {
                    EntityPlayer player = Server.threads[i].player;
                    if (player != null
                            && player.canBeHit()
                            && CollisionChecks.AABBvAABB(position.x, position.y, 32, 32,
                                    player.position.x, player.position.y,
                                    EntityPlayer.PLAYER_WIDTH, EntityPlayer.PLAYER_HEIGHT))
                    {
                        player.changeHealth(-4);
                    }
                }
            }
            --shootCooldown;
            if (shootCooldown < 0)
            {
                shootCooldown = (int) ((Math.random() * 100) + 100);
                EntityPlayer target = Server.getRandomPlayer();
                if (target != null)
                {
                    double aim = Math.atan2(position.x + 16 - (target.position.x), position.y + 16
                            - (target.position.y));
                    Server.addEntity(new EntityBullet((int) position.x + 16, (int) position.y + 16,
                            aim, 2));
                    Server.addEntity(new EntityBullet((int) position.x + 16, (int) position.y + 16,
                            aim - 0.1, 2));
                    Server.addEntity(new EntityBullet((int) position.x + 16, (int) position.y + 16,
                            aim + 0.1, 2));
                    Server.broadcast(WorldEvents.makePacket((int) position.x + 16,
                            (int) position.y + 16, WorldEvents.SHOOT_GUN, 0));
                }
            }
        }
        if (velocity.y > 1.0)
        {
            velocity.y -= 2;
        }
        else
        {
            velocity.y += 0.1;
        }
        if (position.x < 0 && !facingRight)
        {
            velocity.x *= -1;
            facingRight = !facingRight;
        }
        else if (position.x > Game.GAME_WIDTH - 32 && facingRight)
        {
            velocity.x *= -1;
            facingRight = !facingRight;
        }
        if (position.y < 0)
        {
            position.y = 0;
            velocity.y = 0;
        }
        else if (position.y > Game.GAME_HEIGHT - 32)
        {
            position.y = Game.GAME_HEIGHT - 32;
            velocity.y = 0;
        }
        if (!Packet.isServer && frame == 0)
        {
            Game.instance.particles.put(Game.instance.getParticleID(), new Particle(8,
                    (int) position.x + 16, (int) position.y + 20,
                    (float) ((Math.random() * 2) - 1), -0.75f, 1.0f, 20));
        }
        return health <= 0;
    }

    @Override
    public void draw(double interpolation)
    {
        int x = (int) GenericUtils.lerp(lastPosition.x, position.x, interpolation);
        int y = (int) GenericUtils.lerp(lastPosition.y, position.y, interpolation);
        Sprite.setBlend(true);
        shadow.drawAt(x - 3, y, Sprite.Z_INDEX_BACKGROUND, facingRight);
        Sprite.setBlend(false);
        frames[frame].drawAt(x, y, Sprite.Z_INDEX_MONSTERS, facingRight);
    }

    @Override
    public int getType()
    {
        return AbstractEntity.ENTITY_MONSTER;
    }

    @Override
    public synchronized byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(Serialization.serializeFloat(position.x));
        bytes.write(Serialization.serializeFloat(position.y));
        bytes.write(Serialization.serializeFloat(velocity.x));
        bytes.write(Serialization.serializeFloat(velocity.y));
        bytes.write(Serialization.serializeInt(facingRight ? 1 : 0));
        bytes.write(Serialization.serializeInt(health));
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
        facingRight = (getInt == 1) ? true : false;

        getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset
                + 4));
        offset += 4;
        health = getInt;
    }

}
