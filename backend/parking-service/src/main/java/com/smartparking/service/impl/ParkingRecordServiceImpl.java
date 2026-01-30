package com.smartparking.service.impl;

import com.smartparking.common.error.BusinessException;
import com.smartparking.common.error.ErrorCode;
import com.smartparking.dao.mapper.ParkingLotMapper;
import com.smartparking.dao.mapper.ParkingRecordMapper;
import com.smartparking.dao.mapper.ParkingSpotMapper;
import com.smartparking.dao.mapper.UserMapper;
import com.smartparking.domain.model.ParkingLot;
import com.smartparking.domain.model.ParkingRecord;
import com.smartparking.domain.model.ParkingSpot;
import com.smartparking.domain.model.User;
import com.smartparking.service.MessageService;
import com.smartparking.service.ParkingRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingRecordServiceImpl implements ParkingRecordService {

    private final ParkingRecordMapper recordMapper;
    private final ParkingSpotMapper spotMapper;
    private final ParkingLotMapper lotMapper;
    private final UserMapper userMapper;
    private final com.smartparking.dao.mapper.ManagerMapper managerMapper;
    private final MessageService messageService;

    public ParkingRecordServiceImpl(ParkingRecordMapper recordMapper,
                                    ParkingSpotMapper spotMapper,
                                    ParkingLotMapper lotMapper,
                                    UserMapper userMapper,
                                    com.smartparking.dao.mapper.ManagerMapper managerMapper,
                                    MessageService messageService) {
        this.recordMapper = recordMapper;
        this.spotMapper = spotMapper;
        this.lotMapper = lotMapper;
        this.userMapper = userMapper;
        this.managerMapper = managerMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional
    public ParkingRecord book(Long userId, Long vehicleId, Long lotId) {
        // Check active record
        ParkingRecord active = recordMapper.selectActiveRecord(vehicleId);
        if (active != null) {
            if (active.getStatus() == 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "You already have a booking");
            } else {
                throw new BusinessException(ErrorCode.CONFLICT, "Vehicle is already parked");
            }
        }

        ParkingSpot spot = spotMapper.selectFirstAvailable(lotId);
        if (spot == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "No available spots");
        }

        // Create record
        ParkingRecord record = new ParkingRecord();
        record.setUserId(userId);
        record.setVehicleId(vehicleId);
        record.setLotId(lotId);
        record.setSpotId(spot.getSpotId());
        record.setStatus(0); // BOOKED
        record.setPaymentStatus(0); // Unpaid
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);

        // Update spot
        spot.setIsOccupied(true);
        spot.setLastUpdated(LocalDateTime.now());
        spotMapper.update(spot);

        // Update lot available count
        updateLotAvailability(lotId);

        // Notify
        notifyManagerAndUser(record, spot, "预约成功", "有人预约了您的车位");

        return record;
    }

    @Override
    @Transactional
    public ParkingRecord cancel(Long userId, Long recordId) {
        System.out.println("DEBUG: Service cancel - userId: " + userId + ", recordId: " + recordId);
        ParkingRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            System.out.println("DEBUG: Record not found: " + recordId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "Record not found");
        }
        
        System.out.println("DEBUG: Record found: " + record.getRecordId() + ", owner: " + record.getUserId() + ", status: " + record.getStatus());

        // Check ownership
        if (!record.getUserId().equals(userId)) {
             System.out.println("DEBUG: Ownership mismatch. Record owner: " + record.getUserId() + ", request user: " + userId);
             throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot cancel other user's record");
        }
        
        if (record.getStatus() != 0) {
            System.out.println("DEBUG: Invalid status: " + record.getStatus());
            throw new BusinessException(ErrorCode.CONFLICT, "Only booked records can be cancelled");
        }

        record.setStatus(3); // CANCELLED
        recordMapper.update(record);

        // Release spot
        ParkingSpot spot = spotMapper.selectById(record.getSpotId());
        if (spot != null) {
            spot.setIsOccupied(false);
            spot.setLastUpdated(LocalDateTime.now());
            spotMapper.update(spot);
        }

        updateLotAvailability(record.getLotId());
        
        // Notify
        try {
            notifyManagerAndUser(record, spot, "预约取消", "用户取消了预约");
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }

        return record;
    }

    @Override
    @Transactional
    public ParkingRecord entry(Long userId, Long vehicleId, Long lotId) {
         // Check active record
        ParkingRecord active = recordMapper.selectActiveRecord(vehicleId);
        if (active != null) {
            if (active.getStatus() == 0) {
                // Convert booking to parked
                return entry(userId, vehicleId, lotId, active.getSpotId());
            } else {
                 throw new BusinessException(ErrorCode.CONFLICT, "Vehicle is already parked");
            }
        }

        ParkingSpot spot = spotMapper.selectFirstAvailable(lotId);
        if (spot == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "No available spots");
        }
        return entry(userId, vehicleId, lotId, spot.getSpotId());
    }

    @Override
    @Transactional
    public ParkingRecord entry(Long userId, Long vehicleId, Long lotId, Long spotId) {
        // Check active record
        ParkingRecord active = recordMapper.selectActiveRecord(vehicleId);
        
        if (active != null) {
            if (active.getStatus() == 1) { // Already parked
                 throw new BusinessException(ErrorCode.CONFLICT, "Vehicle is already parked");
            } else if (active.getStatus() == 0) { // Booked -> Parked
                if (!active.getLotId().equals(lotId)) {
                     throw new BusinessException(ErrorCode.CONFLICT, "Booking is for a different lot");
                }
                // Update existing record
                active.setEntryTime(LocalDateTime.now());
                active.setStatus(1); // PARKED
                recordMapper.update(active);
                return active;
            }
        }

        // No booking, create new parked record
        ParkingSpot spot = spotMapper.selectById(spotId);
        if (spot == null || Boolean.TRUE.equals(spot.getIsOccupied())) {
             // Double check if spot is occupied by THIS user (in case of race condition or weird state), but simpler to just fail
             // Wait, if I am booking, the spot IS occupied by ME.
             // So if active is null (no booking), spot shouldn't be occupied.
             // If active is not null (booking), spot IS occupied.
             // Logic above handled booking case.
             throw new BusinessException(ErrorCode.CONFLICT, "Spot is occupied or invalid");
        }

        // Create record
        ParkingRecord record = new ParkingRecord();
        record.setUserId(userId);
        record.setVehicleId(vehicleId);
        record.setLotId(lotId);
        record.setSpotId(spotId);
        record.setEntryTime(LocalDateTime.now());
        record.setAmount(BigDecimal.ZERO); // Initialize amount
        record.setPaymentStatus(0); // Unpaid
        record.setStatus(1); // PARKED
        record.setCreatedAt(LocalDateTime.now());
        recordMapper.insert(record);

        // Update spot
        spot.setIsOccupied(true);
        spot.setLastUpdated(LocalDateTime.now());
        spotMapper.update(spot);

        // Update lot available count
        updateLotAvailability(lotId);

        // Notify
        notifyManagerAndUser(record, spot, "入场成功", "用户已入场");

        return record;
    }

    @Override
    @Transactional
    public ParkingRecord exit(Long recordId) {
        ParkingRecord record = recordMapper.selectById(recordId);
        if (record == null || record.getStatus() != 1) { // Must be PARKED
            throw new BusinessException(ErrorCode.NOT_FOUND, "Active parked record not found");
        }

        record.setExitTime(LocalDateTime.now());
        record.setStatus(2); // COMPLETED
        long minutes = Duration.between(record.getEntryTime(), record.getExitTime()).toMinutes();
        record.setDurationMinutes((int) minutes);
        
        // Update lot
        ParkingLot lot = lotMapper.selectById(record.getLotId());
        
        // Calculate fee
        if (lot != null && Integer.valueOf(0).equals(lot.getType())) {
            // Public parking is free
            record.setAmount(BigDecimal.ZERO);
            record.setPaymentStatus(1); // Considered Paid/Free
            record.setPaymentMethod("FREE");
        } else {
            // Commercial: 10 yuan/hour
            BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal(60), 2, java.math.RoundingMode.CEILING);
            record.setAmount(hours.multiply(new BigDecimal("10.00")));
        }
        
        recordMapper.update(record);

        // Release spot
        ParkingSpot spot = spotMapper.selectById(record.getSpotId());
        if (spot != null) {
            spot.setIsOccupied(false);
            spot.setLastUpdated(LocalDateTime.now());
            spotMapper.update(spot);
        }

        // Update lot available count
        updateLotAvailability(record.getLotId());

        return record;
    }

    @Override
    @Transactional
    public ParkingRecord pay(Long recordId, String paymentMethod) {
        ParkingRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Record not found");
        }
        if (record.getPaymentStatus() == 1) {
            return record; // Already paid
        }

        // Check user balance if wallet payment
        if ("WALLET".equals(paymentMethod)) {
            User user = userMapper.selectById(record.getUserId());
            if (user.getBalance().compareTo(record.getAmount()) < 0) {
                throw new BusinessException(ErrorCode.PAYMENT_REQUIRED, "Insufficient balance");
            }
            user.setBalance(user.getBalance().subtract(record.getAmount()));
            userMapper.update(user);
        }

        record.setPaidAmount(record.getAmount());
        record.setPaymentStatus(1);
        record.setPaymentMethod(paymentMethod);
        recordMapper.update(record);
        
        return record;
    }

    @Override
    public List<ParkingRecord> getMyRecords(Long userId) {
        return recordMapper.selectByUserId(userId);
    }

    private void updateLotAvailability(Long lotId) {
        ParkingLot lot = lotMapper.selectById(lotId);
        if (lot != null) {
            lot.setAvailableSpots(spotMapper.countAvailableByLotId(lotId));
            lotMapper.update(lot);
        }
    }

    private void notifyManagerAndUser(ParkingRecord record, ParkingSpot spot, String userTitle, String managerTitle) {
         ParkingLot lot = lotMapper.selectById(record.getLotId());
         if (lot == null) {
             return;
         }
         
         Long managerId = lot.getManagerId();
         if (managerId == null) {
             // 尝试从管理员表查找默认管理员，或者直接报错
             // 为了调试，我们先抛出异常，确认是否是因为这里为空
             // 在生产环境中应该记录日志并忽略，或者通知超级管理员
             // throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Parking lot " + lot.getName() + " has no manager assigned");
             // 暂时先不抛出阻断流程的异常，而是记录一下（无法记录），尝试发送给系统消息？
             // 或者我们硬编码一个管理员ID？不，这不好。
             // 还是抛出异常最直接能被用户感知到
             throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Data Error: Parking lot has no manager assigned");
         }

         // Get manager info for phone number
         com.smartparking.domain.model.Manager manager = managerMapper.selectById(managerId);
         String managerPhone = (manager != null && manager.getPhone() != null) ? manager.getPhone() : "暂无";

         String msgToManager = String.format("%s: 用户ID:%d 停车场:%s, 车位ID:%d", managerTitle, record.getUserId(), lot.getName(), spot.getSpotId());
         // Use lowercase roles and 'parking' type to match frontend tabs
         messageService.send(record.getUserId(), "user", managerId, "manager", msgToManager, "parking");

         // Send message to User (Manager -> User)
         String msgToUser = String.format("%s: %s\n地址: %s\n车位号: %s\n管理员电话: %s", 
             userTitle, lot.getName(), lot.getAddress(), spot.getSpotNumber(), managerPhone);
         messageService.send(managerId, "manager", record.getUserId(), "user", msgToUser, "parking");
    }
}