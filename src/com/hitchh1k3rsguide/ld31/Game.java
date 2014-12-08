package com.hitchh1k3rsguide.ld31;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.glfw.GLFW;

import com.hitchh1k3rsguide.ld31.data.TeamScore;
import com.hitchh1k3rsguide.ld31.entities.AbstractEntity;
import com.hitchh1k3rsguide.ld31.entities.EntityMonster;
import com.hitchh1k3rsguide.ld31.entities.EntityPlayer;
import com.hitchh1k3rsguide.ld31.entities.Particle;
import com.hitchh1k3rsguide.ld31.entities.Platform;
import com.hitchh1k3rsguide.utils.graphics.FontRenderer;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.graphics.SpriteSheet;
import com.hitchh1k3rsguide.utils.input.KeyboardManager;
import com.hitchh1k3rsguide.utils.input.MouseManager;
import com.hitchh1k3rsguide.utils.network.GameClientThread;
import com.hitchh1k3rsguide.utils.network.Packet;
import com.hitchh1k3rsguide.utils.sound.SoundEngine;

public class Game
{

    public static final float GAME_WIDTH = 320;
    public static final float GAME_HEIGHT = 240;

    public HashMap<String, Sprite> sprites = new HashMap<String, Sprite>();
    public SoundEngine soundEngine;
    public KeyboardManager keyboardManager;
    public MouseManager mouseManager;
    public GameClientThread clientThread;
    public FontRenderer fontRenderer;
    public EntityPlayer mainPlayer = new EntityPlayer(true);
    public static Game instance;
    public TeamScore[] teamScores = new TeamScore[6];

    public ConcurrentHashMap<Integer, AbstractEntity> entities = new ConcurrentHashMap<Integer, AbstractEntity>();
    public ConcurrentHashMap<Integer, Particle> particles = new ConcurrentHashMap<Integer, Particle>();
    private boolean showScore = false;

    public Game(SoundEngine soundEngine, KeyboardManager keyboardManager,
            MouseManager mouseManager, GameClientThread clientThread)
    {
        instance = this;

        this.soundEngine = soundEngine;
        this.keyboardManager = keyboardManager;
        this.mouseManager = mouseManager;
        this.clientThread = clientThread;

        SpriteSheet spriteSheet = new SpriteSheet("sprites.png");
        fontRenderer = new FontRenderer(spriteSheet, 0, 0, 5, 7);
        sprites.put("test", new Sprite(spriteSheet, 0, 32, 32, 32));
        sprites.put("snowman", new Sprite(spriteSheet, 32, 32, 32, 32));
        sprites.put("bullet", new Sprite(spriteSheet, 50, 0, 7, 7));
        sprites.put("respawn", new Sprite(spriteSheet, 0, 15, 64, 7));
        sprites.put("ascension", new Sprite(spriteSheet, 0, 64, 234, 7));
        sprites.put("mouseLeft", new Sprite(spriteSheet, 0, 64 + 7, 95, 7));
        sprites.put("moughRight", new Sprite(spriteSheet, 0, 64 + 14, 118, 7));
        sprites.put("mouseAim", new Sprite(spriteSheet, 0, 64 + 21, 66, 7));
        sprites.put("wasdMove", new Sprite(spriteSheet, 0, 64 + 28, 102, 7));
        sprites.put("tabScore", new Sprite(spriteSheet, 0, 64 + 35, 129, 7));
        sprites.put("team", new Sprite(spriteSheet, 0, 64 + 42, 71, 7));

        EntityMonster.initSprites(spriteSheet);
        EntityPlayer.initSprites(spriteSheet);
        Particle.initSprites(spriteSheet);

        for (int i = 0; i < 6; ++i)
        {
            teamScores[i] = new TeamScore();
        }

        new Packet(Packet.JOIN, clientThread.uuid).send(clientThread);
    }

    double timeTracker;

