package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepo extends CrudRepository<Order,Integer> {

    List<Order> findAllByStatusEqualsAndCreationDateBetween(int status, LocalDateTime begin, LocalDateTime end);

    List<Order> findAllByCreatorAndStatusEqualsAndCreationDateBetween(User creator, int status, LocalDateTime begin, LocalDateTime end);

    List<Order> findAllByTargetWarehouseAndStatusEqualsAndCreationDateBetween(Warehouse warehouse, int status, LocalDateTime begin, LocalDateTime end);

    List<Order> findAllByStatusEquals(int status);

    List<Order> findAllByCreatorAndStatusEquals(User creator, int status);

    List<Order> findAllByTargetWarehouseAndStatusEquals(Warehouse warehouse, int status);

    List<Order> findAllByCreationDateBetween(LocalDateTime begin, LocalDateTime end);

    List<Order> findAllByCreatorAndCreationDateBetween(User creator, LocalDateTime begin, LocalDateTime end);

    List<Order> findAllByTargetWarehouseAndCreationDateBetween(Warehouse warehouse,  LocalDateTime begin, LocalDateTime end);


    List<Order> findAllByCreator(User creator);

    List<Order> findAllByTargetWarehouse(Warehouse warehouse);

    List<Order> findAllByTargetWarehouseAndTargetTimeBetween(Warehouse warehouse,LocalDateTime begin,LocalDateTime end);

}
