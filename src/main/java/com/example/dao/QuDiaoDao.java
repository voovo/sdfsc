package com.example.dao;

import com.example.domain.DTO.*;
import com.example.sql.SqlProvider;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuDiaoDao {
    
    @SelectProvider(type=SqlProvider.class, method="selectFlyDataForQuDiao")
    List<FlyData> getFlyDataForQuDiao(@Param("leavePortList") List<String> leavePortList, @Param("enterPortList") List<String> enterPortList, @Param("pointList") List<String> pointList);

    @Select("select th.`FLIGHTID` from trackhis th where th.FLIGHTID != '' AND th.`HIGH` <= 8000 group by th.`FLIGHTID`")
	List<String> getNowCommandARCIDForQuDiao();

    @Select("select f.IFPLID, f.ARCID,f.ADEP,f.ADES,f.RTE,f.EOBT,f.ETA,f.ATD,f.ATA,f.STATUS,f.COUPLE,f.COUPLED,f.SECTOR from fdr f where f.LASTTIME >= DATE_SUB(NOW(), INTERVAL 10 MINUTE) and f.ARCID in (#{arcidList})")
	List<FlyData> getFlyDataByARCIDList(@Param("arcidList") List<String> arcIdList);

    @Select("select FDRID,PTID,ETO from fdrfix where FDRID in (#{fdridList})")
	List<EnterTimeVo> getByFdrIdList(@Param("fdridList") List<String> fdridList);
    
}
