package com.ucpaas.sms.task.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JacksonUtil {
	private static Logger logger = LoggerFactory.getLogger(JacksonUtil.class);
    private  static ObjectMapper mapper = new ObjectMapper(); // create once, reuse

    public static  String toJSON(Object o){
        try {
			return  mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			logger.error("转换JSON异常, object="+o, e);
			return "";
		}
    }
    /**
     * 将json字符串转换成为对象
     *
     * @param json
     * @param classOfT
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    public static <T> T toObject(String json, Class<T> classOfT) throws JsonParseException, JsonMappingException, IOException{
        return mapper.readValue(json, classOfT);
    }
}
