package util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

public class MysqlHelper {

	private String url;
	private String userName;
	private String password;
	private Connection conn = null;
	
	/**
	 * 
	 * @param url
	 * @param userName
	 * @param password
	 */
	public MysqlHelper(String url,String userName, String password){
		this.url=url;
		this.userName=userName;
		this.password= password;
	}
	
	/**
	 * 获取连接
	 * @return conn
	 */
	public Connection getMysqlConnection() {
		try {
			if(conn==null){
				//Class.forName("com.mysql.jdbc.Driver");
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection(url, userName, password);
			}
			return conn;
		} catch (Exception e) {
			e.toString();
			return null;
		}
	}
	
	/**
	 * 关闭结果集
	 * @param rs
	 * @throws SQLException
	 */
	private void closeResultSet(ResultSet rs) throws SQLException {
		if(null != rs) {
			rs.close();
		}
	}
	
	/**
	 * 关闭Statement
	 * @param st
	 * @throws SQLException
	 */
	private void closeStatement(Statement st) throws SQLException {
		if(null != st) {
			st.close();
		}
	}
	
	/**
	 * 关闭连接
	 * @throws SQLException
	 */
	private void closeConn() throws SQLException {
		if(null != conn) {
			conn.close();
			conn = null;
		}
	}
	/**
	 * 查询条数
	 * @return
	 * @throws Exception
	 */
	public Integer getCount(String databaseName, String tableName)throws Exception{
		getMysqlConnection();
		if(conn==null){
			return null;
		}
		Statement statement = (Statement) conn.createStatement();
		ResultSet resultSet = statement.executeQuery("select count(*) from "+databaseName+"."+tableName);
		resultSet.next();
		int row = resultSet.getInt(1);
		return row;
	}
	
