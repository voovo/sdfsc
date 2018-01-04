package com.example.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.domain.DTO.FlyData;
import com.example.service.PortService;
import com.example.service.QuDiaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

@Controller
public class QuDiaoController {
    
    static Logger logger = LoggerFactory.getLogger(QuDiaoController.class);
    
    static Gson gson = new Gson();
    
    @Autowired
    private QuDiaoService quDiaoService;
    
    @RequestMapping("/qudiao")
    public String qudiao(Model model, HttpServletRequest request, HttpSession session) throws ParseException, JsonProcessingException {
        if (session.getAttribute("user") == null) {
            return "/login";
        }
        Date now = PortService.getNOW();
        
        //北扇区
        List<FlyData> leavePorts = quDiaoService.getNorthFlyDataForQuDiao();
        model.addAttribute("leavePorts", leavePorts);
        //入港
        List<FlyData> enterPorts = quDiaoService.getSouthFlyDataForQuDiao();
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

}
