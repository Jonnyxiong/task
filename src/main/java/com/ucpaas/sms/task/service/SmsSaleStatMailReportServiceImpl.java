package com.ucpaas.sms.task.service;

import com.sun.mail.smtp.SMTPAddressFailedException;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.common.util.excel.ExportExcel;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.task.constant.SysConstant;
import com.ucpaas.sms.task.entity.message.Account;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.mapper.message.AccountMapper;
import com.ucpaas.sms.task.model.SaleStat;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.ConfigUtils;
import com.ucpaas.sms.task.util.DateUtilsNew;
import com.ucpaas.sms.task.util.ExcelUtil;
import com.ucpaas.sms.task.util.PropertiesUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 每日短信销售情况邮件报表
 */
@Service
public class SmsSaleStatMailReportServiceImpl implements SmsSaleStatMailReportService {
    @Autowired
    private AccessChannelStatisticsMapper accessChannelStatisticsMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    @Qualifier("javaMailSenderAlarm")
    private JavaMailSender javaMailSenderAlarm;
    private static final Logger logger = LoggerFactory.getLogger(SmsSaleStatMailReportService.class);

    private Account getAccount(String clientid, List<Account> accounts) {
        Account acc = null;
        for (Account account : accounts) {
            if (clientid.equals(account.getClientid())) {
                acc = account;
                break;
            }
        }
        return acc;
    }

    private int addCount(Map<String, Object> realData, Map<String, Object> data, String field) {
        Integer count = realData.get(field) == null ? 0 : Integer.parseInt(realData.get(field).toString());
        return count + Integer.parseInt(data.get(field).toString());
    }

