package com.hitchh1k3rsguide.ld31.entities;

import com.hitchh1k3rsguide.ld31.Game;
import com.hitchh1k3rsguide.ld31.Main;
import com.hitchh1k3rsguide.utils.graphics.Sprite;

public class Platform
{

    public static Platform[] platforms = new Platform[10];
    int x, y, width;

    static
    {
        platforms[0] = new Platform(0, 30, 30);
        platforms[1] = new Platform((int) Game.GAME_WIDTH - 30, 30, 30);
        platforms[2] = new Platform(0, 90, 75);
        platforms[3] = new Platform((int) Game.GAME_WIDTH - 75, 90, 75);
        platforms[4] = new Platform(25, 130, 75);
        platforms[5] = new Platform((int) Game.GAME_WIDTH - 100, 130, 75);
        platforms[6] = new Platform(0, 190, 50);
        platforms[7] = new Platform((int) Game.GAME_WIDTH - 50, 190, 50);
        platforms[8] = new Platform((int) Game.GAME_WIDTH / 2 - 50, 60, 100);
        platforms[9] = new Platform((int) Game.GAME_WIDTH / 2 - 75, 160, 150);
    };

    public Platform(int x, int y, int width)
    {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void draw()
    {
        Main.plainShader.setUniform("spritePos", x, y);
        Main.plainShader.setUniform("spriteSize", width, 5);
        Sprite.drawBox();
    }

}
