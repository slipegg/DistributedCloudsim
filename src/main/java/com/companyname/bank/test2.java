package com.companyname.bank;

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

import com.companyname.models.*;
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

public class test2 {
    // private final CloudSim simulation;
    // private Datacenter datacenter;

    public test2() {
        SqlManager sqlmanager = new SqlManager();
        DatacenterInfo dinfo = sqlmanager.getDataCenterInfo("c1", 20);
        System.out.println(dinfo.hosts.get(0).free_mips);
        // DataCenterSockets c2 = new DataCenterSockets("DataCenterNetwork.yml", "c2");
        // c2.buildLink();
        // DatacenterRegistry dr = c2.get_datacenter_recv("c1", 0);
        // System.out.printf("c2.getDataCenterInfo_map():%s\n", dr);
        // int round = 0;
        // DatacenterRegistry dr;
        // while (true) {
        // dr = c2.get_datacenter_recv("c1", round);
        // System.out.printf("round %d: %s\n", round, dr.getAmount());
        // round++;
        // }
        // 根据文件创建数据中心，包含创建CloudSim和hosts
        // simulation = new CloudSim();
        // DataCenterDeviceManager datacenter_manager = new
        // DataCenterDeviceManager("c1_device.yml");
        // datacenter = datacenter_manager.createDataCenter(simulation);

        // 互相传递初始的数据中心的数据

    }

    public static void main(String[] args) {
        new test2();
    }
}
