package com.example.sql;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

public class SqlProvider {

	public String selectFlyDataForQuDiao(Map<String, Object> params) {
		List<String> leavePortList = (List<String>) params.get("leavePortList");
		List<String> enterPortList = (List<String>) params.get("enterPortList");
		List<String> pointList = (List<String>) params.get("pointList");;
		String leavePorts = null;
		if (!CollectionUtils.isEmpty(leavePortList)) {
			StringBuilder tmpStr = new StringBuilder();
			for (String port : leavePortList) {
				tmpStr.append(",\"").append(port).append("\"");
			}
			leavePorts = tmpStr.substring(1);
		}
		String enterPorts = null;
		if (!CollectionUtils.isEmpty(enterPortList)) {
			StringBuilder tmpStr = new StringBuilder();
			for (String port : enterPortList) {
				tmpStr.append(",\"").append(port).append("\"");
			}
			enterPorts = tmpStr.substring(1);
		}
		String ptids = null;
		if (!CollectionUtils.isEmpty(pointList)) {
			StringBuilder tmpStr = new StringBuilder();
			for (String port : pointList) {
				tmpStr.append(",\"").append(port).append("\"");
			}
			ptids = tmpStr.substring(1);
		}
	    StringBuilder sql = new StringBuilder("select ") 
	    		.append(" f.IFPLID,ff.PTID,ff.ETO,f.ARCID,f.ADEP,f.ADES,f.RTE,f.EOBT,f.ETA,f.ATD,f.ATA,f.STATUS,f.COUPLE,f.COUPLED,f.SECTOR")
	    		.append(" from fdrfix ff ")
	    		.append(" left join fdr f ")
	    		.append(" on ff.`FDRID`=f.`IFPLID` ")
	    		.append(" where ");
	    	if (StringUtils.isNotBlank(leavePorts) && StringUtils.isNotBlank(enterPorts)) {
	    		sql.append(" (f.`ADEP` in (").append(leavePorts).append(") or f.`ADES` in (").append(enterPorts).append(")) ");
	    	} else if (StringUtils.isNotBlank(leavePorts) && StringUtils.isBlank(enterPorts)) {
	    		sql.append(" f.`ADEP` in (").append(leavePorts).append(") ");
	    	} else if (StringUtils.isBlank(leavePorts) && StringUtils.isNotBlank(enterPorts)) {
	    		sql.append(" f.`ADES` in (").append(enterPorts).append(") ");
	    	}
	    		
	    	sql.append(" and f.LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) ")
	    		.append(" and ff.`ETO` >= DATE_FORMAT(DATE_SUB(now(), INTERVAL 477 MINUTE), '%Y%m%d%H%i%S') ")
	    		.append(" and ff.`ETO` <= DATE_FORMAT(DATE_SUB(now(), INTERVAL 302 MINUTE), '%Y%m%d%H%i%S') ")
	    		.append(" and ff.`PTID` in (").append(ptids).append(") ");
	    	return sql.toString();
	}
}
