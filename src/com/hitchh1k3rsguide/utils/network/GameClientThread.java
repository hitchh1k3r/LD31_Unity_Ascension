package com.hitchh1k3rsguide.utils.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import com.hitchh1k3rsguide.utils.GenericUtils;
import com.hitchh1k3rsguide.utils.network.Server.ClientThread;

public class GameClientThread implements Runnable, IPacketQueTarget
{

    public UUID uuid;

    public Socket clientSocket = null;
    private OutputStream os = null;
    private InputStream is = null;

    private boolean closed = false;

    public GameClientThread()
    {
        uuid = GenericUtils.getUUID("uuid");
        GenericUtils.stashUUID("uuid", uuid);

        try
        {
            clientSocket = new Socket(Server.HOST, Server.PORT);
            os = clientSocket.getOutputStream();
            is = clientSocket.getInputStream();
        }
        catch (UnknownHostException e)
        {
            GenericUtils.crash("Could not find server...");
        }
        catch (IOException e)
        {
            GenericUtils.crash("Could not connect to server...");
        }

        if (clientSocket != null && os != null && is != null)
        {
            new Thread(this).start();
        }
    }

    @Override
    public void run()
    {
        try
        {
            while (!closed)
            {
                Packet packet = new Packet(is);
                if (packet.process(this) == Server.CLOSE)
                {
                    closed = true;
                }
            }

            os.close();
            is.close();
            clientSocket.close();
        }
        catch (Exception e)
        {
            if (!(e instanceof IOException))
            {
                e.printStackTrace();
            }
            GenericUtils.crash("You've been disconnected from the server.");
        }
    }

    @Override
    public OutputStream getStream()
    {
        return os;
    }

    @Override
    public ClientThread getClientThread()
    {
        return null;
    }

}
