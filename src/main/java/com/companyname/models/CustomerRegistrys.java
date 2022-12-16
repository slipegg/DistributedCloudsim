package com.companyname.models;

import java.io.Serializable;
import java.util.List;
import com.companyname.models.*;
// import cloudreports.models.*;

public class CustomerRegistrys implements Serializable {
    private List<CustomerRegistry> customers;

    public List<CustomerRegistry> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerRegistry> customers) {
        this.customers = customers;
    }

}
