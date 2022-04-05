package com.tencent.wxcloudrun.model.util;

public enum UserRole{
    user(1), driver(2),warehouse_manager(3),warehouse_worker(4),platform_manager(5);
    final public int value;
    UserRole(int value) {
        this.value = 1;
    }
    public static UserRole of(int value){
        switch (value){
            case 2: return driver;
            case 3: return warehouse_manager;
            case 4: return warehouse_worker;
            case 5: return platform_manager;
            default: return user;
        }
    }
}