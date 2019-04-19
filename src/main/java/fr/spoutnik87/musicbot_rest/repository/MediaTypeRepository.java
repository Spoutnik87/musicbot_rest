package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Media;
import fr.spoutnik87.musicbot_rest.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaTypeRepository extends JpaRepository<MediaType, Long> {

    MediaType findByMedia(Media media);
}
