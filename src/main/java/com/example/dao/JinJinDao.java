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
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectHaveArrivedFlyDataForJinJin")
    List<FlyData> getHaveArrivedFlyDataForJinJin(@Param("pointList") List<String> pointList);

    @SelectProvider(type=JinJinSqlProvider.class, method="selectEnterFlyDataForJinJin")
    List<FlyData> getToArriveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectNowEnterPortForJinJin")
    List<FlyData> getNowEnterPortForJinJin(@Param("pointList") List<String> pointList);
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectPassETOByFdrIdPTIDList")
    List<EnterTimeVo> getPassETOByFdrIdPTIDList(@Param("ifpld") String ifpld, @Param("pointList") List<String> pointList);

    @SelectProvider(type=JinJinSqlProvider.class, method="selectHaveLeavedFlyDataForJinJin")
    List<FlyData> getHaveLeavedFlyDataForJinJin(@Param("pointList") List<String> pointList);

    @SelectProvider(type=JinJinSqlProvider.class, method="selectNowLeavePortForJinJin")
    List<FlyData> getNowLeavePortForJinJin(@Param("pointList") List<String> pointList);
    
    @SelectProvider(type=JinJinSqlProvider.class, method="selectLeaveFlyDataForJinJin")
    List<FlyData> getToLeaveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
}
