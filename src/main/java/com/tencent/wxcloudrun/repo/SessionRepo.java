package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepo extends CrudRepository<Session,Integer> {
    Optional<Session> findBySessionKeyAndCreationDateBefore(String sessionKey, Date date);
    List<Session> findByUser(User user);
    void deleteById(int id);
    Optional<Session> findBySessionKey(String sessionKey);
}
