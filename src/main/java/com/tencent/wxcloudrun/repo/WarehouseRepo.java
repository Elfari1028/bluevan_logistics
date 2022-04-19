package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.Warehouse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepo extends CrudRepository<Warehouse,Integer> {

}