    private boolean send(String receiver, String fileName, String ymr) {
        try {
            String[] mails = receiver.split(",");
            //邮件发送地址（群发）
            InternetAddress[] addresses = new InternetAddress[mails.length];
            for (int i = 0; i < mails.length; i++) {
                addresses[i] = new InternetAddress(mails[i]);
            }
            MimeMessage msg = javaMailSenderAlarm.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "utf-8");
            helper.setFrom("alarm@ucpaas.com");
//			helper.setTo(receiver);
            helper.setTo(addresses);
            helper.setSubject("【" + ymr + "新短信销售情况通报】");
            helper.setText("你好！\n" + "\t\t\t" + ymr + " 的新短信销售情况通报请查看附件内容(统计规则为拆分后的1+3)，谢谢！");
            FileSystemResource file = new FileSystemResource(fileName);
            helper.addAttachment(file.getFilename(), file);
            try {
                //第一次发送
                javaMailSenderAlarm.send(msg);
            } catch (MailSendException e2) {
                ArrayList<InternetAddress> internetAddresses = new ArrayList<>(Arrays.asList(addresses));
                Map<Object, Exception> failedMessages = e2.getFailedMessages();
                Iterator<Entry<Object, Exception>> iterator = failedMessages.entrySet().iterator();
                for (Entry<Object, Exception> objectExceptionEntry : failedMessages.entrySet()) {
                    Exception value = objectExceptionEntry.getValue();
                    removeFailEmailByException(value, internetAddresses);
                    //去掉错误的地址重新发送
                    InternetAddress[] fixAddress = new InternetAddress[internetAddresses.size()];
                    for (int i = 0; i < internetAddresses.size(); i++) {
                        InternetAddress internetAddress = internetAddresses.get(i);
                        fixAddress[i] = internetAddress;
                    }
                    helper.setTo(fixAddress);
                    //失败重发
                    javaMailSenderAlarm.send(msg);
                }
            }
//			javaMailSenderAlarm.send(msg);
            logger.debug("发送Email【成功】：to={}", receiver);
        } catch (Throwable e) {
            logger.error("发送Email【失败】：to={} 错误 --->", receiver, e);
            return false;
        }
        return true;
    }

    private List<Map<String, Object>> getStatReport(String ym, String ymr) {
        // 查询统计数据
        List<Map<String, Object>> maps = accessChannelStatisticsMapper.findSaleEveryDaySendSMSOfMonth(ym, ymr);
        if (Collections3.isEmpty(maps)) {
            return maps;
        }
        // 查询客户数据
        List<Account> accounts = accountMapper.findClientCompanyAndBelongSale();
        // 以公司名称分组，将相同公司的数据合并
        Map<String, Map<String, Object>> data = new HashMap<>();
        for (Map<String, Object> map : maps) {
            // 找到归属客户
            String clientid = map.get("clientid").toString();
            Account account = getAccount(clientid, accounts);
            if (account == null) {
                account = new Account();
                account.setName("");
            }
            String company = account.getName();
            // 若组为空，就放入名称，否则以客户组分
            String groupId = account.getGroupId() == null ? company : account.getGroupId().toString();
            map.put("groupId", groupId);
            map.put("company", company);
            map.put("realname", account.getRealname());
            Map<String, Object> realData = data.get(groupId);
            if (realData == null) {
                data.put(groupId, map);
            } else {
                // 将1到30号的都累加，将总数累加
                realData.put("sendall", addCount(realData, map, "sendall"));
                realData.put("1", addCount(realData, map, "1"));
                realData.put("2", addCount(realData, map, "2"));
                realData.put("3", addCount(realData, map, "3"));
                realData.put("4", addCount(realData, map, "4"));
                realData.put("5", addCount(realData, map, "5"));
                realData.put("6", addCount(realData, map, "6"));
                realData.put("7", addCount(realData, map, "7"));
                realData.put("8", addCount(realData, map, "8"));
                realData.put("9", addCount(realData, map, "9"));
                realData.put("10", addCount(realData, map, "10"));
                realData.put("11", addCount(realData, map, "11"));
                realData.put("12", addCount(realData, map, "12"));
                realData.put("13", addCount(realData, map, "13"));
                realData.put("14", addCount(realData, map, "14"));
                realData.put("15", addCount(realData, map, "15"));
                realData.put("16", addCount(realData, map, "16"));
                realData.put("17", addCount(realData, map, "17"));
                realData.put("18", addCount(realData, map, "18"));
                realData.put("19", addCount(realData, map, "19"));
                realData.put("20", addCount(realData, map, "20"));
                realData.put("21", addCount(realData, map, "21"));
                realData.put("22", addCount(realData, map, "22"));
                realData.put("23", addCount(realData, map, "23"));
                realData.put("24", addCount(realData, map, "24"));
                realData.put("25", addCount(realData, map, "25"));
                realData.put("26", addCount(realData, map, "26"));
                realData.put("27", addCount(realData, map, "27"));
                realData.put("28", addCount(realData, map, "28"));
                realData.put("29", addCount(realData, map, "29"));
                realData.put("30", addCount(realData, map, "30"));
                realData.put("31", addCount(realData, map, "31"));
                // 将clientid追加
                realData.put("clientid", realData.get("clientid").toString() + "," + map.get("clientid").toString());
            }
        }
        // 将原始查询到的数据清空
        maps.clear();
        // 放入分组后的数据
        for (String s : data.keySet()) {
            maps.add(data.get(s));
        }
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).get("realname") == null)
                maps.get(i).put("realname", "");
        }
        // 以发送总数进行排序
        Collections.sort(maps, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
                return Integer.valueOf(arg1.get("sendall").toString())
                        .compareTo(Integer.valueOf(arg0.get("sendall").toString()));
            }
        });
        // 按顺序取出所有的销售并累加该销售的销售总数
        Map<String, Integer> order = new HashMap<>();
        for (int i = 0; i < maps.size(); i++) {
            String realName = maps.get(i).get("realname").toString();
            Integer count = order.get(realName);
            if (count == null) {
                order.put(realName, Integer.valueOf(maps.get(i).get("sendall").toString()));
            } else {
                count = count + Integer.valueOf(maps.get(i).get("sendall").toString());
                order.put(realName, count);
            }
        }
        // 排序后生成新的List
        List<Map.Entry<String, Integer>> list = new ArrayList<>(order.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            // 升序排序
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        Map<String, Object> totalRecord = new HashMap<>();
        totalRecord.put("company", "-");
        totalRecord.put("clientid", "-");
        totalRecord.put("realname", "总计：");
        for (Map<String, Object> map : maps) {
            // 将1到30号的都累加，将总数累加
            totalRecord.put("sendall", addCount(totalRecord, map, "sendall"));
            totalRecord.put("1", addCount(totalRecord, map, "1"));
            totalRecord.put("2", addCount(totalRecord, map, "2"));
            totalRecord.put("3", addCount(totalRecord, map, "3"));
            totalRecord.put("4", addCount(totalRecord, map, "4"));
            totalRecord.put("5", addCount(totalRecord, map, "5"));
            totalRecord.put("6", addCount(totalRecord, map, "6"));
            totalRecord.put("7", addCount(totalRecord, map, "7"));
            totalRecord.put("8", addCount(totalRecord, map, "8"));
            totalRecord.put("9", addCount(totalRecord, map, "9"));
            totalRecord.put("10", addCount(totalRecord, map, "10"));
            totalRecord.put("11", addCount(totalRecord, map, "11"));
            totalRecord.put("12", addCount(totalRecord, map, "12"));
            totalRecord.put("13", addCount(totalRecord, map, "13"));
            totalRecord.put("14", addCount(totalRecord, map, "14"));
            totalRecord.put("15", addCount(totalRecord, map, "15"));
            totalRecord.put("16", addCount(totalRecord, map, "16"));
            totalRecord.put("17", addCount(totalRecord, map, "17"));
            totalRecord.put("18", addCount(totalRecord, map, "18"));
            totalRecord.put("19", addCount(totalRecord, map, "19"));
            totalRecord.put("20", addCount(totalRecord, map, "20"));
            totalRecord.put("21", addCount(totalRecord, map, "21"));
            totalRecord.put("22", addCount(totalRecord, map, "22"));
            totalRecord.put("23", addCount(totalRecord, map, "23"));
            totalRecord.put("24", addCount(totalRecord, map, "24"));
            totalRecord.put("25", addCount(totalRecord, map, "25"));
            totalRecord.put("26", addCount(totalRecord, map, "26"));
            totalRecord.put("27", addCount(totalRecord, map, "27"));
            totalRecord.put("28", addCount(totalRecord, map, "28"));
            totalRecord.put("29", addCount(totalRecord, map, "29"));
            totalRecord.put("30", addCount(totalRecord, map, "30"));
            totalRecord.put("31", addCount(totalRecord, map, "31"));
        }
        // 生成最终的数据
        List<Map<String, Object>> temp = new ArrayList<>();
        // 添加总计
        temp.add(totalRecord);
        for (Map.Entry<String, Integer> sale : list) {
            for (int j = 0; j < maps.size(); j++) {
                if ((maps.get(j).get("realname").toString().equals(sale.getKey()))) {
                    temp.add(maps.get(j));
                }
            }
        }
        return temp;
    }

    /**
     * 后付费计费
     *
     * @return 是否成功
     */
    public boolean statReportAndSend(TaskInfo taskInfo) {
        Calendar begin = Calendar.getInstance();
        logger.debug("【每日短信销售情况邮件报表】开始 = {}", DateUtilsNew.formatDateTime(begin.getTime()));
        String params = null;
        try {
            params = PropertiesUtil.get("SmsSaleStatMailReport");
        } catch (Exception e) {
            logger.error("【每日短信销售情况邮件报表】 读取配置文件错误{}", e);
        }
        if (StringUtils.isBlank(params)) {
            logger.error("【每日短信销售情况邮件报表】 收件人为空");
            return true;
        }
//		String[] mails = params.split(",");
        // 获取当月
        String ym = DateUtilsNew.formatDate(begin.getTime(), "yyyyMM");
        // 获取今天
        String ymrCurr = DateUtilsNew.formatDate(begin.getTime(), "yyyyMMdd");
        // 获取本月的第一天
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 00);
        cal.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        String ymr = DateUtilsNew.formatDate(cal.getTime(), "yyyyMMdd");
        // 获取数据
        List<Map<String, Object>> maps = getStatReport(ym, ymr);
        // 若数据为空取上月的数据
        if (maps == null || maps.size() <= 0) {
            Calendar calendar = Calendar.getInstance();
            // 获取上个月月份
            calendar.add(Calendar.MONTH, -1);
            ym = DateUtilsNew.formatDate(calendar.getTime(), "yyyyMM");
            calendar.set(Calendar.HOUR_OF_DAY, 00);
            calendar.set(Calendar.MINUTE, 00);
            calendar.set(Calendar.SECOND, 00);
            calendar.set(Calendar.MILLISECOND, 00);
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            ymr = DateUtilsNew.formatDate(calendar.getTime(), "yyyyMMdd");
            maps = getStatReport(ym, ymr);
        }
