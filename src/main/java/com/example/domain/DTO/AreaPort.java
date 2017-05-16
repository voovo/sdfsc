package com.example.domain.DTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SunYi on 2016/2/17/0017.
 */
public class AreaPort {
    //航班号（对应数据库中的ARCID）
    String ARCID;
    //机型（对应数据库中的WKTRC）
    String WKTRC;
    //起飞机场（数据库中对应ADEP）
    String ADEP;
    //目的机场（数据库中ADES，所有）
    String ADES;
    //济南时间
    Date JNTime;
    //青岛起飞降落航班到达WFG的时间
    Date WFG;
    //郑州落地，武汉落地的到达REPOL的时间
    Date REPOL;
    //实际起飞时间（ATD）
    Date ATD;
    //注册号
    String ARCREG;
    //二次代码
    String SsrCode;
    //状态
    String STATUS;
    
    String SECTOR;

    //分钟段
    int minutes;

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

    public Date getJNTime() {
        return JNTime;
    }
    
    public void setJNTime(Date jnTime) {
    	this.JNTime = jnTime;
    }

    public void setJNTime(String  JNTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            this.JNTime = simpleDateFormat.parse(JNTime);
        } catch (ParseException e) {
            simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            try {
                this.JNTime = simpleDateFormat.parse(JNTime);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
    }
    public Date getATD() {
        return ATD;
    }

    public void setATD(String ATD) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            this.ATD = simpleDateFormat.parse(ATD);
        } catch (ParseException e) {
            simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            try {
                this.ATD = simpleDateFormat.parse(ATD);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
    }
    public Date getWFG() {
        return WFG;
    }

    public void setWFG(Date WFG) {
        this.WFG = WFG;
    }

    public Date getREPOL() {
        return REPOL;
    }

    public void setREPOL(Date REPOL) {
        this.REPOL = REPOL;
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

	public String getSECTOR() {
		return SECTOR;
	}

	public void setSECTOR(String sECTOR) {
		SECTOR = sECTOR;
	}
    
}
