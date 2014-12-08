package com.hitchh1k3rsguide.ld31.data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class TeamScore
{

    public int[] scores = new int[7];

    public TeamScore()
    {
    }

    public TeamScore(int teamNumber)
    {
        getScores("team_" + teamNumber);
    }

    public String getSkullString()
    {
        return ":%05d   :%05d   :%05d   :%05d   :%05d";
        // offset should be 5:
        // return (scores[0] < 50 ? ":" : "-") + "%05d  " + (scores[1] < 50 ? ":" : "-") + "%05d  "
        //        + (scores[2] < 50 ? ":" : "-") + "%05d  " + (scores[3] < 50 ? ":" : "-") + "%05d  "
        //        + (scores[4] < 50 ? ":" : "-") + "%05d  " + (scores[5] < 50 ? ":" : "-") + "%05d  "
        //        + (scores[6] < 50 ? ":" : "-") + "%05d";
    }

    public void getScores(String path)
    {
        try
        {
            byte[] bytes = new byte[4 * 7];

            FileInputStream fis = new FileInputStream(path);
            fis.read(bytes);

            ByteBuffer intBuffer = ByteBuffer.allocate(4 * 7);
            intBuffer.put(bytes, 0, 4 * 7);
            intBuffer.flip();
            for (int i = 0; i < 7; ++i)
            {
                scores[i] = intBuffer.getInt();
            }
            fis.close();
        }
        catch (Exception e)
        {
        }
    }

    public void stashScores(String path)
    {
        try
        {
            ByteBuffer intBuffer = ByteBuffer.allocate(4 * 7);
            for (int i = 0; i < 7; ++i)
            {
                intBuffer.putInt(scores[i]);
            }
            intBuffer.flip();

            byte[] bytes = intBuffer.array();

            FileOutputStream fos = new FileOutputStream(path);
            fos.write(bytes);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
