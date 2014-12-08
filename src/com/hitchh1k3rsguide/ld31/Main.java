package com.hitchh1k3rsguide.ld31;

import java.io.File;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.glfw.GLFW;
import org.lwjgl.system.glfw.GLFWvidmode;
import org.lwjgl.system.glfw.WindowCallback;
import org.lwjgl.system.glfw.WindowCallbackAdapter;

import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.graphics.Shader;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.input.KeyboardManager;
import com.hitchh1k3rsguide.utils.input.MouseManager;
import com.hitchh1k3rsguide.utils.network.GameClientThread;
import com.hitchh1k3rsguide.utils.sound.SoundEngine;

public class Main
{

    private static final String GAME_TITLE = "Unity Ascension";

    public long windowID;
    private SoundEngine soundEngine;
    private KeyboardManager keyboardManager;
    private MouseManager mouseManager;
    private GameClientThread clientThread;
    public static Shader plainShader;
    private Game game;

    public static Main instance;

    public Main()
    {
        instance = this;
    }

    private int run()
    {
        initGL();
        initShaders();
        initSound();
        initInput();

        game = new Game(soundEngine, keyboardManager, mouseManager, clientThread);

        gameLoop();

        cleanup();

        return 0;
    }

    private void initShaders()
    {
        Sprite.initialize();

        // uniform int color;
        // uniform vec2 spritePos;
        // uniform vec2 spriteSize;
        // uniform float zIndex;
        // attribute float vertIndex;
        plainShader = new Shader("plain", "plain", new String[] { "vertIndex" }, new String[] {
                "color", "spritePos", "spriteSize", "zIndex", "alpha" });
    }

    private void initGL()
    {
        GLFW.glfwInit();

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);

        int WIDTH = 640;
        int HEIGHT = 480;

        windowID = GLFW.glfwCreateWindow(WIDTH, HEIGHT, GAME_TITLE, 0, 0);

        ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(windowID, (GLFWvidmode.width(vidmode) - WIDTH) / 2,
                (GLFWvidmode.height(vidmode) - HEIGHT) / 2);

        GLFW.glfwMakeContextCurrent(windowID);
        GLFW.glfwSwapInterval(1);

        GLContext.createFromCurrent();

        GLFW.glfwShowWindow(windowID);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initSound()
    {
        soundEngine = new SoundEngine();
        soundEngine.initialize();
        soundEngine.preloadSounds(new String[] { "monster_die.wav", "heal.wav", "monster_hurt.wav",
                "player_hurt.wav", "join.wav", "leave.wav", "pickup.wav", "player_die.wav",
                "shoot.wav" });
    }

    private void initInput()
    {
        clientThread = new GameClientThread();

        mouseManager = new MouseManager();

        keyboardManager = new KeyboardManager();
        keyboardManager.setKeyLabel("up", new int[] { GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP });
        keyboardManager.setKeyLabel("left", new int[] { GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT });
        keyboardManager.setKeyLabel("down", new int[] { GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN });
        keyboardManager.setKeyLabel("right", new int[] { GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT });
        keyboardManager.setKeyLabel("jump", new int[] { GLFW.GLFW_KEY_SPACE });
        keyboardManager.setKeyLabel("score", new int[] { GLFW.GLFW_KEY_TAB });
        keyboardManager.setKeyLabel("leave", new int[] { GLFW.GLFW_KEY_ESCAPE });

        WindowCallback.set(windowID, new WindowCallbackAdapter()
        {

            @Override
            public void windowSize(long window, int width, int height)
            {
                super.windowSize(window, width, height);
            }

            @Override
            public void windowFocus(long window, int focused)
            {
                super.windowFocus(window, focused);
            }

            @Override
            public void mouseButton(long window, int button, int action, int mods)
            {
                super.mouseButton(window, button, action, mods);
                mouseManager.processClickEvent(button, action);
            }

            @Override
            public void key(long window, int key, int scancode, int action, int mods)
            {
                super.key(window, key, scancode, action, mods);
                keyboardManager.processEvent(key, action);
            }

            @Override
            public void cursorPos(long window, double xpos, double ypos)
            {
                super.cursorPos(window, xpos, ypos);
                mouseManager.processMoveEvent(xpos, ypos);
            }

        });
    }

    private final double TIME_STEP = 1.0 / 20.0;
    private double timeTracker = 0;

    private void gameLoop()
    {
        double lastTime = GLFW.glfwGetTime();

        GL11.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        while (GLFW.glfwWindowShouldClose(windowID) == GL11.GL_FALSE)
        {
            update(GLFW.glfwGetTime() - lastTime);
            lastTime = GLFW.glfwGetTime();
            keyboardManager.update();
            mouseManager.update();
            GLFW.glfwSwapBuffers(windowID);
            GLFW.glfwPollEvents();
            game.render(timeTracker / TIME_STEP);
        }
    }

    private void update(double time)
    {
        game.update(time);
        timeTracker += time;
        if (timeTracker > TIME_STEP)
        {
            game.firstFixed();
            while (timeTracker > TIME_STEP)
            {
                timeTracker -= TIME_STEP;
                game.fixedStep();
            }
        }
    }

    private void cleanup()
    {
        soundEngine.stopMusic();
        soundEngine.close();
        GLFW.glfwDestroyWindow(windowID);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args)
    {
        System.setProperty("org.lwjgl.librarypath", GenericUtils.getJarDir() + File.separator
                + "natives");

        System.exit(new Main().run());
    }

}
