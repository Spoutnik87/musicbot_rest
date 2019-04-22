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
@Table(name = "Server")
public class Server extends AuditModel implements Serializable {

  @JsonView(Views.Public.class)
  @NonNull
  @Column(nullable = false)
  private String name;

  @JsonView(Views.Public.class)
  @OneToOne(mappedBy = "server")
  @JoinColumn(name = "bot_id")
  @JsonBackReference
  private Bot bot;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<Group> groupSet;

  @JsonView(Views.Public.class)
  @ManyToOne()
  @NonNull
  @JoinColumn(name = "user_id")
  @JsonManagedReference
  private User owner;

  @JsonView(Views.Public.class)
  @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
  @JsonBackReference
  private Set<Category> categorySet;

  /**
   * Check if user is a member in server. (user in one or multiple groups of this server)
   *
   * @param user
   * @return
   */
  public boolean hasUser(User user) {
    for (Group group : this.groupSet) {
      if (group.hasUser(user)) {
        return true;
      }
    }
    return false;
  }
}
