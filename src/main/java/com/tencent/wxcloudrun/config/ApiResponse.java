package com.tencent.wxcloudrun.config;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.HashMap;

@Data
public final class ApiResponse {

  private Integer code;
  private String errorMsg;
  private Object data;
  private Boolean success;
  private Boolean paged;
  private Object pageData;
  private ApiResponse(int code, String errorMsg, Object data, Boolean success, Boolean paged,Object pageData) {
    this.code = code;
    this.errorMsg = errorMsg;
    this.data = data;
    this.success = success;
    this.paged = paged;
    this.pageData = pageData;
  }
  
  public static ApiResponse ok() {
    return new ApiResponse(200, "", new HashMap<>(),true,false, null);
  }

  public static ApiResponse ok(Object data) {
    return new ApiResponse(200, "", data,true,false, null);
  }

  public static ApiResponse ok(Object data, Object pageData){

    return new ApiResponse(200, "", data,true,true,pageData);
  }
  public static ApiResponse error(String errorMsg) {
    return new ApiResponse(502, errorMsg, new HashMap<>(),false,false,null);
  }

  public ApiResponse paged(int size, int currentPage, int totalPage) {
    this.paged = true;
    HashMap<String,Integer> obj = new HashMap<String,Integer> ();
    obj.put("size",size);
    obj.put("current",currentPage);
    obj.put("total",totalPage);
    this.pageData = obj;
    return this;
  }
}
