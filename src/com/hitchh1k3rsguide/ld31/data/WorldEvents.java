package com.hitchh1k3rsguide.ld31.data;

import com.hitchh1k3rsguide.ld31.Game;
import com.hitchh1k3rsguide.ld31.entities.EntityPlayer;
import com.hitchh1k3rsguide.ld31.entities.Particle;
import com.hitchh1k3rsguide.utils.network.Packet;

public class WorldEvents
{

    public static final int SHOOT_HEAL = 0x01;
    public static final int SHOOT_GUN = 0x02;
    public static final int MONSTER_HIT = 0x03;
    public static final int MONSTER_DEATH = 0x04;
    public static final int PLAYER_HIT = 0x05;
    public static final int PLAYER_DEATH = 0x06;
    public static final int PLAYER_SPAWN = 0x07;
    public static final int PLAYER_LEAVE = 0x08;

    public static Packet makePacket(int x, int y, int type, int variant)
    {
        return new Packet(Packet.WORLD_EVENT, new int[] { x, y, type, variant });
    }

    public static void doEvent(int x, int y, int type, int variant)
    {
        switch (type)
        {
            case SHOOT_HEAL:
            {
                for (int i = 0; i < 4; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(9, x,
                            y, randFloat(-1, 1), randFloat(-1, 1), -1.0f, 10));
                }
                Game.instance.soundEngine.playSound("heal.wav");
            }
                break;
            case SHOOT_GUN:
            {
                for (int i = 0; i < 3; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(8, x,
                            y, randFloat(-1, 1), randFloat(-1, 1), -1.0f, 10));
                }
                Game.instance.soundEngine.playSound("shoot.wav");
            }
                break;
            case MONSTER_HIT:
            {
                for (int i = 0; i < 5; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(
                            (int) randFloat(11, 17), x, y, randFloat(-2f, 2), randFloat(-2, 2),
                            0.25f, 20));
                }
                Game.instance.soundEngine.playSound("monster_hurt.wav");
            }
                break;
            case MONSTER_DEATH:
            {
                for (int i = 0; i < 5; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(
                            (int) randFloat(11, 17), x, y, randFloat(-2f, 2), randFloat(-2, 2),
                            0.25f, 20));
                }
                for (int i = 0; i < 10; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(8, x,
                            y, randFloat(-1.5f, 1.5f), randFloat(-1.5f, 1.5f), 0.25f, 20));
                }
                Game.instance.soundEngine.playSound("monster_die.wav");
            }
                break;
            case PLAYER_HIT:
            {
                for (int i = 0; i < 4; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(
                            variant + 10, x, y, randFloat(-1f, 1), randFloat(-2, 2), 2.0f, 15));
                }
                Game.instance.soundEngine.playSound("player_hurt.wav");
            }
                break;
            case PLAYER_DEATH:
            {
                for (int i = 0; i < 10; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(
                            variant + 10, x + EntityPlayer.ITEM_OFFSET_X, y
                                    + EntityPlayer.ITEM_OFFSET_Y, randFloat(-0.5f, 0.5f),
                            randFloat(-0.5f, 0.5f), 0.5f, 30));
                }
                Game.instance.soundEngine.playSound("player_die.wav");
            }
                break;
            case PLAYER_SPAWN:
            {
                for (int i = 0; i < 4; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(
                            variant + 10, x, y, randFloat(-1f, 1), randFloat(-2, 2), -1.0f, 15));
                }
                Game.instance.soundEngine.playSound("join.wav");
            }
                break;
            case PLAYER_LEAVE:
            {
                for (int i = 0; i < 5; ++i)
                {
                    Game.instance.particles.put(Game.instance.getParticleID(), new Particle(8, x,
                            y, randFloat(-0.5f, 0.5f), randFloat(-0.5f, 0.5f), -0.5f, 30));
                }
                Game.instance.soundEngine.playSound("leave.wav");
            }
                break;
        }
    }

    private static float randFloat(float low, float high)
    {
        return (float) (Math.random() * (high - low) + low);
    }
}
