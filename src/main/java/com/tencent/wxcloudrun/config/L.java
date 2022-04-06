package com.tencent.wxcloudrun.config;

import com.tencent.wxcloudrun.WxCloudRunApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class L {

    public static void warn(String msg){
        WxCloudRunApplication.logger.warn(msg);
    }
    public static void info(String msg){
        WxCloudRunApplication.logger.info(msg);
    }
    public static void error(String e){
        WxCloudRunApplication.logger.error(e);
    }
    public static void debug(String s){
        WxCloudRunApplication.logger.debug(s);
    }
}
