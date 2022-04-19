package com.tencent.wxcloudrun.model;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.tencent.wxcloudrun.model.util.Cargo;
import com.tencent.wxcloudrun.model.util.OrderStatus;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Order extends ModelBase implements Serializable {

    private String senderName;

    private String senderPhone;

    @OneToOne
    private Warehouse targetWarehouse;

    private int option; // 0 auto 1 system
    private Date targetTime;
    private String receiverId;
    private String cargos; //jsonfied data of cargos;

    private int status;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<Cargo> getCargos() {
        JSONArray arr = JSON.parseArray(this.cargos);
        ArrayList<Cargo> ret = new ArrayList<Cargo>();
        for (Object obj:arr) {
            if(obj instanceof String){
                ret.add(Cargo.objectify((String)obj));
            }
        }
        return ret;
    }

    public void setCargos(List<Cargo> cargos) {
        JSONArray arr = new JSONArray();
        for (Cargo cargo:cargos) {
            arr.add(cargo.stringify());
        }
        this.cargos = arr.toString();
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public Warehouse getTargetWarehouse() {
        return targetWarehouse;
    }

    public void setTargetWarehouse(Warehouse targetWarehouse) {
        this.targetWarehouse = targetWarehouse;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public Date getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(Date targetTime) {
        this.targetTime = targetTime;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public OrderStatus getStatus() {
        return OrderStatus.from(this.status);
    }

    public void setStatus(OrderStatus status) {
        this.status = status.value;
    }
}


