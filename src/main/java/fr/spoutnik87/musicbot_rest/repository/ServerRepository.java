package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Bot;
import fr.spoutnik87.musicbot_rest.model.Group;
import fr.spoutnik87.musicbot_rest.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    Server findByGroup(Group group);

    Server findByBot(Bot bot);
}
