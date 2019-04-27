package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Bot;
import fr.spoutnik87.musicbot_rest.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

  Optional<Server> findByBot(Bot bot);

  Optional<Server> findByUuid(String uuid);
}
