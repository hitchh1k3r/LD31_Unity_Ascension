package com.hitchh1k3rsguide.utils.graphics;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.hitchh1k3rsguide.utils.GenericUtils;

public class Shader
{

    private int programID;
    private HashMap<String, Integer> attribs = new HashMap<String, Integer>();
    private HashMap<String, Integer> uniforms = new HashMap<String, Integer>();

    public Shader(String vert, String frag, String[] attribs, String[] uniforms)
    {
        boolean okay = true;

        String vertexShader = GenericUtils.loadTextFile("/shaders/" + vert + ".vs");
        String fragmentShader = GenericUtils.loadTextFile("/shaders/" + frag + ".fs");

        int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertShader, vertexShader);
        GL20.glCompileShader(vertShader);
        if (GL20.glGetShaderi(vertShader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            System.err.println("THERE WAS AN ERROR COMPILING A VERTEX SHADER!");
            System.err.println(GL20.glGetShaderInfoLog(vertShader));
            okay = false;
        }

        int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragShader, fragmentShader);
        GL20.glCompileShader(fragShader);
        if (GL20.glGetShaderi(fragShader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            System.err.println("THERE WAS AN ERROR COMPILING A FRAGMENT SHADER!");
            System.err.println(GL20.glGetShaderInfoLog(fragShader));
            okay = false;
        }

        this.programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertShader);
        GL20.glAttachShader(programID, fragShader);
        GL20.glLinkProgram(programID);
        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL11.GL_TRUE)
        {
            System.err.println("THERE WAS AN ERROR LINKING A SHADER PROGRAM!");
            System.err.println(GL20.glGetProgramInfoLog(programID));
            okay = false;
        }

        if (!okay)
        {
            System.exit(-1);
        }

        for (String attrib : attribs)
        {
            this.attribs.put(attrib, GL20.glGetAttribLocation(programID, attrib));
        }

        for (String uniform : uniforms)
        {
            this.uniforms.put(uniform, GL20.glGetUniformLocation(programID, uniform));
        }
    }

    public void use()
    {
        GL20.glUseProgram(programID);
    }

    public int getUniformID(String uniform)
    {
        return uniforms.get(uniform);
    }

    public int getAttribID(String attrib)
    {
        return attribs.get(attrib);
    }

    public void setUniform(String uniform, int value)
    {
        GL20.glUniform1i(uniforms.get(uniform), value);
    }

    public void setUniform(String uniform, float value)
    {
        GL20.glUniform1f(uniforms.get(uniform), value);
    }

    public void setUniform(String uniform, float x, float y)
    {
        GL20.glUniform2f(uniforms.get(uniform), x, y);
    }

    public void setUniform(String uniform, float x, float y, float z)
    {
        GL20.glUniform3f(uniforms.get(uniform), x, y, z);
    }

}