//		String filePath = ConfigUtils.save_path + "/" + ymrCurr + ".xls";
//		Excel excel = new Excel();
//		excel.setPageRowCount(2000); // 设置每页excel显示2000行
//		excel.setFilePath(filePath);
//		excel.setTitle("销售报表");
//		excel.addHeader(20, "公司", "company");
//		excel.addHeader(30, "客户ID", "clientid");
//		excel.addHeader(20, "销售", "realname");
//		excel.addHeader(20, "发送总数", "sendall");
//		excel.addHeader(15, "1日", "1");
//		excel.addHeader(15, "2日", "2");
//		excel.addHeader(15, "3日", "3");
//		excel.addHeader(15, "4日", "4");
//		excel.addHeader(15, "5日", "5");
//		excel.addHeader(15, "6日", "6");
//		excel.addHeader(15, "7日", "7");
//		excel.addHeader(15, "8日", "8");
//		excel.addHeader(15, "9日", "9");
//		excel.addHeader(15, "10日", "10");
//		excel.addHeader(15, "11日", "11");
//		excel.addHeader(15, "12日", "12");
//		excel.addHeader(15, "13日", "13");
//		excel.addHeader(15, "14日", "14");
//		excel.addHeader(15, "15日", "15");
//		excel.addHeader(15, "16日", "16");
//		excel.addHeader(15, "17日", "17");
//		excel.addHeader(15, "18日", "18");
//		excel.addHeader(15, "19日", "19");
//		excel.addHeader(15, "20日", "20");
//		excel.addHeader(15, "21日", "21");
//		excel.addHeader(15, "22日", "22");
//		excel.addHeader(15, "23日", "23");
//		excel.addHeader(15, "24日", "24");
//		excel.addHeader(15, "25日", "25");
//		excel.addHeader(15, "26日", "26");
//		excel.addHeader(15, "27日", "27");
//		excel.addHeader(15, "28日", "28");
//		excel.addHeader(15, "29日", "29");
//		excel.addHeader(15, "30日", "30");
//		excel.addHeader(15, "31日", "31");
//		excel.setDataList(maps);
//		excel.setShowPage(false);
        if (maps == null || maps.size() <= 0) {
            return true;
        }
        List<SaleStat> exportList = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            SaleStat stat = new SaleStat();
            stat.setCompany(getStrValue(map, "company"));
            stat.setClientid(getStrValue(map, "clientid"));
            stat.setRealname(getStrValue(map, "realname"));
            stat.setSendall(getIntValue(map, "sendall"));
            stat.setDay1(getIntValue(map, "1"));
            stat.setDay2(getIntValue(map, "2"));
            stat.setDay3(getIntValue(map, "3"));
            stat.setDay4(getIntValue(map, "4"));
            stat.setDay5(getIntValue(map, "5"));
            stat.setDay6(getIntValue(map, "6"));
            stat.setDay7(getIntValue(map, "7"));
            stat.setDay8(getIntValue(map, "8"));
            stat.setDay9(getIntValue(map, "9"));
            stat.setDay10(getIntValue(map, "10"));
            stat.setDay11(getIntValue(map, "11"));
            stat.setDay12(getIntValue(map, "12"));
            stat.setDay13(getIntValue(map, "13"));
            stat.setDay14(getIntValue(map, "14"));
            stat.setDay15(getIntValue(map, "15"));
            stat.setDay16(getIntValue(map, "16"));
            stat.setDay17(getIntValue(map, "17"));
            stat.setDay18(getIntValue(map, "18"));
            stat.setDay19(getIntValue(map, "19"));
            stat.setDay20(getIntValue(map, "20"));
            stat.setDay21(getIntValue(map, "21"));
            stat.setDay22(getIntValue(map, "22"));
            stat.setDay23(getIntValue(map, "23"));
            stat.setDay24(getIntValue(map, "24"));
            stat.setDay25(getIntValue(map, "25"));
            stat.setDay26(getIntValue(map, "26"));
            stat.setDay27(getIntValue(map, "27"));
            stat.setDay28(getIntValue(map, "28"));
            stat.setDay29(getIntValue(map, "29"));
            stat.setDay30(getIntValue(map, "30"));
            stat.setDay31(getIntValue(map, "31"));
            exportList.add(stat);
        }
        String fileName = ConfigUtils.save_path + "/" + ymrCurr + ".xlsx";
        /*try {
			new ExportExcel("销售报表", SaleStat.class).setDataList(exportList).writeFile(fileName).dispose();
			for (int i = 0; i < mails.length; i++) {
				send(mails[i], fileName, ymrCurr);
			}
			// 删除
			FileUtils.delete(fileName);
		} catch (Exception e){
			logger.debug("【每日短信销售情况邮件报表】导出文件失败 = {}", e);
		}*/
        try {
            new ExportExcel("销售报表", SaleStat.class).setDataList(exportList).writeFile(fileName).dispose();
            send(params, fileName, ymrCurr);
            // 删除
            FileUtils.delete(fileName);
        } catch (Exception e) {
            logger.debug("【每日短信销售情况邮件报表】导出文件失败 = {}", e);
        }
