package com.example.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.dao.QuDiaoDao;
import com.example.domain.DTO.EnterTimeVo;
import com.example.domain.DTO.FlyData;
import com.example.domain.DTO.LeavePort;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

@Service
public class QuDiaoService {
	/*
	 ETA	预计降落时间
	 ATA	实际降落时间
	 
	 TNA	遥墙机场代号
	 ETO	预计到达某点时间
	 
	 EOBT	预计离开时间
	 ATD	实际离开时间
	 */
	
	static Logger logger = LoggerFactory.getLogger(QuDiaoService.class);
    
    static Gson gson = new Gson();
    
    public static final String JN_AIR_PORT = "ZSJN";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("yyyyMMddHHmmss");
    
    @Autowired
    private QuDiaoDao quDiaoDao;
    
    public static Date getNOW(){
    	Date now = new Date();
		Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.HOUR_OF_DAY, -8);
        return c.getTime();
    }
    
    //区调 北扇 航班
    public List<FlyData> getNorthFlyDataForQuDiao() throws ParseException {
    	Date nowTime = getNOW();
        String haveFlyingStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<LeavePort> haveFlyingLeavePortList = null;
        
        // 北扇区 出港
        List<String> northPassPointList = Lists.newArrayList("REPOL","WFG", "GULEK", "BASOV", "P291", "PANKI", "WXI");
        List<String> leavePortList = Lists.newArrayList(JN_AIR_PORT, "ZSQD", "ZBSJ", "ZBTJ", "ZBAA");
        List<String> enterPortList = Lists.newArrayList(JN_AIR_PORT, "ZSQD", "ZHCC", "ZHHH", "ZBSJ", "ZBTJ", "ZBNY");
        List<FlyData> northFlyDataList = quDiaoDao.getFlyDataForQuDiao(leavePortList, enterPortList, northPassPointList);
        
        List<FlyData> northQuDiaoList = new ArrayList<>();
        for (FlyData port : northFlyDataList) {
        	if (!port.getADEP().equals(JN_AIR_PORT) || port.getSECTOR().startsWith("AP")) {
        		// 已经进入 进近，或者进入当前管制，则忽略
        		continue;
        	}
        	
        	Date pass1 = null, pass2 = null;
        	if (port.getPTID().equals("REPOL") || port.getPTID().equals("WFG")) {
        		pass1 = DateUtils.addMinutes(port.getATD() == null ? port.getEOBT() : port.getATD(), 10); 
        	} else if (port.getPTID().equals("GULEK")) {
        		pass1 = port.getATD() == null ? port.getEOBT() : port.getATD(); 
        	}
        	if (null == pass1) {
        		continue;
        	}
        	pass2 = port.getETO();
        	port.setPass1(pass1);
        	port.setPass2(pass2);
			long intervalMis = pass2.getTime() - pass1.getTime();
			int intervalMinutue = (int) (intervalMis / 60000);
			port.setInterval(intervalMinutue);
			port.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 6000);
			if (logger.isDebugEnabled()) {
        		logger.debug("\n---------1----------ARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), pass1, pass2, intervalMinutue);
        	}
			northQuDiaoList.add(port);
        }
        
        // 北扇区 进港
        Map<String, FlyData> northQuDiaoMap = new HashMap<>();
        for (FlyData port : northFlyDataList) {
        	if (!port.getADES().equals(JN_AIR_PORT) || port.getSECTOR().startsWith("AP")) {
        		// 已经进入 进近，或者进入当前管制，则忽略
        		continue;
        	}
        	
        	Date pass1 = null, pass2 = null;
        	if (port.getPTID().equals("REPOL") || port.getPTID().equals("WFG")) {
        		pass1 = port.getETO(); 
        	} else if (port.getPTID().equals("WXI")) {
        		pass1 = port.getETO();
        		pass2 = DateUtils.addMinutes(port.getETO(), 10);
        	} else if (port.getPTID().equals("PANKI")) {
        		pass1 = DateUtils.addMinutes(port.getETO(), -6); 
        		pass2 = port.getETO();
        	} else if (port.getPTID().equals("BASOV") || port.getPTID().equals("P291")) {
        		pass2 = port.getETO();
        	} 
        	if (northQuDiaoMap.containsKey(port.getIFPLID())) {
        		if (null == northQuDiaoMap.get(port.getIFPLID()).getPass1() && null != pass1) {
        			northQuDiaoMap.get(port.getIFPLID()).setPass1(pass1);
        		}
        		if (null == northQuDiaoMap.get(port.getIFPLID()).getPass2() && null != pass2) {
        			northQuDiaoMap.get(port.getIFPLID()).setPass2(pass2);
        		}
        	} else {
        		port.setPass1(pass1);
        		port.setPass2(pass2);
        		northQuDiaoMap.put(port.getIFPLID(), port);
        	}
        	if (null != northQuDiaoMap.get(port.getIFPLID()).getPass1() && null != northQuDiaoMap.get(port.getIFPLID()).getPass2()) {
        		long intervalMis = northQuDiaoMap.get(port.getIFPLID()).getPass2().getTime() - northQuDiaoMap.get(port.getIFPLID()).getPass1().getTime();
        		int intervalMinutue = (int) (intervalMis / 60000);
        		northQuDiaoMap.get(port.getIFPLID()).setInterval(intervalMinutue);
        		northQuDiaoMap.get(port.getIFPLID()).setMinutes((int)(northQuDiaoMap.get(port.getIFPLID()).getPass1().getTime() - nowTime.getTime()) / 6000);
        		if (logger.isDebugEnabled()) {
        			logger.debug("\n---------2-----ADES:ZSJN-----ARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), port.getPass1(), port.getPass2(), intervalMinutue);
        		}
        	}
        }
        
        // 北扇区 飞越
        Map<String, FlyData> northFlyOverQuDiaoMap = new HashMap<>();
        for (FlyData port : northFlyDataList) {
        	if (port.getADES().equals(JN_AIR_PORT) || port.getADEP().equals(JN_AIR_PORT)) {
        		// 飞越的起飞与降落的机场都不是济南
        		continue;
        	}
        	
        	Date pass1 = null, pass2 = null;
        	if (port.getADEP().equals("ZSQD")) {
        		if (port.getPTID().equals("WFG")) {
        			pass1 = port.getETO();
        		}
        		if (port.getPTID().equals("GULEK")) {
        			pass2 = port.getETO();
        		}
        	} else if (port.getADES().equals("ZSQD")) {
        		if (port.getPTID().equals("YQG")) {
        			pass1 = port.getETO();
        		}
        		if (port.getPTID().equals("WFG")) {
        			pass2 = port.getETO();
        		}
        	} else if (port.getADES().equals("ZHCC") || port.getADES().equals("ZHHH")) {
        		if (port.getPTID().equals("REPOL")) {
        			pass1 = port.getETO();
        		}
        		if (port.getPTID().equals("GULEK")) {
        			pass2 = port.getETO();
        		}
        	} else if (port.getADEP().equals("ZBSJ") || port.getADEP().equals("ZBTJ") || port.getADEP().equals("ZBAA")) {
        		if (port.getPTID().equals("PANKI")) {
        			pass1 = port.getETO();
        			pass2 = DateUtils.addMinutes(port.getETO(), 8);
        		}
        	} else if (port.getADES().equals("ZBSJ") || port.getADES().equals("ZBTJ") || port.getADES().equals("ZBNY")) {
        		if (port.getPTID().equals("UDINO")) {
        			pass1 = DateUtils.addMinutes(port.getETO(), 13);
        		}
        		if (port.getPTID().equals("P181")) {
        			pass1 = DateUtils.addMinutes(port.getETO(), 12);
        		}
        		if (port.getPTID().equals("TUMLO")) {
        			pass2 = port.getETO();
        		}
        	}
        	
        	if (pass1 == null && pass2 == null) {
        		// 这里说明查询到的过点并不是要显示过滤的 过点
        		if (logger.isDebugEnabled()) {
        			logger.debug("Ignore FDRFIX {}", gson.toJson(port));
        		}
        		continue;
        	}
    		if (northFlyOverQuDiaoMap.containsKey(port.getIFPLID())) {
        		if (null == northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1() && null != pass1) {
        			northFlyOverQuDiaoMap.get(port.getIFPLID()).setPass1(pass1);
        		}
        		if (null == northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2() && null != pass2) {
        			northFlyOverQuDiaoMap.get(port.getIFPLID()).setPass2(pass2);
        		}
        	} else {
        		port.setPass1(pass1);
        		port.setPass2(pass2);
        		northFlyOverQuDiaoMap.put(port.getIFPLID(), port);
        	}
        	
        	if (null != northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1() && null != northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2()) {
        		long intervalMis = northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2().getTime() - northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1().getTime();
        		int intervalMinutue = (int) (intervalMis / 60000);
        		northFlyOverQuDiaoMap.get(port.getIFPLID()).setInterval(intervalMinutue);
        		northFlyOverQuDiaoMap.get(port.getIFPLID()).setMinutes((int)(northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1().getTime() - nowTime.getTime()) / 6000);
        		if (logger.isDebugEnabled()) {
        			logger.debug("\n---------3----------ARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", northFlyOverQuDiaoMap.get(port.getIFPLID()).getARCID(), northFlyOverQuDiaoMap.get(port.getIFPLID()).getIFPLID(), northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1(), northFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2(), intervalMinutue);
        		}
        	}
        }
        
        
        // 下面是当前正在指挥的飞机
        List<FlyData> nowCommandFLyDataList = null;
        List<String> nowCommandARCIDList = quDiaoDao.getNowCommandARCIDForQuDiao();
        if (!CollectionUtils.isEmpty(nowCommandARCIDList)) {
        	nowCommandFLyDataList = quDiaoDao.getFlyDataByARCIDList(nowCommandARCIDList);
        	List<String> ifplidList = Lists.transform(nowCommandFLyDataList, new Function<FlyData, String>(){
        		@Override
        		public String apply(FlyData arg0) {
        			return arg0.getIFPLID();
        		}});
        	if (!CollectionUtils.isEmpty(ifplidList)) {
        		List<EnterTimeVo> passPointList = quDiaoDao.getByFdrIdList(ifplidList);
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
        			Date pass1 = nowTime;
        			Date pass2 = null;
        			if (port.getADEP().equals(JN_AIR_PORT)) {
        				// 如果是出港飞机
        				for (EnterTimeVo vo : passPointMap.get(port.getIFPLID())) {
        					if (vo.getPTID().equals("REPOL") || vo.getPTID().equals("WFG") || vo.getPTID().equals("GULEK")) {
        						port.setPTID(vo.getPTID());
        						port.setETO(vo.getETO());
        						pass2 = DATE_FORMAT_2.parse(vo.getETO());
        						break;
        					}
        				}
        			} else if (port.getADES().equals(JN_AIR_PORT)) {
        				// 如果是进港飞机
        				for (EnterTimeVo vo : passPointMap.get(port.getIFPLID())) {
        					if (vo.getPTID().equals("BASOV") || vo.getPTID().equals("P291") || vo.getPTID().equals("PANKI")) {
        						port.setPTID(vo.getPTID());
        						pass2 = DATE_FORMAT_2.parse(vo.getETO());
        						port.setETO(pass2);
        						break;
        		        	} else if (vo.getPTID().equals("WXI")) {
        		        		port.setPTID(vo.getPTID());
        		        		pass2 = DateUtils.addMinutes(DATE_FORMAT_2.parse(vo.getETO()), 10);
        		        		port.setETO(pass2);
        		        		break;
        		        	} 
        				}
        			} else {
        				if (port.getADEP().equals("ZSQD")) {
        					pass2 = set2ndPassTime(passPointMap, port, "GULEK", 0);
        	        	} else if (port.getADES().equals("ZSQD")) {
        	        		pass2 = set2ndPassTime(passPointMap, port, "WFG", 0);
        	        	} else if (port.getADES().equals("ZHCC") || port.getADES().equals("ZHHH")) {
        	        		pass2 = set2ndPassTime(passPointMap, port, "GULEK", 0);
        	        	} else if (port.getADEP().equals("ZBSJ") || port.getADEP().equals("ZBTJ") || port.getADEP().equals("ZBAA")) {
        	        		pass2 = set2ndPassTime(passPointMap, port, "PANKI", 8);
        	        	} else if (port.getADES().equals("ZBSJ") || port.getADES().equals("ZBTJ") || port.getADES().equals("ZBNY")) {
        	        		pass2 = set2ndPassTime(passPointMap, port, "TUMLO", 8);
        	        	}
        			}
        			if (pass2 == null) {
        				// 如果过第二点时间为空，说明数据有问题，不予展示
        				continue;
        			}
        			port.setPass1(pass1);
        			port.setPass2(pass2);
        			long intervalMis = pass2.getTime() - nowTime.getTime();
        			int intervalMinutue = (int) (intervalMis / 60000) ;
        			port.setInterval(intervalMinutue);
        			port.setMinutes(0);
        			if (logger.isDebugEnabled()) {
        				logger.debug("\nARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), pass1, pass2, intervalMinutue);
        			}
        		}
        	}
        }
        
        List<FlyData> allList = Lists.newArrayList(northQuDiaoList);
        allList.addAll(northQuDiaoMap.values());
        allList.addAll(northFlyOverQuDiaoMap.values());
        if (!CollectionUtils.isEmpty(nowCommandFLyDataList)) {
        	allList.addAll(nowCommandFLyDataList);
        }
        List<FlyData> filterList = Lists.newArrayList();
        for (FlyData fd : allList) {
        	if (fd.getPass1() != null && fd.getPass2() != null) {
        		filterList.add(fd);
        	}
        }
        return filterList;
    }

	private Date set2ndPassTime(Map<String, List<EnterTimeVo>> passPointMap, FlyData port, String _2ndPassPoint, int addMinute)
			throws ParseException {
		Date pass2 = null;
		for (EnterTimeVo vo : passPointMap.get(port.getIFPLID())) {
			if (vo.getPTID().equals(_2ndPassPoint)) {
				port.setPTID(vo.getPTID());
				if (addMinute == 0) {
					pass2 = DATE_FORMAT_2.parse(vo.getETO());
				} else {
					pass2 = DateUtils.addMinutes(DATE_FORMAT_2.parse(vo.getETO()), addMinute);
				}
				port.setETO(pass2);
				break;
			}
		}
		return pass2;
	}

	public List<FlyData> getSouthFlyDataForQuDiao() {
		Date nowTime = getNOW();
        String haveFlyingStartTime = DATE_FORMAT.format(DateUtils.addHours(nowTime, -1));
        List<LeavePort> haveFlyingLeavePortList = null;
        
        //飞越空中标志点
        List<String> southPassPointList = Lists.newArrayList("UDINO","P240", "P181", "TUMLO", "P223", "DALIM", "P60");		
        
        //出港机场
        List<String> leavePortList = Lists.newArrayList(JN_AIR_PORT, "ZSJG", "ZSLY");
        
        //进港机场
        List<String> enterPortList = Lists.newArrayList(JN_AIR_PORT, "ZBTJ", "ZBNY", "ZBSJ", "ZSJG", "ZSLY");
        List<FlyData> southFlyDataList = quDiaoDao.getFlyDataForQuDiao(leavePortList, enterPortList, southPassPointList);
        
        List<FlyData> southQuDiaoList = new ArrayList<>();
        // 南扇区 出港
        for (FlyData port : southFlyDataList) {
        	if (!port.getADEP().equals(JN_AIR_PORT) || port.getSECTOR().startsWith("AP")) {
        		// 已经进入 进近，或者进入当前管制，则忽略
        		continue;
        	}
        	
        	Date pass1 = null, pass2 = null;
        	if (port.getPTID().equals("UDINO") || port.getPTID().equals("P240")) {
        		pass1 = DateUtils.addMinutes(port.getATD() == null ? port.getEOBT() : port.getATD(), 10);
        		pass2 = port.getETO();
        	}
        	if (null == pass1) {
        		continue;
        	}
        	port.setPass1(pass1);
        	port.setPass2(pass2);
			long intervalMis = pass2.getTime() - pass1.getTime();
			int intervalMinutue = (int) (intervalMis / 60000);
			port.setInterval(intervalMinutue);
			port.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 6000);
			if (logger.isDebugEnabled()) {
        		logger.debug("\n---------South 1----------{} ", port);
        	}
			southQuDiaoList.add(port);
        }
        
        // 南扇区 进港
        for (FlyData port : southFlyDataList) {
        	if (!port.getADES().equals(JN_AIR_PORT) || port.getSECTOR().startsWith("AP")) {
        		// 已经进入 进近，或者进入当前管制，则忽略
        		continue;
        	}
        	
        	Date pass1 = null, pass2 = null;
        	if (port.getPTID().equals("P240") || port.getPTID().equals("P181")) {
        		pass1 = port.getETO();
        		pass2 = DateUtils.addMinutes(port.getATD() == null ? port.getEOBT() : port.getATD(), 15);
        	} else if (port.getPTID().equals("UDINO")) {
        		pass1 = port.getETO();
        		pass2 = DateUtils.addMinutes(port.getATD() == null ? port.getEOBT() : port.getATD(), 16);
        	}
        	if (null == pass1) {
        		continue;
        	}
        	port.setPass1(pass1);
        	port.setPass2(pass2);
			long intervalMis = pass2.getTime() - pass1.getTime();
			int intervalMinutue = (int) (intervalMis / 60000);
			port.setInterval(intervalMinutue);
			port.setMinutes((int)(pass1.getTime() - nowTime.getTime()) / 6000);
			if (logger.isDebugEnabled()) {
        		logger.debug("\n---------South 2----------{} ", port);
        	}
			southQuDiaoList.add(port);
        }
        
        // 南扇区 飞越
        Map<String, FlyData> southFlyOverQuDiaoMap = new HashMap<>();
        for (FlyData port : southFlyDataList) {
        	if (port.getADES().equals(JN_AIR_PORT) || port.getADEP().equals(JN_AIR_PORT)) {
        		// 飞越的航班肯定不是济南啦 
        		continue;
        	}
        	
        	Date pass1 = null, pass2 = null;
        	if (port.getADES().equals("ZBTJ") || port.getADES().equals("ZBNY") || port.getADES().equals("ZBSJ")) {
        		if (port.getPTID().equals("UDINO")) {
        			pass1 = port.getETO();
        			pass2 = DateUtils.addMinutes(port.getETO(), 15);
        		}
        	} else if (port.getADES().equals("TUMLO")) {
        		if (port.getPTID().equals("UDINO")) {
        			pass1 = port.getETO();
        			pass2 = DateUtils.addMinutes(port.getETO(), 15);
        		} else if (port.getPTID().equals("P181")) {
        			pass1 = port.getETO();
        			pass2 = DateUtils.addMinutes(port.getETO(), 14);
        		}
        	} else if (port.getADES().equals("ZBTJ") || port.getADES().equals("ZBNY") || port.getADES().equals("ZBSJ")) {
        		if (port.getPTID().equals("P181")) {
        			pass1 = port.getETO();
        			pass2 = DateUtils.addMinutes(port.getETO(), 14);
        		}
        	} else if (port.getADES().equals("ZSJG")) {
        		if (port.getPTID().equals("P223")) {
        			pass1 = DateUtils.addMinutes(port.getETO(), -10);
        			pass2 = port.getETO();
        		}
        	} else if (port.getADEP().equals("ZSJG")) {
        		if (port.getPTID().equals("P223")) {
	        		pass1 = port.getETO();
	        		pass2 = DateUtils.addMinutes(port.getETO(), 10);
        		}
        	} else if (port.getADES().equals("ZSLY")) {
        		if (port.getPTID().equals("DALIM")) {
        			pass1 = port.getETO();
        		} else if (port.getPTID().equals("P60")) {
        			pass2 = port.getETO();
        		}
        	} else if (port.getADEP().equals("ZSLY")) {
        		if (port.getPTID().equals("P60")) {
        			pass1 = port.getETO();
        		} else if (port.getPTID().equals("DALIM")) {
        			pass2 = port.getETO();
        		}
        	}
        	
        	if (pass1 == null && pass2 == null) {
        		// 这里说明查询到的过点并不是要显示过滤的 过点
        		if (logger.isDebugEnabled()) {
        			logger.debug("Ignore FDRFIX {}", gson.toJson(port));
        		}
        		continue;
        	}
    		if (southFlyOverQuDiaoMap.containsKey(port.getIFPLID())) {
        		if (null == southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1() && null != pass1) {
        			southFlyOverQuDiaoMap.get(port.getIFPLID()).setPass1(pass1);
        		}
        		if (null == southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2() && null != pass2) {
        			southFlyOverQuDiaoMap.get(port.getIFPLID()).setPass2(pass2);
        		}
        	} else {
        		port.setPass1(pass1);
        		port.setPass2(pass2);
        		southFlyOverQuDiaoMap.put(port.getIFPLID(), port);
        	}
        	
        	if (null != southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1() && null != southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2()) {
        		long intervalMis = southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass2().getTime() - southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1().getTime();
        		int intervalMinutue = (int) (intervalMis / 60000);
        		southFlyOverQuDiaoMap.get(port.getIFPLID()).setInterval(intervalMinutue);
        		southFlyOverQuDiaoMap.get(port.getIFPLID()).setMinutes((int)(southFlyOverQuDiaoMap.get(port.getIFPLID()).getPass1().getTime() - nowTime.getTime()) / 6000);
        		if (logger.isDebugEnabled()) {
        			logger.debug("\n---------South 3----------{} ", southFlyOverQuDiaoMap.get(port.getIFPLID()));
        		}
        	}
        }
        
        
        // 下面是当前正在指挥的飞机
        List<FlyData> nowCommandFLyDataList = null;
        List<String> nowCommandARCIDList = quDiaoDao.getNowCommandARCIDForQuDiao();
//        if (!CollectionUtils.isEmpty(nowCommandARCIDList)) {
//        	nowCommandFLyDataList = quDiaoDao.getFlyDataByARCIDList(nowCommandARCIDList);
//        	List<String> ifplidList = Lists.transform(nowCommandFLyDataList, new Function<FlyData, String>(){
//        		@Override
//        		public String apply(FlyData arg0) {
//        			return arg0.getIFPLID();
//        		}});
//        	if (!CollectionUtils.isEmpty(ifplidList)) {
//        		List<EnterTimeVo> passPointList = quDiaoDao.getByFdrIdList(ifplidList);
//        		Map<String, List<EnterTimeVo>> passPointMap = new HashMap<>();
//        		if (!CollectionUtils.isEmpty(passPointList)) {
//        			for (EnterTimeVo vo : passPointList) {
//        				if (passPointMap.containsKey(vo.getFDRID())) {
//        					passPointMap.get(vo.getFDRID()).add(vo);
//        				} else {
//        					passPointMap.put(vo.getFDRID(), Lists.newArrayList(vo));
//        				}
//        			}
//        		}
//        		
//        		for (FlyData port : nowCommandFLyDataList) {
//        			Date pass1 = nowTime;
//        			Date pass2 = null;
//        			if (port.getADEP().equals(JN_AIR_PORT)) {
//        				// 如果是出港飞机
//        				for (EnterTimeVo vo : passPointMap.get(port.getIFPLID())) {
//        					if (vo.getPTID().equals("REPOL") || vo.getPTID().equals("WFG") || vo.getPTID().equals("GULEK")) {
//        						port.setPTID(vo.getPTID());
//        						port.setETO(vo.getETO());
//        						pass2 = DATE_FORMAT_2.parse(vo.getETO());
//        						break;
//        					}
//        				}
//        			} else if (port.getADES().equals(JN_AIR_PORT)) {
//        				// 如果是进港飞机
//        				for (EnterTimeVo vo : passPointMap.get(port.getIFPLID())) {
//        					if (vo.getPTID().equals("BASOV") || vo.getPTID().equals("P291") || vo.getPTID().equals("PANKI")) {
//        						port.setPTID(vo.getPTID());
//        						pass2 = DATE_FORMAT_2.parse(vo.getETO());
//        						port.setETO(pass2);
//        						break;
//        		        	} else if (vo.getPTID().equals("WXI")) {
//        		        		port.setPTID(vo.getPTID());
//        		        		pass2 = DateUtils.addMinutes(DATE_FORMAT_2.parse(vo.getETO()), 10);
//        		        		port.setETO(pass2);
//        		        		break;
//        		        	} 
//        				}
//        			} else {
//        				if (port.getADEP().equals("ZSQD")) {
//        					pass2 = set2ndPassTime(passPointMap, port, "GULEK", 0);
//        	        	} else if (port.getADES().equals("ZSQD")) {
//        	        		pass2 = set2ndPassTime(passPointMap, port, "WFG", 0);
//        	        	} else if (port.getADES().equals("ZHCC") || port.getADES().equals("ZHHH")) {
//        	        		pass2 = set2ndPassTime(passPointMap, port, "GULEK", 0);
//        	        	} else if (port.getADEP().equals("ZBSJ") || port.getADEP().equals("ZBTJ") || port.getADEP().equals("ZBAA")) {
//        	        		pass2 = set2ndPassTime(passPointMap, port, "PANKI", 8);
//        	        	} else if (port.getADES().equals("ZBSJ") || port.getADES().equals("ZBTJ") || port.getADES().equals("ZBNY")) {
//        	        		pass2 = set2ndPassTime(passPointMap, port, "TUMLO", 8);
//        	        	}
//        			}
//        			if (pass2 == null) {
//        				// 如果过第二点时间为空，说明数据有问题，不予展示
//        				continue;
//        			}
//        			port.setPass1(pass1);
//        			port.setPass2(pass2);
//        			long intervalMis = pass2.getTime() - nowTime.getTime();
//        			int intervalMinutue = (int) (intervalMis / 60000) ;
//        			port.setInterval(intervalMinutue);
//        			port.setMinutes(0);
//        			if (logger.isDebugEnabled()) {
//        				logger.debug("\nARCID:{}, FDRID:{} PASS_1 :{}, PASS_2:{}, IntervalMis:{} ", port.getARCID(), port.getIFPLID(), pass1, pass2, intervalMinutue);
//        			}
//        		}
//        	}
//        }
        
        List<FlyData> allList = Lists.newArrayList(southQuDiaoList);
        allList.addAll(southFlyOverQuDiaoMap.values());
        if (!CollectionUtils.isEmpty(nowCommandFLyDataList)) {
        	allList.addAll(nowCommandFLyDataList);
        }
        List<FlyData> filterList = Lists.newArrayList();
        for (FlyData fd : allList) {
        	if (fd.getPass1() != null && fd.getPass2() != null) {
        		filterList.add(fd);
        	}
        }
        return filterList;
	}
}
