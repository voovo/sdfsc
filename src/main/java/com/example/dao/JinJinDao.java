package com.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import com.example.domain.DTO.EnterTimeVo;
import com.example.domain.DTO.FlyData;
import com.example.sql.SqlProvider;

@Repository
public interface JinJinDao {
    
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ETA,ATA,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADES='ZSJN' and ATA is not null and ATA >#{startTime, jdbcType=VARCHAR} and SECTOR!='AP01' and SECTOR!='AP02' order by ATA asc")
    List<FlyData> getHaveArrivedFlyDataForJinJin(@Param("startTime") String startTime);

    @SelectProvider(type=SqlProvider.class, method="selectEnterFlyDataForJinJin")
    List<FlyData> getToArriveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ETA,ATA,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADES='ZSJN' AND COUPLED='Y' and LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and (SECTOR='AP01' or SECTOR='AP02') order by ETA asc")
    List<FlyData> getNowEnterPortForJinJin();
    
    @Select("select FDRID,PTID,ETO from fdrfix where FDRID = #{ifpld,jdbcType=VARCHAR} AND PTID in (#{pointList})")
    List<EnterTimeVo> getJinJinTimeForJinan(@Param("ifpld") String ifpld, @Param("pointList") List<String> pointList);

    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADEP='ZSJN' and ATD is not null and ATD >#{startTime, jdbcType=VARCHAR} and SECTOR!='AP01' and SECTOR!='AP02' order by ATD asc")
    List<FlyData> getHaveLeavedFlyDataForJinJin(@Param("startTime") String startTime);

    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADEP='ZSJN' AND COUPLED='Y' and LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and (SECTOR='AP01' or SECTOR='AP02') order by ETA asc")
    List<FlyData> getNowLeavePortForJinJin();
    
    @SelectProvider(type=SqlProvider.class, method="selectLeaveFlyDataForJinJin")
    List<FlyData> getToLeaveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
}
