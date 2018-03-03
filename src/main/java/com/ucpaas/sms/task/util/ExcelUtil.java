package com.ucpaas.sms.task.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出Excel文件工具类
 *
 * @author tanjiangqiang
 * @date 2018-01-26
 */
public class ExcelUtil {

    /**
     * 创建工作表格并导出
     *
     * @param RedRowName  需要标红的列名
     * @param sheetName   sheet名和文件标题
     * @param headers     每列的表头明
     *                    ，比如['编号','名称']
     * @param columnName  每列表头对应的属性名
     *                    ，比如['id','name']
     * @param columnWidth 设置每列宽度
     *                    ,默认4000,
     * @param exportList  导出的数据
     * @return
     */

    public static Workbook export(String RedRowName, String sheetName, String[] headers, String[] columnName, Integer[] columnWidth, List<Map<String, Object>> exportList) {
        // 创建excel工作簿
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        // 数据的总长度
        int columnNum = 0;

        try {

            // 获得列的数量
            columnNum = exportList.size();

            // 创建表格标题样式
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 15);
            font.setBold(true);
            font.setColor(IndexedColors.RED.getIndex());
            titleStyle.setFont(font);

            // 创建表格内容样式
            CellStyle contentStyle = wb.createCellStyle();
            contentStyle.setAlignment(HorizontalAlignment.CENTER);
            Font contentFont = wb.createFont();
            contentFont.setColor(IndexedColors.RED.getIndex());
            contentStyle.setFont(contentFont);

            // 第一行合并单元格做表头
            Row titleRow = sheet.createRow((short) 0);
            titleRow.setHeight((short) (25 * 20));
            sheet.addMergedRegion(new CellRangeAddress((short) 0, (short) 0, (short) 0, (short) headers.length - 1));
            Cell headerCell = titleRow.createCell((short) 0);
            headerCell.setCellValue(sheetName);
            headerCell.setCellStyle(titleStyle);

            List<String> indexList = new ArrayList<>();

            // 创建表头
            Row headerRow = sheet.createRow((short) 1);
            for (int i = 0; i < columnName.length; i++) {
                Cell cell = headerRow.createCell((short) i);
                String column = columnName[i];
                if (column.indexOf(RedRowName) != -1) {
                    cell.setCellStyle(contentStyle);
                    // 记录标红位置
                    indexList.add(String.valueOf(i));
                }
                cell.setCellValue(column);
                // 如果设置了列宽度，则设置上
                if (columnWidth != null && columnWidth[i] != null) {
                    sheet.setColumnWidth(i, columnWidth[i]);
                } else {
                    sheet.setColumnWidth(i, 4000);
                }
            }

            // 创建数据单元格
            for (int i = 0; i < exportList.size(); i++) {
                Row contentRow = sheet.createRow((short) i + 2);
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = contentRow.createCell((short) j);
                    // 传递的list强转为map
                    Map<String, Object> map = exportList.get(i);
                    String value = map.get(headers[j]) + "";
                    if (value == null || value == "" || value.equals("null")) {
                        value = "";
                    }
                    cell.setCellValue(value);
                    if (indexList.contains(String.valueOf(j))) {
                        cell.setCellStyle(contentStyle);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wb;
    }


    public static Map<String, Object> beanToMap(Object bean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class<? extends Object> type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<String, Object>();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, null);
                }
            }
        }
        return returnMap;
    }

    public static <T> List<Map<String, Object>> beanListToMapList(List<T> beanList, Class<T> T) throws Exception {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (int i = 0, n = beanList.size(); i < n; i++) {
            Object bean = beanList.get(i);
            Map<String, Object> map = beanToMap(bean);
            mapList.add(map);
        }
        return mapList;
    }

}
