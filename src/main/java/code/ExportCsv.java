package code;

import org.springframework.util.CollectionUtils;
import util.CSVUtils;
import util.MysqlHelper;
import util.OracleHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportCsv {


    /*
      oracle导出csv
       */
    public static void ExportOracle() throws Exception {
        String ip = "localhost";
        String port = "1521";
        String database = "orcl";
        String username = "admin";
        String password = "123456";
        String tableName = "t_dict";
        String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + "orcl";
        //文件存放地址
        String path = "C:/Users/Administrator/Desktop";

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
        List<String> list = new ArrayList<>();
        List<Map<String, String>> columnList = oh.getTableProperty(tableName);
        if (!CollectionUtils.isEmpty(columnList)) {
            //先把字段写入进去
            StringBuffer columns = new StringBuffer();
            for (int j = 0; j < columnList.size(); j++) {
                columns.append("\"");
                columns.append(columnList.get(j).get("COLUMN_NAME"));
                columns.append("\"");
                columns.append(",");
            }
            columns.deleteCharAt(columns.length() - 1);//删除最后多余的一个逗号
            list.add(columns.toString());
            CSVUtils.exportCsv(path, tableName,list);
            //写入数据
            String percentage = "";
            for (int i = 1; i <= cycleCount; i++) {
                //写入的进度 15%显示15  小数*100取整
                percentage = String.valueOf((i * 100 / (cycleCount)));
                System.out.println(percentage);
                page = String.valueOf((i - 1) * preCount);
                String sql = "select * from (select rownum as rowno, t.* from " + tableName
                        + " t where rownum <= " + i * preCount + " ) table_alias where table_alias.rowno > " + page;

                rs = st.executeQuery(sql);
                while (rs.next()) {
                    StringBuilder sds = new StringBuilder();
                    for(int j=0;j<columnList.size();j++ ){
                        sds.append("\"");
                        sds.append(rs.getString(columnList.get(j).get("columnName")));
                        sds.append("\"");
                        sds.append(",");
                    }
                    sds.deleteCharAt(sds.length()-1);//删除最后多余的一个逗号
                    list.add(sds.toString());
                }
                CSVUtils.exportCsv(path, tableName,list);
            }
            rs.close();
            st.close();
            conn.close();
        }
    }

    /*
   mysql导出csv
    */
    public static void ExportMysql() throws Exception {
        String ip = "localhost";
        String port = "3306";
        String database = "test";
        String username = "root";
        String password = "root";
        String tableName = "t_dictionary";
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Hongkong&allowMultiQueries=true";
        //文件存放地址
        String path = "C:/Users/Administrator/Desktop";
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

        List<String> list = new ArrayList<>();
        List<HashMap<String, String>> columnList = mh.getMysqlDatabaseTableColumNameInfo(database, tableName);
        if (!CollectionUtils.isEmpty(columnList)) {
            //先把字段写入进去
            StringBuffer columns = new StringBuffer();
            for (int j = 0; j < columnList.size(); j++) {
                columns.append("\"");
                columns.append(columnList.get(j).get("columnName"));
                columns.append("\"");
                columns.append(",");
            }
            columns.deleteCharAt(columns.length() - 1);//删除最后多余的一个逗号
            list.add(columns.toString());
            CSVUtils.exportCsv(path, tableName,list);

            //写入数据
            String percentage = "";
            for(int i=1;i<=cycleCount;i++){
                //写入数据的进度  2%用2表示  12%用12表示  小数*100取整
                percentage=String.valueOf((i*100/(cycleCount)));
                System.out.println(percentage);
                page = String.valueOf((i-1)*preCount);
                String sql = "select * from "+tableName+" limit "
                        +page+","+String.valueOf(preCount);
                rs = st.executeQuery(sql);
                while (rs.next()) {
                    StringBuilder sds = new StringBuilder();
                    for(int j=0;j<columnList.size();j++ ){
                        sds.append("\"");
                        sds.append(rs.getString(columnList.get(j).get("columnName")));
                        sds.append("\"");
                        sds.append(",");
                    }
                    sds.deleteCharAt(sds.length()-1);//删除最后多余的一个逗号
                    list.add(sds.toString());
                }
                CSVUtils.exportCsv(path, tableName,list);
            }
            rs.close();
            st.close();
            conn.close();
        }
    }


}
