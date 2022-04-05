package com.tencent.wxcloudrun.repo;

import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface SessionRepo extends CrudRepository<Session,Integer> {
    Optional<Session> findBySessionKeyAndCreationDateBefore(String sessionKey, Date date);
    Optional<Session> findByUser(User user);

    Optional<Session> findBySessionKey(String sessionKey);
}
