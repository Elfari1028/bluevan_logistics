package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.WxCloudRunApplication;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.L;
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
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/user")
public class UserController {

    final UserService userService;
    final WarehouseService warehouseService;

    public UserController(@Autowired UserService userService, @Autowired WarehouseService warehouseService) {
        this.userService = userService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("/code2session")
    public ApiResponse code2session(@RequestParam(name = "code") String code) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://api.weixin.qq.com/sns/jscode2session?appid=wx3fb342c39ddf2cd7&secret=20e3c200fdaf433818d44c1434714988&js_code=" + code + "&grant_type=authorization_code");
            ObjectMapper mapper = new ObjectMapper();
            HashMap response = client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), HashMap.class));
            return ApiResponse.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error("error");
        }
    }

    @PostMapping("/register")
    public ApiResponse register(@RequestBody JSONObject body) {
        JSONObject data = new JSONObject();
        Optional<User> user = userService.register(
                body.getString("phone"),
                body.getString("name"),
                body.getString("wxName"),
                body.getString("wxUserId"),
                body.getString("wxUnionId"),
                body.getString("wxAvatarUrl"));
        if (user.isPresent()) {
            Optional<Session> session = userService.login(user.get());
            if (session.isPresent() && session.get().getUser() != null) {
                data.put("sessionKey", session.get().getSessionKey());
                data.put("name", session.get().getUser().getName());
                data.put("wxAvatarUrl", session.get().getUser().getWxAvatarUrl());
                data.put("role", session.get().getUser().getRole().value);
                User u = session.get().getUser();
                if (u.getWarehouse() != null) {
                    JSONObject warehouse = new JSONObject();
                    warehouse.put("name", u.getWarehouse().getName());
                    warehouse.put("description", u.getWarehouse().getDescription());
                    warehouse.put("id", u.getWarehouse().getId());
                    warehouse.put("location", u.getWarehouse().getLocation().jsonObjectify());
                    data.put("warehouse", warehouse);
                }
                return ApiResponse.ok(data);
            }
            return ApiResponse.error("注册失败,请稍后重试");
        }
        return ApiResponse.error("注册失败,请稍后重试");
    }

    @PostMapping("/login")
    public ApiResponse login(@RequestBody JSONObject body) {
        JSONObject data = new JSONObject();
        String userId = body.getString("wxUnionId");
        L.info("/login  userId" + userId);
        if (userService.doesUserExists(userId)) {
            Optional<User> user = userService.getUserByWxUnionId(userId);
            if (user.isPresent()) {
                Optional<Session> session = userService.login(user.get());
                if (session.isPresent() && session.get().getUser() != null) {
                    data.put("sessionKey", session.get().getSessionKey());
                    data.put("name", session.get().getUser().getName());
                    data.put("wxUnionId", session.get().getUser().getWxUnionId());
                    data.put("role", session.get().getUser().getRole().value);
                    data.put("wxAvatarUrl", session.get().getUser().getWxAvatarUrl());
                    User u = session.get().getUser();
                    if (u.getWarehouse() != null) {
                        JSONObject warehouse = new JSONObject();
                        warehouse.put("name", u.getWarehouse().getName());
                        warehouse.put("description", u.getWarehouse().getDescription());
                        warehouse.put("id", u.getWarehouse().getId());
                        warehouse.put("location", u.getWarehouse().getLocation().jsonObjectify());
                        data.put("warehouse", warehouse);
                    }
                    return ApiResponse.ok(data);
                }
                return ApiResponse.error("登录失败,请稍后重试");
            }
        }
        return ApiResponse.error("用户不存在,请稍后重试");
    }

    @PostMapping("/edit")
    public ApiResponse edit(@RequestParam(name = "session") String session, @RequestBody JSONObject body) {
        JSONObject data = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);
        if (s.isPresent() && s.get().getUser() != null) {
            User u = s.get().getUser();
            if (u.getRole() != UserRole.platform_manager) {
                return ApiResponse.error("没有权限");
            } else {
                String unionId = body.getString("wxUnionId");
                Optional<User> target = userService.getUserByWxUnionId(unionId);
                if (target.isPresent()) {
                    User user = target.get();
                    if (body.containsKey("phone")) {
                        user.setPhone(body.getString("phone"));
                    }
                    if (body.containsKey("role")) {
                        user.setRole(UserRole.of(body.getIntValue("role")));
                        if (user.getRole() == UserRole.warehouse_manager || user.getRole() == UserRole.warehouse_worker) {
                            Optional<Warehouse> wh = warehouseService.findById(body.getIntValue("warehouseId"));
                            if (!wh.isPresent()) {
                                return ApiResponse.error("仓库不存在");
                            }
                            user.setWarehouse(wh.get());
                        }
                    }
                    if (body.containsKey("name")) {
                        user.setName(body.getString("name"));
                    }
                    userService.saveEditedUser(user);
                    return ApiResponse.ok(data);
                } else {
                    return ApiResponse.error("用户不存在!");
                }
            }
        } else {
            return ApiResponse.error("登录状态错误,请重新登录");
        }
    }

    @PostMapping("/role/edit")
    public ApiResponse editRole(@RequestParam(name = "session") String session, @RequestBody JSONObject body) {
        JSONObject data = new JSONObject();
        Optional<Session> s = userService.isUserLoggedIn(session);
        if (s.isPresent() && s.get().getUser() != null) {
            User u = s.get().getUser();
            if (u.getRole() != UserRole.platform_manager) {
                return ApiResponse.error("没有权限");
            } else {
                String unionId = body.getString("wxUnionId");
                Optional<User> target = userService.getUserByWxUnionId(unionId);
                if (target.isPresent()) {
                    User user = target.get();
                    user.setRole(UserRole.of(body.getIntValue("role")));
                    if (user.getRole() == UserRole.warehouse_manager || user.getRole() == UserRole.warehouse_worker) {
                        Optional<Warehouse> wh = warehouseService.findById(body.getIntValue("warehouseId"));
                        if (!wh.isPresent()) {
                            return ApiResponse.error("仓库不存在");
                        }
                        user.setWarehouse(wh.get());
                    }
                    userService.saveEditedUser(user);
                    return ApiResponse.ok(data);
                } else {
                    return ApiResponse.error("用户不存在!");
                }
            }
        } else {
            return ApiResponse.error("登录状态错误,请重新登录");
        }
    }

    @GetMapping("/list")
    public ApiResponse listAll(
            @RequestParam(name = "session") String session,
            @RequestParam(required = false,name = "role") int role,
            @RequestParam(required = false,name = "page") Integer page,
            @RequestParam(required = false,name = "pageSize",defaultValue = "20") int pageSize,
            @RequestParam(required = false, name = "keyword") String keyword
    ) {
        Optional<Session> s = userService.isUserLoggedIn(session);
        JSONObject ret= new JSONObject();
        if (!s.isPresent()) {
            return ApiResponse.error("请先登录");
        }
        if(keyword == null)keyword = "";
        L.info("keyword:"+keyword);
        if(page == null || page < 0 ){
            Iterable<User> list = null;
            if(role == 0){
                list = userService.userRepo.findAllByWxNameContaining(keyword);
            }
            else{
                list = userService.userRepo.findAllByWxNameContainingAndRole(keyword,UserRole.of(role).value);
            }

            return ApiResponse.ok(userService.usersToJsonArray(list));
        }
        else {
            Page result;
            if(role == 0){
              result = userService.userRepo.findAllByWxNameContaining(keyword,PageRequest.of(page,pageSize));
            }
            else {
                result = userService.userRepo.findAllByWxNameContainingAndRole(keyword, UserRole.of(role).value,PageRequest.of(page,pageSize));
            }
            L.info("page: " +page+ "  total:" + result.getTotalPages());
            return ApiResponse.ok(userService.usersToJsonArray(result)).paged(result.getPageable().getPageSize(), result.getPageable().getPageNumber(),result.getTotalPages());
        }

    }
}


