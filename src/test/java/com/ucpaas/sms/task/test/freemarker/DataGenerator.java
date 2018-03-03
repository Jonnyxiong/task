package com.ucpaas.sms.task.test.freemarker;

import com.ucpaas.sms.task.test.model.ChannelDetail;
import com.ucpaas.sms.task.test.model.CommonlyChannel;
import com.ucpaas.sms.task.test.model.CommonlyChannelDetail;
import com.ucpaas.sms.task.test.model.Overall;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataGenerator {

	public static Map<String, Object> generate() {
		Map<String,Object> data =new HashMap<>();
		//整体情况数据
		List<Overall> overall = new ArrayList<>();
		overall.add(new Overall("2017/6/1", 8278647l, 8278647l, new BigDecimal("0.95"), 7851106l, 41663l, 7851106l, 385878l, new BigDecimal("195130.483"), 385878l,  new BigDecimal("195130.483"), new BigDecimal("2257.3")));
		overall.add(new Overall("2017/6/2", 8215098l, 8215098l, new BigDecimal("0.96"), 7505847l, 41663l, 7851106l, 385878l, new BigDecimal("195130.483"), 385878l,  new BigDecimal("195130.483"), new BigDecimal("-2257.1")));
		overall.add(new Overall("2017/6/3", 7777551l, 7777551l, new BigDecimal("0.96"), 7892769l, 41663l, 7851106l, 385878l, new BigDecimal("195130.483"), 385878l,  new BigDecimal("195130.483"), new BigDecimal("-2257.0")));
		overall.add(new Overall("2017/6/4", 7231167l, 7231167l, new BigDecimal("0.96"), 1432134l, 41663l, 7851106l, 385878l, new BigDecimal("195130.483"), 385878l,  new BigDecimal("195130.483"), new BigDecimal("2257.0")));
		overall.add(new Overall("2017/6/5", 9505152l, 9505152l, new BigDecimal("0.95"), 0l, 41663l, 7851106l, 385878l, new BigDecimal("195130.483"), 385878l,  new BigDecimal("195130.483"), new BigDecimal("2257.1")));
		overall.add(new Overall("合计", 9505152l, 9505152l, new BigDecimal("0.95"), 0l, 41663l, 7851106l, 385878l, new BigDecimal("195130.483"), 385878l,  new BigDecimal("195130.483"), new BigDecimal("2257.1")));
		data.put("overall", overall);
		
		//昨日通道使用明细
		List<ChannelDetail> channelDetails = new ArrayList<>();
		channelDetails.add(new ChannelDetail("2017/6/6", "a00101", "移动行业-阿里", "电信", "7609", "诚立业-电信-营销-301037", 586l, 586l, new BigDecimal("0.30546"), 222l, 0l, 43l, 179l, 0l, 0l, 364l, 0l, 0l, 0l, 0l, new BigDecimal("0.025"), new BigDecimal("4.475"), new BigDecimal("0.021"), 123l, new BigDecimal("4.662"), new BigDecimal("4.662"), new BigDecimal("1")));
		channelDetails.add(new ChannelDetail("2017/6/6", "a00101", "移动行业-阿里", "联通", "2001", "诚立业-电信-营销-301037", 586l, 586l, new BigDecimal("0.30546"), 222l, 0l, 43l, 179l, 0l, 0l, 364l, 0l, 0l, 0l, 0l, new BigDecimal("0.025"), new BigDecimal("4.475"), new BigDecimal("0.021"), 123l, new BigDecimal("4.662"), new BigDecimal("4.662"), new BigDecimal("1")));
		data.put("channelDetails", channelDetails);
		
		
		//本月常用通道信息
		List<CommonlyChannel> ccList = new ArrayList<>();
		List<CommonlyChannelDetail> list = new ArrayList<>();
		list.add(new CommonlyChannelDetail("5626", "惠承通-移动-营销-100030", "0.0265", "aaa", "99.71121%"));
		list.add(new CommonlyChannelDetail("5623", "惠承通-移动-营销-100030", "0.0265", "aaa", "99.71121%"));
		list.add(new CommonlyChannelDetail("5624", "惠承通-移动-营销-100030", "0.0265", "aaa", "99.71121%"));
		list.add(new CommonlyChannelDetail("5625", "惠承通-移动-营销-100030", "0.0265", "aaa", "99.71121%"));
		ccList.add(new CommonlyChannel("移动", list));
		list = new ArrayList<>();
		list.add(new CommonlyChannelDetail("5636", "惠承通-移动-电信-100030", "0.0265", "aaa", "99.71121%"));
		list.add(new CommonlyChannelDetail("5633", "惠承通-移动-电信-100030", "0.0265", "aaa", "99.71121%"));
		ccList.add(new CommonlyChannel("电信", list));
		list = new ArrayList<>();
		list.add(new CommonlyChannelDetail("5646", "惠承通-联通-电信-100030", "0.0265", "aaa", "99.71121%"));
		ccList.add(new CommonlyChannel("联通", list));
		
		List<String> channelOverall = format(ccList);
//		data.put("channelOverall",channelOverall);
		return data;
	}

	private static List<String> format(List<CommonlyChannel> ccList) {
		List<String > channelOverall = new ArrayList<>();
		for(CommonlyChannel cc:ccList){
			StringBuilder sb = new StringBuilder();
			sb.append("<td rowspan=\"").append(cc.getChannelSize()).append("\" height=\"180\" class=\"xl77\" style=\"border-style: solid solid solid; border-bottom-width: 1pt; height: "+(13.8*cc.getChannelSize())+"pt; border-top-width: 0.5pt; border-top-color: windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: middle; border-right-width: 0.5pt; border-left-width: 0.5pt; border-right-color: windowtext; border-left-color: windowtext; white-space: nowrap; text-align: center;\">");
			sb.append(cc.getChanneloperatorstype()).append("</td>");
			boolean isFirstTd = true;
			for(CommonlyChannelDetail ccd:cc.getList()){
				if(!isFirstTd)
					sb = new StringBuilder();
				sb.append("<td class=\"xl72\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap; text-align: center;\">");
				sb.append(ccd.getChannelid()).append("</td>");
				sb.append("<td class=\"xl67\" align=\"left\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
				sb.append(ccd.getChannelremark()).append("</td>");
				sb.append("<td class=\"xl67\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
				sb.append(ccd.getCostfee()).append("</td>");
				sb.append("<td class=\"xl73\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
				sb.append(ccd.getSuccessrate()).append("</td>");
				sb.append(" <td class=\"xl75\" align=\"right\" style=\"border: 0.5pt solid windowtext; padding-top: 1px; padding-right: 1px; padding-left: 1px; color: windowtext; font-size: 9pt; font-family: 微软雅黑, sans-serif; vertical-align: bottom; white-space: nowrap;\">");
				sb.append(ccd.getInOutRate()).append("</td>");
				isFirstTd = false;
				channelOverall.add(sb.toString());
			}
		}
		return channelOverall;
	}


}
