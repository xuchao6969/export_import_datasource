package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;


public class OracleHelper {

	private static Logger log = LoggerFactory.getLogger(OracleHelper.class);
	
	private String url;
	private String username;
	private String password;
	private Connection conn = null;
	
	public OracleHelper(String url,String username, String password){
		this.url=url;
		this.username=username;
		this.password= password;
	}
	
	public Connection getConnection(String sysPwd){
	    try{
	    	if(conn == null){
	    		Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
	    		Properties props = new Properties();
	    		props.put("user", username);
	    		props.put("password", password);
	    		if("sys".equals(username)){
//	    			props.put("user", "system");
	    			props.put("password", sysPwd);
	    			props.put("internal_logon", "sysdba");
	    		}
	    		props.put("defaultRowPrefetch", "15");
	    		conn = DriverManager.getConnection(url, props);// 获取连接
	    	}
	        return conn;
	    }
	    catch (Exception e){
	    	log.error(e.toString());
	        return null;
	    }
	}
	
	private void closeResultSet(ResultSet rs) throws SQLException {
		if(null != rs) {
			rs.close();
		}
	}
	
	private void closePst(PreparedStatement pst) throws SQLException {
		if(null != pst) {
			pst.close();
		}
	}
	
	private void closeConnection() throws SQLException {
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
	public Integer getCount(String tableName){
		getConnection("");
		if(conn==null){
			return null;
		}
		int row=0;
		try {
			String sql = "select count(*) from "+tableName.toUpperCase();
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();//获得结果集
			rs.next();
			row = rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return row;
	}
	
	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> resultSetToList(ResultSet rs) throws SQLException{
		List<Map<String, Object>> reList = new ArrayList<>();
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		while(rs.next()){
			Map<String, Object> map = new HashMap<>();
			for (int i = 1; i <= columnCount; i++) { 
                String columnName =metaData.getColumnLabel(i); 
                String value = rs.getString(columnName); 
                map.put(columnName, value);
            }  
			reList.add(map);
		}
		return reList;
	}
	
	/**
	 * 
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 * @throws Exception
	 */
	public  Boolean checkParam(String tableName) throws SQLException {
		getConnection("");
		if(conn==null){
			return null;
		}
		String sql = "select * from user_all_tables where table_name = " + "'"+tableName.toUpperCase()+"'";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();//获得结果集
        if(rs.next()){ 
        	closeConnection();
        	return true;
        } else{
        	return false;
        }
	}
	
	/**
	 * 
	 * @param index
	 * @param num
	 * @return
	 */
	public static List<Map<String, Object>> checkParam(String index, String num){
		 List<Map<String, Object>> resultList = new ArrayList<>(); 
		 String str="^[-\\+]?[\\d]*$";
		 Pattern pattern = Pattern.compile(str);    
		 if(! pattern.matcher(index).matches()){
			Map<String, Object> map = new HashMap<>();
			map.put("error", "参数"+index+"不合法,请输入整数数字");
			resultList.add(map);
			return resultList;
		 }else if(! pattern.matcher(num).matches()){
			 Map<String, Object> map = new HashMap<>();
			 map.put("error", "参数"+num+"不合法,请输入整数数字");
			 resultList.add(map);
			 return resultList;
		 }
		 return resultList;
	}
	

	/**
	 * 获取库下的表
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> getAllTableList() throws SQLException{
		getConnection("");     
		if(conn==null){
			return null;
		}
        String sql = "select table_name from user_tables";
        PreparedStatement pst = conn.prepareStatement(sql);//
        ResultSet rs = pst.executeQuery();
        List<Map<String, Object>> resultList= resultSetToList(rs) ;
        
        closeResultSet(rs);
        closePst(pst);
        closeConnection();
        
        return resultList;
	}
	
	
	/**
	 * 获取表字段
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> getTableProperty(String tableName) throws Exception{
		 List<Map<String, String>> resultList = new ArrayList<>();
		if(checkParam(tableName)){
			getConnection("");
			if(conn==null){
				return null;
			}
			String sql =  "select ucc.*,utc.COLUMN_NAME,utc.DATA_TYPE,utc.DATA_LENGTH,utc.NULLABLE,utc.COLUMN_ID,utc.DATA_DEFAULT "+
					 "from user_col_comments ucc,user_tab_columns utc where "+
				     "ucc.column_name=utc.column_name and ucc.table_name = utc.table_name"+
				     " and ucc.table_name = '"+ tableName.toUpperCase() +"'";
	        PreparedStatement pst = conn.prepareStatement(sql);//
	        ResultSet rs = pst.executeQuery();
	        ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
			while(rs.next()){
				Map<String, String> map = new HashMap<>();
				for (int i = 1; i <= columnCount; i++) { 
	                String columnName =metaData.getColumnLabel(i); 
	                String value = rs.getString(columnName); 
	                map.put(columnName, value);
	            }  
				resultList.add(map);
			}
			
			closeResultSet(rs);
            closePst(pst);
            closeConnection();
	        return resultList;
		}else{
			Map<String, String> map = new HashMap<>();
			map.put("error", "该"+tableName+"不存在");
			resultList.add(map);
			return resultList;
		}
	}
	
	/**
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getTblDataList(String tableName) throws Exception {
		List<Map<String, Object>> resultList = new ArrayList<>();
		if( checkParam(tableName)){
			getConnection("");
			if(conn==null){
				return null;
			}
	        String sql = "select * from " + tableName;
	        PreparedStatement pst = conn.prepareStatement(sql);//
	        ResultSet rs = pst.executeQuery();
	        resultList= resultSetToList(rs) ;
	        
            closeResultSet(rs);
            closePst(pst);
            closeConnection();
            
	        return resultList;
        }else{
        	Map<String, Object> map = new HashMap<>();
        	map.put("error", "表"+tableName+"不存在");
        	resultList.add(map);
        	return resultList;
        }
        
    }
	
	/**
	 * 创建数据库
	 * @param databaseName
	 * @throws Exception
	 */
	public Map<String, Object> createDatabase(String databaseName,String sysPwd,String spaceAddress, String username,String password) throws Exception{
		Map<String, Object> reMap = new HashMap<>();
		getConnection(sysPwd);//conn是以系统管理员 创建的
		String sql = "";
		if(conn != null){
			//判断表空间是否已经被创建
			sql = "select TABLESPACE_NAME from dba_tablespaces where tablespace_name = "+"'"+databaseName.toUpperCase()+"'";
			PreparedStatement pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			List<String> objList = new ArrayList<>();
			while(rs.next()){
				objList.add(rs.getString("TABLESPACE_NAME"));
			}
			if(!CollectionUtils.isEmpty(objList)){
				if(objList.contains(databaseName.toLowerCase())){
					closeResultSet(rs);
					reMap.put("code", 1);
					reMap.put("msg", "database existed");
				}
			}else{
				//判断用户名是否存在
				sql = "select USERNAME from dba_users where username = "+"'"+username.toUpperCase()+"'";
				pst = conn.prepareStatement(sql);
				rs = pst.executeQuery();
				List<String> userList = new ArrayList<>(); 
				while(rs.next()){
					userList.add(rs.getString("USERNAME"));
				}
				if(!CollectionUtils.isEmpty(userList)){
					if(userList.contains(username.toUpperCase())){
						closeResultSet(rs);
						reMap.put("code", 2);
						reMap.put("msg", "user existed");
					}
				}else{
					closeResultSet(rs);
					//创建数据库文件 表空间
					String address = "";
					if(spaceAddress.contains("\\")){
						address = spaceAddress.replace("\\", "/");
					}else{
						address = spaceAddress;
					}
					if(!address.endsWith("/")){
						address = address+"/";
					}
					sql = "CREATE TABLESPACE "+ databaseName +" LOGGING DATAFILE "
							+"'"+address+databaseName+".dbf"+"'"
							+" SIZE 100M AUTOEXTEND ON NEXT 32M MAXSIZE 500M EXTENT MANAGEMENT LOCAL";
					pst = conn.prepareStatement(sql);
					pst.execute();
					//创建用户与数据库文件形成映射  用户使用此空间
					sql = "CREATE USER "+username+" IDENTIFIED BY "+password
							+" DEFAULT TABLESPACE "+databaseName;
					pst = conn.prepareStatement(sql);
				    pst.execute();
					//为用户添加权限
					sql = "grant connect,resource,dba to "+username;
					pst = conn.prepareStatement(sql);
				    pst.execute();
				    sql = "grant create session to "+username;
				    pst = conn.prepareStatement(sql);//
				    pst.execute();
				    closePst(pst);
		            closeConnection();
		            reMap.put("code", 0);
		            reMap.put("msg", "success");
				}
			}
		}
		return reMap;
	}

	public void deleteDB(String databaseName) throws Exception{
		getConnection("");
		if(conn!=null){
			String sql ="DROP TABLESPACE "+databaseName.toUpperCase()+" INCLUDING CONTENTS AND DATAFILES";
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();
			closePst(pst);
			closeConnection();
		}
	}
	
	public void deleteTable(String tableName) throws Exception{
		getConnection("");
		if(conn!=null){
			String sql ="DROP TABLE "+tableName.toUpperCase();
			PreparedStatement pst = conn.prepareStatement(sql);
			pst.execute();
			closePst(pst);
			closeConnection();
		}
	}
	public static void main(String[] args) {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String url = "jdbc:oracle:thin:@localhost" + ":1521"+ ":"
				+ "orcl";
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url, "root", "123456");
			Statement statement = (Statement) conn.createStatement();
			int rs=0;
			for(int i=0;i<100000;i++){
	        	String sql = "insert into t_dataset values ("
	        			+ "'',"+"'a"+i+"',"+"'b"+i+"',"+null+","+"'20kb',"+1+",'icon')"
	        			;
	        	rs = statement.executeUpdate(sql);
		        }
	        	System.out.println("=======end======");
	        	statement.close();
	        	conn.close();
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
