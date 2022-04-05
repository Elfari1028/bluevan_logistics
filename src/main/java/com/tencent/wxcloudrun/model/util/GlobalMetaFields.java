package com.tencent.wxcloudrun.model.util;

public enum GlobalMetaFields{
    packageType("packageType"), cargoType("cargoType"),banlist("banlist");
    final public String value;
    GlobalMetaFields(String val){
        this.value = val;
    }
    static public GlobalMetaFields of(String str){
        switch (str){
            case "packageType": return packageType;
            case "cargoType":return cargoType;
            case "banlist":return banlist;
        }
        return packageType;
    }
}