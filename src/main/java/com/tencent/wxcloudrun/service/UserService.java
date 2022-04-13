package com.tencent.wxcloudrun.service;

import com.google.common.collect.Lists;
import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.util.UserRole;
import com.tencent.wxcloudrun.repo.SessionRepo;
import com.tencent.wxcloudrun.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private UserRepo userRepo;

    private SessionRepo sessionRepo;


    public UserService(@Autowired UserRepo userRepo, @Autowired SessionRepo sessionRepo) {
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
    }


    public boolean doesUserExists(String wxUnionId) {
        return userRepo.findByWxUnionId(wxUnionId).isPresent();
    }

    public Optional<User> register(String phone, String name, String wxName, String wxAppId, String wxUnionId, String wxAvatarUrl) {
        if (doesUserExists(wxUnionId)) {
            return Optional.empty();
        }
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setWxName(wxName);
        user.setWxUserId(wxAppId);
        user.setWxUnionId(wxUnionId);
        user.setRole(UserRole.user);
        user.setWxAvatarUrl(wxAvatarUrl);
        userRepo.save(user);
        return Optional.of(user);
    }

    public Optional<Session> login(User user) {
        if (!doesUserExists(user.getWxUserId())) {
            return Optional.empty();
        }
        Optional<Session> s = isUserLoggedIn(user.getWxUnionId());
        if (s.isPresent()) {
            return s;
        }
        Session session = new Session();
        session.setSessionKey(UUID.randomUUID().toString());
        session.setLastActiveDate(LocalDateTime.now());
        session.setUser(user);
        sessionRepo.save(session);
        return Optional.of(session);
    }

    public Optional<Session> isUserLoggedIn(String sessionKey) {
        Optional<Session> s = sessionRepo.findBySessionKey(sessionKey);
        if (s.isPresent()) {
            Session session = s.get();
            if (session.getUser() != null && session.getLastActiveDate().isAfter(LocalDateTime.now().minusDays(7))) {
                session.setLastActiveDate(LocalDateTime.now());
                sessionRepo.save(session);
                return Optional.of(session);
            } else {
                sessionRepo.delete(session);
                return Optional.empty();
            }
        } else {
            L.info("session "+sessionKey+" is not present");
            return Optional.empty();
        }
    }

    public Optional<User> getUserByWxUnionId(String id) {
        return userRepo.findByWxUnionId(id);
    }

    public Iterable<User> getAllUsers() {
        return userRepo.findAll();
//        return Lists.newArrayList(userRepo.findAll());
    }

    public void saveEditedUser(User user){
        userRepo.save(user);
    }
}
