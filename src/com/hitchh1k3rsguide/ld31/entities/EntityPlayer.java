package com.hitchh1k3rsguide.ld31.entities;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lwjgl.system.glfw.GLFW;

import com.hitchh1k3rsguide.ld31.Game;
import com.hitchh1k3rsguide.ld31.Main;
import com.hitchh1k3rsguide.ld31.data.TeamScore;
import com.hitchh1k3rsguide.ld31.data.WorldEvents;
import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.graphics.FontRenderer;
import com.hitchh1k3rsguide.utils.graphics.Sprite;
import com.hitchh1k3rsguide.utils.graphics.SpriteSheet;
import com.hitchh1k3rsguide.utils.math.Serialization;
import com.hitchh1k3rsguide.utils.network.Packet;
import com.hitchh1k3rsguide.utils.network.Server;

public class EntityPlayer extends AbstractEntity
{

    public static final int PLAYER_WIDTH = 10;
    public static final int PLAYER_HEIGHT = 20;
    public static final int ITEM_OFFSET_X = 5;
    public static final int ITEM_OFFSET_Y = 10;
    public static final double HALF_PI = 1.57079632679;
    public static final double UPDATE_INTERVAL = 1.0f / 5.0f;

    private static Sprite gun;
    private static Sprite medipack;

    public int health = 24;

    public int team = 0;
    public TeamScore scores;
    private boolean onGround;
    private int equipment = 0;
    private double aim;
    private double coolDown = 0;
    private boolean localPlayer = false;
    private boolean initUpdate = false;
    public int hit = -1;
    public int hurtCooldown = 0;
    public int deathCooldown = 0;
    public int ascension = 0;
    public boolean needsTeam = false;
    private boolean serverLoadAll = false;
    public int attacker = -1;

    public EntityPlayer()
    {
        setToSpawnLocation();
        this.scores = new TeamScore();
    }

    public EntityPlayer(boolean localPlayer)
    {
        this();
        this.localPlayer = localPlayer;
    }

    public void setToSpawnLocation()
    {
        if (Math.random() > 0.5)
        {
            lastPosition.x = Game.GAME_WIDTH - PLAYER_WIDTH;
            position.x = Game.GAME_WIDTH - PLAYER_WIDTH;
        }
        else
        {
            lastPosition.x = 0;
            position.x = 0;
        }
        lastPosition.y = 0;
        position.y = 0;
    }

    public static void initSprites(SpriteSheet spriteSheet)
    {
        gun = new Sprite(spriteSheet, 0, 7, 7, 4);
        medipack = new Sprite(spriteSheet, 7, 7, 8, 8);
    }

    @Override
    public void draw(double interpolation)
    {
        if (deathCooldown > 0 || team == 0)
            return;
        float x = GenericUtils.lerp(lastPosition.x, position.x, interpolation);
        float y = GenericUtils.lerp(lastPosition.y, position.y, interpolation);
        Main.plainShader.use();
        Main.plainShader.setUniform("spriteSize", PLAYER_WIDTH, PLAYER_HEIGHT);
        Main.plainShader.setUniform("zIndex", Sprite.Z_INDEX_PLAYERS);
        Main.plainShader.setUniform("color", team);
        Main.plainShader.setUniform("spritePos", x, y);
        Sprite.drawBox();
        int antiHealth = (24 - health) / 3;
        if (antiHealth > 0)
        {
            Main.plainShader.setUniform("spriteSize", antiHealth, 2);
            Main.plainShader.setUniform("color", 0);
            Main.plainShader.setUniform("spritePos", x + 9 - antiHealth, y + 1);
            Sprite.drawBox();
        }

        Sprite.setBlend(true);
        // aim = 0 => up,   aim = pi => down,   aim < 0 => right
        boolean flip = false;
        double angle = aim;
        if (angle < 0)
        {
            angle *= -1;
        }
        else
        {
            angle = Math.PI - angle;
            flip = true;
        }
        angle -= HALF_PI;
        if (equipment == 0)
        {
            if (flip)
            {
                Sprite.setRotationOffset(-2, 2);
                gun.drawAt((int) (x + ITEM_OFFSET_X - 7), (int) (y + ITEM_OFFSET_Y),
                        Sprite.Z_INDEX_PLAYERS, true, (float) angle);
            }
            else
            {
                Sprite.setRotationOffset(2, 2);
                gun.drawAt((int) (x + ITEM_OFFSET_X), (int) (y + ITEM_OFFSET_Y),
                        Sprite.Z_INDEX_PLAYERS, false, (float) angle);
            }
        }
        else
        {
            if (flip)
            {
                Sprite.setRotationOffset(-4, 2);
                medipack.drawAt((int) (x + ITEM_OFFSET_X - 6), (int) (y + ITEM_OFFSET_Y),
                        Sprite.Z_INDEX_PLAYERS, flip, (float) angle);
            }
            else
            {
                Sprite.setRotationOffset(4, 2);
                medipack.drawAt((int) (x + ITEM_OFFSET_X - 1), (int) (y + ITEM_OFFSET_Y),
                        Sprite.Z_INDEX_PLAYERS, false, (float) angle);
            }
        }
        Sprite.setBlend(false);
    }

