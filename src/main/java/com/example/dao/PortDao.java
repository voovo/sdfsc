package com.example.dao;

import com.example.domain.DTO.*;
import com.example.sql.JinJinSqlProvider;
import com.example.sql.QuDiaoSqlProvider;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by SunYi on 2016/2/16/0016.
 */
@Repository
public interface PortDao {
    
    @Select("SELECT FLIGHTID,RCVTIME FROM trackhis WHERE HIGH<=8000 AND LONGI >='115.24' AND LONGI<'119.32' AND LAT >='34.31' AND LAT < '38.06' AND RCVTIME >= #{startTime, jdbcType=TIMESTAMP} AND FLIGHTID IS NOT NULL AND FLIGHTID != ''")
    List<Trackhis> getNowAndHistoryFlightId(@Param("startTime") Date startTime);
    
    @Select("select ARCID,WKTRC,ADEP,ADES,ATD JNTime,ARCREG,SsrCode,STATUS from fdr where ADEP='ZSJN' and ATD is not null and ATD >=#{startTime, jdbcType=VARCHAR} and ATD <#{endTime, jdbcType=VARCHAR} and SECTOR like 'AP%'")
    List<AreaPort> getHaveFlyingPortFromJinan(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("select ARCID,WKTRC,ADEP,ADES,EOBT JNTime,ARCREG,SsrCode,STATUS from fdr where ADEP='ZSJN' and EOBT is not null and EOBT >=#{startTime,jdbcType=VARCHAR} and EOBT <=#{endTime,jdbcType=VARCHAR} and SECTOR not like 'AP%'")
    List<AreaPort> getWaitToFlyPortFromJinan(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    @Select("select ARCID,WKTRC,ADEP,ADES,ETA JNTime,ARCREG,SsrCode,STATUS from fdr where ADES='ZSJN' and ETA >=#{startTime,jdbcType=VARCHAR} and ETA <=#{endTime,jdbcType=VARCHAR}")
    List<AreaPort> getToArrivePortFromJinan(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    @Select("<script>select ARCID,WKTRC,ADEP,ADES,ATA JNTime,ARCREG,SsrCode,STATUS from fdr where LASTTIME >= DATE_SUB(NOW(), INTERVAL 2 HOUR) and ARCID in <foreach collection=\"list\" item=\"item\" separator=\",\" open=\"(\" close=\")\">#{item}</foreach> and status != 'FIN' order by LASTTIME desc</script>")
    List<AreaPort> getFlightInfoByArcIds(List<String> arcIdList);
    
    @Select("select ARCID,WKTRC,ETA,ADEP,ADES,EOBT,ATD,SsrCode,SECTOR,STATUS from fdr where ADES='ZSJN' order by ETA")
    List<AllPort> getAllPortFromJinan();
    
    
    @Select("select ARCID,WKTRC,ADEP,ADES,ETO JNTime,ARCREG,SsrCode,STATUS from fdr,fdrfix where fdr.ADEP='ZSQD' and fdrfix.FDRID=fdr.IFPLID and fdrfix.PTID='WFG' order by ETO")
    List<AreaPort> getAreaPortFromQingdao();
    @Select("select ARCID,WKTRC,ADEP,ADES,ETO JNTime,ARCREG,SsrCode,STATUS from fdr,fdrfix where fdr.ADES='ZSQD' and fdrfix.FDRID=fdr.IFPLID and fdrfix.PTID='WFG' order by ETO")
    List<AreaPort> getAreaPortToQingdao();

    @Select("select ARCID,WKTRC,ADEP,ADES,ETO JNTime,ARCREG,SsrCode,STATUS from fdr,fdrfix where  fdr.ADES ='ZHCC'  and fdrfix.FDRID=fdr.IFPLID and fdrfix.PTID='REPOL' group by FDRID order by ETO")
    List<AreaPort> getAreaPortToZhengzhou();

    @Select("select ARCID,WKTRC,ADEP,ADES,ETO JNTime,ARCREG,SsrCode,STATUS from fdr,fdrfix where  fdr.ADES ='ZHHH' and fdrfix.FDRID=fdr.IFPLID and fdrfix.PTID='REPOL' group by FDRID order by ETO")
    List<AreaPort> getAreaPortToWuhan();

    @Select("select ARCID,ADEP,ADES,ADES,ATD,EOBT,ETA,ATA from fdr where ADES='ZSJN'")
    List<ExperiencePort> getExperiencePort();

    @Select("select ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ETA,ATA,SsrCode,ARCREG,STATUS,RTE from fdr where ADES='ZSJN'")
    List<ZSJNPort> getEnterZSJNPort();

    @Select("select ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ETA,ATA,SsrCode,ARCREG,STATUS,RTE from fdr where ADEP='ZSJN'")
    List<ZSJNPort> getLeaveZSJNPort();

    /*
     * 
     * ************************************************************************  2017-05-16
     * 
     */
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ETA,ATA,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADES='ZSJN' and ATA is not null and ATA >#{startTime, jdbcType=VARCHAR} and SECTOR not like 'AP%' order by ATA asc")
    List<EnterPort> getHaveArrivedEnterPortFromJinan(@Param("startTime") String startTime);
    
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ETA,ATA,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADES='ZSJN' and ETA >=#{startTime,jdbcType=VARCHAR} and ETA <=#{endTime,jdbcType=VARCHAR} order by ETA asc")
    List<EnterPort> getEnterJinJinFilghtForJinan(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ETA,ATA,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADES='ZSJN' AND COUPLED='Y' and LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and SECTOR like 'AP%' order by ETA asc")
    List<EnterPort> getNowEnterPortFromJinan();
    
    @Select("select FDRID,PTID,ETO from fdrfix where FDRID = #{ifpld,jdbcType=VARCHAR} AND PTID in ('TNA','BASOV','P291','GULEK','P353','P200','ABTUB', 'P292','PANKI') ORDER BY ETO DESC")
    List<EnterTimeVo> getJinJinTimeForJinan(@Param("ifpld") String ifpld);

    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADEP='ZSJN' and ATD is not null and ATD >#{startTime, jdbcType=VARCHAR} and SECTOR not like 'AP%' order by ATD asc")
    List<LeavePort> getHaveFlyingLeavePortFromJinan(@Param("startTime") String startTime);

    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADEP='ZSJN' AND COUPLED='Y' and LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and SECTOR like 'AP%' order by ETA asc")
    List<LeavePort> getNowLeavePortFromJinan();
    
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADEP='ZSJN' and EOBT is not null and EOBT >=#{startTime,jdbcType=VARCHAR} and EOBT <=#{endTime,jdbcType=VARCHAR} order by EOBT asc")
    List<LeavePort> getToFlyLeavePortFromJinan(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    
    /**
     * 以下为区调数据
     */
    @SelectProvider(type=QuDiaoSqlProvider.class, method="selectFlyDataForQuDiao")
    List<FlyData> getFlyDataForQuDiao(@Param("leavePortList") List<String> leavePortList, @Param("enterPortList") List<String> enterPortList, @Param("pointList") List<String> pointList);

    @Select("select th.`FLIGHTID` from trackhis th where th.FLIGHTID != '' AND th.`HIGH` <= 8000 group by th.`FLIGHTID`")
	List<String> getNowCommandARCIDForQuDiao();

    @Select("select f.IFPLID, f.ARCID,f.ADEP,f.ADES,f.RTE,f.EOBT,f.ETA,f.ATD,f.ATA,f.STATUS,f.COUPLE,f.COUPLED,f.SECTOR from fdr f where f.LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and f.ARCID in (#{arcidList})")
	List<FlyData> getFlyDataByARCIDList(@Param("arcidList") List<String> arcIdList);

    @Select("select FDRID,PTID,ETO from fdrfix where FDRID in (#{fdridList})")
	List<EnterTimeVo> getByFdrIdList(@Param("fdridList") List<String> fdridList);
    
}
