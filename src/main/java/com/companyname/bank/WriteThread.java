package com.companyname.bank;

import java.nio.ByteBuffer;
import java.nio.channels.*;

class WriteThread extends Thread {
    private String info;
    private SocketChannel socketChannel;

    public WriteThread(SocketChannel _socketChannel, String _info) {
        this.socketChannel = _socketChannel;
        this.info = _info;
    }

    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            buf.put(info.getBytes());
            buf.flip();
            this.socketChannel.write(buf);
            buf.clear();
            // System.out.println("发送完毕");
            // Thread.sleep(2000);
            // this.socketChannel.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
