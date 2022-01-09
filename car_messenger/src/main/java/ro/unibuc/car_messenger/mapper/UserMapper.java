package ro.unibuc.car_messenger.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ro.unibuc.car_messenger.domain.User;
import ro.unibuc.car_messenger.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto mapToDto(User user);

    @Mapping(target = "roles", ignore = true)
    User mapToEntity(UserDto user);
}
