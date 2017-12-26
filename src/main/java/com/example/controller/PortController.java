package com.example.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.domain.DTO.AreaPort;
import com.example.domain.DTO.EnterPort;
import com.example.domain.DTO.ExperiencePort;
import com.example.domain.DTO.FlyData;
import com.example.domain.DTO.LeavePort;
import com.example.domain.DTO.ZSJNPort;
import com.example.service.PortService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

/**
 * Created by SunYi on 2016/2/16/0016.
 */
@Controller
public class PortController {
    
    static Logger logger = LoggerFactory.getLogger(PortController.class);
    
    static Gson gson = new Gson();
    
    @Autowired
    private PortService portService;
    
    //真正主页，用户在访问 XXXXX/index就会跳转该方法，
    // 这个XXXXX是你的域名，自己电脑上的话一般都是127.0.0.1:8080或者是localhost：8080
    // 8080是端口号，端口号根据tomcat设置而改变，默认值是8080
    @RequestMapping("/jinjin")
    public String home(Model model, HttpServletRequest request, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        
        //出港
        List<LeavePort> leavePorts = portService.getLeavePortTable(now);
        model.addAttribute("leavePorts", leavePorts);
        //入港
        List<EnterPort> enterPorts = portService.getEnterPortTable(now);
        model.addAttribute("enterPorts", enterPorts);
        
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        String message = request.getParameter("message");
        if (message != null) {
            model.addAttribute("message", message);
            model.addAttribute("message", message);
        }
        return "jinjin";
    }
    
    @RequestMapping("/qudiao")
    public String qudiao(Model model, HttpServletRequest request, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        
        //出港
        List<FlyData> leavePorts = portService.getLeavePortTableForQuDiao(now);
        model.addAttribute("leavePorts", leavePorts);
        //入港
        List<FlyData> enterPorts = portService.getEnterPortTableForQuDiao(now);
        model.addAttribute("enterPorts", enterPorts);
        
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        String message = request.getParameter("message");
        if (message != null) {
            model.addAttribute("message", message);
            model.addAttribute("message", message);
        }
        return "qudiao";
    }

    @RequestMapping("/area")
    public String AreaPort(Model model, HttpSession session) throws ParseException, JsonProcessingException {
    	if (session.getAttribute("user") == null) {
    		return "/login";
    	}
        Date now = PortService.getNOW();
        List<AreaPort> areaPorts = portService.getAreaPortTable(now);
        model.addAttribute("allPorts", areaPorts);
        
        if (null != areaPorts) {
            Set<String> arcIdSet = new HashSet<>();
            for (AreaPort port : areaPorts) {
                arcIdSet.add(port.getARCID());
            }
//            logger.debug("arcIdSet:{}", gson.toJson(arcIdSet));
            model.addAttribute("totalCnt", arcIdSet.size());
        }
        
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        return "areaTable";
    }

    private void processList(int[] leaveCountLists, String[] leaveNumberLists) {
        int[] newleaveCountLists = leaveCountLists.clone();
        String[] newleaveNumberLists = leaveNumberLists.clone();
        for (int i = 1; i < leaveCountLists.length; i++) {
            int count = 0;
            String number = "";
            if (i < 5) {
                for (int j = 0; j <= i; j++) {
                    count += newleaveCountLists[j];
                    number += newleaveNumberLists[j];
                }
                leaveCountLists[i] = count;
                leaveNumberLists[i] = number;
            } else {
                for (int j = i - 4; j <= i; j++) {
                    count += newleaveCountLists[j];
                    number += newleaveNumberLists[j];
                }
                leaveCountLists[i] = count;
                leaveNumberLists[i] = number;
            }

        }

    }

    @RequestMapping("/enterZSJN")
    public String enterZSJN(Model model, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        List<ZSJNPort> zsjnPorts = portService.getEnterZSJNPortTable(now);

        model.addAttribute("Ports", zsjnPorts);
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        return "enterZSJN";
    }

    @RequestMapping("/leaveZSJN")
    public String leaveZSJN(Model model, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        List<ZSJNPort> ports = portService.getLeaveZSJNPortTable(now);

        model.addAttribute("Ports", ports);
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        return "leaveZSJN";
    }
    @RequestMapping("/ZSJN")
    public String ZSJN(Model model, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        List<ZSJNPort> leavePorts = portService.getLeaveZSJNPortTable(now);
        List<ZSJNPort> enterPorts = portService.getEnterZSJNPortTable(now);

        model.addAttribute("leavePorts", leavePorts);
        model.addAttribute("enterPorts", enterPorts);
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        return "ZSJN";
    }
    @RequestMapping("/experience")
    public String experience(Model model, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        List<ExperiencePort> experiencePorts = portService.getExperiencePortTable(now);
        model.addAttribute("Ports", experiencePorts);
        model.addAttribute("nowTime", PortService.DATE_FORMAT.format(now));
        model.addAttribute("JNowTime", now);
        return "experience";
    }
    @RequestMapping("/img")
    public String img(Model model, HttpSession session) throws ParseException, JsonProcessingException {
        return "img";
    }
}
