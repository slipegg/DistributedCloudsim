package com.companyname.models;

import java.io.Serializable;

public class DataCenterNetwork implements Serializable {
    private String name;
    private String ip;
    private Integer port;
    private String basic_info;

    public String getName() {
        return name;
    }

    public String setName(String name) {
        return this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public String setIp(String ip) {
        return this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public Integer setPort(Integer port) {
        return this.port = port;
    }

    public String getBasic_info() {
        return basic_info;
    }

    public String setBasic_info(String basic_info) {
        return this.basic_info = basic_info;
    }
}
