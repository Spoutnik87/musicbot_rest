package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
public class User extends AuditModel implements Serializable {

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false, unique = true)
    private String email;

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false, unique = true)
    private String nickname;

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String firstname;

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String lastname;

    @JsonView(Views.Private.class)
    @NonNull
    @Column(nullable = false)
    private String password;
}
