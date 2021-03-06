package com.example.domain.DTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SunYi on 2016/4/6/0006.
 */
public class AllPort {
    //航班号（对应数据库中的ARCID）
    String ARCID;
    //机型（对应数据库中的WKTRC）
    String WKTRC;
    //预落时间
    Date ETA;
    //起飞机场（数据库中对应ADEP）
    String ADEP;
    //目的机场（数据库中ADES，所有）
    String ADES;
    //预起时间（待定）
    Date EOBT;
    //实际起飞时间（ATD）
    Date ATD;
    //注册号
    String ARCREG;
    //二次代码
    String SsrCode;

    String SECTOR;
    //状态
    String STATUS;
    //分钟段
    int minutes;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }


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

    public Date getETA() {
        return ETA;
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

    public String getSECTOR() {
		return SECTOR;
	}

	public void setSECTOR(String sECTOR) {
		SECTOR = sECTOR;
	}

	public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }
    public void setETA(String ETA) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            this.ETA = simpleDateFormat.parse(ETA);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void setEOBT(String EOBT) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        try {
            this.EOBT = simpleDateFormat.parse(EOBT);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

    public void setETA(Date ETA) {
        this.ETA = ETA;
    }

    public Date getEOBT() {
        return EOBT;
    }

    public void setEOBT(Date EOBT) {
        this.EOBT = EOBT;
    }

    public void setATD(Date ATD) {
        this.ATD = ATD;
    }
}

