package com.tencent.wxcloudrun.model.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.L;
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

    @Override
    public JSONObject toJSON() {
           JSONObject obj = new JSONObject();
        this.map.forEach(new BiConsumer<String, Info>() {
            @Override
            public void accept(String s, Info info) {
                obj.put(s,info.toJSON());
            }
        });
        return obj;
    }

    static public WorkloadConfig objectify(String str){
        JSONObject obj = JSON.parseObject(str);
        WorkloadConfig config = new WorkloadConfig();
        L.info("before:"+str + "\n" +"after:"+obj.toString());
        obj.getInnerMap().forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {

                if(o instanceof JSONObject){
                    config.map.put(s,Info.objectifyJSON(((JSONObject) o))) ;
                }
                else if(o instanceof  String){
                    config.map.put(s,Info.objectify((String)o));
                }
            }
        });
        L.info(config.toString());
        return config;
    }

    public static class Info extends BaseObject{
        private String packageType;
        private double maxLoad;
        private List<Double> settings;

        static public Info objectifyJSON(JSONObject obj){
            Info info = new Info();
            info.packageType = obj.getString("packageType");
            info.maxLoad = obj.getDouble("maxLoad");
            info.settings = obj.getJSONArray("settings").toJavaList(Double.class);
            return info;
        }

        static public Info objectify(String str){
            JSONObject obj = JSON.parseObject(str);
            Info info = new Info();
            info.packageType = obj.getString("packageType");
            info.maxLoad = obj.getDouble("maxLoad");
            info.settings = obj.getJSONArray("settings").toJavaList(Double.class);
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

        @Override
        public JSONObject toJSON() {
             JSONObject obj = new JSONObject();
            obj.put("packageType",this.packageType);
            obj.put("maxLoad",this.maxLoad);
            obj.put("settings",this.settings);
            return obj;
        }
    }
}
