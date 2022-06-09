package ro.unibuc.car_messenger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
@Builder @Data @AllArgsConstructor @NoArgsConstructor @Entity
@Table(name = "engines")
public class Engine {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EngineType type;

    @OneToOne
    private Car car;

}
