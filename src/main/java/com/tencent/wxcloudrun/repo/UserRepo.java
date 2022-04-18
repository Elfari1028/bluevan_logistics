package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {
    Optional<User> findByWxUnionId(String wxUnionId);
    Page<User> findAllByRole(int role, Pageable pageable);
    List<User> findAllByRole(int role);

    List<User> findAllByWxNameContainingAndRole(String name,int role);
    Page<User> findAllByWxNameContainingAndRole(String name,int role,Pageable pageable);

    List<User> findAllByWxNameContaining(String name);
    Page<User> findAllByWxNameContaining(String name, Pageable pageable);
}
