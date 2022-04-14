package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.model.util.Cargo;
import com.tencent.wxcloudrun.model.util.OrderStatus;
import com.tencent.wxcloudrun.model.util.UserRole;
import com.tencent.wxcloudrun.service.OrderService;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class OrderController {

    final private OrderService orderService;
    final private UserService userService;
    final private WarehouseService warehouseService;
    public OrderController(@Autowired OrderService orderService, @Autowired UserService userService,@Autowired WarehouseService warehouseService) {
        this.orderService = orderService;
        this.userService = userService;
        this.warehouseService = warehouseService;
    }


    @GetMapping("/list")
    public ApiResponse orderList(@RequestParam(name = "session") String session, @RequestParam(name="date",required = false) String dateStr, @RequestParam(name="status",required = false) int stat){
        Optional<LocalDateTime> date = Optional.empty();
        if(!( dateStr  == null || dateStr.length() == 0)){
           date = Optional.of(LocalDateTime.parse(dateStr));
       }
        Optional<OrderStatus> status = Optional.empty();
        if(stat != -1)
            status = Optional.of(OrderStatus.from(stat));

        Optional<Session> sop = userService.isUserLoggedIn(session);
        if(!sop.isPresent()){
            return ApiResponse.error("请先登录");
        }
        Iterable<Order> orders = orderService.getListByParams(date, status,sop.get().getUser());
        List<JSONObject >ret = new ArrayList<>();
        for (Order order:
             orders) {
            JSONObject object = new JSONObject();
            object.put("id",order.getId());
            object.put("senderName",order.getSenderName());
            object.put("senderPhone",order.getSenderPhone());
            object.put("warehouseId",order.getTargetWarehouse().getId());
            JSONObject warehouse =  new JSONObject();
            warehouse.put("location",order.getTargetWarehouse().getLocation());
            warehouse.put("name",order.getTargetWarehouse().getName());
            warehouse.put("id",order.getTargetWarehouse().getId());
            warehouse.put("description",order.getTargetWarehouse().getDescription());
            object.put("warehouse",warehouse);
            object.put("receiverId",order.getReceiverId());
            object.put("creationDate",order.getCreationDate());
            object.put("option",order.getOption());
            object.put("targetTime",order.getTargetTime());
            object.put("targetEndTime",order.getTargetTime().plusMinutes(order.getTargetWarehouse().getWorktimeConfig().getInterval()));
            object.put("status",order.getStatus().value);
            object.put("note",order.getNote());
            ret.add(object);
        }
        return ApiResponse.ok(ret);
    }


    @GetMapping("/detail")
    public ApiResponse detailOrder(@RequestParam(name = "session") String session, @RequestParam(name = "id") Integer id){
        Optional<Session> s = userService.isUserLoggedIn(session);
        if(!s.isPresent()){
            return ApiResponse.error("请先登录");
        }
        Optional<Order> ord = orderService.getById(id);
        if(!ord.isPresent()){
            return ApiResponse.error("订单不存在");
        }
        Order order = ord.get();
        JSONObject object = new JSONObject();
        object.put("id",order.getId());
        object.put("senderName",order.getSenderName());
        object.put("senderPhone",order.getSenderPhone());
        object.put("warehouseId",order.getTargetWarehouse().getId());
        JSONObject warehouse =  new JSONObject();
        warehouse.put("location",order.getTargetWarehouse().getLocation());
        warehouse.put("name",order.getTargetWarehouse().getName());
        warehouse.put("id",order.getTargetWarehouse().getId());
        warehouse.put("description",order.getTargetWarehouse().getDescription());
        object.put("warehouse",warehouse);
        object.put("cargos",order.getCargos());
        object.put("receiverId",order.getReceiverId());
        object.put("creationDate",order.getCreationDate());
        object.put("option",order.getOption());
        object.put("canEdit",canEdit(s.get(),order));
        object.put("targetTime",order.getTargetTime());
        object.put("targetEndTime",order.getTargetTime().plusMinutes(order.getTargetWarehouse().getWorktimeConfig().getInterval()));
        object.put("status",order.getStatus().value);
        object.put("note",order.getNote());
        return ApiResponse.ok(object);
    }

    boolean canEdit(Session s, Order order){
        return (
                        (  order.getCreator().getWxUnionId().equals(s.getUser().getWxUnionId())
                                &&  order.getStatus() == OrderStatus.created)
                        |
                        ( (s.getUser().getRole() == UserRole.warehouse_manager ||s.getUser().getRole() == UserRole.warehouse_worker )
                                && order.getTargetWarehouse().getId() == s.getUser().getWarehouse().getId() )
                        | s.getUser().getRole() == UserRole.platform_manager  );

    }

    @PostMapping("/create")
    public ApiResponse createOrder(@RequestParam(name = "session") String session, @RequestBody JSONObject body){
        Optional<Session> s = userService.isUserLoggedIn(session);
        if(!s.isPresent()){
            return ApiResponse.error("请先登录");
        }
        User creator = s.get().getUser();

//        if(creator.getRole() != UserRole.driver || creator.getRole() != UserRole.user){
//            return ApiResponse.error("登录用户身份错误!");
//        }
        Order order = new Order();

        order.setCreator(creator);  // 1
        return setOrderData(body, order,creator);
    }

    @PostMapping("/edit")
    public ApiResponse editOrder(@RequestParam(name = "session") String session, @RequestBody JSONObject body){
        Optional<Session> sop =  userService.isUserLoggedIn(session);
        if(!sop.isPresent()){
            return ApiResponse.error("请先登录");
        }


        Optional<Order> ord = orderService.getById(body.getIntValue("id"));
        if(!ord.isPresent()){
            return ApiResponse.error("订单不存在");
        }
        Order order = ord.get();
//        Optional<Warehouse> wh = warehouseService.findById(body.getIntValue("id"));
        switch(sop.get().getUser().getRole()){
            case user:
            case driver:
                if(sop.get().getUser().getId() != order.getCreator().getId())
                    return ApiResponse.error("没有权限：不是您创建的订单");
                if(order.getStatus() != OrderStatus.created){
                    return ApiResponse.error("没有权限，管理员已锁定订单");
                }
                break;
            case warehouse_manager:
            case warehouse_worker:
                if(sop.get().getUser().getWarehouse().getId() != order.getTargetWarehouse().getId()){
                    return ApiResponse.error("没有权限:不是您仓库的订单");
                }
            case platform_manager:
                break;
            default: return ApiResponse.error("没有权限!");
        }

        return setOrderData(body, order,sop.get().getUser());
    }

    private ApiResponse setOrderData(@RequestBody JSONObject body, Order order,User user) {
        order.setSenderName(body.getString("senderName")); // 2
        order.setSenderPhone(body.getString("senderPhone")); // 3
        order.setReceiverId(body.getString("receiverId")); // 4
        List<JSONObject> rawList = body.getJSONArray("cargos").toJavaList(JSONObject.class);
        List<Cargo> list = new ArrayList<Cargo>();
        for (JSONObject jsonObject : rawList) {
            list.add( Cargo.objectify(jsonObject.toString()));
        }
        order.setCargos(list); // 5

        Optional<Warehouse> target = warehouseService.getById(body.getIntValue("warehouseId"));
        if(!target.isPresent()){
            return ApiResponse.error("仓库不存在");
        }
        order.setTargetWarehouse(target.get()); // 6
        order.setNote(body.getString("note"));
        int newOption = (body.getIntValue("option")); // 7
        if(newOption == 0) {
            if (order.getId() == 0 || order.getOption() == 1) {
                order.setOption(newOption);
                order.setTargetTime(warehouseService.pickTimeFor(order));
            }
        }
        else {
            order.setOption(newOption);
            order.setTargetTime(LocalDateTime.parse(body.getString("targetTime").replace(" ","T")));
        }
        if(order.getId() == 0){
            order.setStatus(OrderStatus.created);
        }
        else if(user.getRole().value > 2){
            order.setStatus(OrderStatus.locked);
        }
        order = orderService.saveOrder(order);
        JSONObject ret = new JSONObject();
        ret.put("orderId",order.getId());
        return ApiResponse.ok(ret);
    }

    @PostMapping("/status/edit")
    public ApiResponse switchStatus(@RequestParam(name = "session") String session, @RequestBody JSONObject body){
        Optional<Session> sop =  userService.isUserLoggedIn(session);
        if(!sop.isPresent()){
            return ApiResponse.error("请先登录");
        }
        Optional<Order> ord = orderService.getById(body.getIntValue("orderId"));
        if(!ord.isPresent()){
            return ApiResponse.error("订单不存在");
        }
        OrderStatus newStatus = OrderStatus.from(body.getIntValue("newStatus"));
        Order order = ord.get();
        User user = sop.get().getUser();
        switch (user.getRole()){
            case user:
            case driver:
                if(order.getStatus() != OrderStatus.created && newStatus!= OrderStatus.canceled ){
                    return ApiResponse.error("没有权限进行操作!");
                }
                break;
            case warehouse_manager:
            case warehouse_worker:
               if(order.getTargetWarehouse().getId() != user.getWarehouse().getId()){
                   return ApiResponse.error("没有权限，不是您仓库的订单");
               }
               break;
            case platform_manager:
                break;
            default:return ApiResponse.error("没有权限");
        }
        order.setStatus(newStatus);
        orderService.saveOrder(order);
        return ApiResponse.ok();
    }

}
