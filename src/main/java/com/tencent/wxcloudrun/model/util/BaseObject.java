package com.tencent.wxcloudrun.model.util;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public abstract class BaseObject implements Serializable {

    public abstract String stringify();

    public abstract JSONObject toJSON();
}
