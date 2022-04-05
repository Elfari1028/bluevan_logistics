package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.model.util.*;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final UserService userService;

    public WarehouseController(@Autowired WarehouseService warehouseService,@Autowired UserService userService) {
        this.warehouseService = warehouseService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public ApiResponse list(){
        JSONArray array = new JSONArray();
        Iterable<Warehouse> houses =  warehouseService.getAll();
        for (Warehouse house : houses) {
            JSONObject warehouse = new JSONObject();
            warehouse.put("id",house.getId());
            warehouse.put("name",house.getName());
            warehouse.put("location",house.getLocation());
            warehouse.put("worktime",house.getWorktimeConfig());
            warehouse.put("workload",house.getWorkloadConfig());
            warehouse.put("description",house.getDescription());
            array.add(warehouse);
        }
        return ApiResponse.ok(array);
    }


    @PostMapping("/add")
    public ApiResponse add(@RequestParam(name="session")String session, @RequestBody JSONObject body){
        JSONObject ret = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);
        if(s.isPresent()){
            User user  = s.get().getUser();
            if(user.getRole() != UserRole.platform_manager){
                return ApiResponse.error("没有权限");
            }
            else {
                Warehouse warehouse = new Warehouse();
                warehouse.setName(body.getString("name"));
                warehouse.setLocation(Location.objectify(body.getJSONObject("location").toString()));
                warehouse.setWorktimeConfig(WorktimeConfig.objectify(body.getJSONObject("worktimeConfig").toString()));
                warehouse.setWorkloadConfig(WorkloadConfig.objectify(body.getJSONObject("workloadConfig").toString()));
                warehouse.setDescription(body.getString("description"));
                warehouse = warehouseService.saveWarehouse(warehouse);
                ret.put("id",warehouse.getId());
                return ApiResponse.ok(ret);
            }
        } else {
            return ApiResponse.error("请先登录");
        }
    }
    @PostMapping("/edit")
    public ApiResponse edit(@RequestParam(name="session")String session, @RequestBody JSONObject body){
        JSONObject ret = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);
        if(s.isPresent()){
            User user  = s.get().getUser();
            if(user.getRole() != UserRole.platform_manager || (user.getRole()!=UserRole.warehouse_manager && user.getWarehouse().getId() != body.getInteger("id"))){
                return ApiResponse.error("没有权限");
            }
            else {
                Optional<Warehouse> wareOp = warehouseService.findById(body.getInteger("id"));
                if(!wareOp.isPresent()){
                    return ApiResponse.error("仓库不存在");
                }
                Warehouse warehouse = wareOp.get();
                warehouse.setName(body.getString("name"));
                warehouse.setLocation(Location.objectify(body.getJSONObject("location").toString()));
                warehouse.setWorktimeConfig(WorktimeConfig.objectify(body.getJSONObject("worktimeConfig").toString()));
                warehouse.setWorkloadConfig(WorkloadConfig.objectify(body.getJSONObject("workloadConfig").toString()));
                warehouse.setDescription(body.getString("description"));
                warehouse = warehouseService.saveWarehouse(warehouse);
                ret.put("id",warehouse.getId());
                return ApiResponse.ok(ret);
            }
        } else {
            return ApiResponse.error("请先登录");
        }
    }

}
