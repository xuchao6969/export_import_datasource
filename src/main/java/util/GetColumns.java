package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;


public class GetColumns {
	
	private static Logger log = LoggerFactory.getLogger(GetColumns.class);
	
	public static List<String> getExcelColumn(String totalPath){
		List<Object> objects = (List<Object>) ExcelUtil.readExcel(totalPath).get("list");
		List<String> list = (List<String>) objects.get(0);
		String str = JsonUtil.list(list);
		log.info(str);
		return list;
	}
	
	public static List<String> getTxtColumn(String totalPath,String splitter){
		List<String> list = new ArrayList<>();
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(totalPath), "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(splitter);
				for(String str: arr){
					list.add(str.subSequence(1, str.length()-1).toString());
				}
				break;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<String> getCsvColumn(String totalPath){
		List<String> list = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(totalPath)));
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(",");
				for(String str: arr){
					list.add(str.subSequence(1, str.length()-1).toString());
				}
				break;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public static List<String> getJsonColumn(String totalPath){
		List<String> lists = new ArrayList<>();
		 try {
			 File jsonFile = new File(totalPath);
			 FileReader fileReader = new FileReader(jsonFile);
			 Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
			 int ch = 0;
			 StringBuffer sb = new StringBuffer();
			 while ((ch = reader.read()) != -1) {
			     sb.append((char) ch);
			 }
			 fileReader.close();
			 reader.close();
			 //转换为字符串
			 String jsonStr = sb.toString();
			 //将字符串转为Map对象
			 Map<String, Object> jsonObj = (Map<String, Object>) JsonUtil.json2Map(jsonStr);
			 List<Map<String, Object>> list = (List<Map<String, Object>>) jsonObj.get("RECORDS");
			 if(!CollectionUtils.isEmpty(list)){
				 Map<String, Object> map = list.get(0);
				 Set<String> columns = map.keySet();
				 for(String str: columns){
					 lists.add(str);
				 }
			 }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return lists; 
	}
	
	public static void main(String[] args) throws Exception{
		String fileName = "f:/svn/t_dictionary.csv";
		String file = "C:/export/t_dictionary.xls";
		String f = "C:/Users/Administrator/Desktop/t_project.txt";
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(new File(fileName)));
		String line = "";
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			break;
		}
		Map<String,Object> map = new HashMap<>();
		map.put("key", "1");
		map.put("yek", "2");
		map.put("as", "2");
		Set<String> lll = map.keySet();
		System.out.println(lll);
		
	}

}
