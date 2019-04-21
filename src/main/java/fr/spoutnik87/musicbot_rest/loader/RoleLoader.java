package fr.spoutnik87.musicbot_rest.loader;

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

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    this.roleRepository.save(Role.ADMIN);
    this.roleRepository.save(Role.USER);
  }
}
