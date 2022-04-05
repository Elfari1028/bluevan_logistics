package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.util.GlobalMetaFields;
import com.tencent.wxcloudrun.model.util.UserRole;
import com.tencent.wxcloudrun.service.MetaService;
import com.tencent.wxcloudrun.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/meta")
public class MetaController {

    private UserService userService;

    private MetaService metaService;
    public MetaController(@Autowired UserService userService,@Autowired MetaService metaService){
        this.userService = userService;
        this.metaService = metaService;
    }
    @PostMapping("/set")
    public ApiResponse setField(@RequestParam(name="session")String session,@RequestBody JSONObject body){
        Optional<Session> s = userService.isUserLoggedIn(session);
        if(s.isPresent()){
            User user  = s.get().getUser();
            if(user.getRole() != UserRole.platform_manager){
                return ApiResponse.error("没有权限");
            }
            else {
                metaService.set(GlobalMetaFields.of(body.getString("field")),body.getString("value"));
                return ApiResponse.ok();
            }
        } else {
            return ApiResponse.error("请先登录");
        }
    }

    @GetMapping("/get")
    public ApiResponse getField(@RequestParam(name="session",required = false)String session,@RequestParam(name="field") String field){
        JSONObject data = new JSONObject();
        switch (field){
            case "banlist":data.put("result", metaService.getBanlist());break;
            case "cargoType":data.put("result",metaService.getCargoTypes());break;
            case "packageType":data.put("result",metaService.getPackageTypes());break;
            default:break;
        }
        return ApiResponse.ok(data);
    }
}
