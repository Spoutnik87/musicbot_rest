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
@Table(name = "Media")
public class Media extends AuditModel implements Serializable {

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String name;

    @JsonView(Views.Public.class)
    @NonNull
    @ManyToOne
    @JoinColumn(name = "media_type_id")
    @JsonManagedReference
    private MediaType mediaType;

    @JsonView(Views.Public.class)
    @NonNull
    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonManagedReference
    private Category category;

    @JsonView(Views.Public.class)
    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL)
    @JsonBackReference
    private Set<MediaGroup> mediaGroupSet;
}
