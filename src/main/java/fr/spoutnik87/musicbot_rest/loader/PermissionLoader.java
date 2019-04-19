package fr.spoutnik87.musicbot_rest.loader;

import fr.spoutnik87.musicbot_rest.model.Permission;
import fr.spoutnik87.musicbot_rest.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class PermissionLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        permissionRepository.save(Permission.CREATE_MEDIA);
        permissionRepository.save(Permission.DELETE_MEDIA);
        permissionRepository.save(Permission.READ_MEDIA);
        permissionRepository.save(Permission.CHANGE_MODE);
        permissionRepository.save(Permission.PLAY_MEDIA);
        permissionRepository.save(Permission.STOP_MEDIA);
    }
}
