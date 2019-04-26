package fr.spoutnik87.musicbot_rest.loader;

import fr.spoutnik87.musicbot_rest.UUID;
import fr.spoutnik87.musicbot_rest.model.Role;
import fr.spoutnik87.musicbot_rest.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class RoleLoader implements ApplicationListener<ContextRefreshedEvent> {

  @Autowired private RoleRepository roleRepository;

    @Autowired
    private UUID uuid;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
      this.roleRepository.save(new Role(uuid.v4(), "ADMIN", 1));
      this.roleRepository.save(new Role(uuid.v4(), "USER", 2));
  }
}
