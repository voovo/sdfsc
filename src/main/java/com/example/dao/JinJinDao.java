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
    
    /**
     * 取进近中已经进港的飞机
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectHaveArrivedFlyDataForJinJin")
    List<FlyData> getHaveArrivedFlyDataForJinJin(@Param("pointList") List<String> pointList);

    /**
     * 取进近中未来将进港的飞机
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectEnterFlyDataForJinJin")
    List<FlyData> getToArriveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
    /**
     * 取当前时刻进港的飞机
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectNowEnterPortForJinJin")
    List<FlyData> getNowEnterPortForJinJin(@Param("pointList") List<String> pointList);
    
    /**
     * 根据 ptid 取过点的时间
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectPassETOByFdrIdPTIDList")
    List<EnterTimeVo> getPassETOByFdrIdPTIDList(@Param("ifpld") String ifpld, @Param("pointList") List<String> pointList);

    /**
     * 取已经出港的飞机
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectHaveLeavedFlyDataForJinJin")
    List<FlyData> getHaveLeavedFlyDataForJinJin(@Param("pointList") List<String> pointList);

    /**
     * 取当前时刻正在出港的飞机
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectNowLeavePortForJinJin")
    List<FlyData> getNowLeavePortForJinJin(@Param("pointList") List<String> pointList);
    
    /**
     * 取即将出港的飞机
     */
    @SelectProvider(type=JinJinSqlProvider.class, method="selectLeaveFlyDataForJinJin")
    List<FlyData> getToLeaveFlyDataForJinJin(@Param("pointList") List<String> pointList);
    
}
