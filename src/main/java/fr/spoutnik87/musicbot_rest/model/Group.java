package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
public class Group {

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String name;
}
