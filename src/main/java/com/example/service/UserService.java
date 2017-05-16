package com.example.service;

import com.example.domain.Role;
import com.example.domain.User;
import com.example.service.message.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SunYi on 2016/3/25/0025.
 */
@Service
public class UserService {
    static List<Role> roles;
    static List<User> users;

    static {
        Role admin = new Role("admin", "管理员");
        Role jinjin = new Role("jinjin", "进近");
        Role quyu = new Role("quyu", "区域");
        Role jinchang = new Role("jichang", "机场");

        User adminUser = new User("admin", "admin", "admin@admin.com", admin);
        User jinjinUser = new User("jinjin", "jinjin", "jinjin@admin.com", jinjin);
        User quyuUser = new User("quyu", "quyu", "quyu@admin.com", quyu);
        User jinchangUser = new User("jichang", "jichang", "jichang@admin.com", jinchang);

        roles = new ArrayList<>();
        roles.add(admin);
        roles.add(jinjin);
        roles.add(quyu);
        roles.add(jinchang);
        users = new ArrayList<>();
        users.add(adminUser);
        users.add(jinjinUser);
        users.add(quyuUser);
        users.add(jinchangUser);
    }
//    @Autowired
//    private UserDao userDao;
//    @Autowired
//    private RoleDao RoleDao;

    //    public Message login(User user) {
//        Message message = new Message();
//        if (user == null || user.getUsername() == null) {
//            message.setSuccess(false);
//            message.setReason("输入为空。");
//            return message;
//        }
//        User getUser = userDao.findByUsername(user.getUsername());
//        if (getUser == null || !getUser.getPassword().equals(user.getPassword())) {
//            message.setSuccess(false);
//            message.setReason("用户名或者密码错误。");
//            return message;
//        }
//        message.setReason("登陆成功！");
//        message.setOthers(getUser);
//        return message;
//    }
//
//    public Message register(User user, String role) {
//        Message message = new Message();
//        if (user == null || user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
//            message.setSuccess(false);
//            message.setReason("有未填写信息");
//            return message;
//        }
//        User getUser = userDao.findByUsername(user.getUsername());
//        if (getUser != null) {
//            message.setSuccess(false);
//            message.setReason("该用户已经存在");
//            return message;
//        }
//        user.setRole(RoleDao.findByRole(role));
//        userDao.save(user);
//        message.setReason("注册成功");
//        return message;
//    }
//    public List<Role> getAllRoles(){
//        return RoleDao.findAll();
//    }
    public List<Role> getAllRoles() {
        return roles;
    }

    public Message login(User user) {
        Message message = new Message();
        if (user == null || user.getUsername() == null) {
            message.setSuccess(false);
            message.setReason("输入为空。");
            return message;
        }
        User getUser = findByUsername(user.getUsername());
        if (getUser == null || !getUser.getPassword().equals(user.getPassword())) {
            message.setSuccess(false);
            message.setReason("用户名或者密码错误。");
            return message;
        }
        message.setReason("登陆成功！");
        message.setOthers(getUser);
        return message;
    }

    private User findByUsername(String username) {

        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
}
