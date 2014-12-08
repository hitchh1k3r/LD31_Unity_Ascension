package com.hitchh1k3rsguide.utils.graphics;

public class FontRenderer
{

    private Sprite[] sprites = new Sprite[11];
    private int width;

    public FontRenderer(SpriteSheet spriteSheet, int x, int y, int width, int height)
    {
        this.width = width + 1;
        for (int i = 0; i < 11; ++i)
        {
            sprites[i] = new Sprite(spriteSheet, x + (width * i), y, width, height);
        }
    }

    public void drawString(String string, int x, int y)
    {
        int offset = 0;
        for (int i = 0; i < string.length(); ++i)
        {
            char letter = string.charAt(i);
            if (letter == '-')
            {
                offset += width;
            }
            else if (letter >= 48)
            {
                sprites[letter - 48].drawAt(x + offset, y, Sprite.Z_INDEX_UI);
                offset += width;
            }
            else
            {
                offset += 5;
            }
        }
    }
}
