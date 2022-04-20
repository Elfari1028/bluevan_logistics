package com.tencent.wxcloudrun.model.util;

public enum OrderStatus {
    created(0), canceled(1), locked(2), delivered(3);
    public final int value;
    private OrderStatus(int value) {
        this.value = value;
    }
    public static OrderStatus from(int val){
        switch (val){
            case 0: return created;
            case 1: return canceled;
            case 2: return locked;
            case 3: return delivered;
            default:return created;
        }
    }

    @Override
    public String toString() {
        switch (this){
            case created:
                return "已创建";
            case canceled:
                return  "已取消";
            case locked:
                return  "已锁定";
            case delivered:
                return "已送达";
            default: return "";
        }
    }
}
