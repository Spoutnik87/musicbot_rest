package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
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
}
