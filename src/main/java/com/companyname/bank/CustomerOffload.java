package com.companyname.bank;

import java.util.Random;
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

import java.util.ArrayList;
import java.util.List;
import com.companyname.models.*;

public class CustomerOffload {
    public List<CustomerRegistry> local_run_customers;
    private SqlManager sqlManager;
    private List<String> other_datacenter_name;
    private DataCenterDeviceManager datacenter_manager;
    private CustomerDemandManager customer_manager;
    private DataCenterSockets dataCenterSockets;
    private String datacenter_name;
    private double history_delay = 5.0;
    int index = 0;

    public CustomerOffload(DataCenterDeviceManager datacenter_manager,
            CustomerDemandManager customer_manager,
            SqlManager sqlManager,
            DataCenterSockets dataCenterSockets) {
        this.datacenter_name = datacenter_manager.getDatacenter_name();
        this.sqlManager = sqlManager;
        this.other_datacenter_name = dataCenterSockets.getOtherDatacenterNames();
        this.datacenter_manager = datacenter_manager;
        this.dataCenterSockets = dataCenterSockets;
        this.customer_manager = customer_manager;
    }

    void getFutherCustomer(CloudSim simulation, double now_time) {
        List<RecvTaskInfo> recvTaskInfo = sqlManager.getTasks(datacenter_name, now_time);
        customer_manager.createVmsAndCloudletsFromOthers(recvTaskInfo, simulation);
    }

    void randomOffload(CloudSim simulation, double now_time) {
        int runned_customer_index = customer_manager.getRunned_customer_index();
        CustomerRegistrys customerRegistrys = customer_manager.getNowCustomerRegistrys(now_time);

        Random r = new Random(10);
        for (int i = 0; i < customerRegistrys.getCustomers().size(); i++, runned_customer_index++) {
            CustomerRegistry customer = customerRegistrys.getCustomers().get(i);
            for (int j = 0; j < customer.getAmount(); j++) {
                index = r.nextInt(1 + other_datacenter_name.size());
                System.out.println("random index:" + Integer.toString(index));
                if (index == 0) {
                    System.out.println(
                            customer.getName() + "第" + Integer.toString(j) + "个，卸载在本地数据中心" + datacenter_name + "\n");
                    customer_manager.createVmsAndCloudletsLocallyByCustomerRegistry(customer, simulation, j);
                } else {
                    System.out.println(
                            customer.getName() + "第" + Integer.toString(j) + "个，卸载到其他数据中心"
                                    + other_datacenter_name.get(index - 1) + "\n");
                    sqlManager.insertTaskIntoTable(other_datacenter_name.get(index - 1), runned_customer_index, 1,
                            now_time);
                }
            }
        }
    }

    private double calculate_price(DatacenterRegistry datacenterRegistry, CustomerRegistry customer) {
        double costPerMem = datacenterRegistry.getCostPerMem();
        double costPerStorage = datacenterRegistry.getCostPerStorage();
        double costPerBw = datacenterRegistry.getCostPerBw();

        double price = 0.0;
        for (VmRegistry vm : customer.getVms()) {
            price += vm.getAmount()
                    * (vm.getRam() * costPerMem + vm.getSize() * costPerStorage + vm.getBw() * costPerBw);
        }
        return price;
    }

    void cheapesOffload(CloudSim simulation, double now_time) {
        int runned_customer_index = customer_manager.getRunned_customer_index();
        CustomerRegistrys customerRegistrys = customer_manager.getNowCustomerRegistrys(now_time);

        for (int i = 0; i < customerRegistrys.getCustomers().size(); i++, runned_customer_index++) {
            CustomerRegistry customer = customerRegistrys.getCustomers().get(i);
            for (int j = 0; j < customer.getAmount(); j++) {
                double min_price = 0;
                min_price = calculate_price(datacenter_manager.datacenterRegistry, customer);
                String cheapest_datacenter_name = datacenter_manager.getDatacenter_name();
                for (String datacenter_name : other_datacenter_name) {
                    DatacenterRegistry tmp = datacenter_manager.other_datacenterRegistries.get(datacenter_name);
                    double res = calculate_price(tmp, customer);
                    if (res < min_price) {
                        min_price = res;
                        cheapest_datacenter_name = datacenter_name;
                    }
                }
                if (cheapest_datacenter_name.equals(datacenter_manager.getDatacenter_name())) {
                    System.out.println(
                            customer.getName() + "卸载在本地数据中心" + datacenter_name + "\n");
                    customer_manager.createVmsAndCloudletsLocallyByCustomerRegistry(customer, simulation,
                            j);
                } else {
                    System.out.println(
                            customer.getName() + "卸载到其他数据中心"
                                    + cheapest_datacenter_name + "\n");
                    sqlManager.insertTaskIntoTable(cheapest_datacenter_name, runned_customer_index,
                            customer.getAmount(),
                            now_time);
                }
            }
        }
    }

    private boolean isSuit(String datacenter_name, CustomerRegistry customer, double now_time, boolean isLocal) {
        DatacenterInfo info;
        if (isLocal) {
            info = sqlManager.getDataCenterInfo(datacenter_name, now_time);
        } else {
            info = sqlManager.getDataCenterInfo(datacenter_name, now_time - history_delay);
        }
        if (info.hosts.size() == 0 && isLocal) {
            return true;
        } else if (info.hosts.size() == 0 && !isLocal) {
            return false;
        }
        boolean issuit = true;
        for (VmRegistry vm : customer.getVms()) {
            boolean issuitone = false;
            for (int i = 0; i < info.hosts.size(); i++) {
                HostInfo hinfo = info.hosts.get(i);
                if (hinfo.ram >= vm.getRam() && hinfo.storage >= vm.getSize() && hinfo.bw >= vm.getBw()
                        && hinfo.free_mips >= vm.getMips()) {
                    hinfo.ram -= vm.getRam();
                    hinfo.storage -= vm.getSize();
                    hinfo.bw -= vm.getBw();
                    hinfo.free_mips -= vm.getMips();
                    issuitone = true;
                    break;
                }
            }
            if (!issuitone) {
                issuit = false;
                break;
            }
        }
        return issuit;
    }

    void suitOffload(CloudSim simulation, double now_time) {
        int runned_customer_index = customer_manager.getRunned_customer_index();
        CustomerRegistrys customerRegistrys = customer_manager.getNowCustomerRegistrys(now_time);
        for (int i = 0; i < customerRegistrys.getCustomers().size(); i++, runned_customer_index++) {
            CustomerRegistry customer = customerRegistrys.getCustomers().get(i);
            for (int j = 0; j < customer.getAmount(); j++) {
                if (isSuit(datacenter_manager.getDatacenter_name(), customer, now_time, true)) {
                    System.out.println(
                            customer.getName() + "第" + Integer.toString(j) + "个" + "卸载在本地数据中心" + datacenter_name
                                    + "\n");
                    customer_manager.createVmsAndCloudletsLocallyByCustomerRegistry(customer, simulation,
                            j);
                } else {
                    for (String datacenter_name : other_datacenter_name) {
                        if (isSuit(datacenter_name, customer, now_time, false)) {
                            System.out.println(
                                    customer.getName() + "第" + Integer.toString(j) + "个" + "卸载到其他数据中心"
                                            + datacenter_name + "\n");
                            sqlManager.insertTaskIntoTable(datacenter_name, runned_customer_index,
                                    1,
                                    now_time);
                        }
                    }
                }
            }
        }
    }
}
