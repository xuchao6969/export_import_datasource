package util;

public class MysqlConstantSql {
	
	/**
	 * 获取mysql链接下的所有数据库名称
	 */
	public static final String GET_Mysql_Database = "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA";
	
	/**
	 * 获取指定数据库下所有的表名称
	 */
	public static final String GET_Mysql_Database_Table = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=";
	
	/**
	 * 获取指定表下的所有列名称
	 */
	public static final String GET_Mysql_Database_Table_Column = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA =";

	/**
	 * 获取指定表下的列详情
	 */
	public static final String GET_Mysql_Database_Table_Column_Info = "SELECT COLUMN_NAME,IS_NULLABLE, DATA_TYPE, COLUMN_TYPE,COLUMN_KEY,COLUMN_COMMENT,COLUMN_DEFAULT FROM information_schema.COLUMNS where table_schema =";
	
	/**
	 * 获取指定表的数据条数
	 */
	public static final String GET_Mysql_Database_Table_Number = "SELECT COUNT(*)AS NUMBER FROM ";

	/**
	 * 获取指定表的数据
	 */
	public static final String GET_Mysql_Database_Table_Data = "SELECT * FROM ";
	
}
