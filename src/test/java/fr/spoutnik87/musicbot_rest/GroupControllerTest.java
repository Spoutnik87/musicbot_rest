package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.controller.GroupController;
import fr.spoutnik87.musicbot_rest.repository.GroupRepository;
import fr.spoutnik87.musicbot_rest.repository.ServerRepository;
import fr.spoutnik87.musicbot_rest.repository.UserRepository;
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
                GroupController.class,
                BCryptTestConfig.class,
                WebSecurityTestConfig.class
        })
@WebMvcTest(GroupController.class)
public class GroupControllerTest {
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private GroupRepository groupRepository;
    @MockBean
    private ServerRepository serverRepository;
}
