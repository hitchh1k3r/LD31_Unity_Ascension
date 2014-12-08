package com.hitchh1k3rsguide.ld31.entities;

import java.io.IOException;

import com.hitchh1k3rsguide.utils.math.VectorMath;

abstract public class AbstractEntity
{

    public static final int ENTITY_PLAYER = 0x01;
    public static final int ENTITY_MONSTER = 0x02;
    public static final int ENTITY_BULLET = 0x03;
    public static final int ENTITY_PICKUP = 0x04;

    public VectorMath.Vec2 lastPosition = new VectorMath.Vec2(0, 0);
    public VectorMath.Vec2 position = new VectorMath.Vec2(0, 0);
    public VectorMath.Vec2 velocity = new VectorMath.Vec2(0, 0);

    public abstract int getType();

    public abstract byte[] serialize() throws IOException;

    public abstract void deserialize(byte[] data);

    public abstract void draw(double interpolation);

    public void update(double time)
    {
    }

    public void firstFixed()
    {
        lastPosition.copy(position);
    }

    public boolean fixedUpdate()
    {
        position.add(velocity);
        return false;
    }

    public static AbstractEntity entityFromType(int entityType)
    {
        if (entityType == ENTITY_PLAYER)
        {
            return new EntityPlayer();
        }
        else if (entityType == ENTITY_MONSTER)
        {
            return new EntityMonster(0);
        }
        else if (entityType == ENTITY_BULLET)
        {
            return new EntityBullet();
        }
        else if (entityType == ENTITY_PICKUP)
        {
            return new EntityPickup();
        }
        return null;
    }

}
