package com.ucpaas.sms.task.util;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataUtils {

	public static Integer sumInteger(Integer data,Integer add) {
		if(data==null&&add==null){
			return 0;
		}else if(data==null&&add!=null){
			return add;
		}else if(data!=null&&add==null){
			return data;
		}else if(data!=null&&add!=null){
			return data+add;
		}
		return 0;
	}

	public static BigDecimal sumBigDecimal(BigDecimal data, BigDecimal add) {
		if(data==null&&add==null){
			return BigDecimal.ZERO;
		}else if(data==null&&add!=null){
			return add;
		}else if(data!=null&&add==null){
			return data;
		}else if(data!=null&&add!=null){
			return data.add(add);
		}
		return BigDecimal.ZERO;
	}
}
