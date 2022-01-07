package ro.unibuc.car_messenger.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ro.unibuc.car_messenger.domain.Car;
import ro.unibuc.car_messenger.dto.CarDto;

@Mapper(componentModel = "spring")
public interface CarMapper {

    CarMapper INSTANCE = Mappers.getMapper( CarMapper.class );

    CarDto mapToDto(Car user);
    Car mapToEntity(CarDto user);
}
