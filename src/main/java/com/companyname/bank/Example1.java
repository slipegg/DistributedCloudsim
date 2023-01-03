package com.companyname.bank;

import com.esotericsoftware.yamlbeans.YamlException;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.ResourceLoader;
import org.cloudsimplus.automation.CloudSimulation;
import org.cloudsimplus.automation.YamlCloudScenario;
import org.cloudsimplus.automation.YamlCloudScenarioReader;
import cloudreports.models.*;

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

/**
 * Starts the example, parsing a YAML file containing
 * the simulation scenario, building and running it
 * in CloudSim Plus.
 *
 * @author Manoel Campos da Silva Filho
 */
public class Example1 {
    private Example1() {
        System.out.printf("Starting %s on %s%n", getClass().getSimpleName(), CloudSim.VERSION);

        // Gets the path to the YAML file inside the resource directory.
        final String yamlFilePath = ResourceLoader.getResourcePath(getClass(), "CloudEnvironment1.yml");
        try {
            // Loads the YAML file containing 1 or more simulation scenarios.
            final YamlCloudScenarioReader reader = new YamlCloudScenarioReader(yamlFilePath);
            // Gets the list or parsed scenarios.
            final List<YamlCloudScenario> simulationScenarios = reader.getScenarios();
            final int datacenterNumber = simulationScenarios.get(0).getDatacenters().stream()
                    .mapToInt(DatacenterRegistry::getAmount).sum();
            YamlCloudScenario ss = simulationScenarios.get(0);
            // List<CustomerRegistry> rcusts=ss.getCustomers();
            // CustomerRegistry rcust=rcusts.get(0);
            // List<VmRegistry> vms=rcust.getVms();
            // System.out.println(vms);
            // System.out.println(vms.get(0).getPes());

            List<DatacenterRegistry> drs = simulationScenarios.get(0).getDatacenters();
            DatacenterRegistry dr = drs.get(0);
            System.out.println(dr);

            System.out.println(datacenterNumber);
            // For each existing scenario, creates and runs it in CloudSim Plus, printing
            // results.
            for (YamlCloudScenario scenario : simulationScenarios) {
                new CloudSimulation(scenario).run();
            }
        } catch (FileNotFoundException | YamlException e) {
            System.err
                    .println("Error when trying to load the simulation scenario from the YAML file: " + e.getMessage());
        }
    }

    private void test() {

    }

    public static void main(String[] args) {
        try {
            String testlFilePath = "output.yml";
            HostRegistry hr = new HostRegistry();
            hr.setBw(2333);
            hr.setAmount(2);
            hr.setPes(4);
            hr.setMips(20000);
            hr.setRam(1000);
            hr.setStorage(40000);
            YamlWriter writer = new YamlWriter(new FileWriter(testlFilePath));
            writer.write(hr);
            writer.close();
            System.out.println(hr);
            System.out.println("==============================");

            //////////////////////////////////////
            File file = new File(testlFilePath);
            FileReader fr = new FileReader(file);
            final YamlReader reader = new YamlReader(fr);
            HostRegistry ahr = reader.read(HostRegistry.class);
            System.out.println(ahr);

        } catch (FileNotFoundException e) {
            System.out.println("error! e");
            System.out.println(e);
        } catch (YamlException y) {
            System.out.println("error! y");
            System.out.println(y);
        } catch (IOException o) {
            System.out.println("error! o");
            System.out.println(o);
        }
    }

    public static void main1(String[] args) {
        // new Example1();
        // test();
        try {
            String file_name2 = "CloudEnvironment1.yml";
            String file_name = "test.yml";
            final String testlFilePath = DataCenterSockets.class.getClassLoader().getResource(file_name).getPath();
            File file = new File(testlFilePath);
            FileReader fr = new FileReader(file);
            // final YamlReader reader = new YamlReader(fr);
            // final YamlConfig cfg = reader.getConfig();
            // cfg.setClassTag("datacenter", DatacenterRegistry.class);
            // cfg.setClassTag("customer", CustomerRegistry.class);
            // cfg.setClassTag("san", SanStorageRegistry.class);
            // cfg.setClassTag("hosta", HostRegistry.class);

            BufferedReader br = new BufferedReader(fr);
            String line;
            String ress = "";
            try {
                while ((line = br.readLine()) != null) {
                    // 一行一行地处理...
                    // System.out.println(line);
                    ress = ress + line + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(ress);

            final YamlReader reader = new YamlReader(ress);
            final YamlConfig cfg = reader.getConfig();
            cfg.setClassTag("san", SanStorageRegistry.class);
            cfg.setClassTag("hosta", HostRegistry.class);

            // cfg.setClassTag("cloudlet", CloudletRegistry.class);
            // cfg.setClassTag("vm", VmRegistry.class);
            DatacenterRegistry res = reader.read(DatacenterRegistry.class);
            System.out.println(res);
            System.out.println(res.getClass().getName());
        } catch (FileNotFoundException e) {
            System.out.println("error! e");
            System.out.println(e);
        } catch (YamlException y) {
            System.out.println("error! y");
            System.out.println(y);
        }
    }
}
