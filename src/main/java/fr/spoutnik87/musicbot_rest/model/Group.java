package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "GroupTable")
public class Group extends AuditModel implements Serializable {

  @JsonView(Views.Public.class)
  @NonNull
  @Column(nullable = false)
  private String name;

  @JsonView(Views.Public.class)
  @ManyToOne
  @JoinColumn(name = "server_id")
  @JsonManagedReference
  private Server server;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<UserGroup> userGroupSet;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<MediaGroup> mediaGroupSet;
}
