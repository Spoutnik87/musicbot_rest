package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Group;
import fr.spoutnik87.musicbot_rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByNickname(String nickname);

    List<User> findByGroup(Group group);
}