    public void serverUpdate()
    {
        if (hurtCooldown > 0)
        {
            --hurtCooldown;
        }
        if (canBeHit())
        {
            EntityPlayer attackingPlayer = null;
            if (attacker >= 0 && Server.threads[attacker] != null)
            {
                attackingPlayer = Server.threads[attacker].player;
            }
            if (hit == 0)
            {
                changeHealth(-4);
                if (attackingPlayer != null)
                {
                    if (health <= 0)
                    {
                        attackingPlayer.scores.scores[0] += 10;
                        Server.gameThread.teamScores[attackingPlayer.team - 1].scores[0] += 10;
                    }
                    else
                    {
                        attackingPlayer.scores.scores[0] += 1;
                        Server.gameThread.teamScores[attackingPlayer.team - 1].scores[0] += 1;
                    }
                }
                hit = -1;
            }
            else if (hit == 1)
            {
                if (attackingPlayer != null)
                {
                    int score = (24 - health / 2) + 1;
                    if (score > 5)
                    {
                        score = 5;
                    }
                    attackingPlayer.scores.scores[2] += score;
                    Server.gameThread.teamScores[attackingPlayer.team - 1].scores[2] += score;
                }
                changeHealth(4);
                hit = -1;
            }
        }
        else
        {
            hit = -1;
            attacker = -1;
        }
    }

    public void changeHealth(int deltaHealth)
    {
        if (deltaHealth < -1)
        {
            if (hurtCooldown > 0)
            {
                return;
            }
            hurtCooldown = 10;
        }
        health += deltaHealth;
        if (health > 24)
        {
            health = 24;
        }
        else if (health < 0)
        {
            Server.broadcast(WorldEvents.makePacket((int) position.x, (int) position.y,
                    WorldEvents.PLAYER_DEATH, team));
            health = 24;
            setToSpawnLocation();
            if (ascension > 0)
            {
                ascension = 0;
                deathCooldown = 1;
            }
            else
            {
                deathCooldown = 5;
            }
        }
        else if (deltaHealth < -1)
        {
            Server.broadcast(WorldEvents.makePacket((int) position.x + EntityPlayer.ITEM_OFFSET_X,
                    (int) position.y + EntityPlayer.ITEM_OFFSET_Y, WorldEvents.PLAYER_HIT, team));
        }
    }

    @Override
    public void firstFixed()
    {
        super.firstFixed();
        if (Game.instance.mainPlayer == this)
        {
            if (!canBeHit())
            {
                return;
            }
            aim = Math.atan2(position.x + ITEM_OFFSET_X
                    - (Game.instance.mouseManager.getMouseX() / 2.0), position.y + ITEM_OFFSET_Y
                    - (Game.instance.mouseManager.getMouseY() / 2.0));
        }
    }

