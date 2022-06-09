package ro.unibuc.car_messenger.mapper;

import org.mapstruct.Mapper;
import ro.unibuc.car_messenger.domain.Engine;
import ro.unibuc.car_messenger.dto.EngineDto;

@Mapper(componentModel = "spring")
public interface EngineMapper {
    EngineDto mapToDto(Engine engine);
    Engine mapToEntity(EngineDto engineDto);
}
