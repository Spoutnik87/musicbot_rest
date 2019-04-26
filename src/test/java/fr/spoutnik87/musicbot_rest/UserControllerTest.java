package fr.spoutnik87.musicbot_rest;

import fr.spoutnik87.musicbot_rest.controller.UserController;
import fr.spoutnik87.musicbot_rest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { UserController.class, BCryptTestConfig.class, WebSecurityTestConfig.class})
@WebMvcTest(UserController.class)
@PrepareForTest(UUID.class)
public class UserControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @MockBean protected UserRepository userRepository;

  @MockBean protected ServerRepository serverRepository;
  @MockBean protected GroupRepository groupRepository;

  @BeforeEach
  public void setup() {
    PowerMockito.mockStatic(UUID.class);
  }

  @Test
  public void signup() throws Exception {
    when(UUID.v4()).thenReturn("token");
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
