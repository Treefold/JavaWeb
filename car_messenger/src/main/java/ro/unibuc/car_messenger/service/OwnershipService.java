package ro.unibuc.car_messenger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.car_messenger.domain.Ownership;
import ro.unibuc.car_messenger.domain.OwnershipType;
import ro.unibuc.car_messenger.dto.OwnershipDto;
import ro.unibuc.car_messenger.exception.UniqueException;
import ro.unibuc.car_messenger.mapper.OwnershipMapper;
import ro.unibuc.car_messenger.repo.OwnershipRepo;

import java.util.List;
import java.util.Optional;

@Service @Transactional @Slf4j
public class OwnershipService {
    @Autowired
    private OwnershipRepo ownershipRepo;
    @Autowired
    private OwnershipMapper ownershipMapper;

    public OwnershipDto saveOwnership (OwnershipDto ownershipDto) {
        Ownership ownership = ownershipMapper.mapToEntity(ownershipDto);
        if (ownershipRepo.findFirstByUserAndCar(ownership.getUser(), ownership.getCar()).isPresent()) {
            throw  new UniqueException("The ownership already exists");
        }
        log.info("Saving new ownership {{}} between userId{{}} and carId{{}} to the database", ownership.getCategory(), ownership.getUser().getId(), ownership.getCar().getId());
        return ownershipMapper.mapToDto(ownershipRepo.save(ownership));
    }

    public List<OwnershipDto> findAllByUserId (Long userId) { return ownershipMapper.mapToDto(ownershipRepo.findAllByUserId(userId)); }
    public List<OwnershipDto> findAllByUserIdSortedByCarId (Long userId) { return ownershipMapper.mapToDto(ownershipRepo.findAllByUserId(userId, Sort.by("carId"))); }
    public List<OwnershipDto> findAllByCarId  (Long carId)  { return ownershipMapper.mapToDto(ownershipRepo.findAllByCarId (carId)); }
    public Optional<OwnershipDto> findFirstByUserIdAndCarId (Long userId, Long carId) { return ownershipMapper.mapToDto(ownershipRepo.findFirstByUserIdAndCarId(userId, carId)); }

    public Optional<OwnershipDto> updateOwnership (Long id, OwnershipType category) {
        Optional<Ownership> ownership = ownershipRepo.findById(id);
        if (ownership.isEmpty()) { return Optional.empty(); }
        ownership.get().setCategory(category);
        log.info("Updating ownership id{{}} with value{{}} in the database", id, ownership);
        return ownershipMapper.mapToDto(ownership);
    }

    public void deleteOwnership (Long id) {
        log.info("Deleting ownership id{{}} from the database", id);
        ownershipRepo.deleteById(id);
    }
}
