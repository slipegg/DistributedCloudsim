package com.companyname.models;

public class HostInfo {
    public long host_id;
    public double run_time;
    public long ram;
    public long bw;
    public long storage;
    public int free_pes;
    public double free_mips;

    public HostInfo() {

    }

    public HostInfo(long host_id, double run_time, long ram, long bw, long storage, int free_pes, double free_mips) {
        this.host_id = host_id;
        this.run_time = run_time;
        this.ram = ram;
        this.bw = bw;
        this.storage = storage;
        this.free_pes = free_pes;
        this.free_mips = free_mips;
    }
}
