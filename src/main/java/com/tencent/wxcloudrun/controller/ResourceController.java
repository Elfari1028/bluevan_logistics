package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.config.L;
import com.tencent.wxcloudrun.model.Order;
import com.tencent.wxcloudrun.model.Session;
import com.tencent.wxcloudrun.model.User;
import com.tencent.wxcloudrun.model.util.Cargo;
import com.tencent.wxcloudrun.model.util.Location;
import com.tencent.wxcloudrun.service.OrderService;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.service.WarehouseService;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/download")
public class ResourceController {
    final private OrderService orderService;
    final private UserService userService;
    final private WarehouseService warehouseService;

    public ResourceController(@Autowired OrderService orderService, @Autowired UserService userService, @Autowired WarehouseService warehouseService) {
        this.orderService = orderService;
        this.userService = userService;
        this.warehouseService = warehouseService;
    }

    /**
     * @RequestParam(name = "creationDate", required = false) String creationDate,
     * @RequestParam(name = "targetDate", required = false) String targetDate,
     * @RequestParam(name = "receiverId", required = false) String receiverId,
     * @RequestParam(name = "status", required = false, defaultValue = "-1") int stat,
     * @RequestParam(name = "page", required = false, defaultValue = "0") int page,
     * @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize,
     * @RequestParam(name = "warehouseId", required = false, defaultValue = "-1") Integer warehouseId,
     * @RequestParam(name = "orderId", required = false, defaultValue = "-1") Integer orderId
     */

