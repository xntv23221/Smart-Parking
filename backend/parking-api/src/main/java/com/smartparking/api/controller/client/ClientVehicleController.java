package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.common.security.UserContextHolder;
import com.smartparking.domain.model.Vehicle;
import com.smartparking.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/v1/vehicles")
public class ClientVehicleController {

    private final VehicleService vehicleService;

    public ClientVehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public Result<List<Vehicle>> list() {
        Long userId = UserContextHolder.get().getUserId();
        return Result.ok(vehicleService.getMyVehicles(userId));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Vehicle vehicle) {
        Long userId = UserContextHolder.get().getUserId();
        vehicleService.addVehicle(userId, vehicle);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable("id") Long id) {
        Long userId = UserContextHolder.get().getUserId();
        vehicleService.removeVehicle(userId, id);
        return Result.ok(null);
    }
    
    @PostMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable("id") Long id) {
        Long userId = UserContextHolder.get().getUserId();
        vehicleService.setDefault(userId, id);
        return Result.ok(null);
    }
}
