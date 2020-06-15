package code;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.util.SheetUtils;
import org.springframework.util.CollectionUtils;
import util.MysqlHelper;
import util.OracleHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportXlsOrXlsx {
    public static void main(String[] args) {
        try {
            ExportMysql();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
   oracle导出txt
    */
    public static void ExportOracle() throws Exception {
        final Integer XLS_PRECOUNT = 65535;//xls每个sheet页的容量
        final Integer XLSX_PRECOUNT = 1048576;//xlsx每个sheet页的容量
        String excelType = "xlsx";//xlsx或xls
        String ip = "localhost";
        String port = "1521";
        String database = "orcl";
        String username = "admin";
        String password = "123456";
        String tableName = "t_dict";
        String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + "orcl";
        //文件存放地址
        String path = "C:/Users/Administrator/Desktop/";

        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        String page = "";

        //每次的条数
        int preCount = 50;

        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        conn = DriverManager.getConnection(url, username, password);
        st = conn.createStatement();
        OracleHelper oh = new OracleHelper(url, username, password);
        //获取总条数
        int count = oh.getCount(tableName);
        //循环次数 同时也是分页的页数
        int cycleCount;
        if (0 != count % preCount) {
            cycleCount = count / preCount + 1;
        } else {
            cycleCount = count / preCount;
        }

        String fileName = path+tableName+"."+excelType;
        OutputStream out = new FileOutputStream(fileName);
        ExcelWriter writer = null;
        int sheetContain = 520;//自定义一个sheet存520条数据
        if(excelType.equals("xlsx")){
            writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
//            sheetContain = XLSX_PRECOUNT;//是否使用自定义sheet单元容量  如不使用请取消本行注释
        }else{
            writer = new ExcelWriter(out, ExcelTypeEnum.XLS, true);
//            sheetContain = XLS_PRECOUNT;//是否使用自定义sheet单元容量  如不使用请取消本行注释
        }

        int sheetCount = 0;//定义sheet个数

        if(0 != count % sheetContain){
            sheetCount = count / sheetContain + 1;
        }else{
            sheetCount = count / sheetContain;
        }

        String percentage = "";
        if(sheetCount==1){
            Sheet sheet = new Sheet(1, 0);
            sheet.setSheetName(tableName);
            Table table = new Table(1);
            List<Map<String, String>> columnList = oh.getTableProperty(tableName);
            if(!CollectionUtils.isEmpty(columnList)){
                for(int i=1;i<=cycleCount;i++){
                    percentage=String.valueOf((i*100/(cycleCount)));
                    System.out.println(percentage);
                    page = String.valueOf((i-1)*preCount);
                    String sql= "select * from (select rownum as rowno, t.* from "+tableName
                            +" t where rownum <= "+i*preCount+" ) table_alias where table_alias.rowno > "+page;

                    rs = st.executeQuery(sql);
                    List<List<String>> list = new ArrayList<>();
                    while (rs.next()) {
                        List<String> data = new ArrayList<>();
                        for(int j=0;j<columnList.size();j++ ){
                            String value = rs.getString(columnList.get(j).get("COLUMN_NAME"));
                            data.add(value);
                        }
                        list.add(data);
                    }
                    //添加表头
                    List<List<String>> head = new ArrayList<List<String>>();
                    List<String> headCoulumn = null;
                    for(int y=0;y<columnList.size();y++){
                        headCoulumn = new ArrayList<String>();
                        headCoulumn.add(columnList.get(y).get("COLUMN_NAME"));
                        head.add(headCoulumn);
                    }
                    table.setHead(head);

                    writer.write0(list, sheet,table);
                    System.out.println("=====end====");
                }
                writer.finish();
            }
        }else{
            List<Map<String, String>> columnList = oh.getTableProperty(tableName);
            if(!CollectionUtils.isEmpty(columnList)){
                for(int i=1;i<=cycleCount;i++){
                    percentage=String.valueOf((i*100/(cycleCount)));
                    System.out.println(percentage);
                    page = String.valueOf((i-1)*preCount);
                    String sql= "select * from (select rownum as rowno, t.* from "+tableName
                            +" t where rownum <= "+i*preCount+" ) table_alias where table_alias.rowno > "+page;

                    rs = st.executeQuery(sql);
                    List<List<String>> list = new ArrayList<>();
                    while (rs.next()) {
                        List<String> data = new ArrayList<>();
                        for(int j=0;j<columnList.size();j++ ){
                            String value = rs.getString(columnList.get(j).get("COLUMN_NAME"));
                            data.add(value);
                        }
                        list.add(data);
                    }
                    Sheet sheet = null;
                    Table table = null;
                    int vers = 0;
                    if(0==i*preCount%sheetContain){
                        vers = i*preCount/sheetContain;
                    }else{
                        vers =i*preCount/sheetContain +1;
                    }
                    sheet = new Sheet(vers+1,0);
                    sheet.setSheetName(tableName+"_"+vers);
                    table = new Table(vers+1);
                    //添加表头
                    List<List<String>> head = new ArrayList<List<String>>();
                    List<String> headCoulumn = null;
                    for(int y=0;y<columnList.size();y++){
                        headCoulumn = new ArrayList<String>();
                        headCoulumn.add(columnList.get(y).get("COLUMN_NAME"));
                        head.add(headCoulumn);
                    }
                    table.setHead(head);
                    System.out.println(sheet.getSheetName());

                    writer.write0(list, sheet, table);
                    System.out.println("=====end====");
                }
                writer.finish();
            }
        }
        rs.close();
        st.close();
        conn.close();
    }


    /*
  mysql导出Excel
   */
    public static void ExportMysql() throws Exception {
        final Integer XLS_PRECOUNT = 65535;//xls每个sheet页的容量
        final Integer XLSX_PRECOUNT = 1048576;//xlsx每个sheet页的容量
        String excelType = "xls";//xlsx或xls
        String ip = "localhost";
        String port = "3306";
        String database = "test";
        String username = "root";
        String password = "root";
        String tableName = "t_dictionary";
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Hongkong&allowMultiQueries=true";
        //文件存放地址
        String path = "C:/Users/Administrator/Desktop/";
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;

        String page = "";

        //每次的条数
        int preCount = 50;
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = (Connection) DriverManager.getConnection(url, username, password);
        st = conn.createStatement();
        MysqlHelper mh = new MysqlHelper(url, username, password);
        //获取总条数
        int count = mh.getCount(database, tableName);
        //循环次数 同时也是分页的页数
        int cycleCount;
        if (0 != count % preCount) {
            cycleCount = count / preCount + 1;
        } else {
            cycleCount = count / preCount;
        }

        String fileName = path+tableName+"."+excelType;
        OutputStream out = new FileOutputStream(fileName);
        ExcelWriter writer = null;
        int sheetContain = 520;//自定义一个sheet存500条数据
        if(excelType.equals("xlsx")){
            writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
//            sheetContain = XLSX_PRECOUNT;//是否使用默认的sheet单元容量  如使用请取消本行注释
        }else{
            writer = new ExcelWriter(out, ExcelTypeEnum.XLS, true);
//            sheetContain = XLS_PRECOUNT;//是否使用默认的sheet单元容量  如使用请取消本行注释
        }

        int sheetCount = 0;//定义sheet个数

        if(0 != count % sheetContain){
            sheetCount = count / sheetContain + 1;
        }else{
            sheetCount = count / sheetContain;
        }
        String percentage = "";
        if(sheetCount==1){

            Sheet sheet = new Sheet(1, 0);
            sheet.setSheetName(tableName);
            Table table = new Table(1);
            List<HashMap<String, String>> columnList = mh.getMysqlDatabaseTableColumNameInfo(database,tableName);
            if(!CollectionUtils.isEmpty(columnList)){
                for(int i=1;i<=cycleCount;i++){
                    percentage=String.valueOf((i*100/(cycleCount)));
                    System.out.println(percentage);
                    page = String.valueOf((i-1)*preCount);
                    String sql = "select * from "+tableName+" limit "
                            +page+","+String.valueOf(preCount);
                    rs = st.executeQuery(sql);
                    List<List<String>> list = new ArrayList<>();
                    while (rs.next()) {
                        List<String> data = new ArrayList<>();
                        for(int j=0;j<columnList.size();j++ ){
                            String value = rs.getString(columnList.get(j).get("columnName"));
                            data.add(value);
                        }
                        list.add(data);
                    }
                    //添加表头
                    List<List<String>> head = new ArrayList<List<String>>();
                    List<String> headCoulumn = null;
                    for(int y=0;y<columnList.size();y++){
                        headCoulumn = new ArrayList<String>();
                        headCoulumn.add(columnList.get(y).get("columnName"));
                        head.add(headCoulumn);
                    }
                    table.setHead(head);

                    writer.write0(list, sheet,table);
                    System.out.println("=====end====");
                }
                writer.finish();

            }
        }else{
            List<HashMap<String, String>> columnList = mh.getMysqlDatabaseTableColumNameInfo(database,tableName);
            if(!CollectionUtils.isEmpty(columnList)){
                for(int i=1;i<=cycleCount;i++){
                    percentage=String.valueOf((i*100/(cycleCount)));
                    System.out.println(percentage);
                    page = String.valueOf((i-1)*preCount);
                    String sql = "select * from "+tableName+" limit "
                            +page+","+String.valueOf(preCount);
                    rs = st.executeQuery(sql);
                    List<List<String>> list = new ArrayList<>();
                    while (rs.next()) {
                        List<String> data = new ArrayList<>();
                        for(int j=0;j<columnList.size();j++ ){
                            String value = rs.getString(columnList.get(j).get("columnName"));
                            data.add(value);
                        }
                        list.add(data);
                    }
                    Sheet sheet = null;
                    Table table = null;
                    int vers = 0;
                    if(0==i*preCount%sheetContain){
                        vers = i*preCount/sheetContain;
                    }else{
                        vers =i*preCount/sheetContain +1;
                    }
                    sheet = new Sheet(vers+1,0);
                    sheet.setSheetName(tableName+"_"+vers);
                    table = new Table(vers+1);
                    //添加表头
                    List<List<String>> head = new ArrayList<List<String>>();
                    List<String> headCoulumn = null;
                    for(int y=0;y<columnList.size();y++){
                        headCoulumn = new ArrayList<String>();
                        headCoulumn.add(columnList.get(y).get("columnName"));
                        head.add(headCoulumn);
                    }
                    table.setHead(head);
                    System.out.println(sheet.getSheetName());

                    writer.write0(list, sheet, table);
                    System.out.println("=====end====");
                }
                writer.finish();

            }
        }

        rs.close();
        st.close();
        conn.close();
    }

}
