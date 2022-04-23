package ro.unibuc.car_messenger.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.unibuc.car_messenger.domain.Engine;
import ro.unibuc.car_messenger.dto.EngineDto;
import ro.unibuc.car_messenger.exception.UniqueException;
import ro.unibuc.car_messenger.mapper.EngineMapper;
import ro.unibuc.car_messenger.repo.EngineRepo;

import java.util.Optional;

@Service @Transactional @Slf4j
public class EngineService {
    @Autowired
    private EngineRepo engineRepo;
    @Autowired
    private EngineMapper engineMapper;

    public EngineDto saveEngine (EngineDto engineDto)
    {
        if (engineRepo.findFirstByNumber(engineDto.getNumber()).isPresent()) {
            throw  new UniqueException("The engine already exists");
        }
        log.info("Saving new {} engine {} to the database", engineDto.getType(), engineDto.getNumber());
        return engineMapper.mapToDto(engineRepo.save(engineMapper.mapToEntity(engineDto)));
    }

    public Optional<EngineDto> getEngine (Long id) {

        Optional<EngineDto> engineDto;
        Optional<Engine> engine = engineRepo.findById(id);

        if (engine.isEmpty()) {
            engineDto = Optional.empty();
        } else {
            engineDto = Optional.of(engineMapper.mapToDto(engine.get()));
        }
        return engineDto;
    }

    public void deleteEngine(Long id) {
        log.info("Deleting engine Id{{}} from the database", id);
        engineRepo.deleteById(id);
    }

}

