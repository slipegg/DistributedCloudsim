package com.companyname.models;

public class RecvTaskInfo {
    public String datacenter_name;
    public int index;
    public int amount;

    public RecvTaskInfo() {

    }

    public RecvTaskInfo(String datacenter_name, int index, int amount) {
        this.datacenter_name = datacenter_name;
        this.index = index;
        this.amount = amount;
    }
}
