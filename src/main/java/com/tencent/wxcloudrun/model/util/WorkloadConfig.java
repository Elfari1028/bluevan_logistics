package com.tencent.wxcloudrun.model.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Data
public class WorkloadConfig extends BaseObject{
    private Map<String, Info> map = new HashMap<>();


    public void put(String packageType, Info info){
        map.put(packageType,info);
    }
    public Info get(String packageType){
        return map.get(packageType);
    }
    @Override
    public String stringify() {
        JSONObject obj = new JSONObject();
        this.map.forEach(new BiConsumer<String, Info>() {
            @Override
            public void accept(String s, Info info) {
                obj.put(s,info.stringify());
            }
        });
        return obj.toString();
    }

    static public WorkloadConfig objectify(String str){
        JSONObject obj = JSON.parseObject(str);
        WorkloadConfig config = new WorkloadConfig();
        obj.getInnerMap().forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                if(o instanceof String){
                    config.map.put(s,Info.objectify((String)o)) ;
                }
            }
        });
        return config;
    }

    public static class Info extends BaseObject{
        private String packageType;
        private int maxLoad;
        private List<Integer> settings;

        static public Info objectify(String str){
            JSONObject obj = JSON.parseObject(str);
            Info info = new Info();
            info.packageType = obj.getString("packageType");
            info.maxLoad = obj.getInteger("maxLoad");
            info.settings = obj.getJSONArray("settings").toJavaList(Integer.class);
            return info;
       }
        @Override
        public String stringify() {
            JSONObject obj = new JSONObject();
            obj.put("packageType",this.packageType);
            obj.put("maxLoad",this.maxLoad);
            obj.put("settings",this.settings);
            return obj.toString();
        }
    }
}
