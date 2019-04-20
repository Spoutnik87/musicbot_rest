package fr.spoutnik87.musicbot_rest.repository;

import fr.spoutnik87.musicbot_rest.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);
}
