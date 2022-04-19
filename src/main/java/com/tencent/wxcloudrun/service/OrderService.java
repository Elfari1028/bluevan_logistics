package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.model.util.OrderStatus;
import com.tencent.wxcloudrun.repo.OrderRepo;
import com.tencent.wxcloudrun.repo.UserRepo;
import com.tencent.wxcloudrun.repo.WarehouseRepo;
import org.hibernate.Session;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    final private UserRepo userRepo;
    final private WarehouseRepo warehouseRepo;
    final private OrderRepo orderRepo;
    @PersistenceContext
    private EntityManager em;

    public OrderService(@Autowired UserRepo userRepo, @Autowired WarehouseRepo warehouseRepo, @Autowired OrderRepo orderRepo) {
        this.userRepo = userRepo;
        this.warehouseRepo = warehouseRepo;
        this.orderRepo = orderRepo;
    }

    public Iterable<Order> getListByParams(Optional<LocalDateTime> date, Optional<OrderStatus> status, User user, Pageable pageable) {
        if (date.isPresent()) {
            LocalDateTime end = date.get().plusDays(1);
            if (status.isPresent()) {
                // both
                switch (user.getRole()) {
                    case user:
                    case driver:
                        return orderRepo.findAllByCreatorAndStatusEqualsAndCreationDateBetween(user, status.get().value, date.get(), end);
                    case warehouse_manager:
                    case warehouse_worker:
                        return orderRepo.findAllByTargetWarehouseAndStatusEqualsAndCreationDateBetween(user.getWarehouse(), status.get().value, date.get(), end);
                    case platform_manager:
                        return orderRepo.findAllByStatusEqualsAndCreationDateBetween(status.get().value, date.get(), end);
                    default:
                        return null;
                }
            } else {
                // only date
                switch (user.getRole()) {
                    case user:
                    case driver:
                        return orderRepo.findAllByCreatorAndCreationDateBetween(user, date.get(), end);
                    case platform_manager:
                        return orderRepo.findAllByCreationDateBetween(date.get(), end);
                    case warehouse_worker:
                    case warehouse_manager:
                        return orderRepo.findAllByTargetWarehouseAndCreationDateBetween(user.getWarehouse(), date.get(), end);
                    default:
                        return null;
                }
            }
        }
        if (status.isPresent()) {
            // only status
            switch (user.getRole()) {
                case user:
                case driver:
                    return orderRepo.findAllByCreatorAndStatusEquals(user, status.get().value);
                case warehouse_manager:
                case warehouse_worker:
                    return orderRepo.findAllByTargetWarehouseAndStatusEquals(user.getWarehouse(), status.get().value);
                case platform_manager:
                    return orderRepo.findAllByStatusEquals(status.get().value);
                default:
                    return null;
            }
        } else {
            // none
            switch (user.getRole()) {
                case user:
                case driver:
                    return orderRepo.findAllByCreator(user);
                case warehouse_manager:
                case warehouse_worker:
                    return orderRepo.findAllByTargetWarehouse(user.getWarehouse());
                case platform_manager:
                    return orderRepo.findAll();
                default:
                    return null;
            }
        }
    }

    public Page<Order> queryOrders(
            Integer warehouseId,
            String receiverId,
            Integer orderId,
            Integer creatorId,
            Integer status,
            LocalDateTime targetDate,
            LocalDateTime creationDate,
            Pageable pageable) {

//        Session session = em.unwrap(Session.class);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Order> cr = builder.createQuery(Order.class);
        Root<Order> root = cr.from(Order.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (warehouseId != null) {
            Optional<Warehouse> warehouse = warehouseRepo.findById(warehouseId);
            predicates.add(builder.equal(root.join("targetWarehouse"), warehouse.get()));
        }
        if (orderId != null)
            predicates.add(builder.equal(root.get("id"), orderId));
        if (receiverId != null)
            predicates.add(builder.like(root.get("receiverId"), receiverId));
        if (status != null)
            predicates.add(builder.equal(root.get("status"), status));
        if (creatorId != null)
            predicates.add(builder.equal(root.get("creator").get("id"), creatorId));
//        if(creationDate != null)
//            predicates.add(builder.between(root.get("creationDate"),creationDate.withHour(0).withMinute(0),creationDate.withHour(23).withMinute(59)));
        if (targetDate != null) {
            L.info(root.get("targetTime").getJavaType().getName());
//            predicates.add(builder.greaterThanOrEqualTo(root.get("targetTime"),targetDate));
            predicates.add(builder.between(root.get("targetTime"), builder.literal(targetDate), builder.literal(targetDate.plusDays(1))));
        }
        Predicate[] predArray = new Predicate[predicates.size()];
        predicates.toArray(predArray);


        cr.select(root).where(predArray).orderBy(builder.desc(root.get("id")));

//        CriteriaBuilder qb = em.getCriteriaBuilder();
//        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
//        cq.select(qb.count(cq.from(Order.class))).where(predArray);
        if (pageable != null) {
            int count = em.createQuery(cr).getResultList().size();
            TypedQuery<Order> query = em.createQuery(cr).setMaxResults(pageable.getPageSize()).setFirstResult((int) pageable.getOffset());
            Page<Order> pagedResults = new PageImpl<>(query.getResultList(), pageable, count);
            return pagedResults;
        } else {
            List<Order> res = em.createQuery(cr).getResultList();
            return new PageImpl<>(res, PageRequest.of(1, res.size()), res.size());
        }
    }

    public Order saveOrder(Order order) {
        if (order.getId() == 0) {
            order.setCreationDate(LocalDateTime.now());
        } else {
            order.setLastModifiedDate(LocalDateTime.now());
        }
        return orderRepo.save(order);
    }

    public Optional<Order> getById(int id) {
        return orderRepo.findById(id);
    }
}
