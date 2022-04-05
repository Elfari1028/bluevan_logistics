package com.tencent.wxcloudrun.model;

import com.tencent.wxcloudrun.model.util.UserRole;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user_model")
public class User extends ModelBase implements Serializable {

    private String name;

    private String wxName;

    private String phone;

    private String wxAvatarUrl;

    private String wxUnionId;

    private String wxUserId;

    @ManyToOne
    private Warehouse warehouse;


    // 0 user, 1 driver, 2 warehouse-receiver, 3 warehouse-manager 4 platform-manager
    private int role;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWxName() {
        return wxName;
    }

    public void setWxName(String wxName) {
        this.wxName = wxName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWxAvatarUrl() {
        return wxAvatarUrl;
    }

    public void setWxAvatarUrl(String wxAvatarUrl) {
        this.wxAvatarUrl = wxAvatarUrl;
    }

    public String getWxUnionId() {
        return wxUnionId;
    }

    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId;
    }

    public String getWxUserId() {
        return wxUserId;
    }

    public void setWxUserId(String wxUserId) {
        this.wxUserId = wxUserId;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public UserRole getRole() {
        return UserRole.of(this.role);
    }

    public void setRole(UserRole role) {
        this.role = role.value;
    }
}
