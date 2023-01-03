package com.companyname.bank;

import com.zaxxer.hikari.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.datacenters.Datacenter;

import com.companyname.models.*;

public class SqlManager {
    HikariConfig config = null;
    HikariDataSource ds = null;
    String datacenter_name;

    public SqlManager(String datacenter_name) {
        this.datacenter_name = datacenter_name;
        config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://119.8.33.152:3306/cloudsimPlus");
        config.setUsername("root");
        config.setPassword("root1234");
        config.addDataSourceProperty("connectionTimeout", "2000"); // 连接超时：1秒
        config.addDataSourceProperty("idleTimeout", "60000"); // 空闲超时：60秒
        config.addDataSourceProperty("maximumPoolSize", "50"); // 最大连接数：10
        ds = new HikariDataSource(config);

        createHistoryTable(datacenter_name);
        createTask2Table(datacenter_name);
    }

    public void test() {
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {

            // 创建connection
            conn = ds.getConnection();
            statement = conn.createStatement();

            // 执行sql
            rs = statement.executeQuery("select *  from c1_history");

            // 取数据
            if (rs.next()) {
                System.out.println(rs.getString("free_mips"));
            }

            // 关闭connection
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DatacenterInfo getDataCenterInfo(String datacenter_name, double before_time) {
        DatacenterInfo datacenter_info = new DatacenterInfo();
        String sql;
        try {
            Connection connection = ds.getConnection();
            Statement statement = connection.createStatement();
            sql = "select max(run_time) from " + datacenter_name + "_history where run_time<="
                    + String.valueOf(before_time);
            // System.out.println(sql);
            ResultSet resultSet0 = statement.executeQuery(sql);
            double rtime = 0;
            while (resultSet0.next()) {
                rtime = resultSet0.getDouble("max(run_time)");
            }

            sql = "select * from " + datacenter_name + "_history where run_time=" + String.valueOf(rtime);
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Integer host_id = resultSet.getInt("host_id");
                Double run_time = resultSet.getDouble("run_time");
                Integer ram = resultSet.getInt("ram");
                Integer bw = resultSet.getInt("bw");
                Integer storage = resultSet.getInt("storage");
                Integer free_pes = resultSet.getInt("free_pes");
                Double free_mips = resultSet.getDouble("free_mips");
                HostInfo host_info = new HostInfo(host_id, run_time, ram, bw, storage, free_pes, free_mips);
                datacenter_info.hosts.add(host_info);
            }

            if (statement != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datacenter_info;
    }

    public void createHistoryTable(String datacenterName) {
        try {
            Connection connection = ds.getConnection();
            Statement statement0 = connection.createStatement();
            statement0.executeUpdate(String.format("drop table if exists `%s_history`", datacenterName));

            Statement statement = connection.createStatement();
            String sql = String.format("""
                    CREATE TABLE IF NOT EXISTS `%s_history`(
                        `id` INT UNSIGNED AUTO_INCREMENT,
                        `host_id` INTEGER  NOT NULL,
                        `run_time` DOUBLE NOT NULL,
                        `ram` INTEGER NOT NULL,
                        `bw` INTEGER NOT NULL,
                        `storage` INTEGER NOT NULL,
                        `free_pes` INTEGER NOT NULL,
                        `free_mips` DOUBLE NOT NULL,
                        PRIMARY KEY ( `id` )
                     )ENGINE=InnoDB DEFAULT CHARSET=utf8;
                        """, datacenterName);
            statement.executeUpdate(sql);
            if (statement != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertHostInfoIntoTable(String datacenterName, HostInfo hostInfo) {
        try {
            Connection connection = ds.getConnection();
            Statement statement = connection.createStatement();
            String sql = String.format("""
                    insert into %s_history(host_id, run_time ,ram,bw,storage,free_pes,free_mips)
                    values (%d,%f,%d,%d,%d,%d,%f);
                    """, datacenterName, hostInfo.host_id, hostInfo.run_time, hostInfo.ram, hostInfo.bw,
                    hostInfo.storage,
                    hostInfo.free_pes, hostInfo.free_mips);
            statement.executeUpdate(sql);
            if (statement != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTask2Table(String datacenterName) {
        try {
            Connection connection = ds.getConnection();
            Statement statement0 = connection.createStatement();
            statement0.executeUpdate(String.format("drop table if exists `task2%s`", datacenterName));

            Statement statement = connection.createStatement();
            String sql = String.format("""
                    CREATE TABLE IF NOT EXISTS `task2%s`(
                        `id` INT UNSIGNED AUTO_INCREMENT,
                       `datacenter_name` varchar(255) not null,
                       `task_index` INTEGER not null,
                       `amount` INTEGER not null,
                       `send_time` double not null,
                       `is_get` boolean default false,
                        PRIMARY KEY ( `id` )
                     )ENGINE=InnoDB DEFAULT CHARSET=utf8;
                            """, datacenterName);
            statement.executeUpdate(sql);
            if (statement != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertTaskIntoTable(String target_datacenterName, int index, int amount,
            double send_time) {
        try {
            Connection connection = ds.getConnection();
            Statement statement = connection.createStatement();
            String sql = String.format("""
                    insert into task2%s (datacenter_name,task_index,amount,send_time) values ("%s",%d,%d,%f);
                    """, target_datacenterName, datacenter_name, index, amount, send_time);
            // System.out.println(sql);
            statement.executeUpdate(sql);
            if (statement != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RecvTaskInfo> getTasks(String datacenter_name, double time) {
        List<RecvTaskInfo> res = new ArrayList<>();
        try {
            Connection connection = ds.getConnection();
            Statement statement = connection.createStatement();

            String sql = String.format("select * from task2%s where send_time<%f and is_get=false",
                    datacenter_name,
                    time);
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String send_datacenter_name = resultSet.getString("datacenter_name");
                Integer index = resultSet.getInt("task_index");
                Integer amount = resultSet.getInt("amount");
                RecvTaskInfo tmp = new RecvTaskInfo(send_datacenter_name, index, amount);
                res.add(tmp);
            }

            // Connection connection2 = ds.getConnection();
            // Statement statement2 = connection2.createStatement();
            String sql2 = String.format("update task2%s set is_get=true where send_time<%f and is_get=false",
                    datacenter_name, time);
            statement.executeUpdate(sql2);
            if (statement != null) {
                connection.close();
            }
            // if (statement2 != null) {
            // connection2.close();
            // }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) {
        SqlManager s = new SqlManager("c2");
        // DatacenterInfo dinfo = s.getDataCenterInfo("c1", 20);
        // System.out.println(dinfo.hosts.get(0).free_mips);
        // s.createHistoryTable("c2");

        // HostInfo hi = new HostInfo(11, 25.6, 23333, 66666, 888, 111, 222.33);
        // s.insertHostInfoIntoTable("c1", hi);

        // s.createTask2Table("c2");
        s.insertTaskIntoTable("c1", 0, 1, 7.6);
        // s.insertTaskIntoTable("c1", "c2", 110, 2, 7.6);

        // List<RecvTaskInfo> res = s.getTasks("c2", 30);
        // for (RecvTaskInfo r : res) {
        // System.out.println(r.datacenter_name);
        // System.out.println(r.index);
        // System.out.println(r.amount);
    }
}
