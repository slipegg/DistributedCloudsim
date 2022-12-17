package com.companyname.bank;

import cloudreports.models.*;
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

public class test1 {
    // private final CloudSim simulation;
    private Datacenter datacenter;
    private Double INTERVAL = 1.0;
    private CustomerDemandManager cd;

    public test1() {
        String datacenter_name = "c1";
        // 建立数据中心之间的网络连接
        DataCenterSockets c1 = new DataCenterSockets("DataCenterNetwork.yml", datacenter_name);
        // c1.buildLink();

        // 根据文件创建数据中心，包含创建CloudSim和hosts
        CloudSim simulation = new CloudSim(1);
        // simulation.inter
        DataCenterDeviceManager datacenter_manager = new DataCenterDeviceManager(datacenter_name,
                c1.getOtherDatacenterNames());
        datacenter = datacenter_manager.createDataCenter(simulation);
        // System.out.printf("datacenter:%s\n", datacenter);

        // 互相传递初始的数据中心的数据
        // int round = 0;
        // int max_round = 10;
        // while (round < max_round) {
        // c1.sendMsg("c2", "dataCenterInfo", round,
        // datacenter_manager.getFileContext());
        // round++;
        // try {
        // Thread.sleep(2000);
        // } catch (InterruptedException e) {
        // System.out.println(e);
        // }
        // }
        // // 获取客户需求，包括vm和cloudlet
        cd = new CustomerDemandManager("customers.yml");
        cd.createVmsAndCloudlets(simulation, 0.0);
        // System.out.println(cd.getFileContext());
        // 运行模拟器
        // simulation.start();
        simulation.startSync();
        simulation.terminateAt(30);
        while (!cd.isSubmitAllCustomer() || simulation.isRunning()) {
            simulation.runFor(INTERVAL);
            System.out.println(simulation.clock());
            datacenter_manager.updateHost(simulation.clock());
            cd.createVmsAndCloudlets(simulation, simulation.clock());
            PrintInfo();
        }
        // 查看模拟结果
        cd.showResults();
    }

    public void PrintInfo() {
        List<Host> hosts = datacenter.getHostList();
        for (var host : hosts) {
            System.out.print("BusyPesNumber:" + host.getBusyPesNumber());
            System.out.print("BusyPesNumber:" + host.getMips());
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
        new test1();
    }
}
