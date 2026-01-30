package com.smartparking.api.controller.client;

import com.smartparking.common.api.Result;
import com.smartparking.domain.model.ParkingLot;
import com.smartparking.service.ParkingLotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/client/v1/parking-lots")
public class ClientParkingLotController {

    private final ParkingLotService parkingLotService;

    public ClientParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping
    public Result<List<ParkingLot>> list() {
        return Result.ok(parkingLotService.getAllParkingLots());
    }

    @GetMapping("/by-name")
    public Result<ParkingLot> getByName(@org.springframework.web.bind.annotation.RequestParam String name) {
        return Result.ok(parkingLotService.getParkingLotByName(name));
    }
}
