package ro.unibuc.car_messenger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.car_messenger.domain.Car;
import ro.unibuc.car_messenger.dto.CarDto;
import ro.unibuc.car_messenger.exception.UniqueException;
import ro.unibuc.car_messenger.mapper.CarMapper;
import ro.unibuc.car_messenger.repo.CarRepo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service @Transactional @Slf4j
public class CarService {
    @Autowired
    private CarRepo carRepo;
    @Autowired
    private CarMapper carMapper;

    public CarDto saveCar (CarDto carDto) {
        if (carRepo.findByPlateAndCountryCode(carDto.getPlate(), carDto.getCountryCode()).isPresent()) {
            throw  new UniqueException("The car already exists");
        }
        log.info("Saving new car ({}) {} to the database", carDto.getCountryCode(), carDto.getPlate());
        return carMapper.mapToDto(carRepo.save(carMapper.mapToEntity(carDto)));
    }

    public List<CarDto> findAllCars () {
        return carRepo.findAll().stream().map(c -> carMapper.mapToDto(c)).collect(Collectors.toList());
    }

    public Optional<CarDto> findCarById (Long id) {
        Optional<Car> car = carRepo.findById(id);
        if (car.isEmpty()) {return Optional.empty(); }
        return Optional.of(carMapper.mapToDto(car.get()));
    }

    public Optional<CarDto> findCarByPlateAndCountryCode (String plate, String countryCode) {
        Optional<Car> car = carRepo.findByPlateAndCountryCode(plate, countryCode);
        if (car.isEmpty()) {return Optional.empty(); }
        return Optional.of(carMapper.mapToDto(car.get()));
    }

    public Optional<CarDto> updateCar(Long id, CarDto carDto) {
        Optional<Car> car = carRepo.findById(id);
        if (car.isEmpty()) { return Optional.empty(); }
        car.get().setPlate(carDto.getPlate());
        car.get().setCountryCode(carDto.getCountryCode());
        log.info("Updating car Id{{}} with value{({}) {}} in the database", id, carDto.getCountryCode(), carDto.getPlate());
        return Optional.of(carMapper.mapToDto(car.get()));
    }

    public void deleteCar(Long id) {
        log.info("Deleting car Id{{}} from the database", id);
        carRepo.deleteById(id);
    }

}
