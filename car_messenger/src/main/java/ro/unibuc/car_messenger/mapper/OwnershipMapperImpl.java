package ro.unibuc.car_messenger.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.unibuc.car_messenger.domain.Ownership;
import ro.unibuc.car_messenger.domain.Ownership.OwnershipBuilder;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.dto.OwnershipDto.OwnershipDtoBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OwnershipMapperImpl implements OwnershipMapper {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CarMapper carMapper;

    @Override
    public OwnershipDto mapToDto(Ownership ownership) {
        if ( ownership == null ) {
            return null;
        }

        OwnershipDtoBuilder ownershipDto = OwnershipDto.builder();

        ownershipDto.id( ownership.getId() );
        ownershipDto.userDto( userMapper.mapToDto(ownership.getUser()) );
        ownershipDto.carDto( carMapper.mapToDto(ownership.getCar()) );
        ownershipDto.category( ownership.getCategory() );

        return ownershipDto.build();
    }

    @Override
    public Ownership mapToEntity(OwnershipDto ownershipDto) {
        if ( ownershipDto == null ) {
            return null;
        }

        OwnershipBuilder ownership = Ownership.builder();

        ownership.id( ownershipDto.getId() );
        ownership.user( userMapper.mapToEntity(ownershipDto.getUserDto()) );
        ownership.car( carMapper.mapToEntity(ownershipDto.getCarDto()) );
        ownership.category( ownershipDto.getCategory() );

        return ownership.build();
    }

    @Override
    public Optional<OwnershipDto> mapToDto(Optional<Ownership> ownership) {
        if ( ownership.isEmpty() ) { return Optional.empty(); }
        else { return Optional.of(this.mapToDto(ownership.get())); }
    }

    @Override
    public Optional<Ownership> mapToEntity(Optional<OwnershipDto> ownershipDto) {
        if ( ownershipDto.isEmpty() ) { return Optional.empty(); }
        else { return Optional.of(this.mapToEntity(ownershipDto.get())); }
    }

    @Override
    public List<OwnershipDto> mapToDto(List<Ownership> ownerships) {
       return ownerships.stream().map(o -> this.mapToDto(o)).collect(Collectors.toList());
    }

    @Override
    public List<Ownership> mapToEntity(List<OwnershipDto> ownershipDtos) {
        return ownershipDtos.stream().map(o -> this.mapToEntity(o)).collect(Collectors.toList());
    }
}
