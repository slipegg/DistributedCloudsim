package com.companyname.bank;

import java.io.FileNotFoundException;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

// import org.cloudsimplus.automation.PolicyLoader;

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
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;

import java.util.List;
// import cloudreports.models.*;
import com.companyname.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

public class CustomerDemandManager {
    String file_context = "";
    CustomerRegistrys customerRegistrys;
    List<DatacenterBroker> brokers = new ArrayList<>();

    public CustomerDemandManager(String file_name) {
        // 读取任务需求的配置文件
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
            cfg.setClassTag("customer", CustomerRegistry.class);
            cfg.setClassTag("vm", VmRegistry.class);
            cfg.setClassTag("cloudlet", CloudletRegistry.class);

            customerRegistrys = reader.read(CustomerRegistrys.class);
            System.out.println("customerRegistrys:\n" + customerRegistrys);
        } catch (FileNotFoundException e) {
            System.out.println("error! e");
            System.out.println(e);
        } catch (YamlException y) {
            System.out.println("error! y");
            System.out.println(y);
        }
    }

    public void createVmsAndCloudlets(CloudSim simulation) {
        List<CustomerRegistry> customers = customerRegistrys.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            CustomerRegistry customer = customers.get(i);
            for (int customer_amount = 0; customer_amount < customer.getAmount(); customer_amount++) {
                DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
                submitVmsAndCloudlets(broker, customer);
                brokers.add(broker);
            }
        }

    }

    public void showResults() {
        for (DatacenterBroker broker : brokers) {
            final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            new CloudletsTableBuilder(finishedCloudlets).build();
        }
    }

    public String getFileContext() {
        return file_context;
    }

    private void submitVmsAndCloudlets(DatacenterBroker broker, CustomerRegistry customer) {
        List<Vm> vm_list = createVmListForOneBroker(broker, customer);
        List<Cloudlet> cloudlet_list = createCloudletsListForOneBroker(broker, customer);
        System.out.println("vm_list:\n" + vm_list.size());
        System.out.println("cloudlet_list:\n" + cloudlet_list.size());
        broker.submitVmList(vm_list);
        broker.submitCloudletList(cloudlet_list);
    }

    private List<Vm> createVmListForOneBroker(
            final DatacenterBroker broker,
            final CustomerRegistry cr) throws RuntimeException {
        final int totalVmsAmount = cr.getVms().stream().mapToInt(VmRegistry::getAmount).sum();
        final List<Vm> list = new ArrayList<>(totalVmsAmount);
        for (VmRegistry vmr : cr.getVms()) {
            for (int i = 0; i < vmr.getAmount(); i++) {
                list.add(createVm(vmr, broker));
            }
        }
        return list;
    }

    private List<Cloudlet> createCloudletsListForOneBroker(
            final DatacenterBroker broker,
            final CustomerRegistry cr) throws RuntimeException {
        final int totalCloudletsAmount = cr.getCloudlets().stream().mapToInt(CloudletRegistry::getAmount).sum();
        final List<Cloudlet> list = new ArrayList<>(totalCloudletsAmount);
        for (CloudletRegistry clr : cr.getCloudlets()) {
            for (int i = 0; i < clr.getAmount(); i++) {
                list.add(createCloudlet(clr, broker));
            }
        }
        return list;
    }

    private Vm createVm(VmRegistry vmr, DatacenterBroker broker) {
        CloudletScheduler scheduler = PolicyLoader.cloudletScheduler(vmr);
        final Vm vm = new VmSimple(vmr.getMips(), vmr.getPes());
        vm
                .setRam(vmr.getRam())
                .setBw(vmr.getBw())
                .setSize(vmr.getSize())
                .setCloudletScheduler(scheduler)
                .setBroker(broker);
        return vm;
    }

    private Cloudlet createCloudlet(CloudletRegistry cloudletr, DatacenterBroker broker) {
        UtilizationModel cpuUtilization = PolicyLoader.utilizationModel(cloudletr.getUtilizationModelCpu());
        UtilizationModel ramUtilization = PolicyLoader.utilizationModel(cloudletr.getUtilizationModelRam());
        UtilizationModel bwUtilization = PolicyLoader.utilizationModel(cloudletr.getUtilizationModelBw());

        final Cloudlet cloudlet = new CloudletSimple(cloudletr.getLength(), cloudletr.getPes());
        cloudlet
                .setFileSize(cloudletr.getFileSize())
                .setOutputSize(cloudletr.getOutputSize())
                .setUtilizationModelCpu(cpuUtilization)
                .setUtilizationModelRam(ramUtilization)
                .setUtilizationModelBw(bwUtilization)
                .setBroker(broker);
        return cloudlet;
    }

}
