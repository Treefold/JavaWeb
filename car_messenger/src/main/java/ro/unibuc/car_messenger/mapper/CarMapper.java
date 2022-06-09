package ro.unibuc.car_messenger.mapper;

import org.mapstruct.Mapper;
import ro.unibuc.car_messenger.domain.Car;
import ro.unibuc.car_messenger.dto.CarDto;

@Mapper(componentModel = "spring")
public interface CarMapper {
    CarDto mapToDto(Car car);
    Car mapToEntity(CarDto carDto);
}
