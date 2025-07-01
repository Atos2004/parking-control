package br.com.api.parking_control.controllers;

import br.com.api.parking_control.dtos.ParkingSpotDto;
import br.com.api.parking_control.models.ParkingSpotModel;
import br.com.api.parking_control.services.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto){

        if(parkingSpotService.existsByLicensePlate(parkingSpotDto.getLicensePlate())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT: License plate already exists");
        }
        if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT: Parking spot number already exists");
        }
        if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("CONFLICT: Apartment and Block already exist");
        }
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots(){
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotO = parkingSpotService.findById(id);
        if (parkingSpotO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotO.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> DeleteParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> parkingSpotO = parkingSpotService.findById(id);
        if (parkingSpotO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
        }
        parkingSpotService.delete(parkingSpotO.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking spot deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> UpdateParkingSpot(@PathVariable(value = "id") UUID id,
                                             @RequestBody ParkingSpotDto parkingSpotDto){
        Optional<ParkingSpotModel> parkingSpotO = parkingSpotService.findById(id);
        if (parkingSpotO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking spot not found.");
        }
        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotO.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotO.get().getRegistrationDate());
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }

}