	/**
	 * 获取所有的库
	 * @return
	 * @throws Exception
	 */
	public List<String> getMysqlDatabase() throws Exception{
		getMysqlConnection();
		if(conn==null){
			return null;
		}
		Statement statement = (Statement) conn.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA");
		List<String> list = new ArrayList<String>();
		while (resultSet.next()) {
			list.add(resultSet.getString("SCHEMA_NAME"));
		}
		closeResultSet(resultSet);
		closeStatement(statement);
		closeConn();
		
		//去除系统库
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
            String str = (String)it.next();
            if("information_schema".equals(str) ||
            	"mysql".equals(str) ||
            	"performance_schema".equals(str) ||
            	"sys".equals(str)){
                it.remove();
            }        
        }
		return list;
	}
	
	/**
	 * 获取库下的表
	 * @param databaseName
	 * @return
	 * @throws Exception
	 */
	public List<String> getMysqlDatabaseTable(String databaseName) throws Exception {
		String database = "'" + databaseName + "'";
		getMysqlConnection();
		if(conn==null){
			return null;
		}
		Statement statement = (Statement) conn.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from" + database);
		List<String> list = new ArrayList<String>();
		while (resultSet.next()) {
			list.add(resultSet.getString("TABLE_NAME"));
		}
		closeResultSet(resultSet);
		closeStatement(statement);
		closeConn();
		return list;
	}
	
	/**
	 * 获取表信息
	 * @param databaseName
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, String>> getMysqlDatabaseTableColumNameInfo(String databaseName, String tableName) throws Exception {
		
		String database = "'" + databaseName + "'";
		String table = "'" + tableName + "'";
		getMysqlConnection();
		if(conn==null){
			return null;
		}
		Statement statement = (Statement) conn.createStatement();
		String sql = MysqlConstantSql.GET_Mysql_Database_Table_Column_Info + database + "AND TABLE_NAME=" + table;
		ResultSet resultSet = statement.executeQuery(sql);
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		while (resultSet.next()) {
			HashMap<String, String> map = new HashMap<String, String>();
			String isNull = "";
			if (resultSet.getString("IS_NULLABLE").equals("NO")) {
				isNull = "否";
			} else if (resultSet.getString("IS_NULLABLE").equals("YES")) {
				isNull = "是";
			}
			String primaryKey = "";
			if (resultSet.getString("COLUMN_KEY").equals("PRI")) {
				primaryKey = "是";
			} else if (resultSet.getString("COLUMN_KEY").equals(" ")) {
				primaryKey = "否";
			} else {
				primaryKey = "否";
			}
			map.put("isNull", isNull);// 是否为空
			map.put("primaryKey", primaryKey);// 是否主键
			map.put("columnName", resultSet.getString("COLUMN_NAME"));// 字段名字
			map.put("columnType", resultSet.getString("DATA_TYPE"));// 字段类型
			map.put("typelength", resultSet.getString("COLUMN_TYPE"));// 类型长度
			map.put("annotation", resultSet.getString("COLUMN_COMMENT"));// 注释
			map.put("defaultValue", resultSet.getString("COLUMN_DEFAULT"));// 默认值
			list.add(map);
		}
		closeResultSet(resultSet);
		closeStatement(statement);
		closeConn();
		return list;
	}
	
	/**
	 * 获取表数据
	 * @param databaseName
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getTblDataList(String databaseName, String tableName) throws Exception{
		getMysqlConnection();
		if(conn==null){
			return null;
		}
		Statement statement = (Statement) conn.createStatement();
		String sql = "select * form" + databaseName + "." + tableName;
		ResultSet resultSet = statement.executeQuery(sql);
		List<Map<String, Object>> list = resultSet2List(resultSet);
		closeResultSet(resultSet);
		closeStatement(statement);
		closeConn();
		return list;
	}
	
	private List<Map<String, Object>> resultSet2List(ResultSet rs) throws SQLException{
		List<Map<String, Object>> reList = new ArrayList<>();
		ResultSetMetaData rsmd =  (ResultSetMetaData) rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		while(rs.next()){
			Map<String, Object> map = new HashMap<>();
			for(int i =0;i<columnCount;i++) {
				String columnName = rsmd.getColumnLabel(i+1);
				Object columnValue = rs.getObject(i+1);
				map.put(columnName, columnValue);
			}
			reList.add(map);
		}
		return reList;
	}
	
	/**
	 * 创建数据库
	 * @param databaseName
	 * @throws SQLException
	 */
	public void createDatabase(String databaseName) throws SQLException{
		getMysqlConnection();
		if(conn!=null){
			Statement statement = (Statement) conn.createStatement();
			String sql = "create database " + databaseName;
			@SuppressWarnings("unused")
			int count = statement.executeUpdate(sql);
			closeStatement(statement);
			closeConn();
		}
	}

	
	/**
	 * 删除单个库
	 * @throws Exception 
	 */
	public void deleteDB(String databaseName) throws Exception{
		getMysqlConnection();
		if(conn!=null){
			Statement statement = (Statement) conn.createStatement();
			String sql = "drop database " + databaseName;
			statement.executeUpdate(sql);
			closeStatement(statement);
			closeConn();
		}
	}
	
	/**
	 * 删除单个库
	 * @throws Exception 
	 */
	public void deleteTable(String databaseName,String tableName) throws Exception{
		getMysqlConnection();
		if(conn!=null){
			Statement statement = (Statement) conn.createStatement();
			String sql = "drop table " + databaseName+"."+tableName;
			statement.executeUpdate(sql);
			closeStatement(statement);
			closeConn();
		}
	}
	
	public static void main(String[] args) {
		String driver = "com.mysql.cj.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/di?useUnicode=true&characterEncoding=utf8&serverTimezone=Hongkong&allowMultiQueries=true";
		String user = "root";
		String password = "123456";
		Connection conn = null;
			try {
				Class.forName(driver);
		        conn = (Connection) DriverManager.getConnection(url, user, password);
		        Statement statement = (Statement) conn.createStatement();
	        	int rs = 0;
	        	for(int i=0;i<100000;i++){
//	        	String sql = "insert into t_dictionary values ( "+0+","+
//	        	"'a"+i+"',"+"'b"+i+"',"+"'c"+i+"',"+"1 )";
	        	String sql = "insert into t_department values ( "+0+","+
	        	"'a"+i+"',"+"0,"+null+","+"'c"+i+"',"+null+","+"'d"+i+"',"+"1,1 )";
	        	rs = statement.executeUpdate(sql);
		        }
	        	System.out.println("=======end======");
	        	statement.close();
	        	conn.close();
			}catch (SQLException e) {
		        e.printStackTrace();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
	}
}
