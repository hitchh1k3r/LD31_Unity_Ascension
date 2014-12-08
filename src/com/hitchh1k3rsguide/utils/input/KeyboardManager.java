package com.hitchh1k3rsguide.utils.input;

import java.util.HashMap;

import org.lwjgl.system.glfw.GLFW;

public class KeyboardManager
{

    private static final int KEY_PRESSED = 1;
    private static final int KEY_DOWN = 2;
    private static final int KEY_RELEASE = 4;

    private HashMap<Integer, String> keyMap = new HashMap<Integer, String>();
    private HashMap<String, Integer> keyStates = new HashMap<String, Integer>();
    private HashMap<String, Boolean> keyDown = new HashMap<String, Boolean>();

    public void setKeyLabel(String label, int[] keyCodes)
    {
        for (int keyCode : keyCodes)
        {
            keyMap.put(keyCode, label);
        }
        keyStates.put(label, 0);
        keyDown.put(label, false);
    }

    public void processEvent(int key, int action)
    {
        if (keyMap.containsKey(key))
        {
            String keyLabel = keyMap.get(key);
            if (action == GLFW.GLFW_PRESS)
            {
                keyStates.put(keyLabel, (keyStates.get(keyLabel) | KEY_PRESSED | KEY_DOWN));
                keyDown.put(keyLabel, true);
            }
            if (action == GLFW.GLFW_RELEASE)
            {
                keyStates.put(keyLabel, (keyStates.get(keyLabel) | KEY_RELEASE));
                keyDown.put(keyLabel, false);
            }
        }
    }

    public void update()
    {
        for (String keyLabel : keyMap.values())
        {
            if (keyDown.get(keyLabel))
            {
                if (keyPress(keyLabel))
                {
                    keyStates.put(keyLabel, keyStates.get(keyLabel) & ~KEY_PRESSED);
                }
            }
            else
            {
                if (keyDown(keyLabel))
                {
                    keyStates.put(keyLabel, 0);
                }
            }
        }
    }

    public boolean keyPress(String keyLabel)
    {
        return (keyStates.get(keyLabel) & KEY_PRESSED) > 0;
    }

    public boolean keyDown(String keyLabel)
    {
        return (keyStates.get(keyLabel) & KEY_DOWN) > 0;
    }

    public boolean keyRelease(String keyLabel)
    {
        return (keyStates.get(keyLabel) & KEY_RELEASE) > 0;
    }

}
