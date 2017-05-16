package com.example.controller;

import com.example.domain.User;
import com.example.service.UserService;
import com.example.service.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by SunYi on 2016/3/25/0025.
 */
@Controller
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public RedirectView index(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return new RedirectView("/jinjin/", true, false, true);

        }else{
            return new RedirectView("/index", true, false, true);

        }
    }
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index2() {
        return "index";
    }

//    @RequestMapping(value = "/login", method = RequestMethod.GET)
//    public String login() {
//        return "login";
//    }

//    @RequestMapping(value = "/register", method = RequestMethod.GET)
//    public String register(Model model) {
//        model.addAttribute("roles", userService.getAllRoles());
//        return "register";
//    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public RedirectView doLogin(HttpServletRequest request, Model model, HttpSession session) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        Message message = userService.login(user);

        if (message.isSuccess()) {
            session.setAttribute("user", message.getOthers());
            session.setAttribute("role", ((User)message.getOthers()).getRole().getRole());
            return new RedirectView("/jinjin/", true, false, true);
        } else {
            model.addAttribute("message", message.getReason());
            return new RedirectView("/index", true, false, true);
        }
    }

//    @RequestMapping(value = "/register", method = RequestMethod.POST)
//    public RedirectView doRegister(HttpServletRequest request, Model model) {
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//        String email = request.getParameter("email");
//        String role = request.getParameter("role");
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(password);
//        user.setEmail(email);
//        Message message = userService.register(user, role);
//        model.addAttribute("message", message.getReason());
//        if (message.isSuccess()) {
//            return new RedirectView("/", true, false, true);
//        } else {
//            return new RedirectView("/register/", true, false, true);
//        }
//    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            session.removeAttribute("user");
            session.removeAttribute("role");
            model.addAttribute("message", "退出登录成功");
        } else {
            model.addAttribute("message", "未登录");
        }
        return "redirect:/";
    }
}
