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
import org.cloudbus.cloudsim.vms.VmCost;

import java.util.List;
// import cloudreports.models.*;
import com.companyname.models.*;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomerDemandManager {
    String file_context = "";
    CustomerRegistrys customerRegistrys;
    List<DatacenterBroker> brokers = new ArrayList<>();
    Integer runned_customer_index = 0;
    public Map<String, CustomerRegistrys> other_customerRegistrys = new HashMap<>();

    private CustomerRegistrys getCustomerRegistrysByFile(String file_name) {
        CustomerRegistrys res = new CustomerRegistrys();
        // 读取任务需求的配置文件
        try {
            final String FilePath = DataCenterSockets.class.getClassLoader().getResource(file_name)
                    .getPath();
            File file = new File(FilePath);
            FileReader fr = new FileReader(file);

            final YamlReader reader = new YamlReader(fr);
            final YamlConfig cfg = reader.getConfig();
            cfg.setClassTag("customer", CustomerRegistry.class);
            cfg.setClassTag("vm", VmRegistry.class);
            cfg.setClassTag("cloudlet", CloudletRegistry.class);

            res = reader.read(CustomerRegistrys.class);
            // 按submit time排序
            Collections.sort(res.getCustomers());

            // System.out.println("customerRegistrys:\n" +
            // customerRegistrys.getCustomers());

        } catch (FileNotFoundException e) {
            System.out.println("error! e");
            System.out.println(e);
        } catch (YamlException y) {
            System.out.println("error! y");
            System.out.println(y);
        }
        return res;
    }

    public CustomerDemandManager(String datacenter_name, List<String> other_datacenter_name) {
        String file_back_str = "_customers.yml";
        String file_name = datacenter_name + file_back_str;
        System.out.println(file_name);
        customerRegistrys = getCustomerRegistrysByFile(file_name);
        for (var name : other_datacenter_name) {
            file_name = name + file_back_str;
            other_customerRegistrys.put(name, getCustomerRegistrysByFile(file_name));
        }
    }

    public CustomerRegistrys getCustomerRegistrys() {
        return customerRegistrys;
    }

    public int getRunned_customer_index() {
        return runned_customer_index;
    }

    public CustomerRegistrys getNowCustomerRegistrys(Double now_time) {
        List<CustomerRegistry> customers = customerRegistrys.getCustomers();
        List<CustomerRegistry> tmp = new ArrayList<>();
        CustomerRegistrys res = new CustomerRegistrys();
        for (; customers != null && runned_customer_index < customers.size()
                && customers.get(runned_customer_index).getSubmit_time() <= now_time; runned_customer_index++) {
            CustomerRegistry customer = customers.get(runned_customer_index);
            tmp.add(customer);
        }
        res.setCustomers(tmp);
        return res;
    }

    public void createVmsAndCloudlets(CloudSim simulation, Double now_time) {
        List<CustomerRegistry> customers = customerRegistrys.getCustomers();
        for (; customers != null && runned_customer_index < customers.size()
                && customers.get(runned_customer_index).getSubmit_time() <= now_time; runned_customer_index++) {
            CustomerRegistry customer = customers.get(runned_customer_index);
            for (int customer_amount = 0; customer_amount < customer.getAmount(); customer_amount++) {
                DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
                broker.setVmDestructionDelay(simulation.getMinTimeBetweenEvents() + 0.1);// VM用完就丢，这里需要斟酌
                submitVmsAndCloudletsForOneCustomer(broker, customer);
                broker.setName(customer.getName() + "类第" + Integer.toString(customer_amount) + "个");
                brokers.add(broker);
            }
        }
    }

    public void createVmsAndCloudletsLocallyByCustomerRegistry(CustomerRegistry customer, CloudSim simulation,
            int amount_index) {
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        broker.setVmDestructionDelay(simulation.getMinTimeBetweenEvents() + 0.1);// VM用完就丢，这里需要斟酌
        submitVmsAndCloudletsForOneCustomer(broker, customer);
        broker.setName(customer.getName() + "类第"
                + Integer.toString(amount_index) + "个");
        brokers.add(broker);
    }

    public void createVmsAndCloudletsFromOthers(List<RecvTaskInfo> recvTaskInfos, CloudSim simulation) {
        for (RecvTaskInfo recvTaskInfo : recvTaskInfos) {
            CustomerRegistry customer = other_customerRegistrys.get(recvTaskInfo.datacenter_name).getCustomers()
                    .get(recvTaskInfo.index);
            customer.setAmount(recvTaskInfo.amount);
            for (int customer_amount = 0; customer_amount < customer.getAmount(); customer_amount++) {
                DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
                broker.setVmDestructionDelay(simulation.getMinTimeBetweenEvents() + 0.1);// VM用完就丢，这里需要斟酌
                submitVmsAndCloudletsForOneCustomer(broker, customer);
                broker.setName("从" + recvTaskInfo.datacenter_name + "来的" + customer.getName() + "类第"
                        + Integer.toString(customer_amount) + "个");
                brokers.add(broker);
            }
        }
    }

    public void showResults() {
        for (DatacenterBroker broker : brokers) {
            final List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
            new CloudletsTableBuilder(finishedCloudlets).setTitle(broker.getName()).build();
        }
        for (DatacenterBroker broker : brokers) {
            System.out.println();
            double totalCost = 0.0;
            int totalNonIdleVms = 0;
            double processingTotalCost = 0, memoryTotaCost = 0, storageTotalCost = 0, bwTotalCost = 0;
            for (final Vm vm : broker.getVmCreatedList()) {
                final VmCost cost = new VmCost(vm);
                processingTotalCost += cost.getProcessingCost();
                memoryTotaCost += cost.getMemoryCost();
                storageTotalCost += cost.getStorageCost();
                bwTotalCost += cost.getBwCost();

                totalCost += cost.getTotalCost();
                totalNonIdleVms += vm.getTotalExecutionTime() > 0 ? 1 : 0;
                System.out.println(cost);
            }
            System.out.printf(
                    "%s total cost ($) for %3d VMs in %3d created VMs : %8.2f$ %13.2f$ %17.2f$ %12.2f$ %15.2f$%n",
                    broker.getName(), totalNonIdleVms, broker.getVmsNumber(),
                    processingTotalCost, memoryTotaCost, storageTotalCost, bwTotalCost, totalCost);
        }

    }

    public String getFileContext() {
        return file_context;
    }

    public boolean isSubmitAllCustomer() {
        return runned_customer_index >= customerRegistrys.getCustomers().size();
    }

    public List<DatacenterBroker> getBrokers() {
        return brokers;
    }

    private void submitVmsAndCloudletsForOneCustomer(DatacenterBroker broker, CustomerRegistry customer) {
        List<Vm> vm_list = createVmListForOneBroker(broker, customer);
        List<Cloudlet> cloudlet_list = createCloudletsListForOneBroker(broker, customer);
        // System.out.println("vm_list:\n" + vm_list.size());
        // System.out.println("cloudlet_list:\n" + cloudlet_list.size());
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
        // TODO(2022.12.17) 更多的动态资源使用率如何实现
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
