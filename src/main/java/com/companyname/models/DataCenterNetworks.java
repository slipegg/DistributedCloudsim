package com.companyname.models;

import java.io.Serializable;
import java.util.List;

public class DataCenterNetworks implements Serializable {
    private List<DataCenterNetwork> datacenternetworks;

    public List<DataCenterNetwork> getDataCenterNetworks() {
        return datacenternetworks;
    }

    public void setDataCenterNetworks(List<DataCenterNetwork> datacenternetworks) {
        this.datacenternetworks = datacenternetworks;
    }
}
