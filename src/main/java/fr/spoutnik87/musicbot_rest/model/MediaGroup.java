package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
@Table(name = "MediaGroup")
public class MediaGroup extends AuditModel implements Serializable {

    @JsonView(Views.Public.class)
    @NonNull
    @ManyToOne
    @JoinColumn(name = "media_id")
    @JsonManagedReference
    private Media media;

    @JsonView(Views.Public.class)
    @NonNull
    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonManagedReference
    private Group group;
}
