package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "UserTable")
public class User extends AuditModel implements Serializable {

  @JsonView(Views.Public.class)
  @NonNull
  @JsonProperty("id")
  @Column(nullable = false, unique = true)
  private String uuid;

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

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @NonNull
  @Column(nullable = false)
  private String password;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<UserGroup> userGroupSet;

  @JsonView(Views.Public.class)
  @NonNull
  @ManyToOne
  @JoinColumn(name = "role_id")
  @JsonManagedReference
  private Role role;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<Server> serverSet;

  /**
   * Check if an user own a server.
   *
   * @param server
   * @return
   */
  public boolean isOwner(Server server) {
    for (Server s : serverSet) {
      if (s.getId() == server.getId()) {
        return true;
      }
    }
    return false;
  }
}
