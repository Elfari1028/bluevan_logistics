package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.Warehouse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepo extends CrudRepository<Warehouse,Integer> {
    Optional<Warehouse> findByIdAndDeletedFalse(Integer integer);
    List<Warehouse> findAllByDeletedFalse();
}
