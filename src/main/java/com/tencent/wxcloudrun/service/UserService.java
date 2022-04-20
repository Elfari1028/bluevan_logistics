package com.tencent.wxcloudrun.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.util.OrderStatus;
import com.tencent.wxcloudrun.model.util.UserRole;
import com.tencent.wxcloudrun.repo.SessionRepo;
import com.tencent.wxcloudrun.repo.UserRepo;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
public class UserService {

    public UserRepo userRepo;

    private SessionRepo sessionRepo;

    public final String appId ="wx3fb342c39ddf2cd7";
    public final String secret = "20e3c200fdaf433818d44c1434714988";
    public final String msgCreationNoticeTemplate = "nRK8CvemTqeSb5VLGaPdciHZl0rLBgF3BqxsQ251HPA";
    public final String msgStatusNoticeTemplate = "wTaj_TAHqRewG-VzGx7k1d4KXq-TnJ1lbVF3WTu7W2E";
    public final String msgReceiveNoticeTemplate = "skpDKmBtroUTHbWNDpMt_l8cUiMD_t23c7rnKCnwELE";


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

        Optional<Session> s = findValidSessionForUser(user);
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

    public Optional<Session> findValidSessionForUser(User u){
        List<Session> slist = sessionRepo.findByUser(u);
        Session s = null;
        if(slist.size() > 1){
            for (int i = 0 ; i < slist.size() - 1; i ++) {
                sessionRepo.delete(slist.get(i));
            }
            s = slist.get(slist.size() - 1);
        }
       else if(slist.size() == 1){
          s = slist.get(0);
       }
        if (s!=null) {
            Session session = s;
            if (session.getLastActiveDate().isAfter(LocalDateTime.now().minusDays(7))) {
                session.setLastActiveDate(LocalDateTime.now());
                sessionRepo.save(session);
                return Optional.of(session);
            } else {
                sessionRepo.delete(session);
                return Optional.empty();
            }
        } else {
            L.info("session is not present for user "+u.getId());
            return Optional.empty();
        }
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

    public void saveEditedUser(User user){
        userRepo.save(user);
    }

    public Iterable<User> getAllUsersOfRole(UserRole role) {
       return userRepo.findAllByRole(role.value);
    }

    public JSONArray usersToJsonArray(Iterable<User> users){
        JSONArray userList = new JSONArray();
        for (User u : users) {
            JSONObject user = new JSONObject();
            user.put("phone", u.getPhone());
            user.put("avatar", u.getWxAvatarUrl());
            user.put("wxUnionId", u.getWxUnionId());
            user.put("name", u.getName());
            user.put("role", u.getRole().value);
            if (u.getWarehouse() != null) {
                JSONObject warehouse = new JSONObject();
                warehouse.put("name", u.getWarehouse().getName());
                warehouse.put("description", u.getWarehouse().getDescription());
                warehouse.put("id", u.getWarehouse().getId());
                warehouse.put("location", u.getWarehouse().getLocation().jsonObjectify());
                user.put("warehouse", warehouse);
            }
            userList.add(user);}
        return userList;
    }

    public void sendCreationMessage( Order order){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.weixin.qq.com/cgi-bin/message/subscribe/send");
            JSONObject body = new JSONObject();
            body.put("touser",order.getCreator().getWxUserId());
            body.put("template_id",msgCreationNoticeTemplate);
            JSONObject data = new JSONObject();
            JSONObject key = new JSONObject(); key.put("value",order.getId());
            data.put("character_string1",key);
            key = new JSONObject(); key.put("value",order.getReceiverId());
            data.put("thing4",key);
            key = new JSONObject(); key.put("value",order.getNote().length() > 19 ? order.getNote().substring(0,16) + "..." : order.getNote());
            data.put("thing6",key);
            key = new JSONObject(); key.put("value",order.getTargetWarehouse().getName());
            data.put("thing8",key);
            key = new JSONObject(); key.put("value",order.getTargetTime().toLocalDate().toString());
            data.put("time7",key);
            body.put("data",data);
            StringEntity requestEntity = new StringEntity(
                    body.toString(),
                    ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            ObjectMapper mapper = new ObjectMapper();
            HashMap response = client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), HashMap.class));
            L.info("sendCreationMsgResponse:"+response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendStatusChange(Order order){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.weixin.qq.com/cgi-bin/message/subscribe/send");
            JSONObject body = new JSONObject();
            body.put("touser",order.getCreator().getWxUserId());
            body.put("template_id",msgStatusNoticeTemplate);
            JSONObject data = new JSONObject();
            JSONObject key = new JSONObject(); key.put("value",order.getId());
            data.put("character_string1",key);
            OrderStatus status = order.getStatus();
        String phrase = null;
        String note = null;
        switch (status){
            case created:
                return;
            case canceled:
                phrase = "已取消";
                note = "取消后无法再开启，若需操作请重新下单";
                break;
            case locked:
                phrase = "已锁定";
                note = "订单已由管理员修改内容,请查看核实";
                break;
            case delivered:
                sendArrival(order);
                return ;
        }
            key = new JSONObject(); key.put("value",phrase);
            data.put("phrase2",key);
            key = new JSONObject(); key.put("value",note);
            data.put("thing3",key);
            body.put("data",data);
            StringEntity requestEntity = new StringEntity(
                    body.toString(),
                    ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            ObjectMapper mapper = new ObjectMapper();
            HashMap response = client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), HashMap.class));
            L.info("sendStatusChange:"+response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendArrival( Order order){
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("https://api.weixin.qq.com/cgi-bin/message/subscribe/send");
            JSONObject body = new JSONObject();
            body.put("touser",order.getCreator().getWxUserId());
            body.put("template_id",msgReceiveNoticeTemplate);
            JSONObject data = new JSONObject();
            JSONObject key = new JSONObject(); key.put("value",order.getId());
            data.put("character_string2",key);
            key = new JSONObject(); key.put("value",order.getReceiverId());
            data.put("thing4",key);
            String note = order.getNote();
            if(note.length() > 16) note = note.substring(0,16) + "...";
            key = new JSONObject(); key.put("value",note);
            data.put("thing6",key);
            String name = order.getCargos().get(0).getName();
            if(name.length() > 12){
                name = name.substring(0,12);
            }
            key = new JSONObject(); key.put("value","\"" +name +"\"等"+order.getCargos().size()+"种货物");
            data.put("thing7",key);
            key = new JSONObject(); key.put("value",order.getArrivalTime().toLocalDate().toString());
            data.put("time3",key);
            body.put("data",data);
            StringEntity requestEntity = new StringEntity(
                    body.toString(),
                    ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            ObjectMapper mapper = new ObjectMapper();
            HashMap response = client.execute(request, httpResponse ->
                    mapper.readValue(httpResponse.getEntity().getContent(), HashMap.class));
            L.info("sendArrival:"+response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

