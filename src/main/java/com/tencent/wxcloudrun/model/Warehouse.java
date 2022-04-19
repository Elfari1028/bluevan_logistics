package com.tencent.wxcloudrun.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.model.util.*;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Entity
public class Warehouse extends ModelBase implements Serializable {

    private String name;

    private String description;

    private String worktimeConfig;

    private String workloadConfig;

    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorktimeConfig getWorktimeConfig() {
        return WorktimeConfig.objectify(this.worktimeConfig);
    }

    public void setWorktimeConfig(WorktimeConfig worktimeConfig) {
        this.worktimeConfig = worktimeConfig.stringify();
    }

    public Location getLocation() {
        return Location.objectify(location);
    }

    public void setLocation(Location location) {
        this.location = location.stringify();
    }


    public WorkloadConfig getWorkloadConfig() {
        return WorkloadConfig.objectify(workloadConfig);
    }

    public void setWorkloadConfig(WorkloadConfig workloadConfig) {
        this.workloadConfig = workloadConfig.stringify();
    }

}

