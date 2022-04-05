package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<User,Integer> {
    Optional<User> findByWxUnionId(String wxUnionId);
}
