package ro.unibuc.car_messenger.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.persistence.GenerationType.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor @Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Ownership> ownerships;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    public boolean isAdmin () {
        return Role.isAdmin(this.roles);
    }

    public boolean testPassword(String password) {
        return this.getPassword().equals(password);
    }

}
