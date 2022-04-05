package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.model.util.UserRole;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.service.WarehouseService;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    final UserService userService;
    final WarehouseService warehouseService;

    public UserController(@Autowired UserService userService,@Autowired WarehouseService warehouseService) {
        this.userService = userService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/code2session")
    public ApiResponse code2session(@RequestParam( name="code") String code){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://api.weixin.qq.com/sns/jscode2session?appid=wx4a6892851b35a0ed&secret=0e288d74d5d6d0327167925742782465&js_code=" + code + "&grant_type=authorization_code");
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
    @PostMapping("/register")
    public ApiResponse register(@RequestBody JSONObject body){
        JSONObject data = new JSONObject();
       Optional<User> user = userService.register(
                body.getString("phone"),
                body.getString("name"),
                body.getString("wxName"),
                body.getString("wxUserId"),
                body.getString("wxUnionId"),
                body.getString("wxAvatarUrl"));
        if(user.isPresent()){
           Optional<Session> session =  userService.login(user.get());
           if(session.isPresent()&& session.get().getUser()!=null){
               data.put("sessionKey",session.get().getSessionKey());
               data.put("name",session.get().getUser().getName());
               data.put("wxUnionId",session.get().getUser().getWxUnionId());
               data.put("wxAvatarUrl",session.get().getUser().getWxAvatarUrl());
               return ApiResponse.ok(data);
           }
           return ApiResponse.error("注册失败,请稍后重试");
        }
        return ApiResponse.error("注册失败,请稍后重试");
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody JSONObject body){
        JSONObject data = new JSONObject();
        if( userService.doesUserExists(body.getString("wxUnionId"))){
            Optional<User> user = userService.getUserByWxUnionId("wxUnionId");
            if(user.isPresent()){
                Optional<Session> session =  userService.login(user.get());
                if(session.isPresent() && session.get().getUser()!=null){
                    data.put("sessionKey",session.get().getSessionKey());
                    data.put("name",session.get().getUser().getName());
                    data.put("wxUnionId",session.get().getUser().getWxUnionId());
                    data.put("wxAvatarUrl",session.get().getUser().getWxAvatarUrl());
                    return ApiResponse.ok(data);
                }
                return ApiResponse.error("登录失败,请稍后重试");
            }
        }
        return ApiResponse.error("用户不存在,请稍后重试");
    }

    @PostMapping("/edit")
    public ApiResponse edit(@RequestParam(name = "session") String session, @RequestBody JSONObject body){
        JSONObject data = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);
        if(s.isPresent() && s.get().getUser()!=null){
            User u = s.get().getUser();
            if(u.getRole() != UserRole.platform_manager){
                return ApiResponse.error("没有权限");
            }
            else {
                String unionId = body.getString("wxUnionId");
                Optional<User> target = userService.getUserByWxUnionId(unionId);
                if(target.isPresent()){
                    User user = target.get();
                    if(body.containsKey("phone")){
                        user.setPhone(body.getString("phone"));
                    }
                    if(body.containsKey("role")){
                        user.setRole(UserRole.of(body.getIntValue("role")));
                        if(user.getRole() == UserRole.warehouse_manager || user.getRole() == UserRole.warehouse_worker){
                            Optional<Warehouse> wh =  warehouseService.findById(body.getIntValue("warehouseId"));
                            if(!wh.isPresent()){
                                return ApiResponse.error("仓库不存在");
                            }
                            user.setWarehouse(wh.get());
                        }
                    }
                    if(body.containsKey("name")){
                        user.setName(body.getString("name"));
                    }
                    userService.saveEditedUser(user);
                    return ApiResponse.ok(data);
                }
                else {
                    return ApiResponse.error("用户不存在!");
                }
            }
        }
        else {
            return ApiResponse.error("登录状态错误,请重新登录");
        }
    }

    @GetMapping("/list")
    public ApiResponse listAll(@RequestParam(name = "session") String session){
        Optional<Session> s =  userService.isUserLoggedIn(session);
        if(!s.isPresent()){
            return ApiResponse.error("请先登录");
        }
        JSONArray data = new JSONArray();
        Iterable<User> list = userService.getAllUsers();
        for (User u:list) {
            JSONObject user = new JSONObject();
            user.put("phone",u.getPhone());
            user.put("wxUnionId",u.getWxUnionId());
            user.put("name",u.getName());
            user.put("role",u.getRole());
            if(u.getWarehouse() != null){
                JSONObject warehouse = new JSONObject();
                warehouse.put("name",u.getWarehouse().getName());
                warehouse.put("description",u.getWarehouse().getDescription());
                warehouse.put("id",u.getWarehouse().getId());
                warehouse.put("location",u.getWarehouse().getLocation().jsonObjectify());
                user.put("warehouse",warehouse);
            }
            data.add(user);
        }
        return ApiResponse.ok(data);
    }
}


