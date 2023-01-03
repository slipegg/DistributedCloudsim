package com.companyname.bank;

import java.util.List;
// import cloudreports.models.*;
import com.companyname.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.io.FileNotFoundException;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.nio.channels.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

class DatacenterRegistryRecv {
    private Map<String, HashMap<Integer, DatacenterRegistry>> recv_map = new HashMap<String, HashMap<Integer, DatacenterRegistry>>();
    private Map<String, HashMap<Integer, Integer>> round_map = new HashMap<String, HashMap<Integer, Integer>>();

    // private int interval;
    private int size;

    public DatacenterRegistryRecv() {
        size = 100;
    }

    public DatacenterRegistryRecv(int _size) {
        size = _size;
    }

    public void updateInfo(String datacaenter_name, int round, DatacenterRegistry info) {
        if (!recv_map.containsKey(datacaenter_name)) {
            HashMap<Integer, DatacenterRegistry> tmp = new HashMap<>();
            tmp.put(round % size, info);
            recv_map.put(datacaenter_name, tmp);

            HashMap<Integer, Integer> rtmp = new HashMap<>();
            rtmp.put(round % size, round);
            round_map.put(datacaenter_name, rtmp);
        } else {
            var tmp = recv_map.get(datacaenter_name);
            tmp.put(round % size, info);

            var rtmp = round_map.get(datacaenter_name);
            rtmp.put(round % size, round);
        }
    }

    public DatacenterRegistry getInfo(String datacaenter_name, int round) {
        int max_try = 10000;// 最多等10秒
        while (max_try > 0) {
            if (round_map.get(datacaenter_name) != null && round_map.get(datacaenter_name).containsKey(round % size)
                    && round_map.get(datacaenter_name).get(round % size) == round) {
                return recv_map.get(datacaenter_name).get(round % size);

            } else {
                try {
                    Thread.sleep(100);
                    max_try -= 1;
                } catch (InterruptedException e) {
                    System.out.println("DatacenterRegistryRecv.getInfo() error!\n" + e);
                }
            }
        }
        return null;
    }
}

class CloudletRegistryRecv {
    private Map<String, HashMap<Integer, CloudletRegistry>> recv_map = new HashMap<String, HashMap<Integer, CloudletRegistry>>();
    private Map<String, HashMap<Integer, Integer>> round_map = new HashMap<String, HashMap<Integer, Integer>>();

    // private int interval;
    private int size;

    public CloudletRegistryRecv() {
        size = 100;
    }

    public CloudletRegistryRecv(int _size) {
        size = _size;
    }

    public void updateInfo(String datacaenter_name, int round, CloudletRegistry info) {
        if (!recv_map.containsKey(datacaenter_name)) {
            HashMap<Integer, CloudletRegistry> tmp = new HashMap<>();
            tmp.put(round % size, info);
            recv_map.put(datacaenter_name, tmp);

            HashMap<Integer, Integer> rtmp = new HashMap<>();
            rtmp.put(round % size, round);
            round_map.put(datacaenter_name, rtmp);
        } else {
            var tmp = recv_map.get(datacaenter_name);
            tmp.put(round % size, info);

            var rtmp = round_map.get(datacaenter_name);
            rtmp.put(round % size, round);
        }
    }

    public CloudletRegistry getInfo(String datacaenter_name, int round) {
        int max_try = 10000;// 最多等10秒
        while (max_try > 0) {
            if (round_map.get(datacaenter_name) != null && round_map.get(datacaenter_name).containsKey(round % size)
                    && round_map.get(datacaenter_name).get(round % size) == round) {
                return recv_map.get(datacaenter_name).get(round % size);

            } else {
                try {
                    Thread.sleep(100);
                    max_try -= 1;
                } catch (InterruptedException e) {
                    System.out.println("CloudletRegistryRecv.getInfo() error!\n" + e);
                }
            }
        }
        return null;
    }
}

public class DataCenterSockets {
    // 数据中心网络基础信息
    public DataCenterNetwork datacenter_network;
    public List<DataCenterNetwork> other_datacenter_networks = new ArrayList<>();
    List<String> other_datacenter_names = new ArrayList<>();
    // 创建的socket连接
    Map<String, SocketChannel> other_socket_map = new HashMap<>();
    public final Map<String, String> msg_map = new HashMap<>();
    private Map<String, DatacenterRegistry> dataCenterInfo_map = new HashMap<>();

    // 信息传递的中介
    private DatacenterRegistryRecv datacenterRegistryRecv = new DatacenterRegistryRecv();
    private CloudletRegistryRecv cloudletRegistryRecv = new CloudletRegistryRecv();

    // 记录各个数据中心当前的模拟时间
    private Map<String, Double> datacenter_simulation_time_map = new HashMap<>();

