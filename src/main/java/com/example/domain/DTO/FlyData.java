package com.example.domain.DTO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FlyData {
	static SimpleDateFormat simpleDateFormat_NoSec = new SimpleDateFormat("yyyyMMddHHmm");
	static SimpleDateFormat simpleDateFormat_HaveSec  = new SimpleDateFormat("yyyyMMddHHmmss");
	
	//ID
	String IFPLID;
	// 经过的点
	String PTID;
	// 过点时间
	Date ETO;
    //航班号（对应数据库中的ARCID）
    String ARCID;
    //起飞机场（数据库中对应ADEP）
    String ADEP;
    //目的机场（数据库中ADES，所有）
    String ADES;
    
    String RTE;
    //预起时间（待定）
    Date EOBT;
    // 预计降落时间
    Date ETA;
    //实际起飞时间（ATD）
    Date ATD;
    // 实际降落时间
    Date ATA;
    //状态
    String STATUS;
    // 过点时间1
    Date pass1;
    // 过点时间2
    Date pass2;
    
    String SECTOR;
    // 生命周期时长（分钟）
    int interval;
    // 生命周期中开始时间距当前时间有几分钟
    int minutes;
    
    String direction;

    public String getIFPLID() {
		return IFPLID;
	}

	public void setIFPLID(String iFPLID) {
		IFPLID = iFPLID;
	}

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

    /**
     * 起飞机场
     */
    public String getADEP() {
        return ADEP;
    }

    public void setADEP(String ADEP) {
        this.ADEP = ADEP;
    }

    /**
     * 降落机场
     */
    public String getADES() {
        return ADES;
    }

    public void setADES(String ADES) {
        this.ADES = ADES;
    }

    public Date getEOBT() {
        return EOBT;
    }

    public void setEOBT(String EOBT) {
    	this.EOBT = transStr2Date(EOBT);
    }

    public Date getATD() {
        return ATD;
    }

    public void setATD(String ATD) {
    	this.ATD = transStr2Date(ATD);
    }

    public String getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }
    public String getDirection() {
       String  direction = "direction";
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

    public String getRTE() {
        return RTE;
    }

    public void setRTE(String RTE) {
        this.RTE = RTE;
    }

	public String getSECTOR() {
		return SECTOR;
	}

	public void setSECTOR(String sECTOR) {
		SECTOR = sECTOR;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public Date getETO() {
		return ETO;
	}

	public void setETO(String eTO) {
		ETO = transStr2Date(eTO);
	}
	
	public void setETO(Date eTO) {
		ETO = eTO;
	}

	public String getPTID() {
		return PTID;
	}

	public void setPTID(String pTID) {
		PTID = pTID;
	}

	public Date getETA() {
		return ETA;
	}

	public void setETA(String eTA) {
		ETA = transStr2Date(eTA);
	}

	public Date getATA() {
		return ATA;
	}

	public void setATA(String aTA) {
		ATA = transStr2Date(aTA);
	}
	
	public Date getPass1() {
		return pass1;
	}

	public void setPass1(Date pass1) {
		this.pass1 = pass1;
	}

	public Date getPass2() {
		return pass2;
	}

	public void setPass2(Date pass2) {
		this.pass2 = pass2;
	}

	public static Date transStr2Date(String str) {
		try {
			if (str.length() == 12) {
				return simpleDateFormat_NoSec.parse(str);
			} else if (str.length() == 14) {
				return simpleDateFormat_HaveSec.parse(str);
			}
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	@Override
	public String toString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		return new StringBuilder("FDRID:").append(this.IFPLID)
				.append(", ARCID:").append(this.ARCID)
				.append(", Pass1:").append(simpleDateFormat.format(this.pass1))
				.append(", Pass2:").append(simpleDateFormat.format(this.pass2))
				.append(", IntervalMis:").append(this.interval)
				.append(", Minutes:").append(this.minutes).toString();
	}
}
