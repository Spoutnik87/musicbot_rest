package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class AuditModel implements Serializable {

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
}
