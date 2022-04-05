package com.tencent.wxcloudrun.model.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Location extends BaseObject {
    private BigDecimal latitude;

    private BigDecimal longitude;

    private  String locationName;

    private String address;

    private String province;

    private String city;

    private String district;


    @Override
    public String stringify() {
        JSONObject obj =  jsonObjectify();
        if(obj!=null){
            return obj.toString();
        }
        else {
            return null;
        }
    }

    public JSONObject jsonObjectify(){
        try{
            JSONObject obj = new JSONObject();
            obj.put("longitude",longitude.toString());
            obj.put("latitude",latitude.toString());
            obj.put("locationName",locationName);
            obj.put("address",address);
            obj.put("province",province);
            obj.put("city",city);
            obj.put("district",district);
            return obj;
        }catch (Exception e){
            return  null;
        }
    }

   public static Location objectify(String str){
        try{
            Location location  = new Location();
            JSONObject obj = JSON.parseObject(str);
            location.longitude = new BigDecimal( obj.getString("longitude"));
            location.latitude = new BigDecimal( obj.getString("latitude"));
            location.locationName = obj.getString("locationName");
            location.address = obj.getString("address");
            location.province = obj.getString("province");
            location.city = obj.getString("city");
            location.district = obj.getString("district");
            return location;
        }catch (Exception e){
            return null;
        }
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
