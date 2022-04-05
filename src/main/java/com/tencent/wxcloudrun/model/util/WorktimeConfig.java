package com.tencent.wxcloudrun.model.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class WorktimeConfig extends BaseObject {
    private TimeOfDay startTime;
    private TimeOfDay stopTime;
    private int interval;
    private int offset;
    private int percentage;



    @Override
    public String stringify() {
        JSONObject obj =  new JSONObject();
        obj.put("startTime",startTime.stringify());
        obj.put("stopTime",stopTime.stringify());
        obj.put("interval",interval);
        obj.put("offset",offset);
        obj.put("percentage",percentage);
        return obj.toString();
    }

    public static WorktimeConfig objectify(String str) {
        try{
            WorktimeConfig config = new WorktimeConfig();
            JSONObject obj  = JSON.parseObject(str);
            config.startTime =  TimeOfDay.objectify(obj.getString("startTime"));
            config.stopTime =  TimeOfDay.objectify(obj.getString("stopTime"));
            config.interval = obj.getInteger("interval");
            config.offset = obj.getInteger("offset");
            config.percentage = obj.getInteger("object");
            return config;

        }catch (Exception e){
            return null;
        }
    }

    public TimeOfDay getStartTime() {
        return startTime;
    }

    public void setStartTime(TimeOfDay startTime) {
        this.startTime = startTime;
    }

    public TimeOfDay getStopTime() {
        return stopTime;
    }

    public void setStopTime(TimeOfDay stopTime) {
        this.stopTime = stopTime;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
