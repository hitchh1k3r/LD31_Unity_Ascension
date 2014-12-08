package com.hitchh1k3rsguide.utils.graphics;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import com.hitchh1k3rsguide.utils.GenericUtils;

public class Sprite
{

    public static final float Z_INDEX_UI = 100.0f;
    public static final float Z_INDEX_PROJECTILES = 50.0f;
    public static final float Z_INDEX_PLAYERS = 40.0f;
    public static final float Z_INDEX_MONSTERS = 30.0f;
    public static final float Z_INDEX_DROPS = 20.0f;
    public static final float Z_INDEX_BACKGROUND = 10.0f;

    private SpriteSheet sheet;
    private float[] uvs = new float[4];
    private float width, height;
    private static int overrideColor = -1;
    private static boolean useBlending = false;

    private static int boxVBO;
    private static int spriteProgram;
    private static int attribVertIndex;
    private static int uniformTexture0, uniformSpritePos, uniformSpriteSize, uniformSpriteUVs,
            uniformZIndex, uniformColor, uniformAlpha, uniformAngle, uniformRotationOffset;

    public Sprite(SpriteSheet sheet, int x, int y, int width, int height)
    {
        this.sheet = sheet;
        uvs[0] = (float) x / sheet.getWidth();
        uvs[1] = (float) y / sheet.getHeight();
        uvs[2] = (float) (x + width) / sheet.getWidth();
        uvs[3] = (float) (y + height) / sheet.getHeight();
        this.width = width;
        this.height = height;
    }

    public void drawAt(int x, int y, float zIndex)
    {
        drawAt(x, y, zIndex, false);
    }

    public static void drawBox()
    {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, boxVBO);
        GL20.glEnableVertexAttribArray(attribVertIndex);
        GL20.glVertexAttribPointer(attribVertIndex, 1, GL11.GL_FLOAT, false, 0, 0);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3 * 2);
    }

    public static void initialize()
    {
        ////////////////////////////////////////////////////////////////////////////////////
        // Generate VBO:

        FloatBuffer data = BufferUtils.createFloatBuffer(3 * 2).put(
                new float[] { 0, 1, 2, 2, 3, 4 });
        data.flip();

        boxVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, boxVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);

        ////////////////////////////////////////////////////////////////////////////////////
        // Generate sprite shader:

        boolean okay = true;

        String vertexShader = GenericUtils.loadTextFile("/shaders/sprite.vs");
        String fragmentShader = GenericUtils.loadTextFile("/shaders/sprite.fs");

        int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertShader, vertexShader);
        GL20.glCompileShader(vertShader);
        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            System.err.println("THERE WAS AN ERROR COMPILING THE SPRITE VERTEX SHADER!");
            System.err.println(GL20.glGetShaderInfoLog(vertShader));
            okay = false;
        }

        int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragShader, fragmentShader);
        GL20.glCompileShader(fragShader);
        if (GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            System.err.println("THERE WAS AN ERROR COMPILING THE SPRITE FRAGMENT SHADER!");
            System.err.println(GL20.glGetShaderInfoLog(fragShader));
            okay = false;
        }

        spriteProgram = GL20.glCreateProgram();
        GL20.glAttachShader(spriteProgram, vertShader);
        GL20.glAttachShader(spriteProgram, fragShader);
        GL20.glLinkProgram(spriteProgram);
        if (GL20.glGetProgrami(spriteProgram, GL20.GL_LINK_STATUS) != GL11.GL_TRUE)
        {
            System.err.println("THERE WAS AN ERROR LINKING THE SPRITE SHADER PROGRAM!");
            System.err.println(GL20.glGetProgramInfoLog(spriteProgram));
            okay = false;
        }

        if (!okay)
        {
            System.exit(-1);
        }

        attribVertIndex = GL20.glGetAttribLocation(spriteProgram, "vertIndex");

        uniformTexture0 = GL20.glGetUniformLocation(spriteProgram, "texture0");
        uniformSpritePos = GL20.glGetUniformLocation(spriteProgram, "spritePos");
        uniformSpriteSize = GL20.glGetUniformLocation(spriteProgram, "spriteSize");
        uniformSpriteUVs = GL20.glGetUniformLocation(spriteProgram, "spriteUVs");
        uniformZIndex = GL20.glGetUniformLocation(spriteProgram, "zIndex");
        uniformColor = GL20.glGetUniformLocation(spriteProgram, "color");
        uniformAlpha = GL20.glGetUniformLocation(spriteProgram, "alpha");
        uniformAngle = GL20.glGetUniformLocation(spriteProgram, "angle");
        uniformRotationOffset = GL20.glGetUniformLocation(spriteProgram, "rotationOffset");
    }

    public void drawAt(int x, int y, float zIndex, boolean flip)
    {
        GL20.glUseProgram(spriteProgram);

        GL20.glUniform1i(uniformTexture0, 0);
        sheet.bindTo(0);

        GL20.glUniform1i(uniformAlpha, useBlending ? 1 : 0);
        GL20.glUniform1i(uniformColor, overrideColor);
        GL20.glUniform1f(uniformZIndex, zIndex);
        GL20.glUniform2f(uniformSpritePos, x + (flip ? width : 0), y);
        GL20.glUniform2f(uniformSpriteSize, (flip ? -1 : 1) * width, height);
        GL20.glUniform4f(uniformSpriteUVs, uvs[0], uvs[1], uvs[2], uvs[3]);

        drawBox();
    }

    public static void setColor(int color)
    {
        overrideColor = color;
    }

    public static void setBlend(boolean useBlending)
    {
        Sprite.useBlending = useBlending;
        if (useBlending)
        {
            GL11.glEnable(GL11.GL_BLEND);
        }
        else
        {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    public void drawAt(int x, int y, float zIndex, boolean flip, float angle)
    {
        GL20.glUseProgram(spriteProgram);
        GL20.glUniform1f(uniformAngle, angle);
        drawAt(x, y, zIndex, flip);
        GL20.glUniform1f(uniformAngle, 0);
    }

    public static void setRotationOffset(float x, float y)
    {
        GL20.glUseProgram(spriteProgram);
        GL20.glUniform2f(uniformRotationOffset, x, y);
    }

}