    public Map<String, DatacenterRegistry> getDataCenterInfo_map() {
        return dataCenterInfo_map;
    }

    public DataCenterSockets(String datacenter_network_file_name, String datacenter_name) {
        // 读取网络配置文件
        try {
            final String FilePath = DataCenterSockets.class.getClassLoader().getResource(datacenter_network_file_name)
                    .getPath();
            final File file = new File(FilePath);
            final FileReader fr = new FileReader(file);

            final YamlReader reader = new YamlReader(fr);
            final YamlConfig cfg = reader.getConfig();
            cfg.setClassTag("DataCenterNetwork", DataCenterNetwork.class);
            DataCenterNetworks res = reader.read(DataCenterNetworks.class);
            for (DataCenterNetwork dc : res.getDataCenterNetworks()) {
                if (datacenter_name.equals(dc.getName())) {
                    datacenter_network = dc;
                } else {
                    other_datacenter_networks.add(dc);
                    other_datacenter_names.add(dc.getName());
                    datacenter_simulation_time_map.put(dc.getName(), 0.0);
                }
            }
            System.out.println(datacenter_network.getName());
            System.out.println(other_datacenter_networks);
        } catch (FileNotFoundException e) {
            System.out.println("error! e");
            System.out.println(e);
        } catch (YamlException y) {
            System.out.println("error! y");
            System.out.println(y);
        }
    }

    public void buildLink() {
        // 创建epoll来接受消息
        SelectThread selectThread = new SelectThread(datacenter_network, msg_map, datacenterRegistryRecv,
                other_datacenter_networks.size(), datacenter_simulation_time_map);
        selectThread.start();

        List<ReconnectThread> reconnectThreads = new ArrayList<>();
        for (int i = 0; i < other_datacenter_networks.size(); i++) {
            String ip = other_datacenter_networks.get(i).getIp();
            int port = other_datacenter_networks.get(i).getPort();
            ReconnectThread rthread = new ReconnectThread(ip, port);
            rthread.start();
            reconnectThreads.add(rthread);
        }
        try {
            for (int i = 0; i < other_datacenter_networks.size(); i++) {
                ReconnectThread rThread = reconnectThreads.get(i);
                rThread.join();
                other_socket_map.put(other_datacenter_networks.get(i).getName(), rThread.getReturnValue());
                System.out.printf("%d线程完成\n", i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("初始化连接完成");
        // selectThread.interrupt();
    }

    public void synchronization(double now_time) {
        for (String name : other_datacenter_names) {
            sendMsg(name, "time", String.format("%.2f\n", now_time));
        }
        for (String name : other_datacenter_names) {
            while (now_time - datacenter_simulation_time_map.get(name) > 1.0) {
                try {
                    Thread.sleep(1);
                    ;// 是否要睡眠
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendMsg(String datacenter_name, String type, String message) {
        String sendMsg = datacenter_network.getName() + " " + type + " " + message;
        SocketChannel socketChannel = other_socket_map.get(datacenter_name);

        Thread wt = new WriteThread(socketChannel, sendMsg);
        wt.start();

        // System.out.printf("%s向%s发送了:%s", datacenter_name,
        // datacenter_network.getName(), message);
    }

    public void sendMsg(String datacenter_name, String type, int round, String message) {
        String sendMsg = datacenter_network.getName() + " " + type + " " + Integer.toString(round) + " " + message;
        Thread wt = new WriteThread(other_socket_map.get(datacenter_name), sendMsg);
        wt.start();
        // System.out.printf("%s向%s发送了:%s", datacenter_name,
        // datacenter_network.getName(), message);
    }

    public void sendInitDataCenterInfo(String target_datacenter_name, String file_context) {
        String sendMsg = datacenter_network.getName() + " " + "dataCenterInfo " + file_context;
        Thread wt = new WriteThread(other_socket_map.get(target_datacenter_name), sendMsg);
        wt.start();
        // System.out.printf("%s向%s发送了:%s", target_datacenter_name,
        // datacenter_network.getName(), file_context);
    }

    public List<String> getOtherDatacenterNames() {
        return other_datacenter_names;
    }

    public DatacenterRegistry get_datacenter_recv(String datacenter_name, Integer round) {
        var res = datacenterRegistryRecv.getInfo(datacenter_name, round);
        if (res == null) {
            System.out.println("等待超时！");
            System.exit(-1);
        }
        return res;
    }

    public CloudletRegistry get_cloudlet_recv(String datacenter_name, Integer round) {
        var res = cloudletRegistryRecv.getInfo(datacenter_name, round);
        if (res == null) {
            System.out.println("等待超时！");
            System.exit(-1);
        }
        return res;
    }
}
