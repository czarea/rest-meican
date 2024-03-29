package com.czarea.rest.meican.entity;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zhouzx
 */
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String email;
    private String phone;
    /**
     * 0：注销；1：正常；
     */
    private Integer status;
    private String password;
    private Date createTime;
    private Date updateTime;
    private String tabUniqueId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getTabUniqueId() {
        return tabUniqueId;
    }

    public void setTabUniqueId(String tabUniqueId) {
        this.tabUniqueId = tabUniqueId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", status=" + status +
            ", password='" + password + '\'' +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            ", tabUniqueId='" + tabUniqueId + '\'' +
            '}';
    }
}
