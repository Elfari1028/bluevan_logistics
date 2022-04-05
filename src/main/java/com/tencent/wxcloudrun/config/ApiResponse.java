package com.tencent.wxcloudrun.config;

import lombok.Data;

import java.util.HashMap;

@Data
public final class ApiResponse {

  private Integer code;
  private String errorMsg;
  private Object data;
  private Boolean success;
  private ApiResponse(int code, String errorMsg, Object data, Boolean success) {
    this.code = code;
    this.errorMsg = errorMsg;
    this.data = data;
    this.success = success;
  }
  
  public static ApiResponse ok() {
    return new ApiResponse(200, "", new HashMap<>(),true);
  }

  public static ApiResponse ok(Object data) {
    return new ApiResponse(200, "", data,true);
  }

  public static ApiResponse error(String errorMsg) {
    return new ApiResponse(502, errorMsg, new HashMap<>(),false);
  }
}
