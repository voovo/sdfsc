package com.example.service;

import com.example.controller.PortController;
import com.example.dao.PortDao;
import com.example.domain.DTO.*;
import com.google.gson.Gson;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
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

/**
 * Created by SunYi on 2016/2/16/0016.
 */
@Service
public class PortService {
	
	static Logger logger = LoggerFactory.getLogger(PortController.class);
    
    static Gson gson = new Gson();
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyyMMddHHmmss");
    
    @Autowired
    private PortDao portDao;
    
    public static Date getNOW(){
    	Date now = new Date();
    	Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR_OF_DAY, -8);
        return c.getTime();
//    	DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;  
//    	Date now = null;
//		try {
//			now = format.parse("2017-05-16 13:30:00");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	return now;
    }
    
    //出港航班
    public List<LeavePort> getLeavePortTable(Date nowTime) {
        
        String haveFlyingStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<LeavePort> haveFlyingLeavePortList = portDao.getHaveFlyingLeavePortFromJinan(haveFlyingStartTime);
        logger.debug("haveFlyingLeavePortList:{}", gson.toJson(haveFlyingLeavePortList));
        String startTime = DATE_FORMAT_2.format(DateUtils.addMinutes(PortService.getNOW(), 2));
        String endTime = DATE_FORMAT_2.format(DateUtils.addDays(PortService.getNOW(), 2));
        List<LeavePort> toFlyLeavePortList = portDao.getToFlyLeavePortFromJinan(startTime, endTime);
        logger.debug("toFlyLeavePortList:{}", gson.toJson(toFlyLeavePortList));
        
        List<LeavePort> nowLeavePortList = portDao.getNowLeavePortFromJinan();
        logger.debug("nowLeavePortList:{}", gson.toJson(nowLeavePortList));
        
        List<LeavePort> allList = new ArrayList<>();
        if (null != haveFlyingLeavePortList && haveFlyingLeavePortList.size() > 0) {
            allList.addAll(haveFlyingLeavePortList);
        }
        if (null != toFlyLeavePortList && toFlyLeavePortList.size() > 0) {
            allList.addAll(toFlyLeavePortList);
        }
        
        for (Iterator<LeavePort> it = allList.iterator(); it.hasNext(); ) {
            LeavePort port = it.next();
            Date time = port.getATD();
            if (time == null) {
                time = port.getEOBT();
            }
            if (time != null) {
                long min = time.getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                //一小时内的出港情况
                if (minute > 180 || minute < -60) {
                    it.remove();
                } else {
                    port.setMinutes(minute);
                    logger.debug("LeavePort:{}", gson.toJson(port));
                }
            } else {
                it.remove();
            }

        }
        if (null != nowLeavePortList && nowLeavePortList.size() > 0) {
            for (LeavePort port : nowLeavePortList) {
                port.setMinutes(0);
                allList.add(port);
            }
        }
        return allList;
    }

    //进港表航班
    public List<EnterPort> getEnterPortTable(Date nowTime) throws ParseException {
        if (null == nowTime) {
            nowTime = getNOW();
        }
        String haveArrivedStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<EnterPort> haveArrivedEnterPortList = portDao.getHaveArrivedEnterPortFromJinan(haveArrivedStartTime);
        logger.debug("过去一小时进近入港数据:{}", gson.toJson(haveArrivedEnterPortList));
        String startTime = DATE_FORMAT_2.format(PortService.getNOW());
        String endTime = DATE_FORMAT_2.format(DateUtils.addHours(PortService.getNOW(), 3));
        List<EnterPort> toArriveEnterPortList = portDao.getEnterJinJinFilghtForJinan(startTime, endTime);
        logger.debug("未来三小时进近入港航班:{}", gson.toJson(toArriveEnterPortList));
        List<EnterPort> jinjinList = new ArrayList<>();
        for (EnterPort ep : toArriveEnterPortList) {
        	List<EnterTimeVo> enterTimeList = portDao.getJinJinTimeForJinan(ep.getIFPLID());
        	if (null == enterTimeList || enterTimeList.size() <= 0) {
        		continue;
        	}
        	EnterTimeVo tna = enterTimeList.get(0);
        	if (enterTimeList.size() > 1) {
        		for (int i=1; i<enterTimeList.size(); i++) {
        			EnterTimeVo et = enterTimeList.get(i);
        			Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
        			Date outJinJinTime = DATE_FORMAT_2.parse(et.getETO());
        			long intervalMis = tnaTime.getTime() - outJinJinTime.getTime();
        			int intervalMinutue = (int) (intervalMis / 60000) ;
        			EnterPort tep = new EnterPort();
        			BeanUtils.copyProperties(ep, tep);
        			tep.setETA(DateUtils.addMinutes(outJinJinTime, 0-intervalMinutue));
        			jinjinList.add(tep);
        		}
        	}
        }
        
        List<EnterPort> allList = new ArrayList<>();
        if (null != haveArrivedEnterPortList && haveArrivedEnterPortList.size() > 0) {
            allList.addAll(haveArrivedEnterPortList);
        }
        if (null != jinjinList && jinjinList.size() > 0) {
            allList.addAll(jinjinList);
        }
        
        for (Iterator<EnterPort> it = allList.iterator(); it.hasNext(); ) {
            EnterPort port = it.next();
            Date time = port.getETA();
            if (time != null) {
                long min = time.getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                //一小时内的进港情况
                if (minute > 180 || minute < -60) {
                    it.remove();
                } else {
                    port.setMinutes(minute);
                    logger.debug("EnterPort:{}", gson.toJson(port));
                }
            } else {
                it.remove();
            }
        }
//        if (null != nowEnterPortList && nowEnterPortList.size() > 0) {
//            for (EnterPort port : nowEnterPortList) {
//                port.setMinutes(0);
//                allList.add(port);
//            }
//        }
        return allList;
    }

    public List<AllPort> getAllPortTable(Date nowTime) {
        List<AllPort> leavePorts = portDao.getAllPortFromJinan();
        for (Iterator<AllPort> it = leavePorts.iterator(); it.hasNext(); ) {
            AllPort port = it.next();
            Date time = port.getATD();
            if (time == null) {
                time = port.getEOBT();
            }
            if (time != null) {
                long min = time.getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                //一小时内的出港情况
                if (minute > 180 || minute < -60) {
                    it.remove();
                } else {
                    port.setMinutes(minute);
                }
            } else {
                it.remove();
            }

        }
        return leavePorts;
    }

    

    public List<AreaPort> getAreaPortToZhengzhouAndWuhan(Date nowTime) {
        List<AreaPort> areaPorts = portDao.getAreaPortToZhengzhou();
        List<AreaPort> areaPorts2 = portDao.getAreaPortToWuhan();
        areaPorts.addAll(areaPorts2);
        for (Iterator<AreaPort> it = areaPorts.iterator(); it.hasNext(); ) {
            AreaPort port = it.next();
            Date time = port.getJNTime();
            if (time != null) {
                long min = time.getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                //到达REPOL的时间至30分钟之后
//                if (minute < 0 || minute > 30) {
                if (minute < -60 || minute > 180) {
                    it.remove();
                } else {
                    port.setMinutes(minute);
                }
            } else {
                it.remove();
            }

        }
        return areaPorts;
    }
    
    //区域调度航班
    public List<AreaPort> getAreaPortTable(Date nowTime) {
        List<AreaPort> allPorts = new ArrayList<>();
        // 取曲线中的当前时间点以及之前一小时的数据
        Date startTime = DateUtils.addHours(new Date(), -1);
        List<Trackhis> trackhisList = portDao.getNowAndHistoryFlightId(startTime);
        logger.debug("TrackhisList:{}", gson.toJson(trackhisList));
        if (null != trackhisList && trackhisList.size() > 0) {
            Set<String> sameMinuteDeald = new HashSet<>();
            Set<String> arcidSet = new HashSet<>();
            for (Trackhis th : trackhisList) {
            	arcidSet.add(th.getFLIGHTID());
            }
            
            List<String> arcIdList = new ArrayList<>();
            arcIdList.addAll(arcidSet);
            List<AreaPort> flightList = portDao.getFlightInfoByArcIds(arcIdList);
            Map<String, AreaPort> flightMap = new HashMap<>();
            if (null != flightList && flightList.size() > 0) {
            	for (AreaPort port : flightList) {
            		if (flightMap.containsKey(port.getARCID())) {
            			continue;
            		}
            		flightMap.put(port.getARCID(), port);
            	}
            }
            
            
            
            for (Trackhis th : trackhisList) {
            	AreaPort port = new AreaPort();
            	if (null == flightMap.get(th.getFLIGHTID())) {
            		continue;
            	}
            	BeanUtils.copyProperties(flightMap.get(th.getFLIGHTID()), port);
                long min = DateUtils.addHours(th.getRCVTIME(), -8).getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                if (sameMinuteDeald.contains(port.getARCID() + ":" + minute)) {
                    continue;
                }
                port.setJNTime(flightMap.get(th.getFLIGHTID()).getJNTime());
                port.setMinutes(minute);
                allPorts.add(port);
                if (logger.isDebugEnabled()) {
                    logger.debug("#####000000####### ID:{}   Minute:{}", port.getARCID(), minute);
                }
                sameMinuteDeald.add(port.getARCID() + ":" + minute);
            }
        }
        
        /*
         * 接下来是未来的预测数据
         */
        
    	String pstartTime = DATE_FORMAT_2.format(nowTime);
    	String pendTime = DATE_FORMAT_2.format(DateUtils.addHours(nowTime, 4));

    	// 已经起飞的飞机
    	List<AreaPort> haveFlyingPorts = portDao.getHaveFlyingPortFromJinan(pstartTime, pendTime);
    	logger.debug("haveFlyingPorts:{}", gson.toJson(haveFlyingPorts));
    	if (null != haveFlyingPorts && haveFlyingPorts.size() > 0) {
    	    for (Iterator<AreaPort> it = haveFlyingPorts.iterator(); it.hasNext(); ) {
    	        AreaPort port = it.next();
    	        // 起飞了多久
    	        long flyingTime = port.getJNTime().getTime() - nowTime.getTime();
    	        if (flyingTime > 14 * 60 * 1000 && flyingTime > 29 * 60 * 1000) {
    	            // 如果起飞时间超过14分钟，就会在上面的trackhis中查询到了
    	            // 如果起飞时间超过29分钟，就已经飞出
    	            it.remove();
    	            continue;
    	        }
    	        int minute = (int) (flyingTime / (60 * 1000));     // 已经飞了几分钟了
    	        int minMinute = 14 - minute;        // 从当前时间算起进入区调需要的时间
    	        int maxMinute = 29 - minute;        // 从当前时间算起飞出区调需要的时间
    	        for (int i=minMinute; i<=maxMinute; i++) {
    	            AreaPort newPort = new AreaPort();
    	            BeanUtils.copyProperties(port, newPort);
    	            newPort.setMinutes(i);
                    allPorts.add(newPort);
                    if (logger.isDebugEnabled()) {
                        logger.debug("#####111111111####### ID:{}   Minute:{}", port.getARCID(), i);
                    }
    	        }
    	    }
    	}
    	
//    	等待起飞的飞机
    	List<AreaPort> toFlyPorts = portDao.getWaitToFlyPortFromJinan(pstartTime, pendTime);
    	logger.debug("toFlyPorts:{}", gson.toJson(toFlyPorts));
        if (null != toFlyPorts && toFlyPorts.size() > 0) {
            for (Iterator<AreaPort> it = toFlyPorts.iterator(); it.hasNext(); ) {
                AreaPort port = it.next();
                // 等多久才起飞
                long waitTime = port.getJNTime().getTime() - nowTime.getTime();
                if (waitTime > (3 * 60 * 60 * 1000 - 29 * 60 * 1000)) {
                    // 如果等待时间超过 （3小时 － 29分钟）
                    it.remove();
                    continue;
                }
                int minute = (int) (waitTime / (60 * 1000));     // 需要等几分钟
                int minMinute = 14 + minute;        // 从当前时间算起进入区调需要的时间
                int maxMinute = 29 + minute;        // 从当前时间算起飞出区调需要的时间
                for (int i=minMinute; i<=maxMinute; i++) {
                    AreaPort newPort = new AreaPort();
                    BeanUtils.copyProperties(port, newPort);
                    newPort.setMinutes(i);
                    allPorts.add(newPort);
                    if (logger.isDebugEnabled()) {
                        logger.debug("#####222222222222####### ID:{}   Minute:{}", port.getARCID(), i);
                    }
                }
            }
        }
        
        // 还未降落的飞机
        List<AreaPort> toArrivePorts = portDao.getToArrivePortFromJinan(pstartTime, pendTime);
        logger.debug("toArrivePorts:{}", gson.toJson(toArrivePorts));
        if (null != toArrivePorts && toArrivePorts.size() > 0) {
            for (Iterator<AreaPort> it = toArrivePorts.iterator(); it.hasNext(); ) {
                AreaPort port = it.next();
                // 等多久才降落
                long waitTime = port.getJNTime().getTime() - nowTime.getTime();
                if (waitTime > (3 * 60 * 60 * 1000 - 14 * 60 * 1000)) {
                    // 如果等待时间超过 （3小时 － 14分钟）
                    it.remove();
                    continue;
                }
                int minute = (int) (waitTime / (60 * 1000));     // 需要等几分钟
                int minMinute = minute - 29;        // 从当前时间算起进入区调需要的时间
                int maxMinute = minute - 14;        // 从当前时间算起飞出区调需要的时间
                for (int i=minMinute; i<=maxMinute; i++) {
                    AreaPort newPort = new AreaPort();
                    BeanUtils.copyProperties(port, newPort);
                    newPort.setMinutes(i);
                    allPorts.add(newPort);
                    if (logger.isDebugEnabled()) {
                        logger.debug("#####333333333333####### ID:{}   Minute:{}", port.getARCID(), i);
                    }
                }
            }
        }
        
        return allPorts;
    }
    

    //经验列表的航班
    public List<ExperiencePort> getExperiencePortTable(Date nowTime) {
        List<ExperiencePort> experiencePort = portDao.getExperiencePort();
        for (Iterator<ExperiencePort> it = experiencePort.iterator(); it.hasNext(); ) {
            ExperiencePort port = it.next();
            Date time = port.getETA();
            if (time != null) {
                long min = time.getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                //一小时内的进港情况
                if (minute > 180 || minute < -60) {
                    it.remove();
                } else {
                    port.setMinutes(minute);
                }
            } else {
                it.remove();
            }
        }
        return experiencePort;
    }

    //获得进出济南的航班
    public List<ZSJNPort> getEnterZSJNPortTable(Date nowTime,List<ZSJNPort> zsjnPorts) {
        for (Iterator<ZSJNPort> it = zsjnPorts.iterator(); it.hasNext(); ) {
            ZSJNPort port = it.next();
            Date time = port.getETA();
            if (time != null) {
                long min = time.getTime() - nowTime.getTime();
                int minute = (int) (min / (60 * 1000));
                //一小时内的进港情况
                if (minute > 180 || minute < -60) {
                    it.remove();
                } else {
                    port.setMinutes(minute);
                }
            } else {
                it.remove();
            }
        }
        return zsjnPorts;
    }
    
    public List<ZSJNPort> getEnterZSJNPortTable(Date time) throws ParseException {
        List<ZSJNPort> zsjnPorts = portDao.getEnterZSJNPort();
        return getEnterZSJNPortTable(time,zsjnPorts);
    }
    
    public List<ZSJNPort> getLeaveZSJNPortTable(Date time) throws ParseException {
        List<ZSJNPort> zsjnPorts = portDao.getLeaveZSJNPort();
        return getEnterZSJNPortTable(time,zsjnPorts);
    }
}
