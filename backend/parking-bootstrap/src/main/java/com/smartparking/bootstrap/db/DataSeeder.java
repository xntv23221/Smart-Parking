package com.smartparking.bootstrap.db;

import com.smartparking.dao.mapper.ManagerMapper;
import com.smartparking.dao.mapper.ParkingLotMapper;
import com.smartparking.dao.mapper.ParkingSpotMapper;
import com.smartparking.dao.mapper.SystemAdminMapper;
import com.smartparking.dao.mapper.UserMapper;
import com.smartparking.dao.mapper.VehicleMapper;
import com.smartparking.domain.model.Manager;
import com.smartparking.domain.model.ParkingLot;
import com.smartparking.domain.model.ParkingSpot;
import com.smartparking.domain.model.SystemAdmin;
import com.smartparking.domain.model.User;
import com.smartparking.domain.model.Vehicle;
import com.smartparking.manager.security.PasswordService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements InitializingBean {

    private final boolean seedDevData;
    private final String adminUsername;
    private final String adminPassword;
    private final String clientUsername;
    private final String clientPassword;
    private final String managerUsername;
    private final String managerPassword;

    private final SystemAdminMapper systemAdminMapper;
    private final UserMapper userMapper;
    private final ManagerMapper managerMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingSpotMapper spotMapper;
    private final VehicleMapper vehicleMapper;
    private final PasswordService passwordService;

    public DataSeeder(@Value("${db.seedDevData:false}") boolean seedDevData,
                      @Value("${db.seed.admin.username:admin}") String adminUsername,
                      @Value("${db.seed.admin.password:admin123}") String adminPassword,
                      @Value("${db.seed.client.username:user}") String clientUsername,
                      @Value("${db.seed.client.password:user123}") String clientPassword,
                      @Value("${db.seed.manager.username:client}") String managerUsername,
                      @Value("${db.seed.manager.password:client123}") String managerPassword,
                      SystemAdminMapper systemAdminMapper,
                      UserMapper userMapper,
                      ManagerMapper managerMapper,
                      ParkingLotMapper parkingLotMapper,
                      ParkingSpotMapper spotMapper,
                      VehicleMapper vehicleMapper,
                      PasswordService passwordService) {
        this.seedDevData = seedDevData;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.clientUsername = clientUsername;
        this.clientPassword = clientPassword;
        this.managerUsername = managerUsername;
        this.managerPassword = managerPassword;
        this.systemAdminMapper = systemAdminMapper;
        this.userMapper = userMapper;
        this.managerMapper = managerMapper;
        this.parkingLotMapper = parkingLotMapper;
        this.spotMapper = spotMapper;
        this.vehicleMapper = vehicleMapper;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() throws Exception {
        System.out.println("DataSeeder: Checking seedDevData = " + seedDevData);
        if (!seedDevData) {
            return;
        }

        cleanupLegacyClientUser();
        seedAdmin();
        seedManager();
        seedClient();
        seedParkingLot();
        System.out.println("DataSeeder: Seeding completed.");
    }

    private void cleanupLegacyClientUser() {
        // If we are using "client" as manager username, we must ensure it doesn't exist as a User
        // to avoid login conflicts (since AuthService checks User table first).
        if ("client".equals(managerUsername)) {
            User legacy = userMapper.selectByUsername("client");
            if (legacy != null) {
                userMapper.deleteById(legacy.getUserId());
            }
        }
        
        // Also ensure "admin" username is not in User table
        User adminUser = userMapper.selectByUsername(adminUsername);
        if (adminUser != null) {
            System.out.println("DataSeeder: Removing conflicting User 'admin'");
            userMapper.deleteById(adminUser.getUserId());
        }
    }

    private void seedAdmin() {
        SystemAdmin existing = systemAdminMapper.selectByUsername(adminUsername);
        if (existing != null) {
            // Update password if needed, or just skip
            // For dev convenience, let's update password to match config
            String newHash = passwordService.hash(adminPassword);
            existing.setPasswordHash(newHash);
            systemAdminMapper.update(existing);
            System.out.println("DataSeeder: Updated Admin password.");
            return;
        }

        SystemAdmin admin = new SystemAdmin();
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordService.hash(adminPassword));
        admin.setRole("super");
        admin.setRealName("Super Admin");
        admin.setPhone("13800138000");
        admin.setCreatedAt(LocalDateTime.now());
        
        systemAdminMapper.insert(admin);
        System.out.println("DataSeeder: Created Admin user.");
    }

    private void seedManager() {
        Manager existing = managerMapper.selectByUsername(managerUsername);
        if (existing != null) {
            String newHash = passwordService.hash(managerPassword);
            existing.setPasswordHash(newHash);
            managerMapper.update(existing);
            return;
        }

        Manager manager = new Manager();
        manager.setUsername(managerUsername);
        manager.setPasswordHash(passwordService.hash(managerPassword));
        manager.setRealName("Parking Manager");
        manager.setPhone("13900139001");
        manager.setStatus(1); // 1 = Enabled
        manager.setCreatedAt(LocalDateTime.now());
        
        managerMapper.insert(manager);
    }

    private void seedClient() {
        User existing = userMapper.selectByUsername(clientUsername);
        User user = existing;
        if (existing != null) {
            // Update password for dev convenience
            String newHash = passwordService.hash(clientPassword);
            existing.setPasswordHash(newHash);
            userMapper.update(existing);
        } else {
            user = new User();
            user.setUsername(clientUsername);
            user.setPasswordHash(passwordService.hash(clientPassword));
            user.setPhone("13900139000");
            user.setNickname("Test User");
            user.setBalance(new BigDecimal("100.00"));
            user.setStatus(1); // 1 = Normal
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userMapper.insert(user);
        }
        
        seedVehicle(user.getUserId());
    }
    
    private void seedVehicle(Long userId) {
        List<Vehicle> vehicles = vehicleMapper.selectByUserId(userId);
        if (vehicles.isEmpty()) {
            Vehicle v = new Vehicle();
            v.setUserId(userId);
            v.setPlateNumber("黑A88888");
            v.setBrand("Tesla");
            v.setModel("Model 3");
            v.setColor("White");
            v.setVehicleType(1); // Small
            v.setIsDefault(true);
            v.setCreatedAt(LocalDateTime.now());
            vehicleMapper.insert(v);
            System.out.println("DataSeeder: Created default vehicle for user " + userId);
        }
    }

    private void seedParkingLot() {
        Manager manager = managerMapper.selectByUsername(managerUsername);
        if (manager == null) {
            return;
        }

        List<ParkingLot> lots = parkingLotMapper.selectAll();
        
        // 1. Create or Update "My Parking Lot" (Commercial)
        ParkingLot myLot = lots.stream().filter(l -> "我的停车场".equals(l.getName())).findFirst().orElse(null);
        if (myLot == null) {
            ParkingLot lot = new ParkingLot();
            lot.setName("我的停车场");
            lot.setAddress("市中心广场");
            lot.setTotalSpots(100);
            lot.setAvailableSpots(50);
            lot.setManagerId(manager.getManagerId());
            lot.setLatitude(new BigDecimal("45.7478858"));
            lot.setLongitude(new BigDecimal("127.2128487"));
            lot.setStatus(1); // 1 = Open
            lot.setType(1); // 1 = Commercial
            lot.setCreatedAt(LocalDateTime.now());
            parkingLotMapper.insert(lot);
            seedSpots(lot.getLotId(), lot.getTotalSpots());
        } else {
            // Update existing to ensure type is set
            myLot.setManagerId(manager.getManagerId());
            myLot.setType(1); // Ensure Commercial
            parkingLotMapper.update(myLot);
            seedSpots(myLot.getLotId(), myLot.getTotalSpots());
        }

        // 2. Create "Public Parking Lot" (Public/Free) if not exists
        ParkingLot publicLot = lots.stream().filter(l -> "公共停车场".equals(l.getName())).findFirst().orElse(null);
        if (publicLot == null) {
            ParkingLot lot = new ParkingLot();
            lot.setName("公共停车场");
            lot.setAddress("市民公园");
            lot.setTotalSpots(200);
            lot.setAvailableSpots(150);
            lot.setManagerId(manager.getManagerId()); // Bind to same manager for simplicity
            lot.setLatitude(new BigDecimal("45.7480000")); // Nearby
            lot.setLongitude(new BigDecimal("127.2150000"));
            lot.setStatus(1);
            lot.setType(0); // 0 = Public
            lot.setCreatedAt(LocalDateTime.now());
            parkingLotMapper.insert(lot);
            seedSpots(lot.getLotId(), lot.getTotalSpots());
        } else {
            seedSpots(publicLot.getLotId(), publicLot.getTotalSpots());
        }
    }

    private void seedSpots(Long lotId, Integer totalSpots) {
        if (totalSpots == null || totalSpots <= 0) return;
        
        List<ParkingSpot> spots = spotMapper.selectByLotId(lotId);
        if (spots.isEmpty()) {
            System.out.println("DataSeeder: Seeding " + totalSpots + " spots for lot " + lotId + "...");
            // For performance, we might want to batch insert, but for now simple loop is fine for < 1000
            for (int i = 1; i <= totalSpots; i++) {
                ParkingSpot spot = new ParkingSpot();
                spot.setLotId(lotId);
                spot.setSpotNumber(String.format("A-%03d", i));
                spot.setSpotType(1); // Normal
                spot.setIsOccupied(false);
                spot.setLastUpdated(LocalDateTime.now());
                spotMapper.insert(spot);
            }
            System.out.println("DataSeeder: Created " + totalSpots + " spots for lot " + lotId);
        }
    }
}
