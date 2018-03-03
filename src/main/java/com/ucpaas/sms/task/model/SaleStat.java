package com.ucpaas.sms.task.model;

import com.ucpaas.sms.common.util.excel.annotation.ExcelField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SaleStat {

	private String company;
	private String clientid;
	private String realname;
	private Integer sendall;
	private String groupId;

	private Integer day1;
	private Integer day2;
    private Integer day3;
    private Integer day4;
    private Integer day5;
    private Integer day6;
    private Integer day7;
    private Integer day8;
    private Integer day9;
    private Integer day10;
    private Integer day11;
    private Integer day12;
    private Integer day13;
    private Integer day14;
    private Integer day15;
    private Integer day16;
    private Integer day17;
    private Integer day18;
    private Integer day19;
    private Integer day20;
    private Integer day21;
    private Integer day22;
    private Integer day23;
    private Integer day24;
    private Integer day25;
    private Integer day26;
    private Integer day27;
    private Integer day28;
    private Integer day29;
    private Integer day30;
    private Integer day31;

    private Integer mostAverage;
    private Integer powerAverage;

    private Integer mostAverageTotal;
    private Integer powerAverageTotal;


    @ExcelField(title = "公司", align = 2, sort = 10)
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @ExcelField(title = "客户ID", align = 2, sort = 20)
    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    @ExcelField(title = "销售", align = 2, sort = 30)
    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    @ExcelField(title = "发送总数", align = 2, sort = 40)
    public Integer getSendall() {
        return sendall;
    }

    public void setSendall(Integer sendall) {
        this.sendall = sendall;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @ExcelField(title = "1日", align = 2, sort = 50)
    public Integer getDay1() {
        return day1;
    }

    public void setDay1(Integer day1) {
        this.day1 = day1;
    }

    @ExcelField(title = "2日", align = 2, sort = 60)
    public Integer getDay2() {
        return day2;
    }

    public void setDay2(Integer day2) {
        this.day2 = day2;
    }

    @ExcelField(title = "3日", align = 2, sort = 70)
    public Integer getDay3() {
        return day3;
    }

    public void setDay3(Integer day3) {
        this.day3 = day3;
    }

    @ExcelField(title = "4日", align = 2, sort = 80)
    public Integer getDay4() {
        return day4;
    }

    public void setDay4(Integer day4) {
        this.day4 = day4;
    }

    @ExcelField(title = "5日", align = 2, sort = 90)
    public Integer getDay5() {
        return day5;
    }

    public void setDay5(Integer day5) {
        this.day5 = day5;
    }

    @ExcelField(title = "6日", align = 2, sort = 100)
    public Integer getDay6() {
        return day6;
    }

    public void setDay6(Integer day6) {
        this.day6 = day6;
    }

    @ExcelField(title = "7日", align = 2, sort = 110)
    public Integer getDay7() {
        return day7;
    }

    public void setDay7(Integer day7) {
        this.day7 = day7;
    }

    @ExcelField(title = "8日", align = 2, sort = 120)
    public Integer getDay8() {
        return day8;
    }

    public void setDay8(Integer day8) {
        this.day8 = day8;
    }

    @ExcelField(title = "9日", align = 2, sort = 130)
    public Integer getDay9() {
        return day9;
    }

    public void setDay9(Integer day9) {
        this.day9 = day9;
    }

    @ExcelField(title = "10日", align = 2, sort = 140)
    public Integer getDay10() {
        return day10;
    }

    public void setDay10(Integer day10) {
        this.day10 = day10;
    }

    @ExcelField(title = "11日", align = 2, sort = 150)
    public Integer getDay11() {
        return day11;
    }

    public void setDay11(Integer day11) {
        this.day11 = day11;
    }

    @ExcelField(title = "12日", align = 2, sort = 160)
    public Integer getDay12() {
        return day12;
    }

    public void setDay12(Integer day12) {
        this.day12 = day12;
    }

    @ExcelField(title = "13日", align = 2, sort = 170)
    public Integer getDay13() {
        return day13;
    }

    public void setDay13(Integer day13) {
        this.day13 = day13;
    }

    @ExcelField(title = "14日", align = 2, sort = 180)
    public Integer getDay14() {
        return day14;
    }

    public void setDay14(Integer day14) {
        this.day14 = day14;
    }

    @ExcelField(title = "15日", align = 2, sort = 190)
    public Integer getDay15() {
        return day15;
    }

    public void setDay15(Integer day15) {
        this.day15 = day15;
    }

    @ExcelField(title = "16日", align = 2, sort = 200)
    public Integer getDay16() {
        return day16;
    }

    public void setDay16(Integer day16) {
        this.day16 = day16;
    }

    @ExcelField(title = "17日", align = 2, sort = 210)
    public Integer getDay17() {
        return day17;
    }

    public void setDay17(Integer day17) {
        this.day17 = day17;
    }

    @ExcelField(title = "18日", align = 2, sort = 220)
    public Integer getDay18() {
        return day18;
    }

    public void setDay18(Integer day18) {
        this.day18 = day18;
    }

    @ExcelField(title = "19日", align = 2, sort = 230)
    public Integer getDay19() {
        return day19;
    }

    public void setDay19(Integer day19) {
        this.day19 = day19;
    }

    @ExcelField(title = "20日", align = 2, sort = 240)
    public Integer getDay20() {
        return day20;
    }

    public void setDay20(Integer day20) {
        this.day20 = day20;
    }

    @ExcelField(title = "21日", align = 2, sort = 250)
    public Integer getDay21() {
        return day21;
    }

    public void setDay21(Integer day21) {
        this.day21 = day21;
    }

    @ExcelField(title = "22日", align = 2, sort = 260)
    public Integer getDay22() {
        return day22;
    }

    public void setDay22(Integer day22) {
        this.day22 = day22;
    }

    @ExcelField(title = "23日", align = 2, sort = 270)
    public Integer getDay23() {
        return day23;
    }

    public void setDay23(Integer day23) {
        this.day23 = day23;
    }

    @ExcelField(title = "24日", align = 2, sort = 280)
    public Integer getDay24() {
        return day24;
    }

    public void setDay24(Integer day24) {
        this.day24 = day24;
    }

    @ExcelField(title = "25日", align = 2, sort = 290)
    public Integer getDay25() {
        return day25;
    }

    public void setDay25(Integer day25) {
        this.day25 = day25;
    }

    @ExcelField(title = "26日", align = 2, sort = 300)
    public Integer getDay26() {
        return day26;
    }

    public void setDay26(Integer day26) {
        this.day26 = day26;
    }

    @ExcelField(title = "27日", align = 2, sort = 310)
    public Integer getDay27() {
        return day27;
    }

    public void setDay27(Integer day27) {
        this.day27 = day27;
    }

    @ExcelField(title = "28日", align = 2, sort = 320)
    public Integer getDay28() {
        return day28;
    }

    public void setDay28(Integer day28) {
        this.day28 = day28;
    }

    @ExcelField(title = "29日", align = 2, sort = 330)
    public Integer getDay29() {
        return day29;
    }

    public void setDay29(Integer day29) {
        this.day29 = day29;
    }

    @ExcelField(title = "30日", align = 2, sort = 340)
    public Integer getDay30() {
        return day30;
    }

    public void setDay30(Integer day30) {
        this.day30 = day30;
    }

    @ExcelField(title = "31日", align = 2, sort = 350)
    public Integer getDay31() {
        return day31;
    }

    public void setDay31(Integer day31) {
        this.day31 = day31;
    }

    public Integer getMostAverage() {
        return mostAverage;
    }

    public void setMostAverage(Integer mostAverage) {
        this.mostAverage = mostAverage;
    }

    public Integer getPowerAverage() {
        return powerAverage;
    }

    public void setPowerAverage(Integer powerAverage) {
        this.powerAverage = powerAverage;
    }

    public Integer getMostAverageTotal() {
        return mostAverageTotal;
    }

    public void setMostAverageTotal(Integer mostAverageTotal) {
        this.mostAverageTotal = mostAverageTotal;
    }

    public Integer getPowerAverageTotal() {
        return powerAverageTotal;
    }

    public void setPowerAverageTotal(Integer powerAverageTotal) {
        this.powerAverageTotal = powerAverageTotal;
    }

    /**
     * 总和
     *
     * @return
     */
    public Integer getSum() {
        return this.getDay1() + this.getDay2() + this.getDay3() + this.getDay4() + this.getDay5() + this.getDay6() + this.getDay7() + this.getDay8() + this.getDay9() + this.getDay10()
                + this.getDay11() + this.getDay12() + this.getDay13() + this.getDay14() + this.getDay15() + this.getDay16() + this.getDay17() + this.getDay18() + this.getDay19() + this.getDay20()
                + this.getDay21() + this.getDay22() + this.getDay23() + this.getDay24() + this.getDay25() + this.getDay26() + this.getDay27() + this.getDay28() + this.getDay29() + this.getDay30() + this.getDay31();
    }

    /**
     * 绝对平均
     *
     * @return
     */
    public Integer getMostAverage(int number) {
        Integer sum = this.getDay1() + this.getDay2() + this.getDay3() + this.getDay4() + this.getDay5() + this.getDay6() + this.getDay7() + this.getDay8() + this.getDay9() + this.getDay10()
                + this.getDay11() + this.getDay12() + this.getDay13() + this.getDay14() + this.getDay15() + this.getDay16() + this.getDay17() + this.getDay18() + this.getDay19() + this.getDay20()
                + this.getDay21() + this.getDay22() + this.getDay23() + this.getDay24() + this.getDay25() + this.getDay26() + this.getDay27() + this.getDay28() + this.getDay29() + this.getDay30() + this.getDay31();
        this.mostAverage = sum / number;
        return this.mostAverage;
    }

    /**
     * 加权平均
     *
     * @return
     */
    public Integer getPowerAverage(int number) {
        List<Integer> list = new ArrayList<>(31);
        if (number > 1) {
            list.add(this.getDay1());
        }
        if (number >= 2) {
            list.add(this.getDay2());
        }
        if (number >= 3) {
            list.add(this.getDay3());
        }
        if (number >= 4) {
            list.add(this.getDay4());
        }
        if (number >= 5) {
            list.add(this.getDay5());
        }
        if (number >= 6) {
            list.add(this.getDay6());
        }
        if (number >= 7) {
            list.add(this.getDay7());
        }
        if (number >= 8) {
            list.add(this.getDay8());
        }
        if (number >= 9) {
            list.add(this.getDay9());
        }
        if (number >= 10) {
            list.add(this.getDay10());
        }
        if (number >= 11) {
            list.add(this.getDay11());
        }
        if (number >= 12) {
            list.add(this.getDay12());
        }
        if (number >= 13) {
            list.add(this.getDay13());
        }
        if (number >= 14) {
            list.add(this.getDay14());
        }
        if (number >= 15) {
            list.add(this.getDay15());
        }
        if (number >= 16) {
            list.add(this.getDay16());
        }
        if (number >= 17) {
            list.add(this.getDay17());
        }
        if (number >= 18) {
            list.add(this.getDay18());
        }
        if (number >= 19) {
            list.add(this.getDay19());
        }
        if (number >= 20) {
            list.add(this.getDay20());
        }
        if (number >= 21) {

            list.add(this.getDay21());
        }
        if (number >= 22) {
            list.add(this.getDay22());
        }
        if (number >= 23) {
            list.add(this.getDay23());
        }
        if (number >= 24) {
            list.add(this.getDay24());
        }
        if (number >= 25) {
            list.add(this.getDay25());
        }
        if (number >= 26) {
            list.add(this.getDay26());
        }
        if (number >= 27) {
            list.add(this.getDay27());
        }
        if (number >= 28) {
            list.add(this.getDay28());
        }
        if (number >= 29) {
            list.add(this.getDay29());
        }
        if (number >= 30) {
            list.add(this.getDay30());
        }
        if (number == 31) {
            list.add(this.getDay31());
        }

        Collections.sort(list);
        list.remove(0);
        list.remove(list.size() - 1);

        Iterator<Integer> iterator = list.iterator();
        int sum = 0;
        while (iterator.hasNext()) {
            sum += iterator.next();
        }
        this.powerAverage = sum / number;
        return this.powerAverage = sum / number;
    }
}
