package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.constant.RoleEnum;
import fr.spoutnik87.musicbot_rest.controller.UserController;
import fr.spoutnik87.musicbot_rest.model.Role;
import fr.spoutnik87.musicbot_rest.model.User;
import fr.spoutnik87.musicbot_rest.repository.GroupRepository;
import fr.spoutnik87.musicbot_rest.repository.RoleRepository;
import fr.spoutnik87.musicbot_rest.repository.ServerRepository;
import fr.spoutnik87.musicbot_rest.repository.UserRepository;
import fr.spoutnik87.musicbot_rest.util.BCryptTestConfig;
import fr.spoutnik87.musicbot_rest.util.SpringApplicationContextTestConfig;
import fr.spoutnik87.musicbot_rest.util.Util;
import fr.spoutnik87.musicbot_rest.util.WebSecurityTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                SpringApplicationContextTestConfig.class,
                UserController.class,
                BCryptTestConfig.class,
                WebSecurityTestConfig.class
        })
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected BCryptPasswordEncoder bCryptPasswordEncoder;

  @MockBean protected UserRepository userRepository;
  @MockBean
  protected RoleRepository roleRepository;
  @MockBean protected ServerRepository serverRepository;
  @MockBean protected GroupRepository groupRepository;

  @MockBean(name = "UUID")
  protected UUID uuid;

  @BeforeEach
  public void setup() {
    Mockito.when(uuid.v4()).thenReturn("token");
  }

  @Test
  public void testSignup() throws Exception {
    Mockito.when(roleRepository.findByName(RoleEnum.USER.getName()))
            .thenReturn(new Role("token", "USER", 2));
    Map<String, Object> params = new HashMap<>();
    params.put("email", "test@test.com");
    params.put("password", "password");
    params.put("nickname", "Nickname");
    params.put("firstname", "Firstname");
    params.put("lastname", "Lastname");
    Util.basicTestWithBody(
        mockMvc,
        HttpMethod.POST,
        "/user",
        new HashMap<>(),
        params,
        HttpStatus.CREATED,
        "{\"id\":\"token\",\"email\":\"test@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\",\"lvl\":2}}");
  }

  @Test
  public void testLoginSuccess() throws Exception {
    Mockito.when(userRepository.findByEmail("user@test.com"))
            .thenReturn(
                    new User(
                            "token",
                            "user@test.com",
                            "Nickname",
                            "Firstname",
                            "Lastname",
                            bCryptPasswordEncoder.encode("password"),
                            new Role("token", "USER", 2)));
    Map<String, Object> params = new HashMap<>();
    params.put("email", "user@test.com");
    params.put("password", "password");
    Util.basicTestWithBody(
            mockMvc,
            HttpMethod.POST,
            "/login",
            new HashMap<>(),
            params,
            HttpStatus.OK,
            "{\"id\":\"token\",\"email\":\"user@test.com\",\"nickname\":\"Nickname\",\"firstname\":\"Firstname\",\"lastname\":\"Lastname\",\"role\":{\"id\":\"token\",\"name\":\"USER\",\"lvl\":2}}");
  }

  @Test
  public void testLoginFail() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("email", "user@test.com");
    params.put("password", "password");
    Util.basicTestWithBody(
            mockMvc,
            HttpMethod.POST,
            "/login",
            new HashMap<>(),
            params,
            HttpStatus.UNAUTHORIZED);
  }
}
