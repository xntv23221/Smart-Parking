package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.ParkingRecord;
import com.smartparking.service.ParkingRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/v1/records")
public class ClientRecordController {

    private final ParkingRecordService recordService;
    private final com.smartparking.service.VehicleService vehicleService;

    public ClientRecordController(ParkingRecordService recordService, com.smartparking.service.VehicleService vehicleService) {
        this.recordService = recordService;
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public Result<List<ParkingRecord>> list() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(recordService.getMyRecords(userId));
    }

    @PostMapping("/book")
    public Result<ParkingRecord> book(@RequestBody Map<String, Long> params) {
        Long userId = UserContextHolder.get().getUserId();
        Long lotId = params.get("lotId");
        Long vehicleId = params.get("vehicleId");

        if (vehicleId == null) {
            // Find default vehicle
            List<com.smartparking.domain.model.Vehicle> vehicles = vehicleService.getMyVehicles(userId);
            vehicleId = vehicles.stream().filter(v -> Boolean.TRUE.equals(v.getIsDefault())).findFirst()
                    .map(com.smartparking.domain.model.Vehicle::getVehicleId)
                    .orElse(vehicles.isEmpty() ? null : vehicles.get(0).getVehicleId());

            if (vehicleId == null) {
                throw new com.smartparking.common.error.BusinessException(com.smartparking.common.error.ErrorCode.VALIDATION_ERROR, "No vehicle found");
            }
        }

        return Result.ok(recordService.book(userId, vehicleId, lotId));
    }

    @PostMapping("/cancel")
    public Result<ParkingRecord> cancel(@RequestBody Map<String, Long> params) {
        Long userId = UserContextHolder.get().getUserId();
        Long recordId = params.get("recordId");
        System.out.println("DEBUG: Cancel request - userId: " + userId + ", recordId: " + recordId);
        return Result.ok(recordService.cancel(userId, recordId));
    }

    @PostMapping("/entry")
    public Result<ParkingRecord> entry(@RequestBody Map<String, Long> params) {
        Long userId = UserContextHolder.get().getUserId();
        Long lotId = params.get("lotId");
        Long vehicleId = params.get("vehicleId");
        
        if (vehicleId == null) {
             // Find default vehicle
             List<com.smartparking.domain.model.Vehicle> vehicles = vehicleService.getMyVehicles(userId);
             vehicleId = vehicles.stream().filter(v -> Boolean.TRUE.equals(v.getIsDefault())).findFirst()
                 .map(com.smartparking.domain.model.Vehicle::getVehicleId)
                 .orElse(vehicles.isEmpty() ? null : vehicles.get(0).getVehicleId());
             
             if (vehicleId == null) {
                 throw new com.smartparking.common.error.BusinessException(com.smartparking.common.error.ErrorCode.VALIDATION_ERROR, "No vehicle found");
             }
        }
        
        Long spotId = params.get("spotId");
        if (spotId != null) {
            return Result.ok(recordService.entry(userId, vehicleId, lotId, spotId));
        } else {
            return Result.ok(recordService.entry(userId, vehicleId, lotId));
        }
    }
    
    @PostMapping("/exit")
    public Result<ParkingRecord> exit(@RequestBody Map<String, Long> params) {
        Long recordId = params.get("recordId");
        return Result.ok(recordService.exit(recordId));
    }
    
    @PostMapping("/pay")
    public Result<ParkingRecord> pay(@RequestBody Map<String, Object> params) {
        Long recordId = ((Number) params.get("recordId")).longValue();
        String method = (String) params.get("method");
        return Result.ok(recordService.pay(recordId, method));
    }
}
