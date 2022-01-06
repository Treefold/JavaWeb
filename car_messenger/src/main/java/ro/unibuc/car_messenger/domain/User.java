package ro.unibuc.car_messenger.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;

import static javax.persistence.GenerationType.*;

@Builder @Data @AllArgsConstructor @NoArgsConstructor @Entity
@Table(name = "users")
public class User {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 4, max = 40)
    @Pattern(regexp = "^(.+)@(.+)\\.(.+)$")
    @Column(name = "email", unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(min = 6, max = 20)
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    public boolean isAdmin () {
        return Role.isAdmin(this.roles);
    }

    public boolean testPassword(String password) {
        return this.getPassword().equals(password);
    }

}
