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

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
public class Bot extends AuditModel implements Serializable {

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String name;

    @JsonView(Views.Public.class)
    @OneToOne(mappedBy = "bot", cascade = CascadeType.ALL)
    @JoinColumn(name = "server_id")
    @JsonManagedReference
    private Server server;
}
