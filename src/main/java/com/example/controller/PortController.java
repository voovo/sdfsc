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
