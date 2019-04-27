package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.controller.ServerController;
import fr.spoutnik87.musicbot_rest.repository.*;
import fr.spoutnik87.musicbot_rest.util.BCryptTestConfig;
import fr.spoutnik87.musicbot_rest.util.SpringApplicationContextTestConfig;
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                SpringApplicationContextTestConfig.class,
                ServerController.class,
                BCryptTestConfig.class,
                WebSecurityTestConfig.class
        })
@WebMvcTest(ServerController.class)
public class ServerControllerTest {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ServerRepository serverRepository;
    @MockBean
    private BotRepository botRepository;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private UserGroupRepository userGroupRepository;
    @MockBean
    private PermissionRepository permissionRepository;
}
