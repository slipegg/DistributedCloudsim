package com.companyname.bank;

import com.companyname.models.HostInfo;
import com.companyname.models.RecvTaskInfo;
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

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.util.Log;
import com.companyname.models.*;

public class c1 {
    // private final CloudSim simulation;
    private Datacenter datacenter;
    private Double INTERVAL = 0.1;
    private CustomerDemandManager customerManager;
    CloudSim simulation;
    SqlManager sqlManager;
    String datacenter_name;
    double last_submit_history_time = -1;
    double terminateTime = 30.0;

    long startTime;

    public c1() {
        Log.setLevel(Level.WARN);
        datacenter_name = "c1";
        // 建立与数据库的连接，包含建表
        sqlManager = new SqlManager(datacenter_name);
        // 建立数据中心之间的网络连接
        DataCenterSockets dataCenterSockets = new DataCenterSockets("DataCenterNetwork.yml", datacenter_name);
        dataCenterSockets.buildLink();

        startTime = System.currentTimeMillis();// 获取开始时间
        // 根据文件创建数据中心，包含创建CloudSim和hosts
        simulation = new CloudSim(0.1);
        DataCenterDeviceManager datacenter_manager = new DataCenterDeviceManager(datacenter_name,
                dataCenterSockets.getOtherDatacenterNames());
        datacenter = datacenter_manager.createDataCenter(simulation);
        System.out.println("初始化datacenter_manager时间： " + (System.currentTimeMillis() - startTime) + "ms");
        // System.out.printf("datacenter:%s\n", datacenter);

        // 获取客户需求，包括vm和cloudlet
        startTime = System.currentTimeMillis();
        customerManager = new CustomerDemandManager(datacenter_name, dataCenterSockets.getOtherDatacenterNames());
        System.out.println("初始化customerManager运行时间： " + (System.currentTimeMillis() - startTime) + "ms");

        // sqlManager.insertTaskIntoTable("c2", datacenter_name, 2, 2, 5.5);

        // System.out.println();
        // simulation.start();

        // 创建任务分配模型
        startTime = System.currentTimeMillis();
        CustomerOffload customerOffload = new CustomerOffload(datacenter_manager, customerManager, sqlManager,
                dataCenterSockets);

        // 初始化任务
        customerOffload.suitOffload(simulation, 0);
        System.out.println("创建任务分配模型初始化运行时间： " + (System.currentTimeMillis() - startTime) + "ms");

        // 运行模拟器
        startTime = System.currentTimeMillis();
        System.out.println("同步中...");
        dataCenterSockets.synchronization(0.0);
        System.out.println("同步结束...");
        simulation.startSync();
        simulation.terminateAt(terminateTime);
        long run_start_time = 0;
        long cloudsim_run_total_time = 0;
        long suitOffload_total_time = 0;
        long futher_total_time = 0;
        long history_total_time = 0;
        long syn_total_time = 0;
        while (!customerManager.isSubmitAllCustomer() || simulation.isRunning()) {
            run_start_time = System.currentTimeMillis();
            simulation.runFor(INTERVAL);
            cloudsim_run_total_time += (System.currentTimeMillis() - run_start_time);

            run_start_time = System.currentTimeMillis();
            customerOffload.suitOffload(simulation, simulation.clock());
            suitOffload_total_time += (System.currentTimeMillis() - run_start_time);

            run_start_time = System.currentTimeMillis();
            customerOffload.getFutherCustomer(simulation, simulation.clock());
            futher_total_time += (System.currentTimeMillis() - run_start_time);

            // System.out.println(simulation.clock());
            // datacenter_manager.updateHost(simulation.clock());
            run_start_time = System.currentTimeMillis();
            UpdateHistory();
            history_total_time += (System.currentTimeMillis() - run_start_time);

            System.out.println(simulation.clock());
            // System.out.println(customerManager.isSubmitAllCustomer());
            // System.out.println(simulation.isRunning());
            if (simulation.clock() - terminateTime > 2) {
                simulation.terminate();
            }
            run_start_time = System.currentTimeMillis();
            dataCenterSockets.synchronization(simulation.clock());
            syn_total_time += (System.currentTimeMillis() - run_start_time);
            // simulation.terminate();
        }
        System.out.println("cloudsimplus模拟运行时间： " + cloudsim_run_total_time + "ms");
        System.out.println("suitOffload_total_time： " + suitOffload_total_time + "ms");
        System.out.println("futher_total_time " + futher_total_time + "ms");
        System.out.println("history_total_time " + history_total_time + "ms");
        System.out.println("syn_total_time " + syn_total_time + "ms");

        System.out.println("程序模拟运行时间： " + (System.currentTimeMillis() - startTime) + "ms");
        // 查看模拟结果
        customerManager.showResults();
        long endTime = System.currentTimeMillis(); // 获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
        System.out.println(endTime);
        System.exit(0);
    }

    public void UpdateHistory() {
        if (simulation.clock() >= last_submit_history_time + 1) {
            last_submit_history_time = simulation.clock();
            List<Host> hosts = datacenter.getHostList();
            for (var host : hosts) {
                // System.out.println("host.getCpuMipsUtilization():" +
                // Double.toString(host.getCpuMipsUtilization()));
                // System.out.println("host.getTotalAvailableMips():" +
                // Double.toString(host.getTotalAvailableMips()));
                HostInfo hi = new HostInfo(host.getId(), simulation.clock(),
                        host.getRamProvisioner().getAvailableResource(), host.getBwProvisioner().getAvailableResource(),
                        host.getStorage().getAvailableResource(), host.getFreePeList().size(),
                        host.getTotalAvailableMips());
                sqlManager.insertHostInfoIntoTable(datacenter_name, hi);// 性能瓶颈，可以考虑改为异步
            }
        }
    }

    public static void main(String[] args) {
        new c1();
    }
}