    public void update(double time)
    {
        if (mainPlayer.needsTeam)
        {
            if (mouseManager.buttonPress(GLFW.GLFW_MOUSE_BUTTON_LEFT))
            {
                int team = testTeamSet();
                if (team > 0)
                {
                    mainPlayer.needsTeam = false;
                    new Packet(Packet.SET_TEAM, team).send(Game.instance.clientThread);
                }
            }
        }
        timeTracker += time;
        if (timeTracker > EntityPlayer.UPDATE_INTERVAL)
        {
            timeTracker = 0;
            try
            {
                new Packet(Packet.ENTITY_UPDATE, new Object[] { -1, mainPlayer.serialize() })
                        .send(clientThread);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        mainPlayer.update(time);
        for (AbstractEntity entity : entities.values())
        {
            entity.update(time);
        }
        if (keyboardManager.keyPress("score"))
        {
            showScore = !showScore;
        }
    }

    private int testTeamSet()
    {
        int x = (int) (mouseManager.getMouseX() / 2);
        int y = (int) (mouseManager.getMouseY() / 2);
        if (y > 85 && y < 85 + 50)
        {
            for (int i = 0; i < 3; ++i)
            {
                if (x > (i * 70) + 65 && x < (i * 70) + 65 + 50)
                {
                    return i + 1;
                }
            }
        }

        if (y > 145 && y < 145 + 50)
        {
            for (int i = 0; i < 3; ++i)
            {
                if (x > (i * 70) + 65 && x < (i * 70) + 65 + 50)
                {
                    return i + 4;
                }
            }
        }
        return 0;
    }

    public void firstFixed()
    {
        mainPlayer.firstFixed();
        for (AbstractEntity entity : entities.values())
        {
            entity.firstFixed();
        }
    }

    public void fixedStep()
    {
        mainPlayer.fixedUpdate();
        for (AbstractEntity entity : entities.values())
        {
            entity.fixedUpdate();
        }
        Iterator<Particle> particleIt = particles.values().iterator();
        while (particleIt.hasNext())
        {
            Particle particle = particleIt.next();
            if (!particle.fixedUpdate())
            {
                particleIt.remove();
            }
        }
    }

    public void render(double interpolation)
    {
        if (mainPlayer.ascension > 0)
        {
            GL11.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        }
        else
        {
            GL11.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
        }
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        // sprites.get("test").drawAt(160 + 16 - 64, 120 - 16 - 64, Sprite.Z_INDEX_MONSTERS);
        // sprites.get("snowman").drawAt(160 - 16 - 64, 120 - 16 - 64, Sprite.Z_INDEX_MONSTERS);
        // fontRenderer.drawString("0420420420", 50, 130);

        Main.plainShader.use();
        Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_PROJECTILES);
        Main.plainShader.setUniform("spriteSize", 2.0f, 2.0f);
        GL11.glEnable(GL11.GL_BLEND);
        for (Particle particle : particles.values())
        {
            particle.draw(interpolation);
        }
        GL11.glDisable(GL11.GL_BLEND);
        Main.plainShader.setUniform("alpha", 1.0f);
        Main.plainShader.setUniform("color", (mainPlayer.ascension > 0) ? 7 : 0);
        for (int i = 0; i < Platform.platforms.length; ++i)
        {
            Platform platform = Platform.platforms[i];
            platform.draw();
        }

        Main.plainShader.setUniform("color", 0);
        Main.plainShader.setUniform("spritePos", 0.0f, GAME_HEIGHT - 12);
        Main.plainShader.setUniform("spriteSize", GAME_WIDTH, 12);
        Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_UI);
        Sprite.drawBox();

        if (mainPlayer.ascension > 0)
        {
            sprites.get("ascension").drawAt((int) ((GAME_WIDTH - 234) / 2),
                    (int) (Game.GAME_HEIGHT - 10), Sprite.Z_INDEX_UI);
        }
        else
        {
            mainPlayer.drawScore(fontRenderer);
        }
        mainPlayer.draw(interpolation);
        for (AbstractEntity entity : entities.values())
        {
            entity.draw(interpolation);
        }

        if (mainPlayer.needsTeam)
        {
            GL11.glEnable(GL11.GL_BLEND);
            Main.plainShader.use();
            Main.plainShader.setUniform("color", 0);
            Main.plainShader.setUniform("spritePos", 20.0f, 20.0f);
            Main.plainShader.setUniform("spriteSize", GAME_WIDTH - 40.0f, GAME_HEIGHT - 52.0f);
            Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_UI);
            Main.plainShader.setUniform("alpha", 0.5f);
            Sprite.drawBox();
            Main.plainShader.setUniform("alpha", 1.0f);
            // draw boxes here
            Main.plainShader.setUniform("spriteSize", 50.0f, 50.0f);
            for (int i = 0; i < 3; ++i)
            {
                Main.plainShader.setUniform("color", i + 1);
                Main.plainShader.setUniform("spritePos", i * 70.0f + 65.0f, 85.0f);
                Sprite.drawBox();
            }

            for (int i = 0; i < 3; ++i)
            {
                Main.plainShader.setUniform("color", i + 4);
                Main.plainShader.setUniform("spritePos", i * 70.0f + 65.0f, 145.0f);
                Sprite.drawBox();
            }

            sprites.get("wasdMove").drawAt(25, 25, Sprite.Z_INDEX_UI);
            sprites.get("mouseAim").drawAt(25, 35, Sprite.Z_INDEX_UI);
            sprites.get("mouseLeft").drawAt(25, 45, Sprite.Z_INDEX_UI);
            sprites.get("moughRight").drawAt(25, 55, Sprite.Z_INDEX_UI);
            sprites.get("team").drawAt(25, 65, Sprite.Z_INDEX_UI);

        }
        else if ((showScore || mainPlayer.deathCooldown > 0) && mainPlayer.ascension == 0)
        {
            GL11.glEnable(GL11.GL_BLEND);
            Main.plainShader.use();
            Main.plainShader.setUniform("color", 0);
            Main.plainShader.setUniform("spritePos", 20.0f, 20.0f);
            Main.plainShader.setUniform("spriteSize", GAME_WIDTH - 40.0f, GAME_HEIGHT - 52.0f);
            Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_UI);
            Main.plainShader.setUniform("alpha", 0.5f);
            Sprite.drawBox();
            Main.plainShader.setUniform("alpha", 1.0f);
            GL11.glDisable(GL11.GL_BLEND);

