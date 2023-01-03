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

public class c2 {
    // private final CloudSim simulation;
    private Datacenter datacenter;
    private Double INTERVAL = 0.1;
    private CustomerDemandManager customerManager;
    CloudSim simulation;
    SqlManager sqlManager;
    String datacenter_name;
    double last_submit_history_time = -1;
    double terminateTime = 30.0;

    public c2() {
        Log.setLevel(Level.WARN);
        datacenter_name = "c2";
        // 建立与数据库的连接，包含建表
        sqlManager = new SqlManager(datacenter_name);
        // 建立数据中心之间的网络连接
        DataCenterSockets dataCenterSockets = new DataCenterSockets("DataCenterNetwork.yml", datacenter_name);
        dataCenterSockets.buildLink();

        // 根据文件创建数据中心，包含创建CloudSim和hosts
        simulation = new CloudSim(0.1);
        DataCenterDeviceManager datacenter_manager = new DataCenterDeviceManager(datacenter_name,
                dataCenterSockets.getOtherDatacenterNames());
        datacenter = datacenter_manager.createDataCenter(simulation);
        // System.out.printf("datacenter:%s\n", datacenter);

        // 获取客户需求，包括vm和cloudlet
        customerManager = new CustomerDemandManager(datacenter_name, dataCenterSockets.getOtherDatacenterNames());

        // sqlManager.insertTaskIntoTable("c2", datacenter_name, 2, 2, 5.5);

        // System.out.println();
        // simulation.start();

        // 创建任务分配模型
        CustomerOffload customerOffload = new CustomerOffload(datacenter_manager, customerManager, sqlManager,
                dataCenterSockets);

        // 初始化任务
        customerOffload.suitOffload(simulation, 0);

        long startTime = System.currentTimeMillis();// 获取开始时间

        // 运行模拟器
        System.out.println("同步中...");
        dataCenterSockets.synchronization(0.0);
        System.out.println("同步结束...");
        simulation.startSync();
        simulation.terminateAt(terminateTime);
        while (!customerManager.isSubmitAllCustomer() || simulation.isRunning()) {
            simulation.runFor(INTERVAL);
            customerOffload.suitOffload(simulation, simulation.clock());
            customerOffload.getFutherCustomer(simulation, simulation.clock());
            // System.out.println(simulation.clock());
            // datacenter_manager.updateHost(simulation.clock());
            // cd.createVmsAndCloudlets(simulation, simulation.clock());
            UpdateHistory();
            System.out.println(simulation.clock());
            // System.out.println(customerManager.isSubmitAllCustomer());
            // System.out.println(simulation.isRunning());
            if (simulation.clock() - terminateTime > 2) {
                simulation.terminate();
            }
            dataCenterSockets.synchronization(simulation.clock());
        }

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
                HostInfo hi = new HostInfo(host.getId(), simulation.clock(),
                        host.getRamProvisioner().getAvailableResource(), host.getBwProvisioner().getAvailableResource(),
                        host.getStorage().getAvailableResource(), host.getFreePeList().size(),
                        host.getTotalAvailableMips());
                sqlManager.insertHostInfoIntoTable(datacenter_name, hi);// 性能瓶颈，可以考虑改为异步
                // System.out.print("BusyPesNumber:" + host.getBusyPesNumber());
                // System.out.print(" getMips:" + host.getMips());
                // System.out.print(" getTotalMipsCapacity:" + host.getTotalMipsCapacity());
                // System.out.print(" getTotalAvailableMips:" + host.getTotalAvailableMips() +
                // "\n");
            }
        }
        // List<DatacenterBroker> brokers = cd.getBrokers();
        // for (var broker : brokers) {
        // List<Vm> vms = broker.getVmExecList();
        // System.out.println(vms);
        // for (var vm : vms) {
        // System.out.print(vm.getCurrentRequestedRam() + " ");
        // System.out.print(vm.getCurrentRequestedMips() + " ");
        // System.out.print(vm.getCurrentRequestedBw() + " ");
        // }
        // }
    }

    public static void main(String[] args) {
        new c2();
    }
}
