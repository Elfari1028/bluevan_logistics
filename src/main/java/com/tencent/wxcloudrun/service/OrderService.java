package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.util.OrderStatus;
import com.tencent.wxcloudrun.repo.OrderRepo;
import com.tencent.wxcloudrun.repo.UserRepo;
import com.tencent.wxcloudrun.repo.WarehouseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class OrderService {
    final private UserRepo userRepo;
    final private WarehouseRepo warehouseRepo;
    final private OrderRepo orderRepo;
    public OrderService(@Autowired UserRepo userRepo, @Autowired WarehouseRepo warehouseRepo,@Autowired OrderRepo orderRepo) {
        this.userRepo = userRepo;
        this.warehouseRepo = warehouseRepo;
        this.orderRepo = orderRepo;
    }

    public Iterable<Order> getListByParams(Optional<LocalDateTime> date, Optional<OrderStatus> status, User user) {
        if(date.isPresent()){
            LocalDateTime end = date.get().plusDays(1);
            if(status.isPresent()){
                // both
                switch (user.getRole()){
                    case user:
                    case driver:
                        return orderRepo.findAllByCreatorAndStatusEqualsAndCreationDateBetween(user,status.get().value,date.get(),end);
                    case warehouse_manager:
                    case warehouse_worker:
                        return orderRepo.findAllByTargetWarehouseAndStatusEqualsAndCreationDateBetween(user.getWarehouse(),status.get().value,date.get(),end);
                    case platform_manager:
                        return orderRepo.findAllByStatusEqualsAndCreationDateBetween(status.get().value,date.get(),end);
                    default:
                        return null;
                }
            }
            else {
              // only date
                switch (user.getRole()){
                    case user:
                    case driver:
                        return orderRepo.findAllByCreatorAndCreationDateBetween(user,date.get(),end);
                    case platform_manager:
                        return orderRepo.findAllByCreationDateBetween(date.get(),end);
                    case warehouse_worker:
                    case warehouse_manager:
                        return orderRepo.findAllByTargetWarehouseAndCreationDateBetween(user.getWarehouse(),date.get(),end);
                    default:
                        return null;
                }
            }
        }
        if(status.isPresent()){
            // only status
            switch (user.getRole()){
                case user:
                case driver:
                    return orderRepo.findAllByCreatorAndStatusEquals(user,status.get().value);
                case warehouse_manager:
                case warehouse_worker:
                    return orderRepo.findAllByTargetWarehouseAndStatusEquals(user.getWarehouse(), status.get().value);
                case platform_manager:
                    return orderRepo.findAllByStatusEquals(status.get().value);
                default:
                    return null;
            }
        }
        else {
            // none
            switch (user.getRole()){
                case user:
                case driver:
                    return orderRepo.findAllByCreator(user);
                case warehouse_manager:
                case warehouse_worker:
                    return orderRepo.findAllByTargetWarehouse(user.getWarehouse());
                case platform_manager:
                    return orderRepo.findAll();
                default:return null;
            }
        }
    }

    public Order saveOrder(Order order) {
       if(order.getId() == 0){
           order.setCreationDate(LocalDateTime.now());
       }
       else {
           order.setLastModifiedDate(LocalDateTime.now());
       }
        return orderRepo.save(order);
    }

    public Optional<Order> getById(int id){
        return orderRepo.findById(id);
    }
}
