package com.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import com.example.domain.DTO.EnterTimeVo;
import com.example.domain.DTO.FlyData;
import com.example.sql.JinJinSqlProvider;

@Repository
public interface JinJinDao {
    
    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ETA,ATA,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADES='ZSJN' and ATA is not null and ATA >#{startTime, jdbcType=VARCHAR} and SECTOR!='AP01' and SECTOR!='AP02' order by ATA asc")
    List<FlyData> getHaveArrivedFlyDataForJinJin(@Param("startTime") String startTime);

    @SelectProvider(type=JinJinSqlProvider.class, method="selectEnterFlyDataForJinJin")
    List<FlyData> getToArriveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectNowEnterPortForJinJin")
    List<FlyData> getNowEnterPortForJinJin(@Param("pointList") List<String> pointList);
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectPassETOByFdrIdPTIDList")
    List<EnterTimeVo> getPassETOByFdrIdPTIDList(@Param("ifpld") String ifpld, @Param("pointList") List<String> pointList);

    @Select("select IFPLID,ARCID,WKTRC,ADEP,ADES,ATD,EOBT,ARCREG,SsrCode,STATUS,RTE,SECTOR from fdr where ADEP='ZSJN' and ATD is not null and ATD >#{startTime, jdbcType=VARCHAR} and SECTOR!='AP01' and SECTOR!='AP02' order by ATD asc")
    List<FlyData> getHaveLeavedFlyDataForJinJin(@Param("startTime") String startTime);

    @SelectProvider(type=JinJinSqlProvider.class, method="selectNowLeavePortForJinJin")
    List<FlyData> getNowLeavePortForJinJin(@Param("pointList") List<String> pointList);
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectLeaveFlyDataForJinJin")
    List<FlyData> getToLeaveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
}
