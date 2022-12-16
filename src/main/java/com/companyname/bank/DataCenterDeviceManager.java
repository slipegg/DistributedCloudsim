package com.companyname.bank;

import java.util.List;
import com.companyname.models.*;
// import cloudreports.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
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

import java.io.FileNotFoundException;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class DataCenterDeviceManager {
    private String file_context = "";
    public DatacenterRegistry datacenterRegistry;

    public String getFileContext() {
        return file_context;
    }

    public DataCenterDeviceManager(String file_name) {
        // 读取数据中心的配置文件
        try {
            final String FilePath = DataCenterSockets.class.getClassLoader().getResource(file_name)
                    .getPath();
            File file = new File(FilePath);
            FileReader fr = new FileReader(file);

            String line;
            BufferedReader br = new BufferedReader(fr);
            try {
                while ((line = br.readLine()) != null) {
                    // 一行一行地处理...
                    // System.out.println(line);
                    file_context = file_context + line + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(file_context);
            final YamlReader reader = new YamlReader(file_context);
            final YamlConfig cfg = reader.getConfig();
            cfg.setClassTag("san", SanStorageRegistry.class);
            cfg.setClassTag("hosts", HostRegistry.class);

            datacenterRegistry = reader.read(DatacenterRegistry.class);
            System.out.println(datacenterRegistry);
        } catch (FileNotFoundException e) {
            System.out.println("error! e");
            System.out.println(e);
        } catch (YamlException y) {
            System.out.println("error! y");
            System.out.println(y);
        }
    }

    public DatacenterSimple createDataCenter(CloudSim simulation) {
        List<HostRegistry> host_register = datacenterRegistry.getHosts();
        final var hostList = new ArrayList<Host>(host_register.size());
        for (int i = 0; i < host_register.size(); i++) {
            for (int host_amount = 0; host_amount < host_register.get(i).getAmount(); host_amount++) {
                final var host = createHost(host_register.get(i));
                hostList.add(host);
            }
        }

        // Uses a VmAllocationPolicySimple by default to allocate VMs
        return new DatacenterSimple(simulation, hostList);
    }

    private Host createHost(HostRegistry host_registry) {
        final var peList = new ArrayList<Pe>(host_registry.getPes());
        // List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < host_registry.getPes(); i++) {
            // Uses a PeProvisionerSimple by default to provision PEs for VMs
            peList.add(new PeSimple(host_registry.getMips()));
        }

        /*
         * Uses ResourceProvisionerSimple by default for RAM and BW provisioning
         * and VmSchedulerSpaceShared for VM scheduling.
         */
        return new HostSimple(host_registry.getRam(), host_registry.getBw(), host_registry.getStorage(), peList);

    }
}
