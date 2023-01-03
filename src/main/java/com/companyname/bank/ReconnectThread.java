package com.companyname.bank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class ReconnectThread extends Thread {
    private SocketChannel success_socket;
    private String hostName;
    private Integer port;

    public ReconnectThread(String _hostName, int _port) {
        this.hostName = _hostName;
        this.port = _port;
        System.out.println(success_socket);
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Thread.sleep(1000);
                SocketChannel socketChannel_1 = SocketChannel.open();
                socketChannel_1.connect(new InetSocketAddress(this.hostName, this.port));
                this.success_socket = socketChannel_1;
                break;
            } catch (Exception e) {
                // e.printStackTrace();
                // System.out.printf("重连%s:%d中\n", hostName, port);
            }
        }
    }

    public SocketChannel getReturnValue() {
        return this.success_socket;
    }
}
