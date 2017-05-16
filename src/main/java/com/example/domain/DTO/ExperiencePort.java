package com.example.domain.DTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SunYi on 2016/4/5/0005.
 */
public class ExperiencePort {
    String ARCID;
    //起飞机场（数据库中对应ADEP）
    String ADEP;
    //目的机场（数据库中ADES，所有）
    String ADES;
    //起飞时间 预起时间EOBT实际起飞时间ATD
    Date ATD;
    Date EOBT;
    Date startTime;
    //落地时间 预计落地ETA实际落地ATA
    Date ETA;
    Date ATA;
    Date endTime;

    //飞行时间 落地-起飞
    String flyTime;
    //跑道
    String runway;
    //分钟段
    int minutes;

    public String getARCID() {
        return ARCID;
    }

    public void setARCID(String ARCID) {
        this.ARCID = ARCID;
    }

    public String getADEP() {
        return ADEP;
    }

    public void setADEP(String ADEP) {
        this.ADEP = ADEP;
    }

    public String getADES() {
        return ADES;
    }

    public void setADES(String ADES) {
        this.ADES = ADES;
    }

    public Date getATD() {
        return ATD;
    }

    public void setATD(String ATD) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            this.ATD = simpleDateFormat.parse(ATD);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getEOBT() {
        return EOBT;
    }

    public void setEOBT(String EOBT) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            this.EOBT = simpleDateFormat.parse(EOBT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getStartTime() {
        if (ATD != null) {
            startTime = ATD;
        } else {
            startTime = EOBT;
        }
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getETA() {
        return ETA;
    }

    public void setETA(String ETA) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            this.ETA = simpleDateFormat.parse(ETA);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getATA() {
        return ATA;
    }

    public void setATA(String ATA) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            this.ATA = simpleDateFormat.parse(ATA);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Date getEndTime() {
        if (ETA != null) {
            endTime = ETA;
        } else {
            endTime = ATA;
        }
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getFlyTime() {
        long diff = getEndTime().getTime() - getStartTime().getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
//        long diffHours = diff / (60 * 60 * 1000) % 24;
//        long diffDays = diff / (24 * 60 * 60 * 1000);
        flyTime = diffHours + "小时 " + diffMinutes + "分钟";
        return flyTime;
    }


    public String getRunway() {
        return runway;
    }

    public void setRunway(String runway) {
        this.runway = runway;
    }

    public void setFlyTime(String flyTime) {
        this.flyTime = flyTime;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}
