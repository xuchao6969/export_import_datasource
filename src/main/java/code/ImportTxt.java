package code;

import util.GetColumns;
import util.GetFileLineNum;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportTxt {

    /**
     * oracle导入txt 表要对应 oracle我设置的是主键自增的
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
        String path = "C:/Users/Administrator/Desktop/t_dictionary.txt";

        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        //获取文件总行数
        Integer lineNum = GetFileLineNum.getLineNum(path);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(path), "utf-8");
        @SuppressWarnings("resource")
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        String sql = "";
        List<String> columns = GetColumns.getTxtColumn(path, ",");
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
        int y = 0;// 避免第一行表头被作为数据插入
        StringBuffer sbff = new StringBuffer();
        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        conn = DriverManager.getConnection(url, username, password);
        String sqlstr = "into "+tableName;
        while ((line = br.readLine()) != null) {
            if (y > 0) {
                sbff.append(sqlstr);
                sbff.append(sbf.toString());
                sbff.append(" ( ");
                String[] arr = line.split(",");
                String str = "";
                for (int a = 0; a < columns.size(); a++) {
                    if(a==0){
                        sbff.append("''");//oracle设置的是主键自增
                    }else{
                        sbff.append("'");
                        sbff.append(str.substring(1, str.length()-1));
                        sbff.append("'");
                    }
                    sbff.append(",");

                }
                sbff.deleteCharAt(sbff.length() - 1);// 删除最后多余的一个逗号
                sbff.append(") ");

                m++;
                if (0 == m % preCount) {
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
                    percentMsg = (times*preCount)*100/lineNum;
                    System.out.println(""+percentMsg);
                    sbff.setLength(0);// 清空stringbuffer
                }
            }
            y++;
            line=null;
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
            percentMsg = 100;
            System.out.println(""+percentMsg);
        }else{
            System.out.println("100");
        }
    }

    /**
     * mysql导入txt 表要对应 mysql我设置的是主键自增的
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
        String path = "C:/Users/Administrator/Desktop/t_dictionary.txt";

        //获取文件总行数
        Integer lineNum = GetFileLineNum.getLineNum(path);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(path), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        String sql = "";
        //拼接字段
        StringBuffer sbf = new StringBuffer();
        sbf.append("(");
        //获取文件第一行 字段
        List<String> columns = GetColumns.getTxtColumn(path, ",");
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
        int y = 0;// 避免第一行表头被作为数据插入
        StringBuffer sbff = new StringBuffer();
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = (Connection) DriverManager.getConnection(url, username, password);
        Statement st = null;
        while ((line = br.readLine()) != null) {
            if (y > 0) {
                sbff.append(" ( ");
                String[] arr = line.split(",");
                for (int a = 0; a < columns.size(); a++) {
                    if (a == 0) {
                        sbff.append("0");
                        sbff.append(",");
                    } else {
                        sbff.append(arr[a]);
                        sbff.append(",");
                    }
                }
                sbff.deleteCharAt(sbff.length() - 1);// 删除最后多余的一个逗号
                sbff.append("),");
                m++;
                if (0 == m % preCount) {
                    sbff.deleteCharAt(sbff.length() - 1);// 删除最后多余的一个逗号
                    sql = "insert into " + tableName + sbf.toString() + sbff.toString();
                    System.out.println(sql);

                    st = (Statement) conn.createStatement();
                    int row = st.executeUpdate(sql);
                    times++;
                    percentMsg = (times*preCount)*100/lineNum;
                    System.out.println("=========>>>>>>"+percentMsg);
                    sbff.setLength(0);// 清空stringbuffer
                }
            }
            y++;
        }
        if(sbff.length()>0){
            sbff.deleteCharAt(sbff.length() - 1);// 删除最后多余的一个逗号
            sql = "insert into " + tableName + sbf.toString() + sbff.toString();
            System.out.println(sql);
            st = (Statement) conn.createStatement();
            int row = st.executeUpdate(sql);
            percentMsg = 100;
            System.out.println("==========>>>>>>"+percentMsg);
        }else{
            System.out.println("==========>>>>>>"+100);
        }

    }

}
