package com.jh.user.util;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;

    /**
     *  *
     *
     * @Title: ExportExcelUtil.java
     * @Package com.jarmsystem.web.util   类描述: 基于POI的javaee导出Excel工具类
     * @author ives
     * @date 2019年9月10日 15:59:57
     * @version V1.0
     */
    public class ExPortExcelUtil {
        /**
         * @param response    请求
         * @param fileName    文件名 如："用户表"
         * @param excelHeader excel表头数组，存放"管理员#admin"格式字符串，"管理员"为excel标题行， "admin"为对象字段名
         * @param dataLis     数据集合，需与表头数组中的字段名一致，并且符合javabean规范（驼峰命名法）
         * @return 返回一个HSSFWorkbook
         * @throws Exception
         */
        public static <T> HSSFWorkbook export(HttpServletResponse response, String fileName, String[] excelHeader,
                                              Collection<T> dataLis) throws Exception {
            response.setContentType("application/x-download");
            response.setCharacterEncoding("utf-8");// 处理编码问题
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + new String(fileName.getBytes("gbk"), "iso8859-1") + ".xls");// 表头编码问题
            // 创建一个工作薄
            HSSFWorkbook wb = new HSSFWorkbook();
            // 设置标题样式
            HSSFCellStyle titleStyle = wb.createCellStyle();
            // 设置单元格边框样式
            titleStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框 细边线
            titleStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 下边框 细边线
            titleStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框 细边线
            titleStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框 细边线
            // 设置单元格对齐方式
            titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
            titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中
            // 设置字体样式
            Font titleFont = wb.createFont();
            titleFont.setFontHeightInPoints((short) 15);// 字体高度
            titleFont.setFontName("黑体");// 字体样式
            titleStyle.setFont(titleFont);

            // 在 workBook 工作簿中添加一个sheet(工作表) 对应excel文件中的sheet
            HSSFSheet sheet = wb.createSheet();
            // 标题数组
            String[] titleArray = new String[excelHeader.length];
            // 字段名数组
            String[] fieldArray = new String[excelHeader.length];
            for (int i = 0; i < excelHeader.length; i++) {
                String[] tempArray = excelHeader[i].split("#");
                titleArray[i] = tempArray[0];
                fieldArray[i] = tempArray[1];
            }

            // 在sheet中添加标题行
            HSSFRow row = sheet.createRow(0);// 行数从0开始
            // 自动设置宽度
            sheet.autoSizeColumn(0);
            // 设置表格默认列宽度
            sheet.setDefaultColumnWidth(20);

            // 为标题行赋值
            for (int i = 0; i < titleArray.length; i++) {
                // 需要序号就需要+1 因为0号位被序号占用
                HSSFCell titleCell = row.createCell(i);
                titleCell.setCellValue(titleArray[i]);
                titleCell.setCellStyle(titleStyle);
                sheet.autoSizeColumn(i + 1); // 0 号被序号占用

            }

            // 字段的数据样式 标题和字段的数据样式不同，需分开设置
            HSSFCellStyle dataStyle = wb.createCellStyle();
            // 设置数据边框
            dataStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            dataStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
            dataStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            dataStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

            // 设置居中样式
            dataStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平居中
            dataStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直居中

            // 设置数据字体
            Font dataFont = wb.createFont();
            dataFont.setFontHeightInPoints((short) 12);// 字体高度
            dataFont.setFontName("宋体");// 字体
            dataStyle.setFont(dataFont);

            // 遍历数据行，产生数据行
            Iterator<T> it = dataLis.iterator();
            int index = 0;
            while (it.hasNext()) {
                index++; // 老话 0号位被占用
                row = sheet.createRow(index);
                T t = it.next();
                // 利用反射 根据传过来的字段名数组，动态调用对应的getxxx()方法得到属性值
                for (int i = 0; i < fieldArray.length; i++) {
                    // 需要序号 就需要 i+1
                    HSSFCell dataCell = row.createCell(i);
                    dataCell.setCellStyle(dataStyle);
                    sheet.autoSizeColumn(i);
                    String fieldName = fieldArray[i];
                    // 取得对应的getxxx()方法  实体类命名一定要用驼峰才能分割成功
                    String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Class<? extends Object> tCls = t.getClass();// 泛型为Object以及所有Object的子类
                    Method getMethod = tCls.getMethod(getMethodName, new Class[]{});// 通过方法名得到对应的方法
                    Object value = getMethod.invoke(t, new Object[]{});// 动态调用方法,得到属性值
                    if (value != null) {
                        dataCell.setCellValue(value.toString());// 为当前列赋值
                    }
                }

            }

            OutputStream outputStream = response.getOutputStream();// 打开流
            wb.write(outputStream);// HSSFWorkbook写入流
            wb.close();// HSSFWorkbook关闭
            outputStream.flush();// 刷新流
            outputStream.close();// 关闭流

            // excel 各种样式
            // XSSFCellStyle.ALIGN_CENTER 居中对齐
            // XSSFCellStyle.ALIGN_LEFT 左对齐
            // XSSFCellStyle.ALIGN_RIGHT 右对齐
            // XSSFCellStyle.VERTICAL_TOP 上对齐
            // XSSFCellStyle.VERTICAL_CENTER 中对齐
            // XSSFCellStyle.VERTICAL_BOTTOM 下对齐
            // CellStyle.BORDER_DOUBLE 双边线
            // CellStyle.BORDER_THIN 细边线
            // CellStyle.BORDER_MEDIUM 中等边线
            // CellStyle.BORDER_DASHED 虚线边线
            // CellStyle.BORDER_HAIR 小圆点虚线边线
            // CellStyle.BORDER_THICK 粗边线

            return wb;
        }
    }



