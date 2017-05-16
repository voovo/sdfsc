package com.example.domain.DTO;

import com.example.service.PortService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SunYi on 2016/4/6/0006.
 */
public class ZSJNPort {
    //航班号（对应数据库中的ARCID）
    String ARCID;
    //机型（对应数据库中的WKTRC）
    String WKTRC;
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
    //注册号
    String ARCREG;
    //二次代码
    String SsrCode;
    //状态
    String STATUS;
    String RTE;
    //分钟段
    int minutes;
    //方向
    String direction;

    public String getARCID() {
        return ARCID;
    }

    public void setARCID(String ARCID) {
        this.ARCID = ARCID;
    }

    public String getWKTRC() {
        return WKTRC;
    }

    public void setWKTRC(String WKTRC) {
        this.WKTRC = WKTRC;
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

    public String getARCREG() {
        return ARCREG;
    }

    public void setARCREG(String ARCREG) {
        this.ARCREG = ARCREG;
    }

    public String getSsrCode() {
        return SsrCode;
    }

    public void setSsrCode(String ssrCode) {
        SsrCode = ssrCode;
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public boolean isTokenOff() throws ParseException {
        return getStartTime().before(PortService.getNOW());
    }

    public String getRTE() {
        return RTE;
    }

    public void setRTE(String RTE) {
        this.RTE = RTE;
    }

    public String getDirection() {
        direction = "direction";
        if (RTE.indexOf("GULEK") != -1) {
            direction = "W";
        } else if (RTE.indexOf("PANKI") != -1) {
            direction = "N";
        } else if (RTE.indexOf("P291") != -1 || RTE.indexOf("BASOV") != -1) {
            direction = "E";
        } else if (RTE.indexOf("ASTUB") != -1 || RTE.indexOf("P200") != -1 || RTE.indexOf("P292") != 0) {
            direction = "S";
        }
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

}
