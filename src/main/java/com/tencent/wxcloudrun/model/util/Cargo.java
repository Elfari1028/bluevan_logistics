package com.tencent.wxcloudrun.model.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class Cargo extends BaseObject{

    private String name;
    private int count;
    private String cargoType;
    private String packageType;
    private int volume;
    private int weight;
    private String note;


    public static Cargo objectify(String str){
        try{
            Cargo cargo  = new Cargo();
            JSONObject obj = JSON.parseObject(str);
            cargo.name = obj.getString("name");
            cargo.count = obj.getInteger("count");
            cargo.cargoType = obj.getString("cargoType");
            cargo.packageType = obj.getString("packageType");
            cargo.volume = obj.getInteger("volume");
            cargo.weight = obj.getInteger("weight");
            cargo.note = obj.getString("note");
            return cargo;

        }catch (Exception e){
            return null;
        }
    }

    @Override
    public String stringify() {
       try{
           JSONObject obj = new JSONObject();
           obj.put("name",name);
           obj.put("count",count);
           obj.put("cargoType",cargoType);
           obj.put("packageType",packageType);
           obj.put("volume",volume);
           obj.put("weight",weight);
           obj.put("note",note);
           return obj.toString();
       }catch (Exception e){
           return  null;
       }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
