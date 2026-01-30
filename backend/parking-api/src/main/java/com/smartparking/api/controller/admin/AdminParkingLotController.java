package com.smartparking.api.controller.admin;

import com.smartparking.common.api.Result;
import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.common.security.UserRole;
import com.smartparking.domain.model.ParkingLot;
import com.smartparking.service.ParkingLotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/parking-lots")
public class AdminParkingLotController {

    private final ParkingLotService parkingLotService;

    public AdminParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping
    public Result<List<ParkingLot>> list() {
        UserRole role = UserContextHolder.get().getRole();
        Long userId = UserContextHolder.get().getUserId();

        if (role == UserRole.ADMIN) {
            return Result.ok(parkingLotService.getAllParkingLots());
        } else if (role == UserRole.MANAGER) {
            return Result.ok(parkingLotService.getParkingLotsByManager(userId));
        } else {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问");
        }
    }

    @PostMapping
    public Result<Void> create(@RequestBody ParkingLot newLot) {
        UserRole role = UserContextHolder.get().getRole();

        if (role != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有管理员可以创建停车场");
        }

        if (newLot.getManagerId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "必须绑定停车场管理员");
        }

        if (newLot.getName() == null || newLot.getAddress() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "停车场名称和地址不能为空");
        }

        parkingLotService.createParkingLot(newLot);
        return Result.ok(null);
    }

    @PutMapping("/{lotId}")
    public Result<Void> update(@PathVariable("lotId") Long lotId, @RequestBody ParkingLot updatedLot) {
        UserRole role = UserContextHolder.get().getRole();
        Long userId = UserContextHolder.get().getUserId();

        ParkingLot existing = parkingLotService.getParkingLotById(lotId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "停车场不存在");
        }

        if (role == UserRole.MANAGER) {
            if (!existing.getManagerId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权修改该停车场");
            }
            // Managers can only update spots and status
            existing.setTotalSpots(updatedLot.getTotalSpots());
            existing.setAvailableSpots(updatedLot.getAvailableSpots());
            if (updatedLot.getStatus() != null) {
                existing.setStatus(updatedLot.getStatus());
            }
        } else if (role == UserRole.ADMIN) {
            // Admins can update everything
            existing.setName(updatedLot.getName());
            existing.setAddress(updatedLot.getAddress());
            existing.setTotalSpots(updatedLot.getTotalSpots());
            existing.setAvailableSpots(updatedLot.getAvailableSpots());
            existing.setLatitude(updatedLot.getLatitude());
            existing.setLongitude(updatedLot.getLongitude());
            existing.setStatus(updatedLot.getStatus());
            if (updatedLot.getManagerId() != null) {
                existing.setManagerId(updatedLot.getManagerId());
            }
        }

        parkingLotService.updateParkingLot(existing);
        return Result.ok(null);
    }
}
