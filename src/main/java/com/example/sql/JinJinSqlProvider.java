package com.example.sql;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

public class JinJinSqlProvider {

	
	public String selectLeaveFlyDataForJinJin(Map<String, Object> params) {
		List<String> pointList = (List<String>) params.get("pointList");;
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
	    		.append(" where f.`ADEP`='ZSJN'");
	    		
	    sql.append(" and ff.`ETO` >= DATE_FORMAT(DATE_SUB(now(), INTERVAL 477 MINUTE), '%Y%m%d%H%i%S') ")
	    		.append(" and ff.`ETO` <= DATE_FORMAT(DATE_SUB(now(), INTERVAL 302 MINUTE), '%Y%m%d%H%i%S') ")
	    		.append(" and ff.`PTID` in (").append(ptids).append(") ");
	    	return sql.toString();
	}
	
	public String selectEnterFlyDataForJinJin(Map<String, Object> params) {
		List<String> pointList = (List<String>) params.get("pointList");;
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
	    		.append(" where f.`ADES`='ZSJN'");
	    		
	    sql.append(" and ff.`ETO` >= DATE_FORMAT(DATE_SUB(now(), INTERVAL 477 MINUTE), '%Y%m%d%H%i%S') ")
	    		.append(" and ff.`ETO` <= DATE_FORMAT(DATE_SUB(now(), INTERVAL 302 MINUTE), '%Y%m%d%H%i%S') ")
	    		.append(" and ff.`PTID` in (").append(ptids).append(") ");
	    	return sql.toString();
	}
	
	public String selectPassETOByFdrIdPTIDList(Map<String, Object> params) {
		String ifpld = (String) params.get("ifpld");
		List<String> pointList = (List<String>) params.get("pointList");;
		String ptids = null;
		if (!CollectionUtils.isEmpty(pointList)) {
			StringBuilder tmpStr = new StringBuilder();
			for (String port : pointList) {
				tmpStr.append(",\"").append(port).append("\"");
			}
			ptids = tmpStr.substring(1);
		}
	    StringBuilder sql = new StringBuilder("select FDRID,PTID,ETO from fdrfix where FDRID = '").append(ifpld).append("' AND PTID in (").append(ptids).append(") ");
	    return sql.toString();
	}
	
	public String selectNowEnterPortForJinJin(Map<String, Object> params) {
		List<String> pointList = (List<String>) params.get("pointList");;
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
	    		.append(" where f.`ADES`='ZSJN'");
	    		
	    sql.append(" AND f.COUPLED='Y' and f.LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and (f.SECTOR='AP01' or f.SECTOR='AP02') ")
	    		.append(" and ff.`PTID` in (").append(ptids).append(") ");
	    	return sql.toString();
	}
	
	public String selectNowLeavePortForJinJin(Map<String, Object> params) {
		List<String> pointList = (List<String>) params.get("pointList");;
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
	    		.append(" where f.`ADEP`='ZSJN'");
	    		
	    sql.append(" AND f.COUPLED='Y' and f.LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and (f.SECTOR='AP01' or f.SECTOR='AP02') ")
	    		.append(" and ff.`PTID` in (").append(ptids).append(") ");
	    	return sql.toString();
	}
}
