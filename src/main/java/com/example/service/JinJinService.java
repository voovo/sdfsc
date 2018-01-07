package com.example.service;

import com.example.dao.JinJinDao;
import com.example.domain.DTO.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class JinJinService {
	/*
	 ETA	预计降落时间
	 ATA	实际降落时间
	 
	 TNA	遥墙机场代号
	 ETO	预计到达某点时间
	 
	 EOBT	预计离开时间
	 ATD	实际离开时间
	 */
	
	static Logger logger = LoggerFactory.getLogger(JinJinService.class);
    
    static Gson gson = new Gson();
    
    public static final String JN_AIR_PORT = "ZSJN";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyyMMddHHmmss");
    
    public static final List<String> JinJinPassPointList = Lists.newArrayList("GULEK","P292", "ABTUB", "P200", "BASOV", "P291", "PANKI");
    
    @Autowired
    private JinJinDao jinjinDao;
    
    public static Date getNOW(){
    	Date now = new Date();
		Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR_OF_DAY, -8);
        return c.getTime();
    }
    
    //进近出港航班
    public List<FlyData> getLeavePortTable(Date nowTime) throws ParseException {
        
        String haveFlyingStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<FlyData> haveFlyingLeavePortList = jinjinDao.getHaveLeavedFlyDataForJinJin(haveFlyingStartTime);
        
        List<String> toLeavePassPointList =  Lists.newArrayList("TNA", "GULEK","P292", "ABTUB", "P200", "BASOV", "P291", "PANKI");
        
        List<FlyData> toFlyLeavePortList = jinjinDao.getToLeaveFlyDataForJinJin(toLeavePassPointList);
        
        List<FlyData> jinjinList = new ArrayList<>();
        Map<String, FlyData> leaveJinJinMap = new HashMap<>();
        for (FlyData fd : toFlyLeavePortList) {
        	if (fd.getSECTOR().startsWith("AP")) {
        		continue;
        	}
        	
        	Date pass1 = fd.getATD(), pass2 = null;
        	if (pass1 == null && fd.getPTID().equals("TNA")) {
        		pass1 = fd.getETO();
        	}
        	if (JinJinPassPointList.contains(fd.getPTID())) {
        		pass2 = fd.getETO(); 
        	}
        	if (leaveJinJinMap.containsKey(fd.getIFPLID())) {
        		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != pass1) {
        			leaveJinJinMap.get(fd.getIFPLID()).setPass1(pass1);
        		}
        		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass2() && null != pass2) {
        			leaveJinJinMap.get(fd.getIFPLID()).setPass2(pass2);
        		}
        	} else {
        		fd.setPass1(pass1);
        		fd.setPass2(pass2);
        		leaveJinJinMap.put(fd.getIFPLID(), fd);
        	}
        	if (null != leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != leaveJinJinMap.get(fd.getIFPLID()).getPass2()) {
        		long intervalMis = leaveJinJinMap.get(fd.getIFPLID()).getPass2().getTime() - leaveJinJinMap.get(fd.getIFPLID()).getPass1().getTime();
        		int intervalMinutue = (int) (intervalMis / 60000);
        		leaveJinJinMap.get(fd.getIFPLID()).setInterval(intervalMinutue);
        		leaveJinJinMap.get(fd.getIFPLID()).setMinutes((int)(leaveJinJinMap.get(fd.getIFPLID()).getPass1().getTime() - nowTime.getTime()) / 6000);
        		if (logger.isDebugEnabled()) {
        			logger.debug("\n---------Leave---1-----ARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", fd.getARCID(), fd.getIFPLID(), leaveJinJinMap.get(fd.getIFPLID()).getPass1(), leaveJinJinMap.get(fd.getIFPLID()).getPass2(), intervalMinutue);
        		}
        		jinjinList.add(leaveJinJinMap.get(fd.getIFPLID()));
        	}
        }
        
        
        List<String> nowPassPointList = Lists.newArrayList("BASOV","P291","GULEK","P353","P200","ABTUB", "P292","PANKI");
        List<FlyData> nowLeavePortList = jinjinDao.getNowLeavePortForJinJin(nowPassPointList);
        
        List<FlyData> nowjinjinList = new ArrayList<>();
        if (null != nowLeavePortList && nowLeavePortList.size() > 0) {
            for (FlyData fd : nowLeavePortList) {
            	Date pass1 = nowTime, pass2 = null;
            	if (nowPassPointList.contains(fd.getPTID())) {
            		pass2 = fd.getETO(); 
            	}
            	if (leaveJinJinMap.containsKey(fd.getIFPLID())) {
            		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != pass1) {
            			leaveJinJinMap.get(fd.getIFPLID()).setPass1(pass1);
            		}
            		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass2() && null != pass2) {
            			leaveJinJinMap.get(fd.getIFPLID()).setPass2(pass2);
            		}
            	} else {
            		fd.setPass1(pass1);
            		fd.setPass2(pass2);
            		leaveJinJinMap.put(fd.getIFPLID(), fd);
            	}
            	if (null != leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != leaveJinJinMap.get(fd.getIFPLID()).getPass2()) {
            		long intervalMis = leaveJinJinMap.get(fd.getIFPLID()).getPass2().getTime() - leaveJinJinMap.get(fd.getIFPLID()).getPass1().getTime();
            		int intervalMinutue = (int) (intervalMis / 60000);
            		FlyData tmp = leaveJinJinMap.get(fd.getIFPLID());
            		tmp.setInterval(intervalMinutue);
            		tmp.setMinutes(0);
            		nowjinjinList.add(tmp);
            		if (logger.isDebugEnabled()) {
            			logger.debug("\n---------Leave---2-----ARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", tmp.getARCID(), tmp.getIFPLID(), tmp.getPass1(), tmp.getPass2(), intervalMinutue);
            		}
            	}
            }
        }
        
        List<FlyData> allList = new ArrayList<>();
        if (null != jinjinList && jinjinList.size() > 0) {
            allList.addAll(jinjinList);
        }
        
        if (null != nowjinjinList && nowjinjinList.size() > 0) {
            allList.addAll(nowjinjinList);
        }
        Set<String> doneSet = new HashSet<>();
        List<FlyData> retList = new ArrayList<>();
        for (FlyData port : allList) {
        	if (!doneSet.contains(port.getARCID() + ":" + port.getATD())) {
        		retList.add(port);
        		doneSet.add(port.getARCID() + ":" + port.getATD());
        	}
        }
        return retList;
    }

    //进近进港表航班
    public List<FlyData> getEnterPortTable(Date nowTime) throws ParseException {
        if (null == nowTime) {
            nowTime = getNOW();
        }
        String haveArrivedStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<FlyData> haveArrivedEnterPortList = jinjinDao.getHaveArrivedFlyDataForJinJin(haveArrivedStartTime);
        
        List<FlyData> toArriveEnterPortList = jinjinDao.getToArriveFlyDataForJinJin(JinJinPassPointList);
        List<FlyData> jinjinList = new ArrayList<>();
        for (FlyData fd : toArriveEnterPortList) {
        	if (fd.getSECTOR().startsWith("AP")) {
        		continue;
        	}
        	Date pass1 = null, pass2 = null;
        	if (fd.getPTID().equals("GULEK")) {
        		pass1 = fd.getETO();
        		pass2 = DateUtils.addMinutes(fd.getETO(), 22); 
        	} else if (fd.getPTID().equals("P292") || fd.getPTID().equals("ABTUB") || fd.getPTID().equals("P200")) {
        		pass1 = fd.getETO();
        		pass2 = DateUtils.addMinutes(fd.getETO(), 13); 
        	} else if (fd.getPTID().equals("BASOV")) {
        		pass1 = fd.getETO();
        		pass2 = DateUtils.addMinutes(fd.getETO(), 20); 
        	} else if (fd.getPTID().equals("P291") || fd.getPTID().equals("PANKI") ) {
        		pass1 = fd.getETO();
        		pass2 = DateUtils.addMinutes(fd.getETO(), 15); 
        	}
        	if (null == pass1) {
        		continue;
        	}
        	fd.setPass1(pass1);
        	fd.setPass2(pass2);
			long intervalMis = pass2.getTime() - pass1.getTime();
        	int intervalMinutue = (int) (intervalMis / 60000) ;
        	fd.setInterval(intervalMinutue);
        	fd.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 6000);
        	if (logger.isDebugEnabled()) {
        		logger.debug("\n---------Enter--1------------ARCID:{}, FDRID:{} PASS1 :{}, PASS2:{}, IntervalMis:{} ", fd.getARCID(), fd.getIFPLID(), pass1, pass2, intervalMinutue);
        	}
        	jinjinList.add(fd);
        }
        
        List<String> nowPassPointList = Lists.newArrayList("TNA","BASOV","P291","GULEK","P353","P200","ABTUB", "P292","PANKI");
        List<FlyData> nowEnterPortList = jinjinDao.getNowEnterPortForJinJin(nowPassPointList);
        List<FlyData> nowjinjinList = new ArrayList<>();
        if (null != nowEnterPortList && nowEnterPortList.size() > 0) {
            for (FlyData fd : nowEnterPortList) {
            	Date pass1 = nowTime, pass2 = null;
            	if (fd.getPTID().equals("GULEK")) {
            		pass2 = DateUtils.addMinutes(fd.getETO(), 22); 
            	} else if (fd.getPTID().equals("P292") || fd.getPTID().equals("ABTUB") || fd.getPTID().equals("P200")) {
            		pass2 = DateUtils.addMinutes(fd.getETO(), 13); 
            	} else if (fd.getPTID().equals("BASOV")) {
            		pass2 = DateUtils.addMinutes(fd.getETO(), 20); 
            	} else if (fd.getPTID().equals("P291") || fd.getPTID().equals("PANKI") ) {
            		pass2 = DateUtils.addMinutes(fd.getETO(), 15); 
            	}
            	if (null == pass2) {
            		continue;
            	}
            	fd.setPass1(pass1);
            	fd.setPass2(pass2);
    			long intervalMis = pass2.getTime() - pass1.getTime();
            	int intervalMinutue = (int) (intervalMis / 60000) ;
            	fd.setInterval(intervalMinutue);
            	fd.setMinutes(0);
    			if (logger.isDebugEnabled()) {
            		logger.debug("\n----------Enter---2---------ARCID:{}, FDRID:{} PASS1 :{}, PASS2:{}, IntervalMis:{} ", fd.getARCID(), fd.getIFPLID(), pass1, pass2, intervalMinutue);
            	}
    			nowjinjinList.add(fd);
            }
        }
        
        List<FlyData> allList = new ArrayList<>();
        if (null != jinjinList && jinjinList.size() > 0) {
            allList.addAll(jinjinList);
        }
        
        
        if (null != nowjinjinList && nowjinjinList.size() > 0) {
            allList.addAll(nowjinjinList);
        }
        Set<String> doneSet = new HashSet<>();
        List<FlyData> retList = new ArrayList<>();
        for (FlyData port : allList) {
        	if (!doneSet.contains(port.getARCID() + ":" + port.getInterval())) {
        		retList.add(port);
        		doneSet.add(port.getARCID() + ":" + port.getInterval());
        	}
        }
        return retList;
    }
    
}