    @GetMapping(path = "/orders")
    public ResponseEntity<Resource> download(@RequestParam(name = "code") String code) throws IOException {


//         ...
        byte[] decodedBytes = Base64.getDecoder().decode(code);
        String decodedString = new String(decodedBytes);
        JSONObject params = JSON.parseObject(decodedString);
        String creationDate = params.getString("creationDate");
        String targetDate = params.getString("targetDate");
        String receiverId = params.getString("receiverId");
        Integer stat = params.getInteger("status");
        if (stat == null) stat = -1;
        Integer warehouseId = params.getInteger("warehouseId");
        if (warehouseId == null) warehouseId = -1;
        Integer orderId = params.getInteger("orderId");
        if (orderId == null) orderId = -1;


        LocalDateTime creationDatetime = null;
        LocalDateTime targetDateTime = null;
        if (!(creationDate == null || creationDate.length() == 0)) {
            creationDatetime = LocalDateTime.parse(creationDate);
        }
        if (!(targetDate == null || targetDate.length() == 0)) {
            targetDateTime = LocalDateTime.parse(targetDate);
        }

        String sessionKey = params.getString("session");
        if (sessionKey == null) {
            L.error("download : sessionKey not present");
            return ResponseEntity.badRequest().body(null);
        }
        Optional<Session> sop = userService.isUserLoggedIn(sessionKey);
        if (!sop.isPresent()) {
            L.error("download : session not present");
            return ResponseEntity.badRequest().body(null);
        }

        User user = sop.get().getUser();
        Integer whId = null;
        Integer creatorId = null;
        Integer oId = orderId <= 0 ? null : orderId;
        switch (user.getRole()) {
            case user:
            case driver:
                whId = warehouseId <= 0 ? null : warehouseId;
                creatorId = user.getId();
                break;
            case warehouse_manager:
            case warehouse_worker:
                whId = user.getWarehouse().getId();
                break;
            case platform_manager:
                whId = warehouseId <= 0 ? null : warehouseId;
                break;
        }

        Page<Order> page = orderService.queryOrders(whId, receiverId, oId, creatorId, stat < 0?null:stat, targetDateTime, creationDatetime, null);
        List<Order> orders = page.getContent();           // A        B       C           D           E           F           G           H       I       J           K           L           M    N           O      P      Q        R       S          T      U     V
                                // 1        2       3           4           5           6           7           8       9       10          11         12           13  14          15      16
        String[] columns =      {"订单ID" , "发货人", "发货人电话","收货人ID","订单备注","订单创建时间", "订单状态","入库时间", "仓库",    ""      , ""      , ""      ,       "", ""     ,"货物信息", ""   ,  ""     , ""     , ""      ,   ""  , "", ""};
        String[] subColumns =   {""      ,  ""    ,  ""        ,  ""     ,""       ,""         ,  ""      ,""      ,  "仓库ID" ,"仓库名称","仓库省份","仓库城市","仓库县/区","仓库地址","货物名称","箱数","平均件数","货物类型","包装类型","体积","重量","备注"};

        Workbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        Sheet sheet = workbook.createSheet("订单列表");
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLUE.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        // Row for Header
        Row headerRow = sheet.createRow(0);
        // Header
        for (int col = 0; col < columns.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(columns[col]);
            cell.setCellStyle(headerCellStyle);
        }

        sheet.addMergedRegion(CellRangeAddress.valueOf("I1:N1")); // 仓库
        sheet.addMergedRegion(CellRangeAddress.valueOf("O1:V1")); // 货物
        Row subHeader = sheet.createRow(1);
        for (int col = 0; col < columns.length; col++) {
            Cell cell = subHeader.createCell(col);
            if( col < 8){
                sheet.addMergedRegion(new CellRangeAddress(0,1,col,col));
            }
            cell.setCellValue(subColumns[col]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowIdx = 2;
        for (Order order : orders) {
            Row row = sheet.createRow(rowIdx);
            int col = 0;
            row.createCell(col ++).setCellValue(order.getId());
            setRowSafe(row,col++,order.getSenderName());
            setRowSafe(row,col++,order.getSenderPhone());
            setRowSafe(row,col++,order.getReceiverId());
            setRowSafe(row,col++,order.getNote());
            setRowSafe(row,col++,order.getCreationDate());
            setRowSafe(row,col++,order.getStatus().toString());
            setRowSafe(row,col++,order.getArrivalTime());
            //
            setRowSafe(row,col++,order.getTargetWarehouse().getId());
            setRowSafe(row,col++,order.getTargetWarehouse().getName());
            setRowSafe(row,col++,order.getTargetWarehouse().getLocation().getProvince());
            setRowSafe(row,col++,order.getTargetWarehouse().getLocation().getCity());
            setRowSafe(row,col++,order.getTargetWarehouse().getLocation().getDistrict());
            setRowSafe(row,col++,order.getTargetWarehouse().getLocation().getAddress());
            //
            int rowTop = rowIdx;
            List<Cargo> cargos = order.getCargos();
            int cargoCnt = 0;
            for (Cargo cargo: cargos) {
                int begin = col;
                if(cargoCnt > 0)row = sheet.createRow(rowIdx);
                setRowSafe(row, begin++,cargo.getName());
                setRowSafe(row,begin++,cargo.getCount());
                setRowSafe(row,begin++,cargo.getSubcount());;
                setRowSafe(row,begin++,cargo.getCargoType());
                setRowSafe(row,begin++,cargo.getPackageType());
                setRowSafe(row,begin++,cargo.getVolume());
                setRowSafe(row,begin++,cargo.getWeight());
                setRowSafe(row,begin++,cargo.getNote());
                cargoCnt++;
                rowIdx ++;
            }
            if(rowTop != rowIdx - 1)for(int i = 0; i < col; i++)
                sheet.addMergedRegion(new CellRangeAddress(rowTop,rowIdx - 1,i,i));
        }

        ByteArrayOutputStream fileOut = new ByteArrayOutputStream();
        workbook.write(fileOut);
        workbook.close();

        HttpHeaders header = new HttpHeaders();
        LocalDateTime now =  LocalDateTime.now();
        String filename = "" + now.getYear() + "_"+now.getMonth()+"_"+now.getDayOfMonth()+"_"+now.getHour()+"_"+now.getMinute()+"auto_generated.xlsx";
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+filename);
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        ByteArrayResource resource = new ByteArrayResource(fileOut.toByteArray());
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(fileOut.size())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);

    }
    private void setRowSafe(Row row, int col, Object data){
        Cell cell =row.createCell(col);
        if(data == null)cell.setCellValue("");
        if(data instanceof LocalDateTime)cell.setCellValue(data.toString());
        if(data instanceof String)cell.setCellValue((String) data);
        if(data instanceof Integer)cell.setCellValue((Integer) data);
        if(data instanceof Double)cell.setCellValue((Double) data);
        if(data instanceof Float)cell.setCellValue((Float) data);


    }

}
