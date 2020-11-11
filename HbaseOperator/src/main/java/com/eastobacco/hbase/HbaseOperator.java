package com.eastobacco.hbase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eastobacco.hbase.operator.HbaseFunction;


public class HbaseOperator {
    private static final Logger logger = LoggerFactory.getLogger(HbaseOperator.class);
    public final static String TABLE_NAME = "IOT:DEVICE_PREDICT_CONTROL_INFO";

    public static Configuration conf = null;
    public static Connection conn = null;

    public static void main(String[] args) throws Exception {
        /**
         * 声明静态配置:连接HBase
         */
            conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", "10.100.100.102:2181,10.100.100.103:2181,10.100.100.104:2181");
            conf.set("hbase.zookeeper.property.client", "2181");
            try{
                conn = ConnectionFactory.createConnection(conf);
            }catch (Exception e){
                e.printStackTrace();
            }
        TableName table = TableName.valueOf(TABLE_NAME);

            //query Hbase
        HbaseFunction hbaseFunction = new HbaseFunction(conn);
        hbaseFunction.scanTable(table);

    }

}
