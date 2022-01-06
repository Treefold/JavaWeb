package ro.unibuc.car_messenger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.Collection;

import static javax.persistence.GenerationType.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor @Entity
@Table(name = "roles")
public class Role {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType name;

    public boolean isAdmin () {
        return this.getName() == RoleType.ADMIN;
    }

    public static boolean isAdmin (Collection<Role> roles) {
        for (Role role : roles) {
            if (role.isAdmin()) { return true; }
        }
        return false;
    }

}
