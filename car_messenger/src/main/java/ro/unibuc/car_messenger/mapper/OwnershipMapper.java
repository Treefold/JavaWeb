package ro.unibuc.car_messenger.mapper;

import ro.unibuc.car_messenger.domain.Ownership;
import ro.unibuc.car_messenger.dto.OwnershipDto;

import java.util.List;
import java.util.Optional;

public interface OwnershipMapper {
    OwnershipDto mapToDto(Ownership ownership);
    Ownership mapToEntity(OwnershipDto ownershipDto);
    Optional<OwnershipDto> mapToDto(Optional<Ownership> ownership);
    Optional<Ownership> mapToEntity(Optional<OwnershipDto> ownershipDto);
    List<OwnershipDto> mapToDto(List<Ownership> ownership);
    List<Ownership> mapToEntity(List<OwnershipDto> ownershipDto);
}
