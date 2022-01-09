package ro.unibuc.car_messenger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
import static ro.unibuc.car_messenger.domain.OwnershipType.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor @Entity
@Table(name = "ownerships", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueOwnershipRelation", columnNames = {"user_id", "car_id"})
})
public class Ownership {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "category", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    private OwnershipType category;

    public boolean isOwner     () { return this.category == OWNER; }
    public boolean isCoowner   () { return this.category == COOWNER; }
    public boolean isInvited   () { return this.category == INVITED; }
    public boolean isRequested () { return this.category == REQUESTED; }
    public boolean isAtLeastCoowner () {return this.isOwner() || this.isCoowner();}

}
