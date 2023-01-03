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

import com.companyname.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

class SelectThread extends Thread {
    public Map<String, String> info_map = new HashMap<String, String>();
    private String hostName;
    private Integer port;

    private DataCenterNetwork datacenter_network;
    private Map<String, String> msg_map;
    private int connectNum;
    private DatacenterRegistryRecv datacenterRegistryRecv;
    private Map<String, Double> datacenter_simulation_time_map;
    // private Map<String, List<Integer,DatacenterRegistry>()> dataCenterInfo_map;

    public SelectThread(DataCenterNetwork _datacenter_network, Map<String, String> _msg_map,
            DatacenterRegistryRecv _datacenterRegistryRecv, int connectNum,
            Map<String, Double> datacenter_simulation_time_map) {
        // info_map.put("c1", "");
        // info_map.put("c2", "");
        // info_map.put("c3", "");
        this.datacenter_network = _datacenter_network;
        this.msg_map = _msg_map;
        this.datacenterRegistryRecv = _datacenterRegistryRecv;
        this.connectNum = connectNum;
        this.datacenter_simulation_time_map = datacenter_simulation_time_map;
        // this.dataCenterInfo_map = _dataCenterInfo_map;
    }

    @Override
    public void run() {
        System.out.printf("%s:%d的selecThread启动,等待其他数据中心连接中...\n", datacenter_network.getIp(),
                datacenter_network.getPort());
        ByteBuffer byteBuffer = ByteBuffer.allocate(15);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // 开启挑选器
            Selector selector = Selector.open();
            // 开启ServerSocketChannel通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket()
                    .bind(new InetSocketAddress(datacenter_network.getIp(), datacenter_network.getPort()));
            // 设置非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 在挑选器中注册通道(服务器通道)
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            SelectableChannel sc = null;

            int connected_num = 0;
            while (true) {
                // 阻塞的挑选方法
                selector.select();
                Iterator<SelectionKey> iterable = selector.selectedKeys().iterator();
                while (iterable.hasNext()) {
                    SelectionKey key = iterable.next();
                    try {
                        if (key.isAcceptable()) {
                            // 服务器通道
                            sc = key.channel();
                            SocketChannel socketChannel = ((ServerSocketChannel) sc).accept();
                            socketChannel.configureBlocking(false); // 设置非阻塞
                            System.out.println("accept:" + socketChannel.getLocalAddress());
                            connected_num++;
                            // 注册读事件监听
                            // socketChannel.register(selector, SelectionKey.OP_READ |
                            // SelectionKey.OP_WRITE); //注册读写时间
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        }
                        if (key.isReadable()) {
                            // 读时间监听获取
                            SocketChannel sc1 = (SocketChannel) key.channel();
                            // System.out.println("getRemoteAddress:" + sc1.getRemoteAddress());
                            Integer len = sc1.read(byteBuffer);
                            if (len < 0) {
                                // System.out.printf("len:%d,连接关闭！\n", len);
                                // selector.selectedKeys().remove(key);
                                key.cancel();
                                continue;
                            }
                            while (len > 0) {
                                // System.out.printf("len:%d\n", len);
                                byteBuffer.flip();
                                bos.write(byteBuffer.array());
                                byteBuffer.clear();
                                len = sc1.read(byteBuffer);
                            }

                            String message = new String(bos.toByteArray());

                            // String host = getInfoHost(message);
                            // String info = getInfo(message);
                            // System.out.printf("%s传来信息 :\n%s\n", host, info);
                            // msg_map.put(host, info);
                            dealMsg(message);
                            // byte[] msg = new String("hello " + new String(bos.toByteArray())).getBytes();
                            bos.reset();
                            // sc1.write(ByteBuffer.wrap(msg, 0, msg.length));
                        }
                    } catch (Exception e) {
                        // 存在异常时, 清除该sokect
                        System.out.println("exception!");
                        e.printStackTrace();
                        key.cancel();
                    }
                }
                // 处理完后清空挑选器内容
                selector.selectedKeys().clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void dealMsg(String _message) {
        // System.out.println("dealMsg:");
        // System.out.println(_message);
        String[] messages = _message.split("\n");
        String[] t;
        for (int i = messages.length - 1; i >= 0; i--) {
            String message = messages[i];
            t = message.split(" ");
            if (t.length != 3) {
                continue;
            } else {
                datacenter_simulation_time_map.put(t[0], Double.valueOf(t[2]));
            }
        }

        // String message = messages[messages.length - 2];
        // System.out.println(_message);
        // System.out.println(messages);
        // System.out.println(message);
        // int index = message.indexOf(" ");
        // String datacenter_name = message.substring(0, index);
        // String tmp = message.substring(index + 1);

        // index = tmp.indexOf(" ");
        // String type = tmp.substring(0, index);
        // tmp = tmp.substring(index + 1);
        // // System.out.printf("type:%s\n", type);

        // if (type.equals("time")) {
        // // index = tmp.indexOf(" ");
        // // String time = tmp.substring(0, index);
        // // System.out.printf("time:%s\n", tmp);
        // try {
        // double t = Double.valueOf(tmp);
        // datacenter_simulation_time_map.put(datacenter_name, Double.valueOf(t));
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // else if (type.equals("dataCenterInfo")) {
        // index = tmp.indexOf(" ");
        // String round = tmp.substring(0, index);
        // System.out.printf("round:%s\n", round);

        // tmp = tmp.substring(index + 1);
        // System.out.printf("msg:%s\n", tmp);

        // addDataCenterInfo(datacenter_name, round, tmp);
        // }
    }

    private void addDataCenterInfo(String datacenter_name, String round, String dataCenterInfo) {
        try {
            System.out.println("addDataCenterInfo");
            final YamlReader reader = new YamlReader(dataCenterInfo);
            final YamlConfig cfg = reader.getConfig();
            cfg.setClassTag("san", SanStorageRegistry.class);
            cfg.setClassTag("hosts", HostRegistry.class);
            DatacenterRegistry res = reader.read(DatacenterRegistry.class);
            // System.out.printf("DatacenterRegistry res =\n%s\n", res);
            // System.out.println(res.getClass().getName());
            // System.out.println("dataCenterInfo_map,put start!");
            datacenterRegistryRecv.updateInfo(datacenter_name, Integer.parseInt(round), res);
            System.out.println("dataCenterInfo_map,put finish!");
        } catch (YamlException e) {
            System.err
                    .println("Error when trying to load the simulation scenario from the YAML file: " + e.getMessage());
        }
    }

}