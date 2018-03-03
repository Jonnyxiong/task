package com.ucpaas.sms.task.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;

public class JFreeChartUtils {
	
	/**
	 * 保存为本地路径(后续从配置文件读取)
	 */
//	private static final String LOCAL_CHART_PATH = "E:/img/";
	
	/** 
	 * @Title: createDataset 
	 * @Description: 创建CategoryDataset对象 
	 * @param rowKeys x轴最下面的文字
	 * @param colKeys x轴下面的key
	 * @param data 数值集合
	 * @return
	 * @return: CategoryDataset
	 */
	public static TimeSeriesCollection createDataset() {
		
		TimeSeries s1 = new TimeSeries("XXXX");
        
        s1.add(new Day(1, 1, 2017),0.20);
        s1.add(new Day(2, 1, 2017),0.60);
        s1.add(new Day(3, 1, 2017),0.70);
        s1.add(new Day(4, 1, 2017),0.80);
        s1.add(new Day(5, 1, 2017),0.90);
        
        s1.add(new Day(6, 1, 2017),0.00);
        s1.add(new Day(7, 1, 2017),0.00);
        s1.add(new Day(8, 1, 2017),0.00);
        s1.add(new Day(9, 1, 2017),0.00);
        s1.add(new Day(10, 1, 2017),0.00);
        
        s1.add(new Day(11, 1, 2017),0.00);
        s1.add(new Day(12, 1, 2017),0.00);
        s1.add(new Day(13, 1, 2017),0.00);
        s1.add(new Day(14, 1, 2017),0.00);
        s1.add(new Day(15, 1, 2017),0.00);
        
        s1.add(new Day(16, 1, 2017),0.00);
        s1.add(new Day(17, 1, 2017),0.00);
        s1.add(new Day(18, 1, 2017),0.00);
        s1.add(new Day(19, 1, 2017),0.00);
        s1.add(new Day(20, 1, 2017),0.00);
        
        s1.add(new Day(21, 1, 2017),0.00);
        s1.add(new Day(22, 1, 2017),0.00);
        s1.add(new Day(23, 1, 2017),0.00);
        s1.add(new Day(24, 1, 2017),0.00);
        s1.add(new Day(25, 1, 2017),0.00);
        
        s1.add(new Day(26, 1, 2017),0.00);
        s1.add(new Day(27, 1, 2017),0.00);
        s1.add(new Day(28, 1, 2017),0.00);
        s1.add(new Day(29, 1, 2017),0.00);
        s1.add(new Day(30, 1, 2017),0.00);
        s1.add(new Day(31, 1, 2017),0.00);
 
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
		
		return dataset;
    }
	
	public static TimeSeriesCollection createDataset(List<Map<String,Object>> list) {
		
		TimeSeries s1 = new TimeSeries("XXXX");
		
		for(int i = 0;i < list.size(); i++){
			
			Map<String,Object> data = list.get(i);
			String data_str = data.get("data").toString();
			DateTime data_dt = new DateTime(data_str);
			int year = data_dt.getYear();
			int month = data_dt.getMonthOfYear();
			int day = data_dt.getDayOfMonth();
			
			String data_value_str = data.get("date_value").toString();
//			BigDecimal gd_value = new BigDecimal(data_value_str);
//			gd_value =  gd_value.setScale(3, BigDecimal.ROUND_HALF_UP);
			
			Double dou_val = Double.valueOf(data_value_str);
			
			s1.add(new Day(day, month, year),dou_val);
		}
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(s1);
		
		return dataset;
	}
	
	
	
	
	
	
	/** 
	 * @Title: createChart 
	 * @Description: TODO
	 * @param categoryDataset
	 * @param title   图片标题
	 * @param xLabel  x轴标题
	 * @param yLabel  y轴标题
	 * @return
	 * @return: JFreeChart
	 */
	public static JFreeChart createChartForSend(TimeSeriesCollection dataset,String title,String xLabel,String yLabel,Color color) {
		
		// 创建JFreeChart对象：ChartFactory.createLineChart  
		
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
				title,  //(标题)
				xLabel, //(X轴标签)
				yLabel, //(Y轴的标签)
				dataset);//(URLs)
		
		
		// 使用CategoryPlot设置各种参数。以下设置可以省略。  
		XYPlot plot = (XYPlot)jfreechart.getPlot();
		jfreechart.setTextAntiAlias(false);
		
