package com.eastobacco.hbase.operator;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

public class HbaseFunction {
    public final static String COLUMN_PREDICT_FAMILY = "predicate";
    public final static String PREDICT_VALUE = "predicateValue";

    public final static String PROCESS_NAME = "MODEL_ZS_5K_LAP5_HC";
    public final static String BATCHID = "";
    public Connection conn;
    public HbaseFunction(Connection conn){
        this.conn = conn;
    }
    /**
     * 创建只有一个列簇的表
     * @throws Exception
     */
    public void createTable() throws Exception{
        Admin admin = conn.getAdmin();
        if (!admin.tableExists(TableName.valueOf("test"))){
            TableName tableName = TableName.valueOf("test");
            //表描述器构造器
            TableDescriptorBuilder tdb = TableDescriptorBuilder.newBuilder(tableName);
            //列族描述器构造器
            ColumnFamilyDescriptorBuilder cdb = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("user"));
            //获得列描述器
            ColumnFamilyDescriptor cfd = cdb.build();
            //添加列族
            tdb.setColumnFamily(cfd);
            //获得表描述器
            TableDescriptor td = tdb.build();
            //创建表
            admin.createTable(td);
        }else {
            System.out.println("表已存在");
        }
        //关闭连接
    }
    /**
     * 添加数据（多个rowKey，多个列族）
     * @throws Exception
     */
    public void insertMany() throws Exception{
        Table table = conn.getTable(TableName.valueOf("test"));
        List<Put> puts = new ArrayList<Put>();
        Put put1 = new Put(Bytes.toBytes("rowKey1"));
        put1.addColumn(Bytes.toBytes("user"), Bytes.toBytes("name"), Bytes.toBytes("wd"));

        Put put2 = new Put(Bytes.toBytes("rowKey2"));
        put2.addColumn(Bytes.toBytes("user"), Bytes.toBytes("age"), Bytes.toBytes("25"));

        Put put3 = new Put(Bytes.toBytes("rowKey3"));
        put3.addColumn(Bytes.toBytes("user"), Bytes.toBytes("weight"), Bytes.toBytes("60kg"));

        Put put4 = new Put(Bytes.toBytes("rowKey4"));
        put4.addColumn(Bytes.toBytes("user"), Bytes.toBytes("sex"), Bytes.toBytes("男"));

        puts.add(put1);
        puts.add(put2);
        puts.add(put3);
        puts.add(put4);
        table.put(puts);
        table.close();
    }
    /**
     * 根据rowKey删除一行数据、或者删除某一行的某个列簇，或者某一行某个列簇某列
     * @param tableName
     * @param rowKey
     * @throws Exception
     */
    public void deleteData(TableName tableName, String rowKey, String columnFamily, String columnName) throws Exception{
        Table table = conn.getTable(tableName);
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        //①根据rowKey删除一行数据
        table.delete(delete);

        //②删除某一行的某一个列簇内容
        delete.addFamily(Bytes.toBytes(columnFamily));

        //③删除某一行某个列簇某列的值
        delete.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
        table.close();
    }
    /**
     * 根据RowKey , 列簇， 列名修改值
     * @param tableName
     * @param rowKey
     * @param columnFamily
     * @param columnName
     * @param columnValue
     * @throws Exception
     */
    public void updateData(TableName tableName, String rowKey, String columnFamily, String columnName, String columnValue) throws Exception{
        Table table = conn.getTable(tableName);
        Put put1 = new Put(Bytes.toBytes(rowKey));
        put1.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(columnValue));
        table.put(put1);
        table.close();
    }
    /**
     * 根据rowKey查询数据
     * @param tableName
     * @param rowKey
     * @throws Exception
     */
    public void getResult(TableName tableName, String rowKey) throws Exception{
        Table table = conn.getTable(tableName);
        //获得一行
        Get get = new Get(Bytes.toBytes(rowKey));
        Result set = table.get(get);
        Cell[] cells = set.rawCells();
        for (Cell cell: cells){
            System.out.println(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()) + "::" +
                    Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
        }
        table.close();
    }

//过滤器 LESS <  LESS_OR_EQUAL <=   EQUAL =   NOT_EQUAL <>   GREATER_OR_EQUAL >=   GREATER >   NO_OP 排除所有

    /**
     * @param tableName
     * @throws Exception
     */
    public  void scanTable(TableName tableName) throws Exception{
        Table table = conn.getTable(tableName);

        //①全表扫描
        Scan scan1 = new Scan();
        ResultScanner rscan1 = table.getScanner(scan1);

        //②rowKey过滤器
        //generateRowKey ：PROCESS_NAME + "_" + modelOutputMsg.getModelInputMsg().getBatchId() + "_" + modelOutputMsg.getModelInputMsg().getDate().getTime();

        Scan scan2 = new Scan();
        //str$ 末尾匹配，相当于sql中的 %str  ^str开头匹配，相当于sql中的str%
        RowFilter filter = new RowFilter(CompareOperator.EQUAL, new RegexStringComparator(PROCESS_NAME+"_"+BATCHID));
        scan2.setFilter(filter);
        ResultScanner rscan2 = table.getScanner(scan2);

        //③列值过滤器
        Scan scan3 = new Scan();
        //下列参数分别为列族，列名，比较符号，值
        SingleColumnValueFilter filter3 = new SingleColumnValueFilter(Bytes.toBytes(COLUMN_PREDICT_FAMILY), Bytes.toBytes(PREDICT_VALUE),
                CompareOperator.NOT_EQUAL, Bytes.toBytes(""));
        scan3.setFilter(filter3);
        ResultScanner rscan3 = table.getScanner(scan3);

        //列名前缀过滤器
        Scan scan4 = new Scan();
        ColumnPrefixFilter filter4 = new ColumnPrefixFilter(Bytes.toBytes(PREDICT_VALUE));
        scan4.setFilter(filter4);
        ResultScanner rscan4 = table.getScanner(scan4);

        //过滤器集合
        Scan scan5 = new Scan();
        FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        SingleColumnValueFilter filter51 = new SingleColumnValueFilter(Bytes.toBytes(COLUMN_PREDICT_FAMILY), Bytes.toBytes(PREDICT_VALUE),
                CompareOperator.EQUAL, Bytes.toBytes("spark"));
        ColumnPrefixFilter filter52 = new ColumnPrefixFilter(Bytes.toBytes(PREDICT_VALUE));
        list.addFilter(filter51);
        list.addFilter(filter52);
        scan5.setFilter(list);
        ResultScanner rscan5 = table.getScanner(scan5);

        //todo 根据需求update rscan
        for (Result rs : rscan4){
            String rowKey = Bytes.toString(rs.getRow());
            System.out.println("row key :" + rowKey);
            Cell[] cells = rs.rawCells();
            for (Cell cell: cells){
                System.out.println(Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength()) + "::"
                        + Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()) + "::"
                        + Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
            System.out.println("-------------------------------------------");
        }
    }
}
