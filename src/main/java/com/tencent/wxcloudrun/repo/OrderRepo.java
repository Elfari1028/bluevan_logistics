package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepo extends CrudRepository<Order,Integer> {

    List<Order> findAllByStatusEqualsAndCreationDateBetween(int status, Date begin, Date end);

    List<Order> findAllByCreatorAndStatusEqualsAndCreationDateBetween(User creator, int status, Date begin, Date end);

    List<Order> findAllByTargetWarehouseAndStatusEqualsAndCreationDateBetween(Warehouse warehouse, int status, Date begin, Date end);

    List<Order> findAllByStatusEquals(int status);

    List<Order> findAllByCreatorAndStatusEquals(User creator, int status);

    List<Order> findAllByTargetWarehouseAndStatusEquals(Warehouse warehouse, int status);

    List<Order> findAllByCreationDateBetween(Date begin, Date end);

    List<Order> findAllByCreatorAndCreationDateBetween(User creator, Date begin, Date end);

    List<Order> findAllByTargetWarehouseAndCreationDateBetween(Warehouse warehouse,  Date begin, Date end);


    List<Order> findAllByCreator(User creator);

    List<Order> findAllByTargetWarehouse(Warehouse warehouse);


}
