package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "RoleEnum")
public class Role extends AuditModel implements Serializable {

  @JsonView(Views.Public.class)
  @NonNull
  @JsonProperty("id")
  @Column(nullable = false, unique = true)
  private String uuid;

  @JsonView(Views.Public.class)
  @NonNull
  @Column(nullable = false)
  private String name;

  @JsonView(Views.Public.class)
  @NonNull
  private int lvl;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonBackReference
  private Set<User> userSet;
}
