package ro.unibuc.car_messenger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;

@Builder @Data @AllArgsConstructor @NoArgsConstructor @Entity
@Table(name = "car", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueCarPlateCountry", columnNames = {"plate", "country_code"})
})
public class Car {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "plate", nullable = false)
    private String plate;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "engine_id")
    private Long engineId;

    @OneToMany(mappedBy = "car")
    private List<Ownership> ownerships;

}