//		if (ExcelUtils.exportExcel(excel)) {
//			for (int i = 0; i < mails.length; i++) {
//				send(mails[i], filePath, ymrCurr);
//			}
//
//			// 删除
//			FileUtils.delete(filePath);
//		}
        Calendar end = Calendar.getInstance();
        logger.debug("【每日短信销售情况邮件报表】结束 = {}", DateUtilsNew.formatDateTime(end.getTime()));
        return true;
    }

    private Integer getIntValue(Map<String, Object> map, String field) {
        Object obj = map.get(field);
        return obj == null ? 0 : Integer.valueOf(obj.toString());
    }

    private String getStrValue(Map<String, Object> map, String field) {
        Object obj = map.get(field);
        return obj == null ? "" : obj.toString();
    }

    /**
     * 根据异常过滤无效的邮件地址
     *
     * @param exception         发送时的异常信息
     * @param internetAddresses 发送地址列表
     */
    public void removeFailEmailByException(Exception exception, ArrayList<InternetAddress> internetAddresses) {
        //判断是否是地址异常
        if (exception instanceof MessagingException) {
            if (exception instanceof SMTPAddressFailedException) {
                SMTPAddressFailedException smtpAddressFailedException = (SMTPAddressFailedException) exception;
                //获取错误的地址
                InternetAddress address = smtpAddressFailedException.getAddress();
                logger.error("发送邮件失败包含无效的地址:" + address);
                //去掉错误的地址
                internetAddresses.remove(address);
            }
            MessagingException nextException = (MessagingException) exception;
            MessagingException nextException1 = (MessagingException) nextException.getNextException();
            if (nextException1 != null) {
                removeFailEmailByException(nextException1, internetAddresses);
            }
        }
    }

    @Override
    public boolean saleReportAndSend(TaskInfo taskInfo) {
        Calendar begin = Calendar.getInstance();
        logger.debug("【每日短信销售情况邮件报表】开始 = {}", DateUtilsNew.formatDateTime(begin.getTime()));
        String params = null;
        try {
            params = PropertiesUtil.get("SmsSaleStatMailReport");
//            params = "tanjiangqiang@ucpaas.com,liulipengju@ucpaas.com";
        } catch (Exception e) {
            logger.error("【每日短信销售情况邮件报表】 读取配置文件错误{}", e);
        }
        if (StringUtils.isBlank(params)) {
            logger.error("【每日短信销售情况邮件报表】 收件人为空");
            return true;
        }
//		String[] mails = params.split(",");
        // 获取当月
        String ym = DateUtilsNew.formatDate(begin.getTime(), "yyyyMM");
        // 获取今天
        String ymrCurr = DateUtilsNew.formatDate(begin.getTime(), "yyyyMMdd");
        // 获取本月的第一天
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 00);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 00);
        cal.set(Calendar.MILLISECOND, 00);
        cal.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
        String ymr = DateUtilsNew.formatDate(cal.getTime(), "yyyyMMdd");
        // 获取数据
        List<Map<String, Object>> maps = getStatReport(ym, ymr);
        // 若数据为空取上月的数据
        if (maps == null || maps.size() <= 0) {
            Calendar calendar = Calendar.getInstance();
            // 获取上个月月份
            calendar.add(Calendar.MONTH, -1);
            ym = DateUtilsNew.formatDate(calendar.getTime(), "yyyyMM");
            calendar.set(Calendar.HOUR_OF_DAY, 00);
            calendar.set(Calendar.MINUTE, 00);
            calendar.set(Calendar.SECOND, 00);
            calendar.set(Calendar.MILLISECOND, 00);
            calendar.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
            ymr = DateUtilsNew.formatDate(calendar.getTime(), "yyyyMMdd");
            maps = getStatReport(ym, ymr);
        }
        if (maps == null || maps.size() <= 0) {
            return true;
        }

        List<SaleStat> exportList = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            SaleStat stat = new SaleStat();
            stat.setCompany(getStrValue(map, "company"));
            stat.setClientid(getStrValue(map, "clientid"));
            stat.setRealname(getStrValue(map, "realname"));
            stat.setSendall(getIntValue(map, "sendall"));
            stat.setDay1(getIntValue(map, "1"));
            stat.setDay2(getIntValue(map, "2"));
            stat.setDay3(getIntValue(map, "3"));
            stat.setDay4(getIntValue(map, "4"));
            stat.setDay5(getIntValue(map, "5"));
            stat.setDay6(getIntValue(map, "6"));
            stat.setDay7(getIntValue(map, "7"));
            stat.setDay8(getIntValue(map, "8"));
            stat.setDay9(getIntValue(map, "9"));
            stat.setDay10(getIntValue(map, "10"));
            stat.setDay11(getIntValue(map, "11"));
            stat.setDay12(getIntValue(map, "12"));
            stat.setDay13(getIntValue(map, "13"));
            stat.setDay14(getIntValue(map, "14"));
            stat.setDay15(getIntValue(map, "15"));
            stat.setDay16(getIntValue(map, "16"));
            stat.setDay17(getIntValue(map, "17"));
            stat.setDay18(getIntValue(map, "18"));
            stat.setDay19(getIntValue(map, "19"));
            stat.setDay20(getIntValue(map, "20"));
            stat.setDay21(getIntValue(map, "21"));
            stat.setDay22(getIntValue(map, "22"));
            stat.setDay23(getIntValue(map, "23"));
            stat.setDay24(getIntValue(map, "24"));
            stat.setDay25(getIntValue(map, "25"));
            stat.setDay26(getIntValue(map, "26"));
            stat.setDay27(getIntValue(map, "27"));
            stat.setDay28(getIntValue(map, "28"));
            stat.setDay29(getIntValue(map, "29"));
            stat.setDay30(getIntValue(map, "30"));
            stat.setDay31(getIntValue(map, "31"));
            exportList.add(stat);
        }
        int mostAverageTotal = 0;
        int powerAverageTotal = 0;
        int mostAverage = 0;
        int powerAverage = 0;
        // 当前几号
        String newDay = DateUtilsNew.getDay();
        // 本月的天数
        int days = DateUtilsNew.getCurrentMonthLastDay();
        Integer intNewDay;
        for (SaleStat saleStat : exportList) {
            // 第一行总计不算
            if ("-".equals(saleStat.getClientid())) {
                continue;
            }
            if ("01".equals(newDay)){
                // 如果是1号,获取的是上个月的数据,则没有预测的加权和平均
                saleStat.setPowerAverageTotal(0);
                saleStat.setPowerAverage(0);
                saleStat.setMostAverageTotal(0);
                saleStat.setMostAverage(0);
            } else {
                intNewDay = Integer.valueOf(newDay);
                // 绝对平均
                saleStat.getMostAverage(intNewDay - 1);
                if (intNewDay > 3) {
                    // 加权平均
                    saleStat.getPowerAverage(intNewDay - 1);
                } else {
                    // 小于三天,则加权跟平均是一样的
                    saleStat.setPowerAverage(saleStat.getMostAverage());
                }
                // 要求平均的天数
                Integer averageDay = (days - intNewDay) + 1;
                // 绝对平均总和 = 发送总数 + 绝对平均总和
                saleStat.setMostAverageTotal(saleStat.getSendall() + (saleStat.getMostAverage() * averageDay));
                // 加权平均总和 = 发送总数 + 加权平均总和
                saleStat.setPowerAverageTotal(saleStat.getSendall() + (saleStat.getPowerAverage() * averageDay));

                mostAverageTotal += saleStat.getMostAverageTotal();
                mostAverage += saleStat.getMostAverage();
                powerAverageTotal += saleStat.getPowerAverageTotal();
                powerAverage += saleStat.getPowerAverage();
            }
        }

        // 第一行总数赋值
        for (SaleStat saleStat : exportList) {
            // 第一行总计不算
            if ("-".equals(saleStat.getClientid())) {
                saleStat.setMostAverageTotal(mostAverageTotal);
                saleStat.setMostAverage(mostAverage);
                saleStat.setPowerAverageTotal(powerAverageTotal);
                saleStat.setPowerAverage(powerAverage);
            }
        }

        String fileName = ConfigUtils.save_path + "/" + ymrCurr + ".xls";
        FileOutputStream fileOutputStream = null;
        try {
            // 获得导出到excel的文件名和表头映射
            Map<String, String[]> heads = head();
            String[] head = heads.get("head");
            String[] columnName = heads.get("columnName");
            List<Map<String, Object>> result = new ArrayList<>(exportList.size());
            for (Object obj : exportList) {
                Map<String, Object> describe = (Map) BeanUtils.describe(obj);
                result.add(describe);
            }
            // 获得导出的数据表格
            Workbook wb = ExcelUtil.export("预测", "销售报表", columnName, head, null,
                    result);
            fileOutputStream = new FileOutputStream(fileName);
            wb.write(fileOutputStream);

            String text = this.getText(ymrCurr, exportList);
            send(text, params, fileName, ymrCurr);
            // 删除
            FileUtils.delete(fileName);
        } catch (Exception e) {
            logger.debug("【每日短信销售情况邮件报表】导出文件失败 = {}", e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.debug("导出流关闭失败 = {}", e);
                }
            }
        }
        Calendar end = Calendar.getInstance();
        logger.debug("【每日短信销售情况邮件报表】结束 = {}", DateUtilsNew.formatDateTime(end.getTime()));
        return true;
    }

    private boolean send(String text, String receiver, String fileName, String ymr) {
        try {
            String[] mails = receiver.split(",");
            //邮件发送地址（群发）
            InternetAddress[] addresses = new InternetAddress[mails.length];
            for (int i = 0; i < mails.length; i++) {
                addresses[i] = new InternetAddress(mails[i]);
            }
            MimeMessage msg = javaMailSenderAlarm.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "utf-8");
            helper.setFrom("alarm@ucpaas.com");
//			helper.setTo(receiver);
            helper.setTo(addresses);
            helper.setSubject("【" + ymr + "新短信销售情况通报】");
            helper.setText(text,true);
            FileSystemResource file = new FileSystemResource(fileName);
            helper.addAttachment(file.getFilename(), file);
            try {
                //第一次发送
                javaMailSenderAlarm.send(msg);
            } catch (MailSendException e2) {
                ArrayList<InternetAddress> internetAddresses = new ArrayList<>(Arrays.asList(addresses));
                Map<Object, Exception> failedMessages = e2.getFailedMessages();
                Iterator<Entry<Object, Exception>> iterator = failedMessages.entrySet().iterator();
                for (Entry<Object, Exception> objectExceptionEntry : failedMessages.entrySet()) {
                    Exception value = objectExceptionEntry.getValue();
                    removeFailEmailByException(value, internetAddresses);
                    //去掉错误的地址重新发送
                    InternetAddress[] fixAddress = new InternetAddress[internetAddresses.size()];
                    for (int i = 0; i < internetAddresses.size(); i++) {
                        InternetAddress internetAddress = internetAddresses.get(i);
                        fixAddress[i] = internetAddress;
                    }
                    helper.setTo(fixAddress);
                    //失败重发
                    javaMailSenderAlarm.send(msg);
                }
            }
//			javaMailSenderAlarm.send(msg);
            logger.debug("发送Email【成功】：to={}", receiver);
        } catch (Throwable e) {
            logger.error("发送Email【失败】：to={} 错误 --->", receiver, e);
            return false;
        }
        return true;
    }

    public static Map<String, String[]> head() {
        // 当前几号
        Integer newDay = Integer.valueOf(DateUtilsNew.getDay());
        // 当月天数
        int days = DateUtilsNew.getCurrentMonthLastDay();
        // 计算头的长度
        int len = 7 + days + days - newDay;
        String[] head;
        String[] columnName;
        if (newDay != 1) {
            head = new String[len];
            columnName = new String[len];
            head[4] = "发送总数预测（加权平均）";
            head[5] = "发送总数预测（绝对平均）";
            columnName[4] = "powerAverageTotal";
            columnName[5] = "mostAverageTotal";
        } else {
            Calendar calendar = Calendar.getInstance();
            // 获取上个月天数
            calendar.add(Calendar.MONTH, -1);
            days = DateUtilsNew.getDaysOfMonth(calendar.getTime());
            len = days + 4;
            head = new String[len];
            columnName = new String[len];
        }
        head[0] = "公司";
        head[1] = "客户ID";
        head[2] = "销售";
        head[3] = "发送总数";
        columnName[0] = "company";
        columnName[1] = "clientid";
        columnName[2] = "realname";
        columnName[3] = "sendall";
        int day = 1;
        for (int i = 0; i < len; i++) {
            if (head[i] == null) {
                if (day >= newDay && newDay != 1) {
                    head[i] = day + "日预测（加权）";
                    head[i + 1] = day + "日预测（绝对）";
                    columnName[i] = "powerAverage";
                    columnName[i + 1] = "mostAverage";
                } else {
                    head[i] = day + "日";
                    columnName[i] = "day" + day;
                }
                day++;
            }
        }
        Map<String, String[]> result = new HashMap<>(2);
        result.put("head", head);
        result.put("columnName", columnName);
        return result;
    }

    private String getText(String ymr, List<SaleStat> exportList) {
        String text = SysConstant.SALE_MAIL_REPORT_TEXT;
        StringBuilder belongsaleText = new StringBuilder();

        Map<String, SaleStat> belongsaleGroup = new HashMap<>();
        // 根据销售分组,求和
        for (SaleStat ss : exportList) {
            String realname = ss.getRealname();
            if (belongsaleGroup.containsKey(realname)) {
                SaleStat value = belongsaleGroup.get(realname);
                value.setSendall(ss.getSendall() + value.getSendall());
                value.setMostAverageTotal(ss.getMostAverageTotal() + value.getMostAverageTotal());
                value.setPowerAverageTotal(ss.getPowerAverageTotal() + value.getPowerAverageTotal());
                belongsaleGroup.put(realname, value);
//                ss.setSendall(ss.getSendall() + belongsaleGroup.get(realname).getSendall());
//                ss.setMostAverageTotal(ss.getMostAverageTotal() + belongsaleGroup.get(realname).getMostAverageTotal());
//                ss.setPowerAverageTotal(ss.getPowerAverageTotal() + belongsaleGroup.get(realname).getPowerAverageTotal());
            } else {
                belongsaleGroup.put(realname, ss);
            }
        }

        List<SaleStat> data = new ArrayList<>(belongsaleGroup.values());
        //自定义比较器来比较链表中的元素
        Collections.sort(data, new Comparator<SaleStat>() {
            //基于entry的值（Entry.getValue()），来排序链表
            @Override
            public int compare(SaleStat o1, SaleStat o2) {
                // 根据发送总数来排序
                return o2.getSendall().compareTo(o1.getSendall());
            }
        });

        for (SaleStat ss : data) {
            if ("-".equals(ss.getClientid())) {
                text = text.replace("averageTotal", String.valueOf(ss.getMostAverageTotal()));
                text = text.replace("weightingTotal", String.valueOf(ss.getPowerAverageTotal()));
                text = text.replace("total", String.valueOf(ss.getSendall()));
                continue;
            }
            String belongsale = SysConstant.BELONGSALE_TEXT;
            if ("".equals(ss.getRealname()) || ss.getRealname() == null) {
                String clientid = ss.getClientid();
                // 每隔35长度追加换行
                String regex = "(.{105})";
                clientid = clientid.replaceAll (regex, "$1\n");
                belongsale = belongsale.replace("belongSaleName", new StringBuilder("").append(" (没有所属销售)").toString());
            } else {
                belongsale = belongsale.replace("belongSaleName", ss.getRealname());
            }
            belongsale = belongsale.replace("belongSaleNumber", String.valueOf(ss.getSendall()));
            belongsale = belongsale.replace("belongSaleTargetNumber", String.valueOf(ss.getMostAverageTotal()));
            belongsale = belongsale.replace("belongSaleWeightingTotal", String.valueOf(ss.getPowerAverageTotal()));
            belongsaleText.append(belongsale);
        }
        text = text.replace("belongsaleText", belongsaleText);
        text = text.replace("DateTime", ymr);
        return text.toString();
    }

//    public static void main(String[] args) {
//        System.err.println(JsonUtil.toJson(head()));
//    }

}