		// 配置字体（解决中文乱码的通用方法)
		
		//x轴和y轴的标签字体
		Font xfont = new Font("微软雅黑", Font.BOLD, 18); // X轴  
		plot.getDomainAxis().setLabelFont(xfont);
//		plot.getDomainAxis().setLabelPaint(new Color(46, 169, 103));
		plot.getDomainAxis().setLabelPaint(color);
		
//		Font yfont = new Font("微软雅黑", Font.BOLD, 18); // Y轴 
//		plot.getRangeAxis().setLabelFont(yfont);
//		plot.getRangeAxis().setLabelPaint(new Color(46, 169, 103));
		
		//y坐标是整数
		NumberAxis numberaxis = (NumberAxis) plot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		//图片标题
		Font titleFont = new Font("微软雅黑", Font.BOLD, 20); 
		jfreechart.getTitle().setFont(titleFont);
//		jfreechart.getTitle().setPaint(new Color(46, 169, 103));
		jfreechart.getTitle().setPaint(color);
		
		jfreechart.getLegend().setVisible(false); //设置X轴底部字体不可见
		
		// 背景色 透明度  
		plot.setBackgroundAlpha(1f);  
		plot.setBackgroundPaint(Color.WHITE);
		
		//设置网格竖线颜色
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setDomainGridlinesVisible(true);
		
		//设置网格横线颜色
		plot.setRangeGridlinePaint(Color.GRAY);
//		plot.setRangeGridlineStroke(new BasicStroke(1));
		plot.setDomainGridlinesVisible(false);
		
		DateAxis domainAxis = (DateAxis) plot.getDomainAxis(); //x轴设置
		DateTickUnit dateTickUnit = new DateTickUnit(DateTickUnitType.DAY, 2, new SimpleDateFormat("dd"));
		domainAxis.setTickUnit(dateTickUnit);
		
		//线条颜色
//		Color lineColor = new Color(46, 169, 103);
		plot.getRenderer().setSeriesPaint(0, color);
		
		XYLineAndShapeRenderer xyrenderer = (XYLineAndShapeRenderer) plot.getRenderer();
		xyrenderer.setSeriesStroke(0, new BasicStroke(3.5F));//设置折线大小
		
