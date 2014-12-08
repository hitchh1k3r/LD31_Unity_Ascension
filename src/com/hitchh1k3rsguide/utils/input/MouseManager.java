package com.hitchh1k3rsguide.utils.input;

import java.util.HashMap;

import org.lwjgl.system.glfw.GLFW;

public class MouseManager
{

    private static final int BUTTON_PRESSED = 1;
    private static final int BUTTON_DOWN = 2;
    private static final int BUTTON_RELEASE = 4;

    private HashMap<Integer, Integer> buttonStates = new HashMap<Integer, Integer>();
    private HashMap<Integer, Boolean> buttonDown = new HashMap<Integer, Boolean>();
    private double xPos;
    private double yPos;

    public void processClickEvent(int button, int action)
    {
        Integer state = buttonStates.get(button);
        if (state == null)
        {
            state = 0;
        }
        if (action == GLFW.GLFW_PRESS)
        {
            buttonStates.put(button, (state | BUTTON_PRESSED | BUTTON_DOWN));
            buttonDown.put(button, true);
        }
        if (action == GLFW.GLFW_RELEASE)
        {
            buttonStates.put(button, (state | BUTTON_RELEASE));
            buttonDown.put(button, false);
        }
    }

    public void processMoveEvent(double x, double y)
    {
        this.xPos = x;
        this.yPos = y;
    }

    public void update()
    {
        for (int button : buttonStates.keySet())
        {
            if (buttonDown.get(button))
            {
                if (buttonPress(button))
                {
                    buttonStates.put(button, buttonStates.get(button) & ~BUTTON_PRESSED);
                }
            }
            else
            {
                if (buttonDown(button))
                {
                    buttonStates.put(button, 0);
                }
            }
        }
    }

    public boolean buttonPress(int button)
    {
        Integer state = buttonStates.get(button);
        if (state == null)
        {
            return false;
        }
        return (state & BUTTON_PRESSED) > 0;
    }

    public boolean buttonDown(int button)
    {
        Integer state = buttonStates.get(button);
        if (state == null)
        {
            return false;
        }
        return (state & BUTTON_DOWN) > 0;
    }

    public boolean buttonRelease(int button)
    {
        Integer state = buttonStates.get(button);
        if (state == null)
        {
            return false;
        }
        return (state & BUTTON_RELEASE) > 0;
    }

    public double getMouseX()
    {
        return xPos;
    }

    public double getMouseY()
    {
        return yPos;
    }

}
