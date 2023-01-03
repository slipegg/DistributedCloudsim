package com.companyname.bank;

import com.companyname.models.CloudletRegistry;
import com.companyname.models.CustomerRegistry;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.*;

import java.util.Random;

public class test3 {
    public static void main(String[] args) {
        Random r = new Random(10);
        for (int j = 0; j < 10; j++) {
            int i = r.nextInt(2);
            System.out.println(i);
        }
        // CustomerDemandManager cd = new CustomerDemandManager("customers.yml");
        // CustomerRegistrys cr = cd.getCustomerRegistrys();
        // System.out.println("===========");
        // System.out.println(cr.getCustomers());
        // DataCenterSockets c1 = new DataCenterSockets("DataCenterNetwork.yml", "c1");
        // DataCenterDeviceManager datacenter_manager = new
        // DataCenterDeviceManager("c1_device.yml");
    }
}
