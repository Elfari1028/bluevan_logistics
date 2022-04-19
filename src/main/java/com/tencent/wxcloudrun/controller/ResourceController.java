package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.service.OrderService;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/download")
public class ResourceController {
    final private OrderService orderService;
    final private UserService userService;
    final private WarehouseService warehouseService;

    public ResourceController(@Autowired OrderService orderService, @Autowired UserService userService, @Autowired WarehouseService warehouseService) {
        this.orderService = orderService;
        this.userService = userService;
        this.warehouseService = warehouseService;
    }

    /**
     * @RequestParam(name = "creationDate", required = false) String creationDate,
     * @RequestParam(name = "targetDate", required = false) String targetDate,
     * @RequestParam(name = "receiverId", required = false) String receiverId,
     * @RequestParam(name = "status", required = false, defaultValue = "-1") int stat,
     * @RequestParam(name = "page", required = false, defaultValue = "0") int page,
     * @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
     * @RequestParam(name = "warehouseId", required = false, defaultValue = "-1") Integer warehouseId,
     * @RequestParam(name = "orderId", required = false, defaultValue = "-1") Integer orderId
     */

    @GetMapping(path = "/orders")
    public ResponseEntity<Resource> download(@RequestParam(name = "code") String code) throws IOException {


//         ...
        byte[] decodedBytes = Base64.getDecoder().decode(code);
        String decodedString = new String(decodedBytes);
        JSONObject params = JSON.parseObject(decodedString);
        String creationDate = params.getString("creationDate");
        String targetDate = params.getString("targetDate");
        String receiverId = params.getString("receiverId");
        Integer stat = params.getInteger("status");
        if (stat == null) stat = -1;
        Integer warehouseId = params.getInteger("warehouseId");
        if (warehouseId == null) warehouseId = -1;
        Integer orderId = params.getInteger("orderId");
        if (orderId == null) orderId = -1;


        LocalDateTime creationDatetime = null;
        LocalDateTime targetDateTime = null;
        if (!(creationDate == null || creationDate.length() == 0)) {
            creationDatetime = LocalDateTime.parse(creationDate);
        }
        if (!(targetDate == null || targetDate.length() == 0)) {
            targetDateTime = LocalDateTime.parse(targetDate);
        }

        String sessionKey = params.getString("session");
        if (sessionKey == null) {
            return null;
        }
        Optional<Session> sop = userService.isUserLoggedIn(sessionKey);
        if (!sop.isPresent()) {
            return null;
        }
        Page<Order> page = orderService.queryOrders(warehouseId, receiverId, orderId, , stat, targetDateTime, creationDatetime, null);
        List<Order> orders = page.getContent();


//        ByteArrayResource resource = new ByteArrayResource();

//        return ResponseEntity.ok()
////                .headers(headers)
//                .contentLength(resource.length())
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
        return null;
    }
}
