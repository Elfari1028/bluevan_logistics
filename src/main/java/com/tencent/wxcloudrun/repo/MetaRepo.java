package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.MetaConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetaRepo extends CrudRepository<MetaConfig,Integer> {
    Optional<MetaConfig> findByFieldName(String fieldName);
}
