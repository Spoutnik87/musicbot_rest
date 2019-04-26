package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import fr.spoutnik87.musicbot_rest.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "Permission")
public class Permission extends AuditModel implements Serializable {

  public static final Permission CREATE_MEDIA = new Permission(UUID.v4(), "Créer un media", "CREATE_MEDIA");
  public static final Permission DELETE_MEDIA =
      new Permission(UUID.v4(), "Supprimer un media", "DELETE_MEDIA");
  public static final Permission READ_MEDIA = new Permission(UUID.v4(), "Voir un media", "READ_MEDIA");
  public static final Permission CHANGE_MODE = new Permission(UUID.v4(), "Changer le mode", "CHANGE_MODE");
  public static final Permission PLAY_MEDIA = new Permission(UUID.v4(), "Jouer un media", "PLAY_MEDIA");
  public static final Permission STOP_MEDIA = new Permission(UUID.v4(), "Arreter un media", "STOP_MEDIA");
  public static final Permission CREATE_CATEGORY =
      new Permission(UUID.v4(), "Créer une catégorie", "CREATE_CATEGORY");
  public static final Permission DELETE_CATEGORY =
      new Permission(UUID.v4(), "Supprimer une catégorie", "DELETE_CATEGORY");

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
  @Column(nullable = false)
  private String value;

  @JsonView(Views.Public.class)
  @ManyToMany(mappedBy = "permissionSet")
  @JsonManagedReference
  private Set<UserGroup> userGroupSet;
}
