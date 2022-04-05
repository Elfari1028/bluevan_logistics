package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.repo.UserRepo;
import com.tencent.wxcloudrun.repo.WarehouseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Service
public class WarehouseService {
    private UserRepo userRepo;
    private WarehouseRepo warehouseRepo;

    public WarehouseService(@Autowired UserRepo userRepo,@Autowired WarehouseRepo warehouseRepo){
        this.userRepo = userRepo;
        this.warehouseRepo = warehouseRepo;
    }

    public Warehouse saveWarehouse(Warehouse warehouse) {
        return this.warehouseRepo.save(warehouse);
    }

    public Iterable<Warehouse> getAll(){
       return this.warehouseRepo.findAll();
    }

    public Optional<Warehouse> findById(int id){
        return this.warehouseRepo.findById(id);
    }
}
