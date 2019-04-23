package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringSecurityTestConfig.class)
@SpringBootTest
public class UserControllerTest {

  protected MockMvc mockMvc;

  @Autowired protected WebApplicationContext webApplicationContext;

  @MockBean protected UserRepository userRepository;
  @MockBean protected ServerRepository serverRepository;
  @MockBean protected GroupRepository groupRepository;
  @MockBean protected BotRepository botRepository;
  @MockBean protected UserGroupRepository userGroupRepository;
  @MockBean protected PermissionRepository permissionRepository;
  /*@MockBean
  protected UserDetailsServiceImpl userDetailsService;
  @MockBean
  protected WebSecurity webSecurity;
  @MockBean
  protected WebSecurityConfiguration webSecurityConfiguration;
  @MockBean
  protected MockMvcAutoConfiguration mockMvcAutoConfiguration;
  @MockBean
  protected ErrorMvcAutoConfiguration errorMvcAutoConfiguration;*/

  @BeforeEach
  public void setup() {
    // this.mockMvc = MockMvcBuilders.standaloneSetup(new UserController()).build();
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
  }

  @AfterEach
  public void stop() {}

  @Test
  public void signup() throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("email", "test@test.com");
    params.put("password", "password");
    params.put("nickname", "Nickname");
    params.put("firstname", "Firstname");
    params.put("lastname", "Lastname");
    Util.basicPrintWithBody(mockMvc, HttpMethod.POST, "/user", new HashMap<>(), params);
  }

  /*@Test
  public void testSignup() throws Exception {
      when(Util.getUUID()).thenReturn("token");

      User u = new User("test@test.com", "aaaaaa", "TestFirstname", "TestLastname", "password");

      MvcResult result = mockMvc.perform(post("/user/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .param("email", "test@test.com")
              .param("firstname", "TestFirstname")
              .param("lastname", "TestLastname")
              .param("password", "password")
              .param("username", "aaaaaa"))
              .andExpect(status().isCreated())
              .andReturn();

      JSONAssert.assertEquals("{\"lastLoginAt\":null,\"email\":\"test@test.com\",\"username\":\"aaaaaa\",\"firstname\":\"TestFirstname\",\"lastname\":\"TestLastname\",\"token\":\"token\"}", result.getResponse().getContentAsString(), true);
  }

  @Test
  public void testSignin() throws Exception {
      when(Util.getUUID()).thenReturn("token");
      User u = new User("test@test.com", "aaaaaa", "TestFirstname", "TestLastname", "password");
      when(repository.findByEmailAndPassword("test@test.com", Util.getSHA256("password"))).thenReturn(u);

      HashMap<String, String> params = new HashMap<>();
      params.put("email", "test@test.com");
      params.put("password", "password");
      UtilTest.basicTest(mockMvc, repository, HttpMethod.POST, "/user/signin", params, HttpStatus.ACCEPTED,
              "{\"lastLoginAt\":null,\"email\":\"test@test.com\",\"username\":\"aaaaaa\",\"firstname\":\"TestFirstname\",\"lastname\":\"TestLastname\",\"token\":\"token\"}");
  }

  @Test
  public void testGetUserSession() throws Exception {
      User u = new User("test@test.com", "aaaaaa", "TestFirstname", "TestLastname", "password");
      u.setToken("token");
      when(repository.findByToken("token")).thenReturn(u);

      HashMap<String, String> params = new HashMap<>();
      params.put("token", "token");

      UtilTest.basicTest(mockMvc, repository, HttpMethod.GET, "/user", params, HttpStatus.OK,
              "{\"lastLoginAt\":null,\"email\":\"test@test.com\",\"username\":\"aaaaaa\",\"firstname\":\"TestFirstname\",\"lastname\":\"TestLastname\",\"token\":\"token\"}");
  }*/
}