		return jfreechart;  
	}
	
	public static JFreeChart createChartForConsume(TimeSeriesCollection dataset,String title,String xLabel,String yLabel,Color color,String sbflag) {
		
		// 创建JFreeChart对象：ChartFactory.createLineChart  
		
		JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(
				title,  //(标题)
				xLabel, //(X轴标签)
				yLabel, //(Y轴的标签)
				dataset);//(URLs)
		
		
		// 使用CategoryPlot设置各种参数。以下设置可以省略。  
		XYPlot plot = (XYPlot)jfreechart.getPlot();
		jfreechart.setTextAntiAlias(false);
		
		// 配置字体（解决中文乱码的通用方法)
		
		//x轴和y轴的标签字体
		Font xfont = new Font("微软雅黑", Font.BOLD, 18); // X轴  
		plot.getDomainAxis().setLabelFont(xfont);
//		plot.getDomainAxis().setLabelPaint(new Color(46, 169, 103));
		plot.getDomainAxis().setLabelPaint(color);
		
//		Font yfont = new Font("微软雅黑", Font.BOLD, 18); // Y轴 
//		plot.getRangeAxis().setLabelFont(yfont);
//		plot.getRangeAxis().setLabelPaint(new Color(46, 169, 103));
		
		if("allZero".equals(sbflag)){
			//y坐标是整数
			NumberAxis numberaxis = (NumberAxis) plot.getRangeAxis();
			numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		
		//图片标题
		Font titleFont = new Font("微软雅黑", Font.BOLD, 20); 
		jfreechart.getTitle().setFont(titleFont);
//		jfreechart.getTitle().setPaint(new Color(46, 169, 103));
		jfreechart.getTitle().setPaint(color);
		
		jfreechart.getLegend().setVisible(false); //设置X轴底部字体不可见
		
		// 背景色 透明度  
		plot.setBackgroundAlpha(1f);  
		plot.setBackgroundPaint(Color.WHITE);
		
		//设置网格竖线颜色
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setDomainGridlinesVisible(true);
		
		//设置网格横线颜色
		plot.setRangeGridlinePaint(Color.GRAY);
//		plot.setRangeGridlineStroke(new BasicStroke(1));
		plot.setDomainGridlinesVisible(false);
		
		DateAxis domainAxis = (DateAxis) plot.getDomainAxis(); //x轴设置
		DateTickUnit dateTickUnit = new DateTickUnit(DateTickUnitType.DAY, 2, new SimpleDateFormat("dd"));
		domainAxis.setTickUnit(dateTickUnit);
		
		//线条颜色
//		Color lineColor = new Color(46, 169, 103);
		plot.getRenderer().setSeriesPaint(0, color);
		
		XYLineAndShapeRenderer xyrenderer = (XYLineAndShapeRenderer) plot.getRenderer();
		xyrenderer.setSeriesStroke(0, new BasicStroke(3.5F));//设置折线大小
		
		return jfreechart;  
	}
	
	
	
	
	/** 
	 * @Title: saveAsFile 
	 * @Description: 保存为文件
	 * @param chart
	 * @param outputPath 保存文件路径
	 * @param weight 图片宽度
	 * @param height 图片高度
	 * @return: void
	 */
	public static void saveAsFile(JFreeChart chart, String outputPath,int width, int height) {  
		FileOutputStream out = null;  
		try {  
			File outFile = new File(outputPath);  
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();  
			}  
			out = new FileOutputStream(outputPath);  
			//保存为PNG
//			ChartUtilities.writeChartAsPNG(out, chart, weight, height);
			
			ChartUtilities.writeChartAsJPEG(out, chart, width, height);
			
			out.flush();  
		} catch (FileNotFoundException e) {
			e.printStackTrace();  
		} catch (IOException e){
			e.printStackTrace();  
		} finally {  
			if (out != null) {
				try {  
					out.close();  
				} catch (IOException e) {  
					// do nothing  
				}  
			}  
		}  
	}
	
	
	//创建图片，并保存到本地路径，返回图片的路径
	public static String createImageAndSave(List<Map<String,Object>> list,String flag){
		
		//步骤1：创建CategoryDataset对象(组装数据)
//		TimeSeriesCollection dataset = createDataset(list);
		StringBuffer sbflag = new StringBuffer();
		TimeSeriesCollection dataset = ChartUtils.createDataset(list,sbflag);
		
		//步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置 
		String title = null;
		String xLabel = null;
		String yLabel = null;
		String outputPath = null;
		
		Color color = null;
		
		JFreeChart freeChart = null;
		if("send".equals(flag)){
			title = "";
			xLabel = "";
			yLabel = "";
			outputPath = ConfigUtils.img_temp_path +"send.jpeg";
			color = new Color(46, 169, 103);
			
			freeChart = createChartForSend(dataset, title, xLabel, yLabel,color);
			
		}else{
			title = "";
			xLabel = "";
			yLabel = "";
			outputPath = ConfigUtils.img_temp_path +"consume.jpeg";
			color = new Color(50, 158, 237);
			freeChart = createChartForConsume(dataset, title, xLabel, yLabel,color,sbflag.toString());
			
		}
		
		//步骤3：将JFreeChart对象输出到文件
		saveAsFile(freeChart, outputPath, 700, 517);
		
		return outputPath;
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		/*//步骤1：创建CategoryDataset对象（准备数据）  
		TimeSeriesCollection dataset = createDataset();
		
		//步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置  
		String title = "";
		String xLabel = "";
		String yLabel = "";
		
		Color color = new Color(50, 158, 237);
		JFreeChart freeChart = createChart(dataset, title, xLabel, yLabel,color);
		
		//步骤3：将JFreeChart对象输出到文件
		String outputPath = "E:\\img\\line.jpeg";
		
		saveAsFile(freeChart, outputPath, 700, 517);
		
		System.out.println("----------------->");*/
	}
	
	
	

}
