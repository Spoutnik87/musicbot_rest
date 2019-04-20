package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.MediaGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaGroupRepository extends JpaRepository<MediaGroup, Long> {}
