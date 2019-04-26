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
@Table(name = "UserGroup")
public class UserGroup extends AuditModel implements Serializable {

  @JsonView(Views.Public.class)
  @NonNull
  @JsonProperty("id")
  @Column(nullable = false, unique = true)
  private String uuid;

  @JsonView(Views.Public.class)
  @NonNull
  @ManyToOne
  @JoinColumn(name = "user_id")
  @JsonManagedReference
  private User user;

  @JsonView(Views.Public.class)
  @NonNull
  @ManyToOne
  @JoinColumn(name = "group_id")
  @JsonManagedReference
  private Group group;

  @JsonView(Views.Public.class)
  @NonNull
  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "user_group_permission",
      joinColumns = @JoinColumn(name = "user_group_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id"))
  @JsonBackReference
  private Set<Permission> permissionSet;
}
