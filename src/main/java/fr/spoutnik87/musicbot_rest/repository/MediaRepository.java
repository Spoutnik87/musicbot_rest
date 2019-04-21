package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Category;
import fr.spoutnik87.musicbot_rest.model.Media;
import fr.spoutnik87.musicbot_rest.model.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

  List<Media> findByMediaType(MediaType mediaType);

  List<Media> findByCategory(Category category);
}
