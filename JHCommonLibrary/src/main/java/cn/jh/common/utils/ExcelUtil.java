package cn.jh.common.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Excel导出封装类
 * 
 * @author yezhiyuan
 * @param <T>
 */
public class ExcelUtil<T> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExcelUtil.class);

	private final String downloadPath = "/usr/share/nginx/html/";

	private final String templatePath = "/product/deploy/exceltemplate/";
    /**
     * 基于Excel 2007模板写入数据
     * 
     * @Title: writeExcel
     * @param：@param file 模板文件
     * @param：@param dataSet 数据集
     * @param：@throws IOException
     * @param：@throws NoSuchMethodException
     * @param：@throws SecurityException
     * @param：@throws InvocationTargetException
     * @return：void
     * @author yezhiyuan
     * @date 2017-3-14 下午3:13:12
     * @throws
     */
    @SuppressWarnings("unused")
    public void writeExcel(File file,Collection<T> dataSet) 
            throws IOException, NoSuchMethodException,SecurityException, 
            InvocationTargetException {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
        XSSFSheet sheet = workbook.getSheetAt(0);
        // 写入内容
        Iterator<T> iterator = dataSet.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            XSSFRow row = sheet.createRow(index);
            T t = (T) iterator.next();
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();

            for (short i = 0; i < fields.length; i++) {
                if (i == 0) {
                    XSSFCell cell = row.createCell(i);
                    cell.setCellValue(index);
                    cell = row.createCell(i + 1);
                    fields[i].setAccessible(true);
                    try {
                        String fieldName = fields[i].getName();
                        String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Object valueObject = fields[i].get(t);
                        Class<? extends Object> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        if (valueObject instanceof String) {
                            cell.setCellValue(valueObject.toString());
                        } else if (valueObject instanceof Date){
                        	cell.setCellValue(DateUtil.getDateStringConvert(new String(),(Date)valueObject, "yyyy-MM-dd HH:mm:ss"));
                        }else {
                            cell.setCellValue(valueObject + "");
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    XSSFCell cell = row.createCell(i + 1);
                    fields[i].setAccessible(true);
                    try {
                        String fieldName = fields[i].getName();
                        String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Object valueObject = fields[i].get(t);
                        Class<? extends Object> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        if (valueObject instanceof String) {
                            if (valueObject == null) {
                                cell.setCellValue("");
                            } else {
                                cell.setCellValue(valueObject.toString());
                            }
                        } else if (valueObject instanceof BigDecimal) {
                            BigDecimal vDecimal = (BigDecimal) value;
                            cell.setCellValue(vDecimal.doubleValue());
                        } else if (valueObject instanceof Integer) {
                            cell.setCellValue((Integer) valueObject);
                        } else if (valueObject instanceof Double) {
                            cell.setCellValue((Double) valueObject);
                        } else if (valueObject instanceof Date){
                        	cell.setCellValue(DateUtil.getDateStringConvert(new String(),(Date)valueObject, "yyyy-MM-dd HH:mm:ss"));
                        }else {
                            if (valueObject == null) {
                                cell.setCellValue("");
                            } else {
                                cell.setCellValue(valueObject.toString());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            index++;
        }
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        outputStream.close();
    }
    /**
     * 下载Excel
     * 
     * @param request
     * @param response
     * @param list 要导出的数据
     * @param model  模板名称
     * @param name 导出Excel文件名
     * @return
     * @throws IOException
     */
    public void download(HttpServletRequest request,HttpServletResponse response,
            List<T> list,String model,String name) throws IOException {
//        ServletOutputStream out = null;
        FileInputStream inputStream = null;
        File file = null;
        try {
//            response.setContentType("multipart/form-data");
            String path = this.getClass().getResource("/").getPath();// 获取模板路径
            path = path +"exceltemplate/";
            path += model + ".xlsx";//excel模板
            if(!new File(path).exists()){
            	path = templatePath + model + ".xlsx";
            }
//            String fileName = name +"_" + System.currentTimeMillis() + ".xlsx";
//            response.setHeader("Content-Disposition","attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
            FileUtils.Copy(path, downloadPath + name);
            file = new File(downloadPath + name);
            writeExcel(file, list);//组装数据
//            out = response.getOutputStream();
//            inputStream = new FileInputStream(file);
//            int b = 0;
//            byte[] buffer = new byte[4096];
//            while ((b = inputStream.read(buffer)) != -1) {
//                out.write(buffer, 0, b);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if(inputStream != null){
                inputStream.close();
        	}
//            out.flush();
//            out.close();
        }
    }

    /**
     * 基于Excel 2003模板写入数据
     * 
     * @Title: writeExcelContent
     * @param：@param file
     * @param：@param dataSet
     * @param：@throws IOException
     * @param：@throws NoSuchMethodException
     * @param：@throws SecurityException
     * @param：@throws InvocationTargetException
     * @return：void
     * @Description：TODO()
     * @date 
     * @throws
     */
    @SuppressWarnings("unused")
    public void writeExcel2003(File file, Collection<T> dataSet)
            throws IOException, NoSuchMethodException, SecurityException,
            InvocationTargetException {
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
        HSSFSheet sheet = workbook.getSheetAt(0);

        // 写入内容
        Iterator<T> iterator = dataSet.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            HSSFRow row = sheet.createRow(index);
            T t = (T) iterator.next();
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();

            for (short i = 0; i < fields.length; i++) {
                if (i == 0) {
                    @SuppressWarnings("deprecation")
                    HSSFCell cell = row.createCell(i);
                    cell.setCellValue(index);
                    cell = row.createCell(i + 1);
                    fields[i].setAccessible(true);
                    try {
                        String fieldName = fields[i].getName();
                        String getMethodName = "get"
                                + fieldName.substring(0, 1).toUpperCase()
                                + fieldName.substring(1);
                        Object valueObject = fields[i].get(t);

                        Class<? extends Object> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,
                                new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        if (valueObject instanceof String) {
                            cell.setCellValue(valueObject.toString());
                        } else {
                            cell.setCellValue(valueObject + "");
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    HSSFCell cell = row.createCell(i + 1);
                    fields[i].setAccessible(true);
                    try {
                        String fieldName = fields[i].getName();
                        String getMethodName = "get"
                                + fieldName.substring(0, 1).toUpperCase()
                                + fieldName.substring(1);
                        Object valueObject = fields[i].get(t);

                        Class<? extends Object> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,
                                new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        if (valueObject instanceof String) {
                            if (valueObject == null) {
                                cell.setCellValue("");
                            } else {
                                cell.setCellValue(valueObject.toString());
                            }

                        } else if (valueObject instanceof BigDecimal) {
                            BigDecimal vDecimal = (BigDecimal) value;
                            cell.setCellValue(vDecimal.doubleValue());
                        } else if (valueObject instanceof Integer) {
                            cell.setCellValue((Integer) valueObject);
                        } else if (valueObject instanceof Double) {
                            cell.setCellValue((Double) valueObject);
                        } else {
                            if (valueObject == null) {
                                cell.setCellValue("");
                            } else {
                                cell.setCellValue(valueObject.toString());
                            }

                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            index++;
        }
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);

        outputStream.close();
    } 
    
    public void writeExcel(File file,Collection<T> dataSet,List<String> rowNames)
            throws IOException, NoSuchMethodException,SecurityException, 
            InvocationTargetException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        // 写入内容
        Iterator<T> iterator = dataSet.iterator();
        int index = 1;
        while (iterator.hasNext()) {
            XSSFRow row = sheet.createRow(index);
            T t = (T) iterator.next();
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();

            for (short i = 0; i < fields.length; i++) {
                if (i == 0) {
                    XSSFCell cell = row.createCell(i);
                    cell.setCellValue(index);
                    cell = row.createCell(i + 1);
                    fields[i].setAccessible(true);
                    try {
                        String fieldName = fields[i].getName();
                        String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Object valueObject = fields[i].get(t);
                        Class<? extends Object> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        if (valueObject instanceof String) {
                            cell.setCellValue(valueObject.toString());
                        } else if (valueObject instanceof Date){
                        	cell.setCellValue(DateUtil.getDateStringConvert(new String(),(Date)valueObject, "yyyy-MM-dd HH:mm:ss"));
                        }else {
                            cell.setCellValue(valueObject + "");
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    XSSFCell cell = row.createCell(i + 1);
                    fields[i].setAccessible(true);
                    try {
                        String fieldName = fields[i].getName();
                        String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Object valueObject = fields[i].get(t);
                        Class<? extends Object> tCls = t.getClass();
                        Method getMethod = tCls.getMethod(getMethodName,new Class[]{});
                        Object value = getMethod.invoke(t, new Object[]{});
                        if (valueObject instanceof String) {
                            cell.setCellValue(valueObject.toString());
                        } else if (valueObject instanceof BigDecimal) {
                            BigDecimal vDecimal = (BigDecimal) value;
                            cell.setCellValue(vDecimal.doubleValue());
                        } else if (valueObject instanceof Integer) {
                            cell.setCellValue((Integer) valueObject);
                        } else if (valueObject instanceof Double) {
                            cell.setCellValue((Double) valueObject);
                        } else if (valueObject instanceof Date){
                        	cell.setCellValue(DateUtil.getDateStringConvert(new String(),(Date)valueObject, "yyyy-MM-dd HH:mm:ss"));
                        }else {
                            if (valueObject == null) {
                                cell.setCellValue("");
                            } else {
                                cell.setCellValue(valueObject.toString());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            index++;
        }
        XSSFRow row = sheet.createRow(0);
        if (row != null) {
        	int i = 0;
            XSSFCell cell = row.createCell(i);
        	cell.setCellValue("序号");
			for (String rowName : rowNames) {
                cell = row.createCell(i+1);
            	cell.setCellValue(rowName);
            	i++;
			}
		}
        OutputStream outputStream = new FileOutputStream(file);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
    
}