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
@Table(name = "Category")
public class Category extends AuditModel implements Serializable {

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
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<Media> mediaSet;

  @JsonView(Views.Public.class)
  @ManyToOne
  @JoinColumn(name = "server_id")
  @JsonManagedReference
  private Server server;
}
