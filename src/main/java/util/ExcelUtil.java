package util;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelUtil {
	
  private static Logger log = LoggerFactory.getLogger(ExcelUtil.class);

   private static Sheet initSheet;

   static {
	  //new Sheet(1,0)中 1表示第一个sheet 依次后推;0表示从第几行读,从0开始...
      initSheet = new Sheet(1, 0);
      initSheet.setSheetName("sheet");
      //设置自适应宽度
      initSheet.setAutoWidth(Boolean.TRUE);
   }

   /**
    * 读取少于1000行数据
    * @param filePath 文件绝对路径
    * @return
    */
   public static List<Object> readLessData(String filePath){
      return readExcelLessData(filePath,null);
   }

   /**
    * 读小于1000行数据, 带样式
    * filePath 文件绝对路径
    * initSheet ：
    *      sheetNo: sheet页码，默认为1
    *      headLineMun: 从第几行开始读取数据，默认为0, 表示从第一行开始读取
    *      clazz: 返回数据List<Object> 中Object的类名
    */
   public static List<Object> readExcelLessData(String filePath, Sheet sheet){
      if(!StringUtils.hasText(filePath)){
         return null;
      }

      sheet = sheet != null ? sheet : initSheet;

      InputStream fileStream = null;
      try {
         fileStream = new BufferedInputStream(new FileInputStream(filePath));
         return EasyExcelFactory.read(fileStream, sheet);
      } catch (FileNotFoundException e) {
         log.info("找不到文件或文件路径错误, 文件：{}", filePath);
      }finally {
         try {
            if(fileStream != null){
               fileStream.close();
            }
         } catch (IOException e) {
            log.info("excel文件读取失败, 失败原因：{}", e);
         }
      }
      return null;
   }
   /**
    * 获取sheet信息 list
    * @param filePath 文件绝对路径
    * @return
    */
	public static List<Integer> getSheetNoList(String filePath) {
//		Map<String, Object> reMap = new HashMap<>();
		List<Integer> reList = new ArrayList<>();
		try {
			InputStream fileStream = new BufferedInputStream(new FileInputStream(filePath));
			ExcelListener excelListener = new ExcelListener();
			ExcelReader excelReader = EasyExcelFactory.getReader(fileStream, excelListener);
	        List<Sheet> list = excelReader.getSheets();
	        if(CollectionUtils.isEmpty(list)){
	        	return null;
	        }else{
	        	for(Sheet s:list){
	        		reList.add(s.getSheetNo());
	        	}
	        	return reList;
	        }
		} catch (FileNotFoundException e) {
			log.info(e.toString());
		}

		return null;
	}

   
   public static Map<String, Object> readExcel(String filePath){
      return readExcelBySheet(filePath,null);
   }
   
   public static Map<String, Object> readExcel(String filePath, Sheet sheet){
	  return readExcelBySheet(filePath,sheet);
   }

   /**
    * 读大于1000行数据, 带样式
    * @param filePath 文件绝对路径
    * @return
    */
   public static Map<String, Object> readExcelBySheet(String filePath, Sheet sheet){
      if(!StringUtils.hasText(filePath)){
         return null;
      }

      sheet = sheet != null ? sheet : initSheet;
      Map<String, Object> map = new HashMap<>();
      InputStream fileStream = null;
      try {
         fileStream = new BufferedInputStream(new FileInputStream(filePath));
         ExcelListener excelListener = new ExcelListener();

         EasyExcelFactory.readBySax(fileStream, sheet, excelListener);
         //记录总条数
         Integer count = excelListener.count;
         log.info("======+++++++++++++++>>>>>>>>>>>>>>>>>>>>"+count);
         map.put("rowCount", count);
         map.put("list", excelListener.getDatas());
         return map;
//         return excelListener.getDatas();
      } catch (FileNotFoundException e) {
         log.error("找不到文件或文件路径错误, 文件：{}", filePath);
      }finally {
         try {
            if(fileStream != null){
               fileStream.close();
            }
         } catch (IOException e) {
            log.error("excel文件读取失败, 失败原因：{}", e);
         }
      }
      return null;
   }

   /**
    * 生成excle
    * @param filePath  绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
    * @param data 数据源
    * @param head 表头
    */
   public static void writeBySimple(String filePath, List<List<Object>> data, List<String> head){
      writeSimpleBySheet(filePath,data,head,null);
   }

   /**
    * 生成excle
    * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
    * @param data 数据源
    * @param sheet excle页面样式
    * @param head 表头
    */
   public static void writeSimpleBySheet(String filePath, List<List<Object>> data, List<String> head, Sheet sheet){
      sheet = (sheet != null) ? sheet : initSheet;

      if(head != null){
         List<List<String>> list = new ArrayList<>();
         head.forEach(h -> list.add(Collections.singletonList(h)));
         sheet.setHead(list);
      }

      OutputStream outputStream = null;
      ExcelWriter writer = null;
      try {
         outputStream = new FileOutputStream(filePath);
         writer = EasyExcelFactory.getWriter(outputStream);
         writer.write1(data,sheet);
      } catch (FileNotFoundException e) {
         log.error("找不到文件或文件路径错误, 文件：{}", filePath);
      }finally {
         try {
            if(writer != null){
               writer.finish();
            }

            if(outputStream != null){
               outputStream.close();
            }

         } catch (IOException e) {
            log.error("excel文件导出失败, 失败原因：{}", e);
         }
      }

   }

   /**
    * 生成excle
    * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
    * @param data 数据源
    */
   public static void writeWithTemplate(String filePath, List<? extends BaseRowModel> data){
      writeWithTemplateAndSheet(filePath,data,null);
   }

   /**
    * 生成excle
    * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
    * @param data 数据源
    * @param sheet excle页面样式
    */
   public static void writeWithTemplateAndSheet(String filePath, List<? extends BaseRowModel> data, Sheet sheet){
      if(CollectionUtils.isEmpty(data)){
         return;
      }

      sheet = (sheet != null) ? sheet : initSheet;
      sheet.setClazz(data.get(0).getClass());

      OutputStream outputStream = null;
      ExcelWriter writer = null;
      try {
         outputStream = new FileOutputStream(filePath);
         writer = EasyExcelFactory.getWriter(outputStream);
         writer.write(data,sheet);
      } catch (FileNotFoundException e) {
         log.error("找不到文件或文件路径错误, 文件：{}", filePath);
      }finally {
         try {
            if(writer != null){
               writer.finish();
            }

            if(outputStream != null){
               outputStream.close();
            }
         } catch (IOException e) {
            log.error("excel文件导出失败, 失败原因：{}", e);
         }
      }

   }

   /**
    * 生成多Sheet的excle
    * @param filePath 绝对路径, 如：/home/chenmingjian/Downloads/aaa.xlsx
    * @param multipleSheelPropetys
    */
   public static void writeWithMultipleSheel(String filePath,List<MultipleSheelPropety> multipleSheelPropetys){
      if(CollectionUtils.isEmpty(multipleSheelPropetys)){
         return;
      }

      OutputStream outputStream = null;
      ExcelWriter writer = null;
      try {
         outputStream = new FileOutputStream(filePath);
         writer = EasyExcelFactory.getWriter(outputStream);
         for (MultipleSheelPropety multipleSheelPropety : multipleSheelPropetys) {
            Sheet sheet = multipleSheelPropety.getSheet() != null ? multipleSheelPropety.getSheet() : initSheet;
            if(!CollectionUtils.isEmpty(multipleSheelPropety.getData())){
               sheet.setClazz(multipleSheelPropety.getData().get(0).getClass());
            }
            writer.write(multipleSheelPropety.getData(), sheet);
         }

      } catch (FileNotFoundException e) {
         log.error("找不到文件或文件路径错误, 文件：{}", filePath);
      }finally {
         try {
            if(writer != null){
               writer.finish();
            }

            if(outputStream != null){
               outputStream.close();
            }
         } catch (IOException e) {
            log.error("excel文件导出失败, 失败原因：{}", e);
         }
      }

   }


   /*********************匿名内部类开始，可以提取出去******************************/

   @Data
   public static class MultipleSheelPropety{

      private List<? extends BaseRowModel> data;

      private Sheet sheet;
   }

   /**
    * 解析监听器，
    * 每解析一行会回调invoke()方法。
    * 整个excel解析结束会执行doAfterAllAnalysed()方法
    *
    * @author: chenmingjian
    * @date: 19-4-3 14:11
    */
   @Getter
   @Setter
   public static class ExcelListener extends AnalysisEventListener {
	  
	  private Integer count=0;

      private List<Object> datas = new ArrayList<>();

      /**
       * 逐行解析
       * object : 当前行的数据
       */
      @Override
      public void invoke(Object object, AnalysisContext context) {
         //当前行
         // context.getCurrentRowNum()
         if (object != null) {
            datas.add(object);
         }
         count++;
      }


      /**
       * 解析完所有数据后会调用该方法
       */
      @Override
      public void doAfterAllAnalysed(AnalysisContext context) {
         //解析结束销毁不用的资源
      }

   }

   /************************匿名内部类结束，可以提取出去***************************/

}