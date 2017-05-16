package com.example.domain;

//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by SunYi on 2016/4/3/0003.
 */
//@Entity
public class Role {
//    @Id
//    @GeneratedValue
    private Long id;

    @NotNull
    private String role;
    @NotNull
    private String name;

//    @OneToMany
    private List<User> users;

    public Role(String role, String name) {
        this.role = role;
        this.name = name;
        this.users = users;
    }

    public Role() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
