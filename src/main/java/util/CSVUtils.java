package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;



public class CSVUtils {
    
    /**
     * 导出
     * @Param  address
     * @param  tableName
     * @param dataList 数据
     * @return
     */
    public static boolean exportCsv(String address, String tableName, List<String> dataList){
        boolean isSucess=false;
//        if(file.exists()){
//    		file.delete();
//    	}
        String addr = "";
    	if(address.contains("\\")){
			addr = address.replace("\\", "/");
		}else{
			addr = address;
		}
		if(!addr.endsWith("/")){
			addr = addr+"/";
		}
        File file = new File(addr+tableName+".csv");
        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out,"UTF-8");
            bw =new BufferedWriter(osw);
            if(dataList!=null && !dataList.isEmpty()){
                for(String data : dataList){
                	bw.append(new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF }));
                    bw.append(data).append("\r");
                }
            }
            isSucess=true;
        } catch (Exception e) {
            isSucess=false;
        }finally{
        	 try {
        		 if(null != bw){
        			 bw.close();
                     bw=null;
        		 }
        		 if(null != osw){
        			 osw.close();
                     osw=null;
        		 }
        		 if(null != out){
        			 out.close();
                     out=null;
        		 }
             } catch (IOException e) {
                 e.printStackTrace();
             } 
            
        }
        
        return isSucess;
    }
    
    /**
     * 导入
     * 
     * @param file csv文件(路径+文件)
     * @return
     */
    public static List<String> importCsv(File file){
        List<String> dataList=new ArrayList<String>();
        
        BufferedReader br=null;
        try { 
            br = new BufferedReader(new FileReader(file));
            String line = ""; 
            while ((line = br.readLine()) != null) { 
                dataList.add(line);
            }
        }catch (Exception e) {
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
 
        return dataList;
    }
    
    
}