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
        
        List<FlyData> haveLeavedFlyDataList = jinjinDao.getHaveLeavedFlyDataForJinJin(JinJinPassPointList);
        
        List<FlyData> leavedJinJinList = new ArrayList<>();
        for (FlyData fd : haveLeavedFlyDataList) {
        	Date pass1 = null, pass2 = null;
        	if (fd.getPTID().equals("GULEK")) {
        		pass2 = fd.getETO();
        	} else if (fd.getPTID().equals("P292") || fd.getPTID().equals("ABTUB") || fd.getPTID().equals("P200")) {
        		pass2 = fd.getETO();
        	} else if (fd.getPTID().equals("BASOV")) {
        		pass2 = fd.getETO();
        	} else if (fd.getPTID().equals("P291") || fd.getPTID().equals("PANKI") ) {
        		pass2 = fd.getETO();
        	}
        	pass1 = fd.getATD();
        	if (null == pass2) {
        		continue;
        	}
        	if (pass2.after(nowTime)) {
        		pass2 = DateUtils.addMinutes(nowTime, -5);
        	}
        	if (pass1.after(pass2)) {
        		continue;
        	}
        	fd.setPass1(pass1);
        	fd.setPass2(pass2);
			long intervalMis = pass2.getTime() - pass1.getTime();
        	int intervalMinutue = (int) (intervalMis / 60000) ;
        	fd.setInterval(intervalMinutue);
        	fd.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 60000);
        	if (logger.isDebugEnabled()) {
        		logger.debug("\n---------Have Leaved--1------------{} ", fd);
        	}
        	leavedJinJinList.add(fd);
        }
        
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
        		Date pointTime = leaveJinJinMap.get(fd.getIFPLID()).getATD();
                if (pointTime == null) {
                    pointTime = leaveJinJinMap.get(fd.getIFPLID()).getEOBT();
                }
        		leaveJinJinMap.get(fd.getIFPLID()).setMinutes((int)(pointTime.getTime() - nowTime.getTime()) / 60000);
        		if (logger.isDebugEnabled()) {
        			logger.debug("\n---------Leave---1-----{}", leaveJinJinMap.get(fd.getIFPLID()));
        		}
        		jinjinList.add(leaveJinJinMap.get(fd.getIFPLID()));
        	}
        }
        
        
        List<String> nowPassPointList = Lists.newArrayList("BASOV","P291","GULEK","P353","P200","ABTUB", "P292","PANKI");
        List<FlyData> nowLeavePortList = jinjinDao.getNowLeavePortForJinJin(nowPassPointList);
        
        List<FlyData> nowjinjinList = new ArrayList<>();
        if (null != nowLeavePortList && nowLeavePortList.size() > 0) {
            for (FlyData fd : nowLeavePortList) {
            	Date pass1 = fd.getATD(), pass2 = null;
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
            		tmp.setMinutes((int)(tmp.getPass1().getTime() - nowTime.getTime()) / 60000);
            		nowjinjinList.add(tmp);
            		if (logger.isDebugEnabled()) {
            			logger.debug("\n---------Leave---2-----{} ", tmp);
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
        if (!CollectionUtils.isEmpty(leavedJinJinList)) {
        	allList.addAll(leavedJinJinList);
        }
//        return allList;
        Set<String> doneSet = new HashSet<>();
        List<FlyData> retList = new ArrayList<>();
        for (FlyData port : allList) {
        	if (!doneSet.contains(port.getARCID())) {
        		retList.add(port);
        		doneSet.add(port.getARCID());
        	}
        }
        return retList;
    }

    //进近进港表航班
    public List<FlyData> getEnterPortTable(Date nowTime) throws ParseException {
        if (null == nowTime) {
            nowTime = getNOW();
        }
        List<FlyData> haveArrivedEnterPortList = jinjinDao.getHaveArrivedFlyDataForJinJin(JinJinPassPointList);
        List<FlyData> arrivedJinJinList = new ArrayList<>();
        for (FlyData fd : haveArrivedEnterPortList) {
        	Date pass1 = null, pass2 = null;
        	if (fd.getPTID().equals("GULEK")) {
        		pass1 = fd.getETO();
        	} else if (fd.getPTID().equals("P292") || fd.getPTID().equals("ABTUB") || fd.getPTID().equals("P200")) {
        		pass1 = fd.getETO();
        	} else if (fd.getPTID().equals("BASOV")) {
        		pass1 = fd.getETO();
        	} else if (fd.getPTID().equals("P291") || fd.getPTID().equals("PANKI") ) {
        		pass1 = fd.getETO();
        	}
        	pass2 = fd.getATA();
        	if (null == pass1) {
        		continue;
        	}
        	if (pass2.after(nowTime)) {
        		pass2 = DateUtils.addMinutes(nowTime, -5);
        	}
        	if (pass1.after(pass2)) {
        		continue;
        	}
        	fd.setPass1(pass1);
        	fd.setPass2(pass2);
			long intervalMis = pass2.getTime() - pass1.getTime();
        	int intervalMinutue = (int) (intervalMis / 60000) ;
        	fd.setInterval(intervalMinutue);
        	fd.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 60000);
        	if (logger.isDebugEnabled()) {
        		logger.debug("\n---------Have Arrived--1------------{} ", fd);
        	}
        	arrivedJinJinList.add(fd);
        }
        
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
        	fd.setMinutes((int)(fd.getETA().getTime() - nowTime.getTime()) / 60000);
        	if (logger.isDebugEnabled()) {
        		logger.debug("\n---------Enter--1------------{} ", fd);
        	}
        	jinjinList.add(fd);
        }
        
        List<String> nowPassPointList = Lists.newArrayList("TNA","BASOV","P291","GULEK","P353","P200","ABTUB", "P292","PANKI");
        List<FlyData> nowEnterPortList = jinjinDao.getNowEnterPortForJinJin(nowPassPointList);
        List<FlyData> nowjinjinList = new ArrayList<>();
        if (null != nowEnterPortList && nowEnterPortList.size() > 0) {
            for (FlyData fd : nowEnterPortList) {
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
            	if (null == pass2) {
            		continue;
            	}
            	fd.setPass1(pass1);
            	fd.setPass2(pass2);
    			long intervalMis = pass2.getTime() - pass1.getTime();
            	int intervalMinutue = (int) (intervalMis / 60000) ;
            	fd.setInterval(intervalMinutue);
            	fd.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 60000);
    			if (logger.isDebugEnabled()) {
            		logger.debug("\n----------Enter---2---------{} ", fd);
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
        if (!CollectionUtils.isEmpty(arrivedJinJinList)) {
        	allList.addAll(arrivedJinJinList);
        }
//        return allList;
        Set<String> doneSet = new HashSet<>();
        List<FlyData> retList = new ArrayList<>();
        for (FlyData port : allList) {
        	if (!doneSet.contains(port.getARCID())) {
        		retList.add(port);
        		doneSet.add(port.getARCID());
        	}
        }
        return retList;
    }
    
}