    @Override
    public void update(double time)
    {
        if (Game.instance.mainPlayer == this)
        {
            if (!canBeHit())
            {
                return;
            }
            float x = 0;
            if (Game.instance.keyboardManager.keyDown("left"))
            {
                x += -5;
            }
            if (Game.instance.keyboardManager.keyDown("right"))
            {
                x += 5;
            }
            velocity.x = x;
            if (coolDown > 0)
            {
                coolDown -= time;
            }
            if (Game.instance.mouseManager.buttonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && coolDown <= 0)
            {
                coolDown = 0.5;
                changeHealth(-1);
                AbstractEntity entity = new EntityBullet((int) position.x + ITEM_OFFSET_X,
                        (int) position.y + ITEM_OFFSET_Y, aim, equipment);
                try
                {
                    new Packet(Packet.ENTITY_ADD, new Object[] { AbstractEntity.ENTITY_BULLET, -1,
                            entity.serialize() }).send(Game.instance.clientThread);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (Game.instance.mouseManager.buttonPress(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                equipment = (equipment == 0) ? 1 : 0;
            }
        }
    }

    @Override
    public boolean fixedUpdate()
    {
        if (!canBeHit())
        {
            return false;
        }
        if (velocity.y < 10.0)
        {
            velocity.y += 1.25;
        }
        if (Game.instance.mainPlayer == this)
        {
            if (onGround && Game.instance.keyboardManager.keyDown("jump"))
            {
                velocity.y = -10;
            }
            if (velocity.y < -1f && !Game.instance.keyboardManager.keyDown("jump"))
            {
                velocity.y *= 0.5f;
            }
        }
        onGround = false;
        super.fixedUpdate();
        if (position.x < 0)
        {
            position.x = 0;
        }
        else if (position.x > Game.GAME_WIDTH - PLAYER_WIDTH)
        {
            position.x = Game.GAME_WIDTH - PLAYER_WIDTH;
        }
        if (position.y < 0)
        {
            position.y = 0;
            velocity.y = 0;
        }
        else if (position.y >= Game.GAME_HEIGHT - PLAYER_HEIGHT - 12)
        {
            position.y = Game.GAME_HEIGHT - PLAYER_HEIGHT - 12;
            velocity.y = 0;
            onGround = true;
        }
        if (velocity.y > 0)
        {
            if (Game.instance.mainPlayer != this || !Game.instance.keyboardManager.keyDown("down"))
            {
                for (int i = 0; i < Platform.platforms.length; ++i)
                {
                    Platform platform = Platform.platforms[i];
                    if (platform.x < position.x + PLAYER_WIDTH
                            && platform.x > position.x - platform.width)
                    {
                        if (platform.y <= position.y + PLAYER_HEIGHT + velocity.y + 0.1
                                && platform.y > position.y + PLAYER_HEIGHT - velocity.y - 0.1)
                        {
                            position.y = platform.y - PLAYER_HEIGHT;
                            velocity.y = 0;
                            onGround = true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void getPlayer(String path)
    {
        try
        {
            byte[] bytes = new byte[4 * 8];

            FileInputStream fis = new FileInputStream(path);
            fis.read(bytes);

            ByteBuffer intBuffer = ByteBuffer.allocate(4 * 8);
            intBuffer.put(bytes, 0, 4 * 8);
            intBuffer.flip();
            for (int i = 0; i < 7; ++i)
            {
                scores.scores[i] = intBuffer.getInt();
            }
            int serialLength = intBuffer.getInt();
            byte[] serialData = new byte[serialLength];
            fis.read(serialData);
            fis.close();

            serverLoadAll = true;
            deserialize(serialData);
            serverLoadAll = false;
        }
        catch (Exception e)
        {
        }
    }

    public void stashPlayer(String path)
    {
        try
        {
            ByteBuffer intBuffer = ByteBuffer.allocate(4 * 8);
            for (int i = 0; i < 7; ++i)
            {
                intBuffer.putInt(scores.scores[i]);
            }
            byte[] serialData = serialize();
            intBuffer.putInt(serialData.length);
            intBuffer.flip();

            byte[] bytes = intBuffer.array();

            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytes);
            fos.write(serialData);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void drawScore(FontRenderer fontRenderer)
    {
        fontRenderer.drawString(String.format(scores.getSkullString(), scores.scores[0],
                scores.scores[2], scores.scores[3], scores.scores[4], scores.scores[6]), 50,
                (int) (Game.GAME_HEIGHT - 10));
    }

    @Override
    public int getType()
    {
        return AbstractEntity.ENTITY_PLAYER;
    }

    @Override
    public synchronized byte[] serialize() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(Serialization.serializeFloat(position.x));
        bytes.write(Serialization.serializeFloat(position.y));
        bytes.write(Serialization.serializeFloat(velocity.x));
        bytes.write(Serialization.serializeFloat(velocity.y));
        bytes.write(Serialization.serializeInt(team));
        bytes.write(Serialization.serializeInt(health));
        bytes.write(Serialization.serializeInt(deathCooldown));
        bytes.write(Serialization.serializeInt(ascension));
        bytes.write(Serialization.serializeInt(equipment));
        bytes.write(Serialization.serializeFloat((float) aim));
        return bytes.toByteArray();
    }

    @Override
    public synchronized void deserialize(byte[] data)
    {
        int offset = 0;
        float x = Serialization.deserializeFloat(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        float y = Serialization.deserializeFloat(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        // if (Packet.isServer || !initUpdate)
        if (((!initUpdate || (deathCooldown > 0 && !Packet.isServer)) && (!Packet.isServer || deathCooldown == 0))
                || serverLoadAll)
        {
            position.x = x;
            position.y = y;
        }

        x = Serialization.deserializeFloat(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        y = Serialization.deserializeFloat(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        //if (Packet.isServer || !initUpdate)
        if (((!initUpdate || (deathCooldown > 0 && !Packet.isServer)) && (!Packet.isServer || deathCooldown == 0))
                || serverLoadAll)
        {
            velocity.x = x;
            velocity.y = y;
        }

        int getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        if (!Packet.isServer || serverLoadAll)
        {
            team = getInt;
        }

        getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        if (!Packet.isServer || serverLoadAll)
        {
            health = getInt;
        }

        getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        if (!Packet.isServer || serverLoadAll)
        {
            deathCooldown = getInt;
        }

        getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        if (!Packet.isServer || serverLoadAll)
        {
            ascension = getInt;
        }

        getInt = Serialization.deserializeInt(Arrays.copyOfRange(data, offset, offset + 4));
        offset += 4;
        if (!initUpdate || serverLoadAll)
        {
            equipment = getInt;
        }

        float getFloat = Serialization.deserializeFloat(Arrays
                .copyOfRange(data, offset, offset + 4));
        offset += 4;
        if (!localPlayer || serverLoadAll)
        {
            aim = getFloat;
        }

        initUpdate = localPlayer;
    }

    public boolean canBeHit()
    {
        return deathCooldown <= 0 && team != 0;
    }

}
