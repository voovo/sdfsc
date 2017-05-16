//package com.example;
//
//import com.example.dao.PortDao;
//import com.example.domain.DTO.AreaPort;
//import com.example.domain.DTO.EnterPort;
//import com.example.domain.DTO.LeavePort;
//import com.example.domain.User;
//import com.example.service.PortService;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.text.ParseException;
//import java.util.List;
//
////import com.example.dao.UserDao;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = DemoApplication.class)
//@Transactional
//@WebAppConfiguration
//public class DemoApplicationTests {
//
//	@Autowired
//	private PortService portService;
//	@Autowired
//	private PortDao portDao;
//	@Autowired
////	private UserDao userDao;
//	@Test
//	public void contextLoads() {
//	}
//
//	@Test
//	public void daotest() throws ParseException {
//		List<LeavePort> leavePorts =  portService.getLeavePortTable();
//		leavePorts.size();
//	}
//	@Test
//	public void daotest2() throws ParseException {
//		List<EnterPort> enterPorts =  portService.getEnterPortTable();
//		enterPorts.size();
//	}
//	@Test
//	public void daotest3() throws ParseException {
//		List<AreaPort> areaPorts =  portDao.getAreaPortFromQingdao();
//		areaPorts.size();
//	}
//	@Test
//	public void daotest4() throws ParseException {
//		List<AreaPort> areaPorts =  portService.getAreaPortTable();
//		areaPorts.size();
//	}
//
//	@Test
//	public void userAddTest() {
//		User user = new User();
//		user.setUsername("admin_1");
//		user.setPassword("admin_1");
////		userDao.save(user);
//
////		User user1 = userDao.findByUsername("admin_1");
//
////		User user = userDao.findByUsername("root");
////		Assert.assertEquals("123", user1.getPassword());
//	}
//}
