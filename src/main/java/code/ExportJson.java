package code;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.CollectionUtils;
import util.JsonFormatTool;
import util.MysqlHelper;
import util.OracleHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportJson {




    /*
   oracle导出json
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

        Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        conn = DriverManager.getConnection(url, username, password);
        st = conn.createStatement();
        String sql = "select * from "+tableName;
        rs = st.executeQuery(sql);
        OracleHelper oh = new OracleHelper(url, username, password);
        List<Map<String, String>> columnList = oh.getTableProperty(tableName);
        if (!CollectionUtils.isEmpty(columnList)) {
            List<JSONObject> list = new ArrayList<>();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                for(int i=0;i<columnList.size();i++ ){
                    String key = columnList.get(i).get("COLUMN_NAME");
                    String value = rs.getString(key);
                    if(null==value){
                        obj.put(key, "");
                    }else{
                        obj.put(key, value);
                    }
                }
                list.add(obj);
            }
            JSONObject jb = new JSONObject();
            jb.put("RECORDS", list);
            boolean flag = write2json(JSONObject.toJSONString(jb),path,tableName);
            System.out.println("<----------------ending------------------->");
        }
    }

    /*
    mysql导出json
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


        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = (Connection) DriverManager.getConnection(url, username, password);
        st = conn.createStatement();
        String sql = "select * from "+tableName;
        rs = st.executeQuery(sql);
        MysqlHelper mh = new MysqlHelper(url, username, password);
        List<HashMap<String, String>> columnList = mh.getMysqlDatabaseTableColumNameInfo(database, tableName);
        if (!CollectionUtils.isEmpty(columnList)) {
            List<JSONObject> list = new ArrayList<>();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                for(int i=0;i<columnList.size();i++ ){
                    String key = columnList.get(i).get("columnName");
                    String value = rs.getString(key);
                    if(null==value){
                        obj.put(key, "");
                    }else{
                        obj.put(key, value);
                    }
                }
                list.add(obj);
            }
            JSONObject jb = new JSONObject();
            jb.put("RECORDS", list);
            boolean flag = write2json(JSONObject.toJSONString(jb),path,tableName);
            System.out.println("<------------------------ending--------------------->");
            rs.close();
            st.close();
            conn.close();
        }
    }


    public static boolean  write2json(String jsonString, String filePath, String fileName) {
        // 标记文件生成是否成功
        boolean flag = true;
        // 拼接文件完整路径
        String fullPath = filePath + File.separator + fileName + ".json";
        // 生成json格式文件
        try {
            // 保证创建一个新文件
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { // 如果已存在,删除旧文件
                file.delete();
            }
            file.createNewFile();
            if(jsonString.indexOf("'")!=-1){
                //将单引号转义一下，因为JSON串中的字符串类型可以单引号引起来的
                jsonString = jsonString.replaceAll("'", "\\'");
            }
            if(jsonString.indexOf("\"")!=-1){
                //将双引号转义一下，因为JSON串中的字符串类型可以单引号引起来的
                jsonString = jsonString.replaceAll("\"", "\\\"");
            }

            if(jsonString.indexOf("\r\n")!=-1){
                //将回车换行转换一下，因为JSON串中字符串不能出现显式的回车换行
                jsonString = jsonString.replaceAll("\r\n", "\\u000d\\u000a");
            }
            if(jsonString.indexOf("\n")!=-1){
                //将换行转换一下，因为JSON串中字符串不能出现显式的换行
                jsonString = jsonString.replaceAll("\n", "\\u000a");
            }

            // 格式化json字符串
            jsonString = JsonFormatTool.formatJson(jsonString);

            // 将格式化后的字符串写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }

        // 返回是否成功的标记
        return flag;
    }

}
