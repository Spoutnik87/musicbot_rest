package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Bot;
import fr.spoutnik87.musicbot_rest.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {

  Optional<Bot> findByServer(Server server);

  Optional<Bot> findByUuid(String uuid);
}
