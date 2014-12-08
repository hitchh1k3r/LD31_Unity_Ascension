package com.hitchh1k3rsguide.ld31.entities;

import com.hitchh1k3rsguide.ld31.Main;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.graphics.SpriteSheet;
import com.hitchh1k3rsguide.utils.math.VectorMath;

public class Particle
{

    // private static Sprite sprite;
    // private VectorMath.Vec2 lastPosition = new VectorMath.Vec2(0, 0);
    private VectorMath.Vec2 position = new VectorMath.Vec2(0, 0);
    private VectorMath.Vec2 velocity = new VectorMath.Vec2(0, 0);
    private float gravityScale;
    private int color;
    private int TTL;
    private float alpha = 1.0f;

    public Particle(int color, int x, int y, float velX, float velY, float gravityScale, int life)
    {
        position.x = x;
        position.y = y;
        velocity.x = velX;
        velocity.y = velY;
        this.gravityScale = gravityScale;
        this.color = color;
        this.TTL = life;
    }

    public static void initSprites(SpriteSheet spriteSheet)
    {
        // sprite = new Sprite(spriteSheet, 57, 0, 3, 3);
    }

    public boolean fixedUpdate()
    {
        position.add(velocity);
        velocity.y += 0.1 * gravityScale;
        if (TTL < 10)
        {
            alpha = TTL / 10.0f;
        }
        return (--TTL > 0);
    }

    public void draw(double interpolation)
    {
        Main.plainShader.setUniform("color", color);
        Main.plainShader.setUniform("spritePos", position.x, position.y);
        Main.plainShader.setUniform("alpha", alpha);
        Sprite.drawBox();
        // Sprite.setColor(color);
        // sprite.drawAt((int) position.x, (int) position.y, Sprite.Z_INDEX_DROPS, false);
        // Sprite.setColor(-1);
    }

}
