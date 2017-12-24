package com.example.service;

import com.example.dao.PortDao;
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

@Service
public class PortService {
	/*
	 ETA	预计降落时间
	 ATA	实际降落时间
	 
	 TNA	遥墙机场代号
	 ETO	预计到达某点时间
	 
	 EOBT	预计离开时间
	 ATD	实际离开时间
	 */
	
	static Logger logger = LoggerFactory.getLogger(PortService.class);
    
    static Gson gson = new Gson();
    
    public static final String AIR_PORT = "ZSJN";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyyMMddHHmmss");
    
    public static final List<String> LEAVE_PASS_POINT = Lists.newArrayList("REPOL", "WFG", "GULEK");
    public static final List<String> ENTER_PASS_POINT = Lists.newArrayList("BASOV", "P291", "PANKI", "WXI");
    
    @Autowired
    private PortDao portDao;
    
    public static Date getNOW(){
    	Date now = new Date();
		Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR_OF_DAY, -8);
        return c.getTime();
    }
    
    //进近出港航班
    public List<LeavePort> getLeavePortTable(Date nowTime) throws ParseException {
        
        String haveFlyingStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<LeavePort> haveFlyingLeavePortList = portDao.getHaveFlyingLeavePortFromJinan(haveFlyingStartTime);
//        logger.debug("haveFlyingLeavePortList:{}", gson.toJson(haveFlyingLeavePortList));
        
        String startTime = DATE_FORMAT_2.format(PortService.getNOW());
        String endTime = DATE_FORMAT_2.format(DateUtils.addHours(PortService.getNOW(), 3));
        List<LeavePort> toFlyLeavePortList = portDao.getToFlyLeavePortFromJinan(startTime, endTime);
        
        List<LeavePort> jinjinList = new ArrayList<>();
        for (LeavePort ep : toFlyLeavePortList) {
        	if (ep.getSECTOR().startsWith("AP")) {
        		continue;
        	}
        	List<EnterTimeVo> leaveTimeList = portDao.getJinJinTimeForJinan(ep.getIFPLID());
        	if (null == leaveTimeList || leaveTimeList.size() != 2) {
        		continue;
        	}
        	EnterTimeVo tna = leaveTimeList.get(1);
        	EnterTimeVo eto = leaveTimeList.get(0);
			Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
			Date outJinJinTime = DATE_FORMAT_2.parse(eto.getETO());
			long intervalMis = outJinJinTime.getTime() - tnaTime.getTime();
			int intervalMinutue = (int) (intervalMis / 60000);
//			ep.setATD(tnaTime);
			ep.setTNA(tnaTime);
			ep.setETO(outJinJinTime);
			ep.setInterval(intervalMinutue);
			if (logger.isDebugEnabled()) {
        		logger.debug("\nARCID:{}, FDRID:{} TNA :{}, ETO:{}, IntervalMis:{} ", ep.getARCID(), ep.getIFPLID(), tna.getETO(), eto.getETO(), intervalMinutue);
        	}
			jinjinList.add(ep);
        }
        
        
        List<LeavePort> nowLeavePortList = portDao.getNowLeavePortFromJinan();
        if (logger.isDebugEnabled()) {
        	for (LeavePort lp : nowLeavePortList) {
        		logger.debug("nowLeavePortList:{}", lp.getARCID());
        	}
        }
        
        List<LeavePort> nowjinjinList = new ArrayList<>();
        if (null != nowLeavePortList && nowLeavePortList.size() > 0) {
            for (LeavePort port : nowLeavePortList) {
                List<EnterTimeVo> enterTimeList = portDao.getJinJinTimeForJinan(port.getIFPLID());
            	if (null == enterTimeList || enterTimeList.size() <= 0) {
            		continue;
            	}
            	EnterTimeVo tna = null;
            	EnterTimeVo eto = null;
            	for (EnterTimeVo et : enterTimeList) {
            		if (et.getPTID().equals("TNA")) {
            			tna = et;
            		} else {
            			eto = et;
            		}
            	}
            	Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
    			Date outJinJinTime = nowTime;
    			long intervalMis = DATE_FORMAT_2.parse(eto.getETO()).getTime() - nowTime.getTime();
    			int intervalMinutue = (int) (intervalMis / 60000) ;
    			port.setATD(nowTime);
    			port.setTNA(tnaTime);
    			port.setETO(outJinJinTime);
    			port.setInterval(intervalMinutue);
    			if (logger.isDebugEnabled()) {
            		logger.debug("\nARCID:{}, FDRID:{} TNA :{}, ETO:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), tna.getETO(), eto.getETO(), intervalMinutue);
            	}
    			nowjinjinList.add(port);
            }
        }
        
        List<LeavePort> allList = new ArrayList<>();
//        if (null != haveFlyingLeavePortList && haveFlyingLeavePortList.size() > 0) {
//            allList.addAll(haveFlyingLeavePortList);
//        }
        if (null != jinjinList && jinjinList.size() > 0) {
            allList.addAll(jinjinList);
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
                	if (!port.getSECTOR().startsWith("AP") && minute > -3 && minute < 3) {
                		it.remove();
                	} else {
                		port.setMinutes(minute);
                	}
                }
            } else {
                it.remove();
            }

        }
        if (null != nowjinjinList && nowjinjinList.size() > 0) {
            for (LeavePort port : nowjinjinList) {
                port.setMinutes(0);
                allList.add(port);
            }
        }
        Set<String> doneSet = new HashSet<>();
        List<LeavePort> retList = new ArrayList<>();
        for (LeavePort port : allList) {
        	if (!doneSet.contains(port.getARCID() + ":" + port.getATD())) {
        		retList.add(port);
        		doneSet.add(port.getARCID() + ":" + port.getATD());
        	}
        }
        return retList;
    }

    //进近进港表航班
    public List<EnterPort> getEnterPortTable(Date nowTime) throws ParseException {
        if (null == nowTime) {
            nowTime = getNOW();
        }
        String haveArrivedStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<EnterPort> haveArrivedEnterPortList = portDao.getHaveArrivedEnterPortFromJinan(haveArrivedStartTime);
//        logger.debug("过去一小时进近入港数据:{}", gson.toJson(haveArrivedEnterPortList));
        
        List<EnterPort> nowEnterPortList = portDao.getNowEnterPortFromJinan();
        
        String startTime = DATE_FORMAT_2.format(DateUtils.addMinutes(PortService.getNOW(), 1));
        String endTime = DATE_FORMAT_2.format(DateUtils.addHours(PortService.getNOW(), 3));
        List<EnterPort> toArriveEnterPortList = portDao.getEnterJinJinFilghtForJinan(startTime, endTime);
//        logger.debug("未来三小时进近入港航班:{}", gson.toJson(toArriveEnterPortList));
        List<EnterPort> jinjinList = new ArrayList<>();
        for (EnterPort ep : toArriveEnterPortList) {
        	if (ep.getSECTOR().startsWith("AP")) {
        		continue;
        	}
        	List<EnterTimeVo> enterTimeList = portDao.getJinJinTimeForJinan(ep.getIFPLID());
        	if (null == enterTimeList || enterTimeList.size() != 2) {
        		continue;
        	}
        	EnterTimeVo tna = enterTimeList.get(0);
        	EnterTimeVo eto = enterTimeList.get(1);
        	Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
        	Date etoTime = DATE_FORMAT_2.parse(eto.getETO());
        	long intervalMis = tnaTime.getTime() - etoTime.getTime();
        	int intervalMinutue = (int) (intervalMis / 60000) ;
        	ep.setInterval(intervalMinutue);
        	ep.setETA(ep.getETA());
        	ep.setTNA(tnaTime);
        	ep.setETO(etoTime);
        	if (logger.isDebugEnabled()) {
        		logger.debug("\nARCID:{}, FDRID:{} TNA :{}, ETO:{}, IntervalMis:{} ", ep.getARCID(), ep.getIFPLID(), tna.getETO(), eto.getETO(), intervalMinutue);
        	}
        	jinjinList.add(ep);
        }
        
        List<EnterPort> nowjinjinList = new ArrayList<>();
        if (null != nowEnterPortList && nowEnterPortList.size() > 0) {
            for (EnterPort port : nowEnterPortList) {
                List<EnterTimeVo> enterTimeList = portDao.getJinJinTimeForJinan(port.getIFPLID());
            	if (null == enterTimeList || enterTimeList.size() <= 0) {
            		continue;
            	}
            	EnterTimeVo tna = null;
            	EnterTimeVo eto = null;
            	for (EnterTimeVo et : enterTimeList) {
            		if (et.getPTID().equals("TNA")) {
            			tna = et;
            		} else {
            			eto = et;
            		}
            	}
            	Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
    			Date outJinJinTime = nowTime;
    			long intervalMis = tnaTime.getTime() - nowTime.getTime();
    			int intervalMinutue = (int) (intervalMis / 60000) ;
    			port.setETA(nowTime);
    			port.setInterval(intervalMinutue);
    			port.setTNA(tnaTime);
    			port.setETO(outJinJinTime);
    			if (logger.isDebugEnabled()) {
            		logger.debug("\nARCID:{}, FDRID:{} TNA :{}, ETO:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), tna.getETO(), eto.getETO(), intervalMinutue);
            	}
    			nowjinjinList.add(port);
            }
        }
        
        List<EnterPort> allList = new ArrayList<>();
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
                	if (!port.getSECTOR().startsWith("AP") && minute > -3 && minute < 3) {
                		it.remove();
                	} else {
                		port.setMinutes(minute);
                	}
                }
            } else {
                it.remove();
            }
        }
        
        if (null != nowjinjinList && nowjinjinList.size() > 0) {
            for (EnterPort port : nowjinjinList) {
                port.setMinutes(0);
                allList.add(port);
            }
        }
        Set<String> doneSet = new HashSet<>();
        List<EnterPort> retList = new ArrayList<>();
        for (EnterPort port : allList) {
        	if (!doneSet.contains(port.getARCID() + ":" + port.getInterval())) {
        		retList.add(port);
        		doneSet.add(port.getARCID() + ":" + port.getInterval());
        	}
        }
        return retList;
    }
    
    //区调出港航班
    public List<LeavePort> getLeavePortTableQuDiao(Date nowTime) throws ParseException {
        
        String haveFlyingStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<LeavePort> haveFlyingLeavePortList = null;
//        logger.debug("haveFlyingLeavePortList:{}", gson.toJson(haveFlyingLeavePortList));
        
        String startTime = DATE_FORMAT_2.format(PortService.getNOW());
        String endTime = DATE_FORMAT_2.format(DateUtils.addHours(PortService.getNOW(), 3));
        // 北扇区
        List<String> northSectorList = Lists.newArrayList("NE","NW");
        List<String> northPassPointList = Lists.newArrayList("REPOL","WFG", "GULEK");
        List<FlyData> northToLeaveList = portDao.getLeavingDataForQuDiao(AIR_PORT, northPassPointList);
        
        List<FlyData> northQuDiaoList = new ArrayList<>();
        for (FlyData port : northToLeaveList) {
        	if (port.getSECTOR().startsWith("AP") || northSectorList.contains(port.getSECTOR())) {
        		// 已经进入 进近，或者进入当前管制，则忽略
        		continue;
        	}
        	
        	Date pass1, pass2 = null;
        	if (port.getPTID().equals("REPOL") || port.getPTID().equals("WFG")) {
        		pass1 = DateUtils.addMinutes(port.getATD(), 10); 
        	} else if (port.getPTID().equals("GULEK")) {
        		pass1 = port.getATD(); 
        	}
        	pass2 = port.getETO();
			long intervalMis = pass2.getTime() - pass1.getTime();
			int intervalMinutue = (int) (intervalMis / 60000);
			port.setInterval(intervalMinutue);
			if (logger.isDebugEnabled()) {
        		logger.debug("\nARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), pass1, pass2, intervalMinutue);
        	}
			northQuDiaoList.add(port);
        }
        
        List<FlyData> nowCommandFLyDataList = null;
        List<String> nowCommandARCIDList = portDao.getNowCommandARCIDForQuDiao();
        if (!CollectionUtils.isEmpty(nowCommandARCIDList)) {
        	nowCommandFLyDataList = portDao.getFlyDataByARCIDList(nowCommandARCIDList);
        	List<String> ifplidList = Lists.transform(nowCommandFLyDataList, new Function<FlyData, String>(){
        		@Override
        		public String apply(FlyData arg0) {
        			return arg0.getIFPLID();
        		}});
        	
        	List<EnterTimeVo> passPointList = portDao.getByFdrIdList(ifplidList);
        	Map<String, List<EnterTimeVo>> passPointMap = new HashMap<>();
        	if (!CollectionUtils.isEmpty(passPointList)) {
        		for (EnterTimeVo vo : passPointList) {
        			if (passPointMap.containsKey(vo.getFDRID())) {
        				passPointMap.get(vo.getFDRID()).add(vo);
        			} else {
        				passPointMap.put(vo.getFDRID(), Lists.newArrayList(vo));
        			}
        		}
        	}
        	
        	for (FlyData port : nowCommandFLyDataList) {
        		if (port.getADEP().equals(AIR_PORT)) {
        			// 如果是出港飞机
        			for (EnterTimeVo vo : passPointMap.get(port.getIFPLID())) {
        				if (northPassPointList.contains(vo.getPTID())) {
        					port.setPTID(vo.getPTID());
        					port.setETO(vo.getETO());
        					break;
        				}
        			}
        		}
        		Date pass1 = nowTime;
            	Date pass2 = port.getETO();
    			long intervalMis = pass2.getTime() - nowTime.getTime();
    			int intervalMinutue = (int) (intervalMis / 60000) ;
    			port.setInterval(intervalMinutue);
    			if (logger.isDebugEnabled()) {
            		logger.debug("\nARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), pass1, pass2, intervalMinutue);
            	}
        	}
        }
        
        
        for (Iterator<FlyData> it = nowCommandFLyDataList.iterator(); it.hasNext(); ) {
        	FlyData port = it.next();
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
                	if (!port.getSECTOR().startsWith("AP") && minute > -3 && minute < 3) {
                		it.remove();
                	} else {
                		port.setMinutes(minute);
                	}
                }
            } else {
                it.remove();
            }
        }
        if (null != nowQuDiaoList && nowQuDiaoList.size() > 0) {
            for (LeavePort port : nowQuDiaoList) {
                port.setMinutes(0);
                allList.add(port);
            }
        }
        Set<String> doneSet = new HashSet<>();
        List<LeavePort> retList = new ArrayList<>();
        for (LeavePort port : allList) {
        	if (!doneSet.contains(port.getARCID() + ":" + port.getATD())) {
        		retList.add(port);
        		doneSet.add(port.getARCID() + ":" + port.getATD());
        	}
        }
        return retList;
    }

    //进近进港表航班
    public List<EnterPort> getEnterPortTable(Date nowTime) throws ParseException {
        if (null == nowTime) {
            nowTime = getNOW();
        }
        String haveArrivedStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<EnterPort> haveArrivedEnterPortList = portDao.getHaveArrivedEnterPortFromJinan(haveArrivedStartTime);
//        logger.debug("过去一小时进近入港数据:{}", gson.toJson(haveArrivedEnterPortList));
        
        List<EnterPort> nowEnterPortList = portDao.getNowEnterPortFromJinan();
        
        String startTime = DATE_FORMAT_2.format(DateUtils.addMinutes(PortService.getNOW(), 1));
        String endTime = DATE_FORMAT_2.format(DateUtils.addHours(PortService.getNOW(), 3));
        List<EnterPort> toArriveEnterPortList = portDao.getEnterJinJinFilghtForJinan(startTime, endTime);
//        logger.debug("未来三小时进近入港航班:{}", gson.toJson(toArriveEnterPortList));
        List<EnterPort> jinjinList = new ArrayList<>();
        for (EnterPort ep : toArriveEnterPortList) {
        	if (ep.getSECTOR().startsWith("AP")) {
        		continue;
        	}
        	List<EnterTimeVo> enterTimeList = portDao.getJinJinTimeForJinan(ep.getIFPLID());
        	if (null == enterTimeList || enterTimeList.size() != 2) {
        		continue;
        	}
        	EnterTimeVo tna = enterTimeList.get(0);
        	EnterTimeVo eto = enterTimeList.get(1);
        	Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
        	Date etoTime = DATE_FORMAT_2.parse(eto.getETO());
        	long intervalMis = tnaTime.getTime() - etoTime.getTime();
        	int intervalMinutue = (int) (intervalMis / 60000) ;
        	ep.setInterval(intervalMinutue);
        	ep.setETA(ep.getETA());
        	ep.setTNA(tnaTime);
        	ep.setETO(etoTime);
        	if (logger.isDebugEnabled()) {
        		logger.debug("\nARCID:{}, FDRID:{} TNA :{}, ETO:{}, IntervalMis:{} ", ep.getARCID(), ep.getIFPLID(), tna.getETO(), eto.getETO(), intervalMinutue);
        	}
        	jinjinList.add(ep);
        }
        
        List<EnterPort> nowjinjinList = new ArrayList<>();
        if (null != nowEnterPortList && nowEnterPortList.size() > 0) {
            for (EnterPort port : nowEnterPortList) {
                List<EnterTimeVo> enterTimeList = portDao.getJinJinTimeForJinan(port.getIFPLID());
            	if (null == enterTimeList || enterTimeList.size() <= 0) {
            		continue;
            	}
            	EnterTimeVo tna = null;
            	EnterTimeVo eto = null;
            	for (EnterTimeVo et : enterTimeList) {
            		if (et.getPTID().equals("TNA")) {
            			tna = et;
            		} else {
            			eto = et;
            		}
            	}
            	Date tnaTime = DATE_FORMAT_2.parse(tna.getETO()); 
    			Date outJinJinTime = nowTime;
    			long intervalMis = tnaTime.getTime() - nowTime.getTime();
    			int intervalMinutue = (int) (intervalMis / 60000) ;
    			port.setETA(nowTime);
    			port.setInterval(intervalMinutue);
    			port.setTNA(tnaTime);
    			port.setETO(outJinJinTime);
    			if (logger.isDebugEnabled()) {
            		logger.debug("\nARCID:{}, FDRID:{} TNA :{}, ETO:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), tna.getETO(), eto.getETO(), intervalMinutue);
            	}
    			nowjinjinList.add(port);
            }
        }
        
        List<EnterPort> allList = new ArrayList<>();
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
                	if (!port.getSECTOR().startsWith("AP") && minute > -3 && minute < 3) {
                		it.remove();
                	} else {
                		port.setMinutes(minute);
                	}
                }
            } else {
                it.remove();
            }
        }
        
        if (null != nowjinjinList && nowjinjinList.size() > 0) {
            for (EnterPort port : nowjinjinList) {
                port.setMinutes(0);
                allList.add(port);
            }
        }
        Set<String> doneSet = new HashSet<>();
        List<EnterPort> retList = new ArrayList<>();
        for (EnterPort port : allList) {
        	if (!doneSet.contains(port.getARCID() + ":" + port.getInterval())) {
        		retList.add(port);
        		doneSet.add(port.getARCID() + ":" + port.getInterval());
        	}
        }
        return retList;
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
                	if (!port.getSECTOR().startsWith("AP") && minute > -3 && minute < 3) {
                		it.remove();
                	} else {
                		port.setMinutes(minute);
                	}
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
