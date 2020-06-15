package code;

import com.alibaba.excel.metadata.Sheet;
import org.springframework.util.CollectionUtils;
import util.ExcelUtil;
import util.GetColumns;
import util.GetFileLineNum;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class ImportXlsOrXlsx {



    /**
     * oracle导入excel 表要对应 oracle我设置的是主键自增的
     * 方法中每次插入oracle的时候都需要重新获取连接 和 mysql的不太一样 oracle如果使用一个连接 然后不断插入 会有游标的问题
     * oracle批量插入的语句如下  需要遍历的时候拼接 into TableName(column1,column2···) valuse ('value1','value2'，···) 注意后边有空格分割服
     * insert all into Student(id, name, sex, age, tel)
     *     into Student(id, name, sex, age, tel) values ('12', 'jack1', '男', 12, '13345674567' )
     *     into Student(id, name, sex, age, tel) values ('13', 'jack2', '男', 13, '13345674567')
     *  select * from dual;
     *
     * @throws Exception
     */
    public static void ImportOracle() throws Exception{
        String ip = "localhost";
        String port = "1521";
        String database = "orcl";
        String username = "admin";
        String password = "123456";
        String tableName = "t_dict";
        String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + "orcl";
        //文件存放地址
        String path = "C:/Users/Administrator/Desktop/t_dictionary.xls";

        List<String> columns = GetColumns.getExcelColumn(path);
        //拼接字段
        StringBuffer sbf = new StringBuffer();
        sbf.append(" ( ");
        for (String m : columns) {
            sbf.append(m);
            sbf.append(",");
        }
        sbf.deleteCharAt(sbf.length() - 1);
        sbf.append(") values");
        int m = 0;
        int preCount = 10;// 每10次拼接一次
        int times = 0;//次数
        int percentMsg = 0; //导入进度  12%显示12  小数*100取整
        String sql = "";

        List<Integer> sheetList = ExcelUtil.getSheetNoList(path);
        Integer sheetNum = sheetList.size();
        if(sheetNum>1){//读取多个sheet
            int x = 0;//记录循环次数
            int percentBySheet = 0;//每个sheet所占的进度
            for(Integer sheetNo: sheetList){
                times=0;//读完一页times清零
                x++;
                percentBySheet = 100/sheetNum;
                Sheet sheet = new Sheet(sheetNo);
                Map<String, Object> excelMap = ExcelUtil.readExcel(path,sheet);
                Integer rowCount = (Integer) excelMap.get("rowCount");//总行数
                List<Object> objects = (List<Object>) excelMap.get("list");//数据

                List<String> dataList = null;
                StringBuffer sbff = new StringBuffer();

                Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
                Connection conn = (Connection) DriverManager.getConnection(url, username, password);
                Statement st = null;
                String sqlstr = "into "+tableName;
                for(int i=1;i<objects.size();i++){
                    dataList = (List<String>) objects.get(i);
                    sbff.append(sqlstr);
                    sbff.append(sbf.toString());
                    sbff.append(" ( ");
                    for(int j=0;j<columns.size();j++){
                        if(j==0){
                            sbff.append("''");
                        }else{
                            sbff.append("'");
                            sbff.append(dataList.get(j));
                            sbff.append("'");
                        }
                        sbff.append(",");
                    }
                    sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                    sbff.append(") ");
                    m++;
                    if(0==m%preCount){
                        sql = "insert all "+sbff.toString()+" select * from dual";
                        System.out.println(sql);
                        if(null==conn){
                            conn=DriverManager.getConnection(url, username, password);
                        }
                        st = (Statement) conn.createStatement();
                        int row = st.executeUpdate(sql);
                        st.close();
                        st=null;
                        conn.close();
                        conn=null;
                        times++;
                        percentMsg =(x-1)*percentBySheet + (times*preCount)*percentBySheet/rowCount;
                        System.out.println("=======>>>>>>>>>"+percentMsg);
                        sbff.setLength(0);//清空stringbuffer
                    }
                }
                if(sbff.length()>0){
                    sql = "insert all "+sbff.toString()+" select * from dual";
                    System.out.println(sql);
                    if(null==conn){
                        conn=DriverManager.getConnection(url, username, password);
                    }
                    st = (Statement) conn.createStatement();
                    int row = st.executeUpdate(sql);
                    st.close();
                    st=null;
                    conn.close();
                    conn=null;
                    percentMsg = x*percentBySheet;
                    System.out.println("=======>>>>>>>>>"+percentMsg);
                }else{
                    percentMsg = x*percentBySheet;
                    System.out.println("=======>>>>>>>>>"+percentMsg);
                }

            }

        }else{//读取一个sheet
            Map<String, Object> excelMap = ExcelUtil.readExcel(path);
            Integer rowCount = (Integer) excelMap.get("rowCount");//总行数
            List<Object> objects = (List<Object>) excelMap.get("list");//数据
            if(CollectionUtils.isEmpty(objects)){
               return;
            }

            List<String> dataList = null;
            StringBuffer sbff = new StringBuffer();
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
            Connection conn = (Connection) DriverManager.getConnection(url, username, password);
            Statement st = null;
            String sqlstr = "into "+tableName;
            for(int i=1;i<objects.size();i++){
                dataList = (List<String>) objects.get(i);
                sbff.append(sqlstr);
                sbff.append(sbf.toString());
                sbff.append(" ( ");
                for(int j=0;j<columns.size();j++){
                    if(j==0){
                        sbff.append("''");
                    }else{
                        sbff.append("'");
                        sbff.append(dataList.get(j));
                        sbff.append("'");
                    }
                    sbff.append(",");
                }
                sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                sbff.append(") ");
                m++;
                if(0==m%preCount){
                    sql = "insert all "+sbff.toString()+" select * from dual";
                    System.out.println(sql);
                    if(null==conn){
                        conn=DriverManager.getConnection(url, username, password);
                    }
                    st = (Statement) conn.createStatement();
                    int row = st.executeUpdate(sql);
                    st.close();
                    st=null;
                    conn.close();
                    conn=null;
                    times++;
                    percentMsg = (times*preCount)*100/rowCount;
                    System.out.println(percentMsg);
                    sbff.setLength(0);//清空stringbuffer
                }
            }
            if(sbff.length()>0){
                sql = "insert all "+sbff.toString()+" select * from dual";
                System.out.println(sql);
                if(null==conn){
                    conn=DriverManager.getConnection(url, username, password);
                }
                st = (Statement) conn.createStatement();
                int row = st.executeUpdate(sql);
                st.close();
                st=null;
                conn.close();
                conn=null;
                System.out.println("100");
            }else{
                System.out.println("100");
            }
        }

    }

    /**
     * mysql导入excel 表要对应 mysql我设置的是主键自增的
     * mysql批量插入的语句是 insert into TableName(`column1`,`column2`,···) values ('value11','value12',···), ('value21','value22',···), ('value31','value32',···)
     * @throws Exception
     */
    public static void ImportMysql() throws Exception{
        String ip = "localhost";
        String port = "3306";
        String database = "test";
        String username = "root";
        String password = "root";
        String tableName = "t_dictionary";
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Hongkong&allowMultiQueries=true";
        //文件存放地址
        String path = "C:/Users/Administrator/Desktop/t_dictionary.xlsX";


        //拼接字段
        StringBuffer sbf = new StringBuffer();
        sbf.append("(");
        //获取文件第一行 字段
        List<String> columns = GetColumns.getExcelColumn(path);
        for (String m : columns) {
            sbf.append("`");
            sbf.append(m);
            sbf.append("`");
            sbf.append(",");
        }
        sbf.deleteCharAt(sbf.length() - 1);
        sbf.append(") values");
        int preCount = 10;// 每10次拼接一次
        int m = 0;
        int times = 0;//次数
        int percentMsg = 0;  //导入进度 12%显示12  小数*100取整
        String sql = "";

        List<Integer> sheetList = ExcelUtil.getSheetNoList(path);
        Integer sheetNum = sheetList.size();
        if(sheetNum>1){//读取多个sheet
            int x = 0;//记录循环次数
            int percentBySheet = 0;//每个sheet所占的进度
            for(Integer sheetNo: sheetList){
                times = 0;//读完一页之后次数清零
                x++;
                percentBySheet = 100/sheetNum;
                Sheet sheet = new Sheet(sheetNo);
                Map<String, Object> excelMap = ExcelUtil.readExcel(path,sheet);
                Integer rowCount = (Integer) excelMap.get("rowCount");//总行数
                List<Object> objects = (List<Object>) excelMap.get("list");//数据

                List<String> dataList = null;
                StringBuffer sbff = new StringBuffer();

                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = (Connection) DriverManager.getConnection(url, username, password);
                Statement st = null;
                for(int i=1;i<objects.size();i++){
                    dataList = (List<String>) objects.get(i);
                    sbff.append(" ( ");
                    for(int j=0;j<columns.size();j++){
                        if(j==0){
                            sbff.append("0");
                        }else{
                            sbff.append("'");
                            sbff.append(dataList.get(j));
                            sbff.append("'");
                        }
                        sbff.append(",");
                    }
                    sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                    sbff.append("),");
                    m++;
                    if(0==m%preCount){
                        sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                        sql = "insert into "+tableName + sbf.toString()+sbff.toString();
                        System.out.println(sql);

                        st = (Statement) conn.createStatement();
                        int row = st.executeUpdate(sql);
                        times++;
                        percentMsg =(x-1)*percentBySheet + (times*preCount)*percentBySheet/rowCount;
                        System.out.println("=======>>>>>>>>>"+percentMsg);
                        sbff.setLength(0);//清空stringbuffer
                    }
                }
                if(sbff.length()>0){
                    sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                    sql = "insert into "+tableName + sbf.toString()+sbff.toString();
                    System.out.println(sql);
                    st = (Statement) conn.createStatement();
                    int row = st.executeUpdate(sql);
                    percentMsg = x*percentBySheet;
                    System.out.println("=======>>>>>>>>>"+percentMsg);
                }else{
                    percentMsg = x*percentBySheet;
                    System.out.println("=======>>>>>>>>>"+percentMsg);
                }

            }

        }else{//读取一个sheet
            Map<String, Object> excelMap = ExcelUtil.readExcel(path);
            Integer rowCount = (Integer) excelMap.get("rowCount");//总行数
            List<Object> objects = (List<Object>) excelMap.get("list");//数据
            if(CollectionUtils.isEmpty(objects)){
                return;
            }

            List<String> dataList = null;
            StringBuffer sbff = new StringBuffer();
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = (Connection) DriverManager.getConnection(url, username, password);
            Statement st = null;
            for(int i=1;i<objects.size();i++){
                dataList = (List<String>) objects.get(i);
                sbff.append(" ( ");
                for(int j=0;j<columns.size();j++){
                    if(j==0){
                        sbff.append("0");
                    }else{
                        sbff.append("'");
                        sbff.append(dataList.get(j));
                        sbff.append("'");
                    }
                    sbff.append(",");
                }
                sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                sbff.append("),");
                m++;
                if(0==m%preCount){
                    sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                    sql = "insert into "+tableName + sbf.toString()+sbff.toString();
                    System.out.println(sql);

                    st = (Statement) conn.createStatement();
                    int row = st.executeUpdate(sql);
                    times++;
                    percentMsg = (times*preCount)*100/rowCount;
                    System.out.println(percentMsg);
                    sbff.setLength(0);//清空stringbuffer
                }
            }
            if(sbff.length()>0){
                sbff.deleteCharAt(sbff.length()-1);//删除最后多余的一个逗号
                sql = "insert into "+tableName + sbf.toString()+sbff.toString();
                System.out.println(sql);
                st = (Statement) conn.createStatement();
                int row = st.executeUpdate(sql);
                System.out.println("100");
            }else{
                System.out.println("100");
            }
        }


    }
    
}
