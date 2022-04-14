package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.model.util.*;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.service.WarehouseService;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.Instant;

@RestController
@RequestMapping("/warehouse")
public class WarehouseController {
    private final WarehouseService warehouseService;
    private final UserService userService;

    public WarehouseController(@Autowired WarehouseService warehouseService, @Autowired UserService userService) {
        this.warehouseService = warehouseService;
        this.userService = userService;
    }


    @GetMapping("/location/describe")
    public ApiResponse list(@RequestParam(name="location") String location){
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            String back = "/ws/geocoder/v1" +"?key="+"O76BZ-ESCKX-MVD45-TYVKU-7ELUF-7VBOW" + "&location=" +location;
            MessageDigest md5 =  MessageDigest.getInstance("MD5");
            md5.update((back + "W4ngUBFr1ZdVJlbqGnpwqWbfZO4PsxP").getBytes());
            byte[] bytearr =  md5.digest();
            StringBuilder builder = new StringBuilder();
            for(byte b : bytearr){
                builder.append(String.format("%02x",b));
            }
            HttpGet request = new HttpGet("https://apis.map.qq.com"+ back + "&sig="+builder.toString());
            ObjectMapper mapper = new ObjectMapper();
            HashMap response = client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), HashMap.class));
            return  ApiResponse.ok(response);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("error");
        }
    }

    @GetMapping("/list")
    public ApiResponse list() {
        JSONArray array = new JSONArray();
        Iterable<Warehouse> houses = warehouseService.getAll();
        for (Warehouse house : houses) {
            JSONObject warehouse = new JSONObject();
            warehouse.put("id", house.getId());
            warehouse.put("name", house.getName());
            warehouse.put("location", house.getLocation());
//            warehouse.put("worktime", house.getWorktimeConfig().stringify());
//            warehouse.put("workload", house.getWorkloadConfig().stringify());
            warehouse.put("description", house.getDescription());
            array.add(warehouse);
        }
        return ApiResponse.ok(array);
    }

    @GetMapping("/get")
    public ApiResponse get(@RequestParam(name = "warehouseId") int id) {
        JSONArray array = new JSONArray();
        Optional<Warehouse> houseOp = warehouseService.findById(id);
        if (!houseOp.isPresent()) {
            return ApiResponse.error("仓库不存在");
        }
        Warehouse house = houseOp.get();
        JSONObject warehouse = new JSONObject();
        warehouse.put("id", house.getId());
        warehouse.put("name", house.getName());
        warehouse.put("location", house.getLocation());
        warehouse.put("worktime", house.getWorktimeConfig().toJSON());
        warehouse.put("workload", house.getWorkloadConfig().toJSON());
        L.info(house.getWorkloadConfig().toString());
        L.info(house.getWorkloadConfig().stringify());
        L.info(JSON.parseObject(house.getWorkloadConfig().stringify()).toString());
        warehouse.put("description", house.getDescription());
        return ApiResponse.ok(warehouse);
    }

    @GetMapping("/time/intervals")
    public ApiResponse getTimeOptions(@RequestParam(name = "warehouseId") int id) {
        Optional<Warehouse> who = warehouseService.findById(id);
        if(!who.isPresent()){
            return ApiResponse.error("仓库不存在");
        }
        Warehouse house = who.get();
        List<Pair<LocalDateTime,LocalDateTime>> list = warehouseService.getAllLegalIntervals(house);
        List<String> retlist = new ArrayList<String>();
        for (Pair<LocalDateTime,LocalDateTime> p:
             list) {
            retlist.add(p.getFirst().toString().replace("T"," ") + "~" +  p.getSecond().toString().substring(11));
        }
        Optional<Warehouse> houseOp = warehouseService.findById(id);
        if (!houseOp.isPresent()) {
            return ApiResponse.error("仓库不存在");
        }
        return ApiResponse.ok(retlist);
    }

    @GetMapping("/delete")
    public ApiResponse delete(@RequestParam(name = "session") String session,@RequestParam(name = "warehouseId") int id) {
        Optional<Session> s = userService.isUserLoggedIn(session);
        if (s.isPresent()) {
            User user = s.get().getUser();
            if (user.getRole() != UserRole.platform_manager) {
                return ApiResponse.error("没有权限");
            }
            boolean result = warehouseService.deleteById(id);
            if(result){
                return ApiResponse.ok();
            }else {
                return ApiResponse.error("仓库不存在");
            }

        } else {
            return ApiResponse.error("请先登录");
        }

    }
    @PostMapping("/add")
    public ApiResponse add(@RequestParam(name = "session") String session, @RequestBody JSONObject body) {
        JSONObject ret = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);

        if (s.isPresent()) {
            User user = s.get().getUser();
            if (user.getRole() != UserRole.platform_manager) {
                return ApiResponse.error("没有权限");
            } else {
                Warehouse warehouse = new Warehouse();
                warehouse.setName(body.getString("name"));
                warehouse.setLocation(Location.objectify(body.getJSONObject("location").toString()));
                warehouse.setWorktimeConfig(WorktimeConfig.objectify(body.getJSONObject("worktimeConfig").toString()));
                L.info(body.getJSONObject("workloadConfig").toString());
                L.info(WorkloadConfig.objectify(body.getJSONObject("workloadConfig").toString()).stringify());
                warehouse.setWorkloadConfig(WorkloadConfig.objectify(body.getJSONObject("workloadConfig").toString()));
                warehouse.setDescription(body.getString("description"));
                warehouse = warehouseService.saveWarehouse(warehouse);
                ret.put("id", warehouse.getId());
                return ApiResponse.ok(ret);
            }
        } else {
            return ApiResponse.error("请先登录");
        }
    }

    @PostMapping("/edit")
    public ApiResponse edit(@RequestParam(name = "session") String session, @RequestBody JSONObject body) {
        JSONObject ret = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);
        if (s.isPresent()) {
            User user = s.get().getUser();
            if (user.getRole() != UserRole.platform_manager && !(user.getRole() == UserRole.warehouse_manager && user.getWarehouse().getId() == body.getInteger("id"))) {
                return ApiResponse.error("没有权限");
            } else {
                Optional<Warehouse> wareOp = warehouseService.findById(body.getInteger("id"));
                if (!wareOp.isPresent()) {
                    return ApiResponse.error("仓库不存在");
                }
                Warehouse warehouse = wareOp.get();
                warehouse.setName(body.getString("name"));
                warehouse.setLocation(Location.objectify(body.getJSONObject("location").toString()));
                warehouse.setWorktimeConfig(WorktimeConfig.objectify(body.getJSONObject("worktimeConfig").toString()));
                warehouse.setWorkloadConfig(WorkloadConfig.objectify(body.getJSONObject("workloadConfig").toString()));
                warehouse.setDescription(body.getString("description"));
                warehouse = warehouseService.saveWarehouse(warehouse);
                ret.put("id", warehouse.getId());
                return ApiResponse.ok(ret);
            }
        } else {
            return ApiResponse.error("请先登录");
        }
    }

}
