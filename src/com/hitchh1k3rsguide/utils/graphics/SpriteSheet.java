package com.hitchh1k3rsguide.utils.graphics;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import de.matthiasmann.twl.utils.PNGDecoder;

public class SpriteSheet
{

    private static final int MAX_TEXTURES = GL11.glGetInteger(GL13.GL_MAX_TEXTURE_UNITS);

    int glTextureID;
    int width;
    int height;

    public SpriteSheet(String filename)
    {
        try
        {
            PNGDecoder dec = new PNGDecoder(getClass().getResourceAsStream("/textures/" + filename));
            width = dec.getWidth();
            height = dec.getHeight();
            final int bpp = 4;
            ByteBuffer buffer = BufferUtils.createByteBuffer(bpp * width * height);
            dec.decode(buffer, width * bpp, PNGDecoder.Format.RGBA);
            buffer.flip();

            glTextureID = GL11.glGenTextures();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureID);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE, buffer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void bindTo(int texture)
    {
        if (texture >= 0 && texture < MAX_TEXTURES)
        {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texture);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureID);
        }
        else
        {
            System.err.println("Tried to bind to more textures than your graphics card supports!");
            System.exit(-1);
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

}