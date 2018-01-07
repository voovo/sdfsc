package com.example.dao;

import com.example.domain.DTO.*;
import com.example.sql.QuDiaoSqlProvider;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuDiaoDao {
    
    @SelectProvider(type=QuDiaoSqlProvider.class, method="selectFlyDataForQuDiao")
    List<FlyData> getFlyDataForQuDiao(@Param("leavePortList") List<String> leavePortList, @Param("enterPortList") List<String> enterPortList, @Param("pointList") List<String> pointList);

    @Select("select th.`FLIGHTID` from trackhis th where th.FLIGHTID != '' AND th.`HIGH` <= 8000 group by th.`FLIGHTID`")
	List<String> getNowCommandARCIDForQuDiao();

    @SelectProvider(type=QuDiaoSqlProvider.class, method="selectFlyDataByARCIDList")
	List<FlyData> getFlyDataByARCIDList(@Param("arcidList") List<String> arcIdList);

    @SelectProvider(type=QuDiaoSqlProvider.class, method="selectByFdrIdList")
	List<EnterTimeVo> getByFdrIdList(@Param("fdridList") List<String> fdridList);
    
}
