package com.hitchh1k3rsguide.utils.network;

import java.io.OutputStream;

import com.hitchh1k3rsguide.utils.network.Server.ClientThread;

public interface IPacketQueTarget
{

    abstract public OutputStream getStream();

    abstract ClientThread getClientThread();

}
