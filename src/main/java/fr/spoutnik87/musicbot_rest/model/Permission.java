package fr.spoutnik87.musicbot_rest.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
@Entity
public class Permission extends AuditModel implements Serializable {

    public static Permission CREATE_MEDIA = new Permission("Créer un media", "CREATE_MEDIA");
    public static Permission DELETE_MEDIA = new Permission("Supprimer un media", "DELETE_MEDIA");
    public static Permission READ_MEDIA = new Permission("Voir un media", "READ_MEDIA");
    public static Permission CHANGE_MODE = new Permission("Changer le mode", "CHANGE_MODE");
    public static Permission PLAY_MEDIA = new Permission("Jouer un media", "PLAY_MEDIA");
    public static Permission STOP_MEDIA = new Permission("Arreter un media", "STOP_MEDIA");

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String name;

    @JsonView(Views.Public.class)
    @NonNull
    @Column(nullable = false)
    private String value;
}