            if (mainPlayer.deathCooldown > 0)
            {
                String num = "" + mainPlayer.deathCooldown;
                fontRenderer.drawString(num,
                        (int) ((GAME_WIDTH - 64 - 5 - (num.length() * 7)) / 2), 25);
                sprites.get("respawn").drawAt(
                        (int) ((GAME_WIDTH - 64 + 5 + (num.length() * 7)) / 2), 25,
                        Sprite.Z_INDEX_UI);
            }

            sprites.get("tabScore").drawAt((int) ((GAME_WIDTH - 129) / 2),
                    (int) (GAME_HEIGHT - 50), Sprite.Z_INDEX_UI);

            for (int team = 0; team < 6; ++team)
            {
                Sprite.setColor(team + 1);
                for (int i = 0; i < 7; ++i)
                {
                    if (i != 1 && i != 5)
                    {
                        int yOffset = 0;
                        if (i == 0)
                        {
                            yOffset = 20;
                        }
                        if (i == 6)
                        {
                            yOffset = -20;
                        }
                        fontRenderer.drawString(String.format("%06d", teamScores[team].scores[i]),
                                (team * 46) + 27, (i * 20) + 50 + yOffset);
                    }
                }
            }
            Sprite.setColor(-1);
        }
    }

    private int particleIndex = 0;

    public int getParticleID()
    {
        return ++particleIndex;
    }
}
