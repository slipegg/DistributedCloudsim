package com.companyname.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DatacenterInfo {
    public List<HostInfo> hosts = new ArrayList<>();

    public DatacenterInfo() {

    }

    public DatacenterInfo(List<HostInfo> hosts) {
        this.hosts = hosts;
    }
}
