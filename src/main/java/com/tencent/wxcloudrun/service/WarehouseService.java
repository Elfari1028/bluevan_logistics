package com.tencent.wxcloudrun.service;

import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.Warehouse;
import com.tencent.wxcloudrun.model.util.Cargo;
import com.tencent.wxcloudrun.model.util.OrderStatus;
import com.tencent.wxcloudrun.model.util.WorkloadConfig;
import com.tencent.wxcloudrun.model.util.WorktimeConfig;
import com.tencent.wxcloudrun.repo.OrderRepo;
import com.tencent.wxcloudrun.repo.UserRepo;
import com.tencent.wxcloudrun.repo.WarehouseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class WarehouseService {
    private UserRepo userRepo;
    private WarehouseRepo warehouseRepo;
    private OrderRepo orderRepo;

    public WarehouseService(@Autowired UserRepo userRepo, @Autowired WarehouseRepo warehouseRepo, @Autowired OrderRepo orderRepo) {
        this.userRepo = userRepo;
        this.warehouseRepo = warehouseRepo;
        this.orderRepo  = orderRepo;
    }

    public  LocalDateTime getClosetLegalTime(Warehouse house) {
        WorktimeConfig worktime = house.getWorktimeConfig();
        WorkloadConfig workload = house.getWorkloadConfig();
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        now = now.plusHours(worktime.getOffset());
        while (now.getHour() < worktime.getStartTime().getHour() ||
                (now.getHour() == worktime.getStartTime().getHour() && now.getMinute() <= worktime.getStopTime().getMinute()) ||
                now.getHour() > worktime.getStopTime().getHour() ||
                (now.getHour() == worktime.getStopTime().getHour() && now.getMinute() >= worktime.getStopTime().getMinute())) {
            now = now.plusMinutes(worktime.getInterval());
        }
        LocalDateTime closetLegalTime =
                now
                        .withHour( worktime.getStartTime().getHour())
                        .withMinute(worktime.getStartTime().getMinute());

        LocalDateTime nextTime = closetLegalTime.plusHours(worktime.getInterval());
        while (closetLegalTime.isBefore(now) && nextTime.isBefore(now)){
            LocalDateTime tmp = nextTime;
            nextTime = closetLegalTime.plusMinutes(worktime.getInterval());
            closetLegalTime = tmp;
        }
        return closetLegalTime;
    }

    public List<Pair<LocalDateTime,LocalDateTime>> getAllLegalIntervals(Warehouse warehouse){
        LocalDateTime closestLegalTime = getClosetLegalTime(warehouse);
        WorktimeConfig config = warehouse.getWorktimeConfig();
        List<Pair<LocalDateTime,LocalDateTime>> ret = new ArrayList<Pair<LocalDateTime,LocalDateTime>>();
        LocalDateTime marker = closestLegalTime.withHour(closestLegalTime.getHour());
        while(closestLegalTime.plusDays(14).isAfter(marker)){
            if(marker.getHour() < config.getStartTime().getHour() || (marker.getHour() == config.getStartTime().getHour() && marker.getMinute() < config.getStartTime().getMinute())){
                marker = marker.withHour(config.getStartTime().getHour()).withMinute(config.getStartTime().getMinute());
            }
            if(marker.getHour() > config.getStopTime().getHour() ||(marker.getHour() == config.getStopTime().getHour() && marker.getMinute() <= config.getStartTime().getMinute()) ){
                marker = marker.withHour(config.getStartTime().getHour()).withMinute(config.getStartTime().getMinute()).plusDays(1);
            }

            LocalDateTime ob1 = marker.plusSeconds(1).minusSeconds(1);
            LocalDateTime ob2 = marker.plusMinutes(config.getInterval());

            if(ob2.getHour() > config.getStopTime().getHour() ||(ob2.getHour() == config.getStopTime().getHour() && ob2.getMinute() <= config.getStartTime().getMinute()) ){
                ob2 = ob2.withHour(config.getStopTime().getHour()).withMinute(config.getStopTime().getMinute());
            }
            ret.add(Pair.of(ob1,ob2));
            marker = ob2.plusSeconds(1).minusSeconds(1);
        }
        return ret;
    }

//    public List<Pair<LocalDateTime,LocalDateTime>> getAllAcceptableIntervals(Warehouse warehouse, List<Cargo> cargos){
//        List<Pair<LocalDateTime,LocalDateTime>> pre = getAllLegalIntervals(warehouse);
//        WorkloadConfig workload = warehouse.getWorkloadConfig();
//        WorktimeConfig worktime = warehouse.getWorktimeConfig();
//        pre.forEach(new Consumer<Pair<LocalDateTime, LocalDateTime>>() {
//            @Override
//            public void accept(Pair<LocalDateTime, LocalDateTime> p) {
//               List<Order> orders = orderRepo.findAllByTargetWarehouseAndTargetTimeBetween(
//                        warehouse,
//                        p.getFirst(),
//                        p.getSecond()
//                );
//               List<Cargo> cargos = new ArrayList<Cargo>();
//                for (Order order:orders) {
//                    cargos.addAll(order.getCargos());
//                }
//            }
//        });
//    }

    public Warehouse saveWarehouse(Warehouse warehouse) {
        return this.warehouseRepo.save(warehouse);
    }

    public Iterable<Warehouse> getAll() {
        return this.warehouseRepo.findAllByDeletedFalse();
    }

    public Optional<Warehouse> findById(int id) {
        return this.warehouseRepo.findByIdAndDeletedFalse(id);
    }

    public Optional<Warehouse> getById(int id) {
        return warehouseRepo.findById(id);
    }

    public boolean deleteById(int id) {
        Optional<Warehouse> h = this.findById(id);
        if (h.isPresent()) {
            Warehouse w = h.get();
            w.setDeleted(true);
            this.saveWarehouse(w);
            return true;
        }
        return false;
    }

    public LocalDateTime pickTimeFor(Order order) {
        L.info("pickTimeFor:"+ order.toString());
        LocalDateTime ret=getAllLegalIntervals(order.getTargetWarehouse()).get(0).getFirst();
        L.info("time is " + ret.toString());
        return ret;
    }
}
