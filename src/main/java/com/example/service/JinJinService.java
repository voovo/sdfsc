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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 进近相关业务逻辑
 */
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
    
    /**
     * 取当前时间
     */
    public static Date getNOW(){
    	Date now = new Date();
		Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR_OF_DAY, -8);
        return c.getTime();
    }
    
    /**
     * 进近出港航班
     */
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
        	String direction = getDirection(fd);
        	pass1 = fd.getATD();
        	if (null == pass2) {
        		continue;
        	}
        	
        	if (pass2.after(nowTime)) {
        		pass1 = DateUtils.addMinutes(nowTime, -5);
        	}
        	if (pass1.after(pass2)) {
        		continue;
        	}
        	// 3.16号，所有出港航班有实际起飞时间的，生命周期在实际起飞的基础上+7
        	if (pass1 != null) {
        		pass2 = DateUtils.addMinutes(pass1, 7);
        	}
        	fd.setPass1(pass1);
        	fd.setPass2(pass2);
        	if (!direction.equals("Direction")) {
    			fd.setDirection(direction);
			}
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
        	if (null != fd.getSECTOR() && fd.getSECTOR().startsWith("AP")) {
        		continue;
        	}
        	
        	Date pass1 = fd.getATD(), pass2 = null;
        	if (pass1 == null && fd.getPTID().equals("TNA")) {
        		pass1 = fd.getETO();
        	}
        	if (JinJinPassPointList.contains(fd.getPTID())) {
        		pass2 = fd.getETO(); 
        	}
        	// 3.16号，所有出港航班有实际起飞时间的，生命周期在实际起飞的基础上+7
        	if (pass1 != null) {
        		pass2 = DateUtils.addMinutes(pass1, 7);
        	}
        	String direction = getDirection(fd);
        	if (leaveJinJinMap.containsKey(fd.getIFPLID())) {
        		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != pass1) {
        			leaveJinJinMap.get(fd.getIFPLID()).setPass1(pass1);
        			if (!direction.equals("Direction")) {
        				leaveJinJinMap.get(fd.getIFPLID()).setDirection(direction);
        			}
        		}
        		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass2() && null != pass2) {
        			leaveJinJinMap.get(fd.getIFPLID()).setPass2(pass2);
        			if (!direction.equals("Direction")) {
        				leaveJinJinMap.get(fd.getIFPLID()).setDirection(direction);
        			}
        		}
        	} else {
        		fd.setPass1(pass1);
        		fd.setPass2(pass2);
        		if (!direction.equals("Direction")) {
        			fd.setDirection(direction);
    			}
        		leaveJinJinMap.put(fd.getIFPLID(), fd);
        	}
        	if (null != leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != leaveJinJinMap.get(fd.getIFPLID()).getPass2()) {
        		if (leaveJinJinMap.get(fd.getIFPLID()).getPass1().before(nowTime)) {
            		// 未出港的飞机，第一个过点时间肯定不能早于现在，如果早于了，认为有问题，忽略
            		continue;
            	}
        		long intervalMis = leaveJinJinMap.get(fd.getIFPLID()).getPass2().getTime() - leaveJinJinMap.get(fd.getIFPLID()).getPass1().getTime();
        		int intervalMinutue = (int) (intervalMis / 60000);
        		leaveJinJinMap.get(fd.getIFPLID()).setInterval(intervalMinutue);
//        		Date pointTime = leaveJinJinMap.get(fd.getIFPLID()).getATD();
//                if (pointTime == null) {
//                    pointTime = leaveJinJinMap.get(fd.getIFPLID()).getEOBT();
//                }
        		leaveJinJinMap.get(fd.getIFPLID()).setMinutes((int)(leaveJinJinMap.get(fd.getIFPLID()).getPass1().getTime() - nowTime.getTime()) / 60000);
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
            	// 3.16号，所有出港航班有实际起飞时间的，生命周期在实际起飞的基础上+7
            	if (pass1 != null) {
            		pass2 = DateUtils.addMinutes(pass1, 7);
            	}
            	String direction = getDirection(fd);
            	if (leaveJinJinMap.containsKey(fd.getIFPLID())) {
            		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass1() && null != pass1) {
            			leaveJinJinMap.get(fd.getIFPLID()).setPass1(pass1);
            			if (!direction.equals("Direction")) {
            				leaveJinJinMap.get(fd.getIFPLID()).setDirection(direction);
            			}
            		}
            		if (null == leaveJinJinMap.get(fd.getIFPLID()).getPass2() && null != pass2) {
            			leaveJinJinMap.get(fd.getIFPLID()).setPass2(pass2);
            			if (!direction.equals("Direction")) {
            				leaveJinJinMap.get(fd.getIFPLID()).setDirection(direction);
            			}
            		}
            	} else {
            		fd.setPass1(pass1);
            		fd.setPass2(pass2);
            		if (!direction.equals("Direction")) {
        				fd.setDirection(direction);
        			}
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
        Collections.sort(retList, new Comparator<FlyData>(){

			@Override
			public int compare(FlyData o1, FlyData o2) {
				if (null != o1.getEOBT() && null != o2.getEOBT()) {
					return o1.getEOBT().compareTo(o2.getEOBT());
				} else{
					return 1;
				}
				
				
			}
        	
        });
        return retList;
    }

	private String getDirection(FlyData fd) {
		String direction = "Direction";
		if (fd.getPTID().equals("GULEK")) {
			direction = "W";
		} else if (fd.getPTID().equals("P292") || fd.getPTID().equals("ABTUB") || fd.getPTID().equals("P200")) {
			direction = "S";
		} else if (fd.getPTID().equals("BASOV") || fd.getPTID().equals("P291")) {
			direction = "E";
		} else if ( fd.getPTID().equals("PANKI") ) {
			direction = "N";
		}
		return direction;
	}

    /**
     * 进近进港表航班
     */
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
//        	} else if (pass2.before(nowTime) && DateUtils.truncatedCompareTo(nowTime, pass2, Calendar.MINUTE) < 3) {
        	} else if (pass2.before(nowTime) && (nowTime.getTime() - pass2.getTime())/(1000*60) < 3) {
        		pass2 = DateUtils.addMinutes(nowTime, -5);
        	}
        	if (pass1.after(pass2)) {
        		continue;
        	}
        	fd.setPass1(pass1);
        	fd.setPass2(pass2);
        	String direction = getDirection(fd);
        	fd.setDirection(direction);
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
        	Date pass1 = null, pass2 = null;
        	if (fd.getPTID().equals("GULEK")) {
        		pass1 = fd.getETO();
        		pass2 = DateUtils.addMinutes(pass1, 22);
        		// TODO zhangpc  20180303 问题：GULEK进港的航班， 考虑过点时间在当前时间之前情况时，过点时间1即为数据库过点时间，过点时间2即在过点时间1的基础上加22分钟
        		if (pass1 != null && pass1.before(nowTime)) {
        			pass1 = DateUtils.addMinutes(pass1, 5);
        		}
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
        	// 未来进港航班，如果实际起飞时间为null（说明未起飞），并且它的预计降落时间在当前时间往后推1小时之内的，则过滤掉
        	
        	Date afterTime = DateUtils.addHours(nowTime, 1);
        	Date afterDate = DateUtils.addMinutes(afterTime, 30);
        	if (fd.getATD() == null && fd.getETA().after(nowTime) && fd.getETA().before(afterDate)) {
        		continue;
        	}
        	
        	fd.setPass1(pass1);
        	fd.setPass2(pass2);
        	String direction = getDirection(fd);
        	fd.setDirection(direction);
			long intervalMis = pass2.getTime() - pass1.getTime();
        	int intervalMinutue = (int) (intervalMis / 60000) ;
        	fd.setInterval(intervalMinutue);
        	fd.setMinutes((int)(fd.getPass1().getTime() - nowTime.getTime()) / 60000);
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
            	if (pass1.after(nowTime)) {
            		pass1 = nowTime;
            	}
            	fd.setPass1(pass1);
            	fd.setPass2(pass2);
            	String direction = getDirection(fd);
            	fd.setDirection(direction);
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
        		// TODO zhangpc 考虑过点时间在当前时间之的情况，看是否符合当前管制的条件
        		if (port.getPass1().after(nowTime) && (port.getSECTOR().startsWith("AP"))) {
        			port.setMinutes(0);
        		} else if (port.getMinutes() < 0 && port.getPass2().after(nowTime) && !port.getSECTOR().startsWith("AP")) {
        			port.setInterval(5);
        			port.setMinutes(5);
        		}
        		retList.add(port);
        		doneSet.add(port.getARCID());
        	}
        }
        Collections.sort(retList, new Comparator<FlyData>(){

			@Override
			public int compare(FlyData o1, FlyData o2) {
				return o1.getPass2().compareTo(o2.getPass2());
				
			}
        	
        });
        return retList;
    }
    
}
